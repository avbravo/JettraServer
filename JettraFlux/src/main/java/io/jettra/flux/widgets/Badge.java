package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

/**
 * Configurable status badge indicator in JettraFlux.
 * Supports severities: success, info, warning, danger, neutral/secondary, active, and primary.
 */
public class Badge extends Widget {
    private final String text;
    private String severity = "primary";
    private String icon = null;
    private String size = "normal";

    private Badge(String text) {
        this.text = text != null ? text : "";
    }

    public static Badge of(String text) {
        return new Badge(text);
    }

    public static Badge of(String text, String severity) {
        return new Badge(text).severity(severity);
    }

    public static Badge active(String text) {
        return of(text, "active");
    }

    public static Badge success(String text) {
        return of(text, "success");
    }

    public static Badge info(String text) {
        return of(text, "info");
    }

    public static Badge warning(String text) {
        return of(text, "warning");
    }

    public static Badge danger(String text) {
        return of(text, "danger");
    }

    public static Badge neutral(String text) {
        return of(text, "neutral");
    }

    public static Badge secondary(String text) {
        return of(text, "secondary");
    }

    public Badge severity(String severity) {
        this.severity = severity != null ? severity : "primary";
        return this;
    }

    public Badge icon(String icon) {
        this.icon = icon;
        return this;
    }

    public Badge size(String size) {
        this.size = size;
        return this;
    }

    public String getText() {
        return text;
    }

    public String getSeverity() {
        return severity;
    }

    @Override
    public String render(ThemeData theme) {
        String bgColor = "var(--primary-color, #3b82f6)";
        String color = "white";
        String border = "none";

        switch (severity.toLowerCase()) {
            case "success" -> {
                bgColor = "rgba(34, 197, 94, 0.18)";
                color = "#4ade80";
                border = "1px solid rgba(34, 197, 94, 0.35)";
            }
            case "info" -> {
                bgColor = "rgba(59, 130, 246, 0.18)";
                color = "#60a5fa";
                border = "1px solid rgba(59, 130, 246, 0.35)";
            }
            case "warning" -> {
                bgColor = "rgba(234, 179, 8, 0.18)";
                color = "#facc15";
                border = "1px solid rgba(234, 179, 8, 0.35)";
            }
            case "danger" -> {
                bgColor = "rgba(239, 68, 68, 0.18)";
                color = "#f87171";
                border = "1px solid rgba(239, 68, 68, 0.35)";
            }
            case "neutral", "secondary" -> {
                bgColor = "rgba(100, 116, 139, 0.2)";
                color = "#cbd5e1";
                border = "1px solid rgba(148, 163, 184, 0.25)";
            }
            case "active" -> {
                bgColor = "rgba(16, 185, 129, 0.15)";
                color = "#34d399";
                border = "1px solid rgba(16, 185, 129, 0.35)";
            }
            case "primary" -> {
                bgColor = "var(--primary-color, #3b82f6)";
                color = "white";
                border = "none";
            }
            default -> {
                bgColor = "rgba(59, 130, 246, 0.18)";
                color = "#60a5fa";
                border = "1px solid rgba(59, 130, 246, 0.35)";
            }
        }

        String minW = "1.5rem", h = "1.5rem", fontS = "0.75rem";
        if ("large".equalsIgnoreCase(size) || "lg".equalsIgnoreCase(size)) {
            minW = "1.75rem";
            h = "1.75rem";
            fontS = "0.875rem";
        } else if ("xlarge".equalsIgnoreCase(size) || "xl".equalsIgnoreCase(size)) {
            minW = "2rem";
            h = "2rem";
            fontS = "1rem";
        }

        String style = "display: inline-flex; align-items: center; justify-content: center; min-width: "
                + minW + "; height: " + h + "; padding: 0 0.6rem; font-size: " + fontS
                + "; font-weight: 700; border-radius: 9999px; background-color: " + bgColor
                + "; color: " + color + "; border: " + border + "; gap: 0.35rem; letter-spacing: 0.3px; white-space: nowrap;";

        StringBuilder sb = new StringBuilder();
        sb.append("<span ").append(renderCommonAttributes(theme, "espresso-badge")).append(" style=\"").append(style).append("\">");
        if (icon != null && !icon.isEmpty()) {
            sb.append("<i class=\"").append(icon).append("\"></i>");
        }
        if (text != null && !text.isEmpty()) {
            sb.append("<span>").append(text).append("</span>");
        }
        sb.append("</span>");
        return sb.toString();
    }
}
