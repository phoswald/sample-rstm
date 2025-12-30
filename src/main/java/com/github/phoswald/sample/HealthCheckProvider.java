package com.github.phoswald.sample;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.github.phoswald.rstm.http.health.HealthCheck;
import com.github.phoswald.rstm.http.health.HealthCheckRegistry;
import com.github.phoswald.rstm.http.server.HttpFilter;

class HealthCheckProvider {

    private final HealthCheckRegistry registry;
    private final Supplier<Connection> connectionFactory;
    private final List<HealthCheck> checks = new ArrayList<>();

    HealthCheckProvider(HealthCheckRegistry registry, Supplier<Connection> connectionFactory) {
        this.registry = registry;
        this.connectionFactory = connectionFactory;
        registerChecks();
    }

    List<HealthCheck> getAllChecks() {
        return List.copyOf(checks);
    }

    HttpFilter createRoute() {
        return registry.createRoute();
    }

    private void registerChecks() {
        checks.add(registry.registerCheck("database", this::checkDatabase));
    }

    private boolean checkDatabase() {
        try(Connection connection = connectionFactory.get()) {
            return connection.isValid(1);
        } catch(Exception e) {
            return false;
        }
    }
}
