package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

public class ElevatedButton extends Widget {
    private final Widget child;

    private ElevatedButton(Widget child) {
        this.child = child;
    }

    public static ElevatedButton of(String text) {
        return new ElevatedButton(Text.of(text));
    }
    
    public static ElevatedButton of(Widget child) {
        return new ElevatedButton(child);
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        sb.append("<button ").append(renderCommonAttributes(theme, "espresso-button", theme.buttonStyle)).append(">\n");
        if (child != null) {
            sb.append(child.render(theme));
        }
        sb.append("</button>\n");
        return sb.toString();
    }
}
