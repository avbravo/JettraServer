package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.Arrays;
import java.util.List;

public class Row extends Widget {
    private final List<Widget> children;

    private Row(List<Widget> children) {
        this.children = children;
    }

    public static Row of(Widget... children) {
        return new Row(Arrays.asList(children));
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        String layoutStyle = "display: flex; flex-direction: row; gap: 8px; align-items: center;";
        
        sb.append("<div ").append(renderCommonAttributes(theme, "")).append("style=\"").append(layoutStyle).append(modifier.getStyles()).append("\">\n");
        for (Widget child : children) {
            sb.append(child.render(theme));
        }
        sb.append("</div>\n");
        return sb.toString();
    }
}
