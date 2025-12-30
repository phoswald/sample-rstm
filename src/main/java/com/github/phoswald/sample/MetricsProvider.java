package com.github.phoswald.sample;

import java.util.List;

import com.github.phoswald.rstm.http.health.HealthCheck;
import com.github.phoswald.rstm.http.metrics.MetricLabel;
import com.github.phoswald.rstm.http.metrics.MetricsRegistry;
import com.github.phoswald.rstm.http.server.HttpFilter;

class MetricsProvider {

    private final MetricsRegistry registry;

    MetricsProvider(MetricsRegistry registry, List<HealthCheck> healthChecks) {
        this.registry = registry;
        registerHeapMetrics();
        registerHealthMetrics(healthChecks);
    }

    HttpFilter createRoute() {
        return registry.createRoute();
    }

    private void registerHeapMetrics() {
        var runtime = Runtime.getRuntime();
        registry.registerGauge("jvm_runtime_maxMemory", runtime::maxMemory);
        registry.registerGauge("jvm_runtime_totalMemory", runtime::totalMemory);
        registry.registerGauge("jvm_runtime_freeMemory", runtime::freeMemory);
        registry.registerGauge("jvm_runtime_availableProcessors", runtime::availableProcessors);
    }

    private void registerHealthMetrics(List<HealthCheck> healthChecks) {
        for(var healthCheck : healthChecks) {
            registry.registerGauge("health_check", () -> healthCheck.function().invoke() ? 1 : 0,
                    new MetricLabel("id", Integer.toString(healthCheck.id())),
                    new MetricLabel("name", healthCheck.name()));
        }
    }
}
