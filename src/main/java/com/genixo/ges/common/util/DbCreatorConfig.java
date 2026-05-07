package com.genixo.ges.common.util;



import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;

@EqualsAndHashCode(callSuper = true)
@Configuration
@ConfigurationProperties(prefix = "spring.datasource")
@Data
@Profile("auto-db-create")
public class DbCreatorConfig extends HikariConfig {
    String url;
    String dbName;
    String dbUrl;

    private Logger logger = LoggerFactory.getLogger(DbCreatorConfig.class);

    @Bean
    public DataSource dataSource() {
        this.setJdbcUrl(url);
        try {
            return new HikariDataSource(this);
        } catch (HikariPool.PoolInitializationException ex) {
            logger.info("{}, Unable to find database {}, creating a new one", Instant.now(), dbName);
            if (createDatabase()) {
                return new HikariDataSource(this);
            }
            throw ex;
        }
    }

    public Boolean createDatabase() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("CREATE DATABASE ");
        stringBuilder.append(dbName);
        try {
            DriverManager.registerDriver(new org.postgresql.Driver());
            Connection connection = DriverManager.getConnection(dbUrl, this.getUsername(), this.getPassword());
            Statement statement = connection.createStatement();
            statement.execute(stringBuilder.toString());
            logger.info("{}, Successfully created the database {}", Instant.now(), dbName);
        } catch (SQLException e) {
            logger.info("{}, Cannot connect to url {}", Instant.now(), dbUrl);
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
