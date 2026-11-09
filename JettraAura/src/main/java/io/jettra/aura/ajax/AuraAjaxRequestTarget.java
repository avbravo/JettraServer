package io.jettra.aura.ajax;

import io.jettra.aura.core.AuraComponent;
import io.jettra.aura.core.AuraPage;
import io.jettra.aura.core.AuraTag;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Objetivo de petición asíncrona Ajax (similar a AjaxRequestTarget en Apache Wicket).
 * Permite actualizar selectivamente fragmentos del DOM en el navegador sin recarga de página completa.
 */
public class AuraAjaxRequestTarget implements Serializable {

    private final AuraPage page;
    private final Map<String, AuraComponent> componentsToUpdate = new LinkedHashMap<>();
    private final List<String> scriptsToExecute = new ArrayList<>();
    private String redirectUrl;

    public AuraAjaxRequestTarget(AuraPage page) {
        this.page = page;
    }

    public AuraPage getPage() {
        return page;
    }

    /**
     * Agrega uno o más componentes a la lista de actualización parcial en el DOM.
     */
    public AuraAjaxRequestTarget add(AuraComponent... components) {
        if (components != null) {
            for (AuraComponent comp : components) {
                if (comp != null) {
                    comp.setOutputMarkupId(true);
                    componentsToUpdate.put(comp.getMarkupId(), comp);
                }
            }
        }
        return this;
    }

    /**
     * Encola un fragmento de script JavaScript para ser ejecutado en el cliente tras la respuesta.
     */
    public AuraAjaxRequestTarget appendJavaScript(String script) {
        if (script != null && !script.isBlank()) {
            scriptsToExecute.add(script);
        }
        return this;
    }

    public void redirect(String url) {
        this.redirectUrl = url;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public Map<String, AuraComponent> getComponentsToUpdate() {
        return componentsToUpdate;
    }

    public List<String> getScriptsToExecute() {
        return scriptsToExecute;
    }

    /**
     * Serializa la respuesta Ajax a JSON para el runtime de cliente `jettra-aura-runtime`.
     */
    public String toJson() {
        if (redirectUrl != null) {
            return "{\"redirect\":\"" + escapeJson(redirectUrl) + "\"}";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{\"updates\":{");

        boolean first = true;
        for (Map.Entry<String, AuraComponent> entry : componentsToUpdate.entrySet()) {
            if (!first) sb.append(",");
            first = false;

            String markupId = entry.getKey();
            AuraComponent comp = entry.getValue();

            // Renderizar el componente individualmente
            AuraTag tag = new AuraTag("div");
            tag.setAuraId(comp.getId());
            tag.setAttribute("id", markupId);

            StringBuilder compOut = new StringBuilder();
            comp.render(tag, "", compOut);

            sb.append("\"").append(escapeJson(markupId)).append("\":\"")
              .append(escapeJson(compOut.toString())).append("\"");
        }
        sb.append("},\"scripts\":[");

        first = true;
        for (String script : scriptsToExecute) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(escapeJson(script)).append("\"");
        }
        sb.append("]}");

        return sb.toString();
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < ' ') {
                        String t = "000" + Integer.toHexString(c);
                        sb.append("\\u").append(t.substring(t.length() - 4));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }
}

