package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

public class Divider extends Widget {
    public enum Layout {
        HORIZONTAL, VERTICAL
    }

    private Layout layout = Layout.HORIZONTAL;
    private final String content;

    private Divider(String content) {
        this.content = content;
    }

    public static Divider of() {
        return new Divider(null);
    }
    
    public static Divider of(String content) {
        return new Divider(content);
    }

    public Divider layout(Layout layout) {
        this.layout = layout;
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        String baseClass = "espresso-divider espresso-divider-" + layout.name().toLowerCase();
        if (content != null) {
            baseClass += " espresso-divider-with-content";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("<div ").append(renderCommonAttributes(theme, baseClass)).append(">");
        
        if (content != null) {
            sb.append("<span class=\"espresso-divider-content\">").append(content).append("</span>");
        }
        
        sb.append("</div>");
        return sb.toString();
    }
}
