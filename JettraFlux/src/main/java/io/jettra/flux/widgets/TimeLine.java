package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.Arrays;
import java.util.List;

public class TimeLine extends Widget {
    private final List<Widget> children;

    private String layout = "vertical";

    private TimeLine(List<Widget> children) {
        this.children = children;
    }

    public static TimeLine of(Widget... children) {
        return new TimeLine(Arrays.asList(children));
    }
    
    public TimeLine layout(String layout) {
        this.layout = layout;
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        boolean isHorizontal = "horizontal".equalsIgnoreCase(layout);
        String style = isHorizontal ? "display: flex; flex-direction: row; width: 100%;" : "display: flex; flex-direction: column;";
        
        sb.append("<div ").append(renderCommonAttributes(theme, "espresso-timeline")).append(" style=\"").append(style).append("\">\n");
        for (Widget child : children) {
            if (child instanceof TimeLineItem) {
                ((TimeLineItem) child).layout(layout);
            }
            sb.append(child.render(theme));
        }
        sb.append("</div>\n");
        return sb.toString();
    }
}
