package io.jettra.flux.widgets;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
public class Tag extends Widget {
    private final String text;
    private String severity = "primary";
    private String icon = "";
    private Widget iconWidget = null;

    private Tag(String text) { this.text = text; }
    public static Tag of(String text) { return new Tag(text); }
    public Tag severity(String severity) { this.severity = severity; return this; }
    public Tag icon(String icon) {
        this.icon = icon;
        return this;
    }
    public Tag icon(Widget iconWidget) { this.iconWidget = iconWidget; return this; }

    @Override
    public String render(ThemeData theme) {
        String bgColor = "var(--primary-color)";
        String color = "white";
        switch (severity.toLowerCase()) {
            case "success": bgColor = "#22c55e"; break;
            case "info": bgColor = "#3b82f6"; break;
            case "warning": bgColor = "#eab308"; color = "#1e293b"; break;
            case "danger": bgColor = "#ef4444"; break;
            case "secondary": bgColor = "#64748b"; break;
            case "primary": default: bgColor = "var(--primary-color)"; break;
        }
        String style = "display: inline-flex; align-items: center; justify-content: center; padding: 0.25rem 0.5rem; font-size: 0.875rem; font-weight: 600; border-radius: 6px; background-color: " + bgColor + "; color: " + color + "; gap: 0.3rem;";
        StringBuilder sb = new StringBuilder();
        sb.append("<span ").append(renderCommonAttributes(theme, "espresso-tag")).append(" style=\"").append(style).append("\">");
        if (iconWidget != null) {
            sb.append("<span style=\"margin-right: 0.5rem; display: flex; align-items: center;\">").append(iconWidget.render(theme)).append("</span>\n");
        } else if (icon != null && !icon.isEmpty()) {
            if (icon.trim().startsWith("<svg")) {
                sb.append("<span style=\"margin-right: 0.5rem; display: flex; align-items: center;\">").append(icon).append("</span>\n");
            } else {
                sb.append("<span style=\"margin-right: 0.5rem; display: flex; align-items: center;\"><i class=\"").append(icon).append("\"></i></span>\n");
            }
        }
        sb.append("<span>").append(text).append("</span>");
        sb.append("</span>");
        return sb.toString();
    }
}
