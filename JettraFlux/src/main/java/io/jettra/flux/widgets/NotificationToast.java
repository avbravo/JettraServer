package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

/**
 * NotificationToast component for reactive toast notifications.
 * Pure Java typed component in JettraFlux.
 */
public class NotificationToast extends Widget {

    public enum ToastType {
        SUCCESS,
        ERROR,
        WARNING,
        INFO
    }

    private String message = "";
    private ToastType type = ToastType.INFO;
    private int durationMs = 4000;
    private boolean visible = false;

    private NotificationToast(String toastId) {
        this.id = toastId;
    }

    public static NotificationToast of(String toastId) {
        return new NotificationToast(toastId);
    }

    public static NotificationToast of(String toastId, String message, ToastType type) {
        NotificationToast toast = new NotificationToast(toastId);
        toast.message = message;
        toast.type = type != null ? type : ToastType.INFO;
        return toast;
    }

    public NotificationToast message(String message) {
        this.message = message;
        return this;
    }

    public NotificationToast type(ToastType type) {
        this.type = type != null ? type : ToastType.INFO;
        return this;
    }

    public NotificationToast duration(int millis) {
        this.durationMs = millis;
        return this;
    }

    public NotificationToast visible(boolean visible) {
        this.visible = visible;
        return this;
    }

    public String getIconClass() {
        return switch (type) {
            case SUCCESS -> "fas fa-check-circle";
            case ERROR -> "fas fa-exclamation-circle";
            case WARNING -> "fas fa-exclamation-triangle";
            case INFO -> "fas fa-info-circle";
        };
    }

    public String getColor() {
        return switch (type) {
            case SUCCESS -> "#10b981";
            case ERROR -> "#ef4444";
            case WARNING -> "#f59e0b";
            case INFO -> "#38bdf8";
        };
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        String display = visible ? "flex" : "none";
        String color = getColor();

        sb.append("<div id=\"").append(id).append("\" ");
        sb.append("class=\"espresso-toast toast-").append(type.name().toLowerCase()).append(" ").append(modifier.getClasses()).append("\" ");
        sb.append("data-type=\"").append(type.name().toLowerCase()).append("\" ");
        sb.append("data-duration=\"").append(durationMs).append("\" ");
        sb.append("style=\"display:").append(display).append("; position:fixed; bottom:24px; right:24px; z-index:999999; ")
          .append("align-items:center; gap:10px; padding:12px 18px; border-radius:8px; ")
          .append("background:var(--j-bg-card, #1e293b); color:var(--j-text-primary, #f8fafc); ")
          .append("border:1px solid ").append(color).append("; box-shadow:0 8px 24px rgba(0,0,0,0.4); ")
          .append("transition:all 0.3s ease; font-size:13px; ").append(modifier.getStyles()).append("\">\n");

        sb.append("  <i class=\"").append(getIconClass()).append("\" style=\"color:").append(color).append("; font-size:16px;\"></i>\n");
        sb.append("  <span class=\"toast-msg\">").append(escapeHtml(message)).append("</span>\n");
        sb.append("</div>\n");

        return sb.toString();
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
