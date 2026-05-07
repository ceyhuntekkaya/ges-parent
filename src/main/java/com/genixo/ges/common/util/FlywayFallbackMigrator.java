package com.genixo.ges.common.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;

/**
 * If Flyway is enabled but auto-configuration didn't create a Flyway bean for any reason,
 * this runner performs a best-effort manual migration using the configured DataSource.
 *
 * This is a safety net specifically for environments where Flyway should run but doesn't.
 */
@Component
@ConditionalOnClass(Flyway.class)
@ConditionalOnProperty(name = "spring.flyway.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnMissingBean(Flyway.class)
public class FlywayFallbackMigrator implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(FlywayFallbackMigrator.class);

    private final DataSource dataSource;
    private final Environment environment;

    public FlywayFallbackMigrator(DataSource dataSource, Environment environment) {
        this.dataSource = dataSource;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.warn("FlywayFallbackMigrator: No Flyway bean found; running manual Flyway migration as fallback.");

        FluentConfiguration cfg = Flyway.configure()
                .dataSource(dataSource)
                .locations(locations())
                .schemas(schemas())
                .table(environment.getProperty("spring.flyway.table", "flyway_schema_history"))
                .baselineOnMigrate(environment.getProperty("spring.flyway.baseline-on-migrate", Boolean.class, false))
                .validateOnMigrate(environment.getProperty("spring.flyway.validate-on-migrate", Boolean.class, true))
                .placeholders(placeholders());

        var result = cfg.load().migrate();
        log.info(
                "FlywayFallbackMigrator: migrate() complete. migrationsExecuted={}, initialSchemaVersion={}, targetSchemaVersion={}",
                result.migrationsExecuted,
                result.initialSchemaVersion,
                result.targetSchemaVersion
        );
    }

    private String[] locations() {
        String raw = environment.getProperty("spring.flyway.locations", "classpath:db/migration");
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toArray(String[]::new);
    }

    private String[] schemas() {
        String raw = environment.getProperty("spring.flyway.schemas", "");
        if (raw == null || raw.isBlank()) return new String[0];
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toArray(String[]::new);
    }

    private Map<String, String> placeholders() {
        Map<String, String> out = new HashMap<>();
        if (!(environment instanceof ConfigurableEnvironment ce)) {
            return out;
        }

        String prefix = "spring.flyway.placeholders.";
        for (PropertySource<?> ps : ce.getPropertySources()) {
            if (!(ps instanceof EnumerablePropertySource<?> eps)) continue;
            for (String name : eps.getPropertyNames()) {
                if (!name.startsWith(prefix)) continue;
                // Important: resolve Spring placeholders like ${ENV:default}
                String value = environment.getProperty(name);
                if (value == null) continue;
                String key = name.substring(prefix.length());
                out.put(key, value);
            }
        }

        // Stable order for logs/debugging (no functional need)
        return out.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, HashMap::new));
    }
}

