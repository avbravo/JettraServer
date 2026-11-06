package io.jettra.server.core.health;

import java.util.Map;

/**
 * Estado reportado por un chequeo de salud.
 */
public record HealthStatus(
    String name,
    State state,
    Map<String, Object> data
) {
    public enum State {
        UP, DOWN
    }

    public static HealthStatus up(String name) {
        return new HealthStatus(name, State.UP, Map.of());
    }

    public static HealthStatus up(String name, Map<String, Object> data) {
        return new HealthStatus(name, State.UP, data);
    }

    public static HealthStatus down(String name, String reason) {
        return new HealthStatus(name, State.DOWN, Map.of("reason", reason));
    }
}
