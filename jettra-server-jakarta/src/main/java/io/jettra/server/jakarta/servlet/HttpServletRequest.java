package io.jettra.server.jakarta.servlet;

import io.jettra.server.core.http.HttpRequest;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

public class HttpServletRequest {

    private final HttpRequest delegate;
    private final ServletContext servletContext;
    private HttpSession session;

    public HttpServletRequest(HttpRequest delegate, ServletContext servletContext) {
        this.delegate = delegate;
        this.servletContext = servletContext;
    }

    private Map<String, String> formParams;

    private Map<String, String> getFormParameters() {
        if (formParams == null) {
            formParams = new java.util.HashMap<>();
            String bodyStr = delegate.getBodyAsString();
            if (bodyStr != null && !bodyStr.isBlank() && bodyStr.contains("=")) {
                for (String pair : bodyStr.split("&")) {
                    String[] kv = pair.split("=", 2);
                    if (kv.length == 2) {
                        try {
                            String k = java.net.URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                            String v = java.net.URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                            formParams.put(k, v);
                        } catch (Exception ignored) {}
                    }
                }
            }
        }
        return formParams;
    }

    public String getMethod() { return delegate.getMethod().name(); }
    public String getRequestURI() { return delegate.getPath(); }
    public String getQueryString() { return delegate.getQueryString(); }
    public String getParameter(String name) {
        String val = delegate.getQueryParam(name);
        if (val != null) return val;
        return getFormParameters().get(name);
    }
    public Map<String, String> getParameterMap() {
        Map<String, String> merged = new java.util.HashMap<>(delegate.getQueryParams());
        merged.putAll(getFormParameters());
        return Collections.unmodifiableMap(merged);
    }
    public String getHeader(String name) { return delegate.getHeader(name); }
    public Enumeration<String> getHeaderNames() { return Collections.enumeration(delegate.getHeaders().keySet()); }

    public Object getAttribute(String name) { return delegate.getAttribute(name); }
    public void setAttribute(String name, Object value) { delegate.setAttribute(name, value); }

    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(delegate.getBody()), StandardCharsets.UTF_8));
    }

    public ServletContext getServletContext() { return servletContext; }

    public HttpSession getSession(boolean create) {
        if (session != null && session.isValid()) {
            return session;
        }
        String cookieHeader = delegate.getHeader("Cookie");
        String sessionId = null;
        if (cookieHeader != null) {
            for (String cookie : cookieHeader.split(";")) {
                String[] parts = cookie.trim().split("=", 2);
                if (parts.length == 2 && "JSESSIONID".equalsIgnoreCase(parts[0])) {
                    sessionId = parts[1].trim();
                    break;
                }
            }
        }
        if (sessionId != null) {
            session = servletContext.getSession(sessionId);
            if (session != null && session.isValid()) {
                return session;
            }
        }
        if (create) {
            String newId = java.util.UUID.randomUUID().toString();
            session = servletContext.getOrCreateSession(newId);
        }
        return session;
    }

    public HttpSession getSession() {
        return getSession(true);
    }

    public HttpRequest getDelegate() {
        return delegate;
    }
}
