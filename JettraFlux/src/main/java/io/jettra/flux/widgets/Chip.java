package io.jettra.flux.widgets;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
public class Chip extends Widget {
    private final String label;
    private String icon;
    private String image = "";
    private Widget iconWidget;
    private Widget imageWidget;
    private boolean removable;
    private Chip(String label) { this.label = label; }
    public static Chip of(String label) { return new Chip(label); }
    public Chip icon(String icon) {
        this.icon = icon;
        return this;
    }

    public Chip image(String image) {
        this.image = image;
        return this;
    }
    public Chip removable(boolean removable) { 
        this.removable = removable;
        return this; 
    }
    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        String style = "display: inline-flex; align-items: center; background: rgba(128,128,128,0.1); color: var(--text-color); border-radius: 16px; padding: 0.3rem 0.75rem; font-size: 0.875rem; font-weight: 500; gap: 0.5rem;";
        if (image != null && !image.isEmpty()) {
            style = style.replace("padding: 0.3rem 0.75rem;", "padding: 0 0.75rem 0 0;");
        }
        sb.append("<div ").append(renderCommonAttributes(theme, "espresso-chip")).append(" style=\"").append(style).append("\">\n");

        if (imageWidget != null) {
            sb.append(imageWidget.render(theme)).append("\n");
        } else if (image != null && !image.isEmpty()) {
            sb.append("<img src=\"").append(image).append("\" alt=\"chip-image\" style=\"width: 2rem; height: 2rem; border-radius: 50%; object-fit: cover; margin-right: 0.5rem;\" />\n");
        } else if (iconWidget != null) {
            sb.append("<span style=\"margin-right: 0.5rem; display: flex; align-items: center;\">").append(iconWidget.render(theme)).append("</span>\n");
        } else if (icon != null && !icon.isEmpty()) {
            sb.append("<span style=\"margin-right: 0.5rem; display: flex; align-items: center;\">").append(io.jettra.flux.widgets.Icon.of(icon).render(theme)).append("</span>\n");
        }

        sb.append("<span>").append(label).append("</span>\n");
        if (removable) {
            sb.append("<span style=\"cursor:pointer; margin-left: 0.25rem; font-size:1rem; opacity:0.6;\"><i class=\"fas fa-times-circle\"></i></span>");
        }
        sb.append("</div>\n");
        return sb.toString();
    }
}
