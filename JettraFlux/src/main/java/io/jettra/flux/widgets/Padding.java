package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

public class Padding extends Widget {
    private final Widget child;
    private final int paddingValue;

    private Padding(int paddingValue, Widget child) {
        this.paddingValue = paddingValue;
        this.child = child;
    }

    public static Padding of(int padding, Widget child) {
        return new Padding(padding, child);
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        String paddingStyle = "padding: " + paddingValue + "px;";
        
        sb.append("<div ").append(renderCommonAttributes(theme, "espresso-padding")).append(" style=\"").append(paddingStyle).append(" ").append(modifier.getStyles()).append("\">\n");
        if (child != null) {
            sb.append(child.render(theme));
        }
        sb.append("</div>\n");
        return sb.toString();
    }
}
