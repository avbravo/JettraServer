package io.jettra.aura.core;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Encapsula la petición HTTP procesada por JettraAura.
 */
public class AuraRequest implements Serializable {

    private final String method;
    private final String uri;
    private final Map<String, String> parameters = new LinkedHashMap<>();
    private final Map<String, String> headers = new LinkedHashMap<>();
    private String body;

    public AuraRequest(String method, String uri) {
        this.method = method != null ? method.toUpperCase() : "GET";
        this.uri = uri != null ? uri : "/";
    }

    public String getMethod() {
        return method;
    }

    public boolean isGet() {
        return "GET".equalsIgnoreCase(method);
    }

    public boolean isPost() {
        return "POST".equalsIgnoreCase(method);
    }

    public String getUri() {
        return uri;
    }

    public String getParameter(String name) {
        return parameters.get(name);
    }

    public Map<String, String> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    public AuraRequest setParameter(String name, String value) {
        if (value != null) {
            parameters.put(name, value);
        } else {
            parameters.remove(name);
        }
        return this;
    }

    public AuraRequest setParameters(Map<String, String> params) {
        if (params != null) parameters.putAll(params);
        return this;
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public AuraRequest setHeader(String name, String value) {
        headers.put(name, value);
        return this;
    }

    public String getBody() {
        return body;
    }

    public AuraRequest setBody(String body) {
        this.body = body;
        return this;
    }

    public boolean isAjax() {
        return "XMLHttpRequest".equalsIgnoreCase(getHeader("X-Requested-With")) ||
               "true".equalsIgnoreCase(getParameter("aura:ajax")) ||
               "true".equalsIgnoreCase(getParameter("ajax"));
    }
}

