package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

public class OutlinedButton extends Widget {
    private final Widget child;

    private OutlinedButton(Widget child) {
        this.child = child;
    }

    public static OutlinedButton of(String text) {
        return new OutlinedButton(Text.of(text));
    }
    
    public static OutlinedButton of(Widget child) {
        return new OutlinedButton(child);
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        String outlinedStyle = theme.buttonStyle.replace("background-color: " + theme.primaryColor, "background-color: transparent") + "; border: 2px solid " + theme.primaryColor + "; color: " + theme.primaryColor + ";";
        
        sb.append("<button ").append(renderCommonAttributes(theme, "espresso-outlined-button"))
          .append(" style=\"").append(outlinedStyle).append(" ").append(modifier.getStyles()).append("\">\n");
        if (child != null) {
            sb.append(child.render(theme));
        }
        sb.append("</button>\n");
        return sb.toString();
    }
}
