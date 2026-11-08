package io.jettra.json;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class JsonObject {
    private final Map<String, Object> members = new LinkedHashMap<>();

    public void addProperty(String property, String value) {
        members.put(property, value);
    }

    public void addProperty(String property, Number value) {
        members.put(property, value);
    }

    public void addProperty(String property, Boolean value) {
        members.put(property, value);
    }
    
    public void addProperty(String property, Character value) {
        members.put(property, value != null ? value.toString() : null);
    }

    public void add(String property, JsonObject value) {
        members.put(property, value);
    }
    
    public void add(String property, JsonArray value) {
        members.put(property, value);
    }

    public boolean has(String memberName) {
        return members.containsKey(memberName);
    }

    public Object get(String memberName) {
        return members.get(memberName);
    }
    
    public JsonObject getAsJsonObject(String memberName) {
        return (JsonObject) members.get(memberName);
    }

    public JsonArray getAsJsonArray(String memberName) {
        return (JsonArray) members.get(memberName);
    }

    public String getAsString(String memberName) {
        Object val = members.get(memberName);
        return val != null ? val.toString() : null;
    }

    public int getAsInt(String memberName) {
        Object val = members.get(memberName);
        if (val instanceof Number) return ((Number) val).intValue();
        if (val == null) return 0;
        return Integer.parseInt(val.toString());
    }
    
    public boolean getAsBoolean(String memberName) {
        Object val = members.get(memberName);
        if (val instanceof Boolean) return (Boolean) val;
        if (val == null) return false;
        return Boolean.parseBoolean(val.toString());
    }

    public Set<Map.Entry<String, Object>> entrySet() {
        return members.entrySet();
    }

    public Set<String> keySet() {
        return members.keySet();
    }
    
    public Map<String, Object> getMap() {
        return members;
    }

    @Override
    public String toString() {
        return new JettraJson().toJson(this);
    }
}
