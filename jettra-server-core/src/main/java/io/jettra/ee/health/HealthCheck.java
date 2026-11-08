package io.jettra.ee.health;

@FunctionalInterface
public interface HealthCheck {
    HealthCheckResponse call();
}

