package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.Arrays;
import java.util.List;

public class Column extends Widget {
    private final List<Widget> children;

    private Column(List<Widget> children) {
        this.children = children;
    }

    public static Column of(Widget... children) {
        return new Column(new java.util.ArrayList<>(Arrays.asList(children)));
    }

    public Column add(Widget child) {
        this.children.add(child);
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        String layoutStyle = "display: flex; flex-direction: column; gap: 8px;";
        
        sb.append("<div ").append(renderCommonAttributes(theme, "")).append("style=\"").append(layoutStyle).append(modifier.getStyles()).append("\">\n");
        for (Widget child : children) {
            sb.append(child.render(theme));
        }
        sb.append("</div>\n");
        return sb.toString();
    }
}
