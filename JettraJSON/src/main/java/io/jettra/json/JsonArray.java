package io.jettra.json;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JsonArray implements Iterable<Object> {
    private final List<Object> elements = new ArrayList<>();

    public void add(String value) {
        elements.add(value);
    }

    public void add(Number value) {
        elements.add(value);
    }

    public void add(Boolean value) {
        elements.add(value);
    }
    
    public void add(JsonObject value) {
        elements.add(value);
    }

    public void add(JsonArray value) {
        elements.add(value);
    }

    public int size() {
        return elements.size();
    }

    public Object get(int i) {
        return elements.get(i);
    }

    public JsonObject getAsJsonObject(int i) {
        return (JsonObject) elements.get(i);
    }
    
    public JsonArray getAsJsonArray(int i) {
        return (JsonArray) elements.get(i);
    }

    public String getAsString(int i) {
        Object val = elements.get(i);
        return val != null ? val.toString() : null;
    }

    public List<Object> getList() {
        return elements;
    }

    @Override
    public Iterator<Object> iterator() {
        return elements.iterator();
    }

    @Override
    public String toString() {
        return new JettraJson().toJson(this);
    }
}
