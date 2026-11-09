package io.jettra.aura.core;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Encapsula la respuesta HTTP generada por JettraAura.
 */
public class AuraResponse implements Serializable {

    private int status = 200;
    private String contentType = "text/html; charset=UTF-8";
    private final Map<String, String> headers = new LinkedHashMap<>();
    private String redirectLocation;
    private StringBuilder content = new StringBuilder();

    public AuraResponse() {}

    public int getStatus() {
        return status;
    }

    public AuraResponse setStatus(int status) {
        this.status = status;
        return this;
    }

    public String getContentType() {
        return contentType;
    }

    public AuraResponse setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public AuraResponse setHeader(String name, String value) {
        headers.put(name, value);
        return this;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getRedirectLocation() {
        return redirectLocation;
    }

    public void sendRedirect(String location) {
        this.status = 302;
        this.redirectLocation = location;
        setHeader("Location", location);
    }

    public void redirect(String location) {
        sendRedirect(location);
    }

    public boolean isRedirect() {
        return redirectLocation != null;
    }

    public AuraResponse write(String text) {
        if (text != null) content.append(text);
        return this;
    }

    public String getContent() {
        return content.toString();
    }

    public AuraResponse setContent(String str) {
        this.content = new StringBuilder(str != null ? str : "");
        return this;
    }
}

