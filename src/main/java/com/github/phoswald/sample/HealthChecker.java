package com.github.phoswald.sample;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

import com.github.phoswald.rstm.http.health.HealthCheckRegistry;
import com.github.phoswald.rstm.http.server.HttpFilter;

public class HealthChecker {

    private final HealthCheckRegistry registry = new HealthCheckRegistry();
    private final Supplier<Connection> connectionFactory;

    public HealthChecker(Supplier<Connection> connectionFactory) {
        this.connectionFactory = connectionFactory;
        registry.registerCheck("database", this::checkDatabase);
    }

    public HttpFilter createRoute() {
        return registry.createRoute();
    }

    private boolean checkDatabase() throws SQLException {
        try(Connection connection = connectionFactory.get()) {
            return connection.isValid(2);
        }
    }
}
