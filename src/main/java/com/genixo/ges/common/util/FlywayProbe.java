package com.genixo.ges.common.util;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Diagnostic runner to prove whether Flyway is on the runtime classpath and whether Spring created Flyway beans.
 * Enabled by default; can be disabled with `ges.flyway-probe.enabled=false`.
 */
@Component
@ConditionalOnProperty(name = "ges.flyway-probe.enabled", havingValue = "true", matchIfMissing = true)
public class FlywayProbe implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(FlywayProbe.class);

    private final ApplicationContext applicationContext;

    public FlywayProbe(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(ApplicationArguments args) {
        Class<?> flywayClass = tryLoad("org.flywaydb.core.Flyway");
        if (flywayClass == null) {
            log.warn("FlywayProbe: Flyway class NOT found on runtime classpath (org.flywaydb.core.Flyway). Flyway cannot run.");
            return;
        }

        log.info("FlywayProbe: Flyway class found on runtime classpath: {}", flywayClass.getName());

        @SuppressWarnings("unchecked")
        Map<String, ?> flywayBeans = (Map<String, ?>) (Map<?, ?>) applicationContext.getBeansOfType((Class<?>) flywayClass);
        if (flywayBeans.isEmpty()) {
            log.warn("FlywayProbe: No Flyway beans found in Spring context. Auto-configuration likely did not activate.");
            return;
        }

        log.info("FlywayProbe: Flyway beans found: {}", flywayBeans.keySet());

        // Attempt to call flyway.info().all() via reflection for additional evidence (safe, no side-effects).
        Object flyway = flywayBeans.values().iterator().next();
        try {
            Object info = flywayClass.getMethod("info").invoke(flyway);
            Object[] all = (Object[]) info.getClass().getMethod("all").invoke(info);
            log.info("FlywayProbe: flyway.info().all().length={}", all == null ? null : all.length);
        } catch (Throwable t) {
            log.warn("FlywayProbe: Could not call flyway.info() via reflection: {}", t.toString());
        }
    }

    private static Class<?> tryLoad(String fqcn) {
        try {
            return Class.forName(fqcn);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}

