package io.jettra.aura.core;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Representa una etiqueta HTML analizada y enriquecida con metadatos del framework JettraAura.
 */
public class AuraTag implements Serializable {

    private String name;
    private String auraId;
    private final Map<String, String> attributes = new LinkedHashMap<>();
    private boolean selfClosing;
    private boolean closing;

    public AuraTag() {}

    public AuraTag(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuraId() {
        return auraId;
    }

    public void setAuraId(String auraId) {
        this.auraId = auraId;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public String getAttribute(String key) {
        return attributes.get(key);
    }

    public AuraTag setAttribute(String key, String value) {
        if (value == null) {
            attributes.remove(key);
        } else {
            attributes.put(key, value);
        }
        return this;
    }

    public AuraTag removeAttribute(String key) {
        attributes.remove(key);
        return this;
    }

    public AuraTag appendAttribute(String key, String value, String separator) {
        String existing = attributes.get(key);
        if (existing == null || existing.isBlank()) {
            attributes.put(key, value);
        } else {
            attributes.put(key, existing + separator + value);
        }
        return this;
    }

    public boolean isSelfClosing() {
        return selfClosing;
    }

    public void setSelfClosing(boolean selfClosing) {
        this.selfClosing = selfClosing;
    }

    public boolean isClosing() {
        return closing;
    }

    public void setClosing(boolean closing) {
        this.closing = closing;
    }

    /**
     * Renderiza la etiqueta HTML de apertura o autocerrada con sus atributos.
     */
    public String renderOpen() {
        StringBuilder sb = new StringBuilder();
        sb.append("<").append(name);
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            String k = entry.getKey();
            // Ignorar atributos internos de Aura para mantener el HTML limpio
            if (isInternalAuraAttribute(k)) continue;
            sb.append(" ").append(k).append("=\"").append(escapeAttribute(entry.getValue())).append("\"");
        }
        if (selfClosing) {
            sb.append(" />");
        } else {
            sb.append(">");
        }
        return sb.toString();
    }

    /**
     * Renderiza la etiqueta de cierre correspondiente.
     */
    public String renderClose() {
        if (selfClosing) return "";
        return "</" + name + ">";
    }

    private static boolean isInternalAuraAttribute(String attr) {
        return "aura:id".equalsIgnoreCase(attr) ||
               "j:id".equalsIgnoreCase(attr) ||
               "wicket:id".equalsIgnoreCase(attr) ||
               "data-aura-id".equalsIgnoreCase(attr);
    }

    private static String escapeAttribute(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;");
    }

    @Override
    public String toString() {
        return renderOpen();
    }
}

