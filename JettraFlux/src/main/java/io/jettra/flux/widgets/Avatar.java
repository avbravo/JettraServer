package io.jettra.flux.widgets;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
public class Avatar extends Widget {
    private String label;
    private String icon;
    private String image;
    private String shape = "square";
    private Widget badge;
    private String size = "normal";

    private Avatar() {}
    public static Avatar of() { return new Avatar(); }
    public static Avatar label(String label) { Avatar a = new Avatar(); a.label = label; return a; }
    public static Avatar icon(String icon) { Avatar a = new Avatar(); a.icon = icon; return a; }
    public static Avatar image(String image) { Avatar a = new Avatar(); a.image = image; return a; }

    public Avatar shape(String shape) { this.shape = shape; return this; }
    public Avatar badge(Widget badge) { this.badge = badge; return this; }
    public Avatar size(String size) { this.size = size; return this; }

    // Backward compatibility
    public static Avatar of(String src) {
        if (src != null && (src.startsWith("http") || src.startsWith("data:"))) {
            return Avatar.image(src);
        } else {
            return Avatar.label(src);
        }
    }

    @Override
    public String render(ThemeData theme) {
        String dim = "2rem", fontS = "1rem";
        if ("large".equalsIgnoreCase(size) || "lg".equalsIgnoreCase(size)) { dim = "3rem"; fontS = "1.5rem"; }
        else if ("xlarge".equalsIgnoreCase(size) || "xl".equalsIgnoreCase(size)) { dim = "4rem"; fontS = "2rem"; }
        
        String br = "circle".equalsIgnoreCase(shape) ? "50%" : "6px";
        String bgColor = (image != null && !image.isEmpty()) ? "transparent" : "var(--surface-200, rgba(128,128,128,0.2))";
        String style = "position: relative; display: inline-flex; align-items: center; justify-content: center; width: " + dim + "; height: " + dim + "; border-radius: " + br + "; background-color: " + bgColor + "; color: var(--text-color); font-size: " + fontS + ";";
        
        StringBuilder sb = new StringBuilder();
        sb.append("<div id=\"").append(id).append("\" class=\"espresso-avatar ").append(modifier.getClasses()).append("\" ");
        sb.append("style=\"").append(style).append(" ").append(modifier.getStyles()).append("\" ");
        for (java.util.Map.Entry<String, String> entry : modifier.getAttributes().entrySet()) {
            sb.append(entry.getKey()).append("=\"").append(entry.getValue()).append("\" ");
        }
        if (onClick != null) {
            sb.append("onclick=\"espressoFire('").append(id).append("')\" ");
        }
        sb.append(">");
        if (image != null && !image.isEmpty()) {
            sb.append("<img src=\"").append(image).append("\" style=\"width:100%; height:100%; object-fit:cover; border-radius:inherit;\" />");
        } else if (icon != null && !icon.isEmpty()) {
            sb.append(io.jettra.flux.widgets.Icon.of(icon).render(theme));
        } else if (label != null && !label.isEmpty()) {
            sb.append("<span style=\"font-weight:600;\">").append(label).append("</span>");
        }
        if (badge != null) {
            String badgeStr = badge.render(theme);
            // Inject absolute positioning into the badge span
            badgeStr = badgeStr.replace("style=\"", "style=\"position: absolute; top: -5px; right: -5px; z-index: 10; ");
            sb.append(badgeStr);
        }
        sb.append("</div>");
        return sb.toString();
    }
}

