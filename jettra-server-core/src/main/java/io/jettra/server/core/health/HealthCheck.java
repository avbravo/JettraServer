package io.jettra.server.core.health;

/**
 * Interfaz para chequeos de salud liveness / readiness.
 */
@FunctionalInterface
public interface HealthCheck {
    HealthStatus check();
}
