package io.jettra.server.jakarta.json;

import io.jettra.json.JettraJson;

/**
 * Envoltorio Jakarta JSON-B 3.0 respaldado por el motor de serialización JettraJSON.
 */
public class Jsonb implements AutoCloseable {

    private final JettraJson engine = new JettraJson();

    public String toJson(Object object) {
        return engine.toJson(object);
    }

    public <T> T fromJson(String str, Class<T> type) {
        return engine.fromJson(str, type);
    }

    public static Jsonb create() {
        return new Jsonb();
    }

    @Override
    public void close() {}
}
