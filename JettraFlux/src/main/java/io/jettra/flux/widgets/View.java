package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.Arrays;
import java.util.List;

public class View extends Widget {
    private final List<Widget> children;

    private View(List<Widget> children) {
        this.children = children;
    }

    public static View of(Widget... children) {
        return new View(Arrays.asList(children));
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        // A View typically takes up full width/height or acts as a main screen section.
        sb.append("<div ").append(renderCommonAttributes(theme, "espresso-view"))
          .append(" style=\"width: 100%; height: 100%; box-sizing: border-box; ")
          .append(modifier.getStyles()).append("\">\n");
        
        for (Widget child : children) {
            sb.append(child.render(theme));
        }
        sb.append("</div>\n");
        return sb.toString();
    }
}
