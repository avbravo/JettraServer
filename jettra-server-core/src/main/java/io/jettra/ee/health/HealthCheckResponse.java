package io.jettra.ee.health;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HealthCheckResponse {

    public enum Status {
        UP, DOWN
    }

    private final String name;
    private final Status status;
    private final Map<String, Object> data;

    public HealthCheckResponse(String name, Status status, Map<String, Object> data) {
        this.name = name;
        this.status = status;
        this.data = data != null ? data : Collections.emptyMap();
    }

    public String getName() {
        return name;
    }

    public Status getStatus() {
        return status;
    }

    public java.util.Optional<Map<String, Object>> getData() {
        return data == null || data.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(data);
    }

    public static Builder named(String name) {
        return new Builder(name);
    }

    public static class Builder {
        private final String name;
        private Status status = Status.UP;
        private final Map<String, Object> data = new HashMap<>();

        public Builder(String name) {
            this.name = name;
        }

        public Builder up() {
            this.status = Status.UP;
            return this;
        }

        public Builder down() {
            this.status = Status.DOWN;
            return this;
        }

        public Builder status(boolean isUp) {
            this.status = isUp ? Status.UP : Status.DOWN;
            return this;
        }

        public Builder withData(String key, Object value) {
            this.data.put(key, value);
            return this;
        }

        public HealthCheckResponse build() {
            return new HealthCheckResponse(name, status, data);
        }
    }
}

