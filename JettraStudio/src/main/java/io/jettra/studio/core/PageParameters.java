package io.jettra.studio.core;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Encapsulates query parameters and URL path parameters passed to a WebPage.
 */
public class PageParameters implements Serializable {

    private final Map<String, String> parameters = new LinkedHashMap<>();

    public PageParameters() {}

    public PageParameters(Map<String, String> initial) {
        if (initial != null) {
            parameters.putAll(initial);
        }
    }

    public static PageParameters of(String key, String value) {
        PageParameters p = new PageParameters();
        p.set(key, value);
        return p;
    }

    public PageParameters set(String key, String value) {
        if (key != null) {
            if (value != null) {
                parameters.put(key, value);
            } else {
                parameters.remove(key);
            }
        }
        return this;
    }

    public String get(String key) {
        return parameters.get(key);
    }

    public String get(String key, String defaultValue) {
        String val = parameters.get(key);
        return val != null ? val : defaultValue;
    }

    public int getInt(String key, int defaultValue) {
        try {
            String v = get(key);
            return (v != null) ? Integer.parseInt(v.trim()) : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String v = get(key);
        return (v != null) ? Boolean.parseBoolean(v.trim()) : defaultValue;
    }

    public Map<String, String> toMap() {
        return Collections.unmodifiableMap(parameters);
    }

    public boolean isEmpty() {
        return parameters.isEmpty();
    }

    @Override
    public String toString() {
        return parameters.toString();
    }
}
