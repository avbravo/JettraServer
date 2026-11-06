package io.jettra.server.jakarta.servlet;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HttpSession {
    private final String id;
    private final long creationTime = System.currentTimeMillis();
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();
    private boolean valid = true;

    public HttpSession(String id) {
        this.id = id;
    }

    public String getId() { return id; }
    public long getCreationTime() { return creationTime; }
    public Object getAttribute(String name) { return attributes.get(name); }
    public void setAttribute(String name, Object value) { attributes.put(name, value); }
    public void removeAttribute(String name) { attributes.remove(name); }
    public void invalidate() { valid = false; attributes.clear(); }
    public boolean isValid() { return valid; }
}
