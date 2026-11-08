package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

public class Message extends Widget {
    public enum Severity {
        SUCCESS, INFO, WARNING, ERROR
    }

    private final String text;
    private final Severity severity;
    private String icon;

    private Message(Severity severity, String text) {
        this.severity = severity;
        this.text = text;
        
        switch (severity) {
            case SUCCESS: this.icon = "fas fa-check-circle"; break;
            case INFO: this.icon = "fas fa-info-circle"; break;
            case WARNING: this.icon = "fas fa-exclamation-triangle"; break;
            case ERROR: this.icon = "fas fa-times-circle"; break;
        }
    }

    public static Message of(Severity severity, String text) {
        return new Message(severity, text);
    }

    public Message icon(String icon) {
        this.icon = icon;
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        String baseClass = "espresso-message espresso-message-" + severity.name().toLowerCase();
        
        StringBuilder sb = new StringBuilder();
        sb.append("<div ").append(renderCommonAttributes(theme, baseClass)).append(">\n");
        sb.append("  <span class=\"espresso-message-icon\"><i class=\"").append(icon).append("\"></i></span>\n");
        sb.append("  <span class=\"espresso-message-text\">").append(text).append("</span>\n");
        sb.append("</div>\n");
        
        return sb.toString();
    }
}
