package io.jettra.server.jakarta.servlet;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServletContext {
    private final String contextPath;
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();
    private final Map<String, String> initParameters = new ConcurrentHashMap<>();
    private final Map<String, HttpSession> sessions = new ConcurrentHashMap<>();

    public ServletContext(String contextPath) {
        this.contextPath = contextPath;
    }

    public String getContextPath() { return contextPath; }
    public Object getAttribute(String name) { return attributes.get(name); }
    public void setAttribute(String name, Object value) { attributes.put(name, value); }
    public void removeAttribute(String name) { attributes.remove(name); }
    public String getInitParameter(String name) { return initParameters.get(name); }
    public void setInitParameter(String name, String value) { initParameters.put(name, value); }

    public HttpSession getOrCreateSession(String id) {
        if (id == null || id.isBlank()) return null;
        return sessions.computeIfAbsent(id, HttpSession::new);
    }

    public HttpSession getSession(String id) {
        return id != null ? sessions.get(id) : null;
    }

    public void removeSession(String id) {
        if (id != null) sessions.remove(id);
    }
}
