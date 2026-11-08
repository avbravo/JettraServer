package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

public class Container extends Widget {
    private Widget child;

    public Container(Widget child) {
        this.child = child;
    }

    public static Container of(Widget child) {
        return new Container(child);
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("<div ").append(renderCommonAttributes(theme, "espresso-container", theme.containerStyle)).append(">\n");
        if (child != null) {
            sb.append(child.render(theme));
        }
        sb.append("</div>\n");
        return sb.toString();
    }
}
