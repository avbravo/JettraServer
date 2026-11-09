package io.jettra.aura.core;

import io.jettra.aura.ajax.AuraAjaxRequestTarget;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * Representa un evento disparado por un componente dentro del ciclo de vida de JettraAura.
 * Soporta tanto eventos síncronos HTTP (GET/POST) como eventos reactivos asíncronos (AJAX).
 */
public class AuraEvent implements Serializable {

    private final AuraComponent source;
    private final String eventType;
    private final AuraRequest request;
    private final AuraResponse response;
    private final AuraAjaxRequestTarget ajaxTarget;

    public AuraEvent(AuraComponent source, String eventType, AuraRequest request, AuraResponse response, AuraAjaxRequestTarget ajaxTarget) {
        this.source = source;
        this.eventType = eventType != null ? eventType : "click";
        this.request = request;
        this.response = response;
        this.ajaxTarget = ajaxTarget;
    }

    public AuraComponent getSource() {
        return source;
    }

    public String getEventType() {
        return eventType;
    }

    public AuraRequest getRequest() {
        return request;
    }

    public AuraResponse getResponse() {
        return response;
    }

    public AuraAjaxRequestTarget getAjaxTarget() {
        return ajaxTarget;
    }

    public boolean isAjax() {
        return ajaxTarget != null;
    }

    public String getParameter(String name) {
        return request != null ? request.getParameter(name) : null;
    }

    public Map<String, String> getParameters() {
        return request != null ? request.getParameters() : Collections.emptyMap();
    }
}

