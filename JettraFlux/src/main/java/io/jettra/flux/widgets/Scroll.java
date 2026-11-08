package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.Arrays;
import java.util.List;

public class Scroll extends Widget {
    private final List<Widget> children;
    private String direction = "both"; // 'x', 'y', or 'both'

    private Scroll(List<Widget> children) {
        this.children = children;
    }

    public static Scroll of(Widget... children) {
        return new Scroll(Arrays.asList(children));
    }

    public Scroll direction(String dir) {
        this.direction = dir;
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        String overflowStyle = "overflow: auto;";
        if ("x".equalsIgnoreCase(direction)) overflowStyle = "overflow-x: auto; overflow-y: hidden;";
        if ("y".equalsIgnoreCase(direction)) overflowStyle = "overflow-y: auto; overflow-x: hidden;";

        sb.append("<div ").append(renderCommonAttributes(theme, "espresso-scroll"))
          .append(" style=\"").append(overflowStyle).append(" ")
          .append(modifier.getStyles()).append("\">\n");
        
        for (Widget child : children) {
            sb.append(child.render(theme));
        }
        sb.append("</div>\n");
        return sb.toString();
    }
}
