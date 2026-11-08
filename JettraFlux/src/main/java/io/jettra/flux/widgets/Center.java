package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

public class Center extends Widget {
    private final Widget child;

    private Center(Widget child) {
        this.child = child;
    }

    public static Center of(Widget child) {
        return new Center(child);
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        String centerStyle = "display: flex; justify-content: center; align-items: center; width: 100%; height: 100%;";
        
        sb.append("<div ").append(renderCommonAttributes(theme, "espresso-center")).append(" style=\"").append(centerStyle).append(" ").append(modifier.getStyles()).append("\">\n");
        if (child != null) {
            sb.append(child.render(theme));
        }
        sb.append("</div>\n");
        return sb.toString();
    }
}
