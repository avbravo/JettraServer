package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Modern elastic flex column container for JettraFlux applications.
 * Ensures consistent flex-direction: column flow without clipping or overflow constraints.
 */
public class FlexColumn extends Widget {

    private final List<Widget> children = new ArrayList<>();

    public FlexColumn() {
        super();
    }

    public static FlexColumn of(Widget... widgets) {
        FlexColumn column = new FlexColumn();
        if (widgets != null) {
            column.children.addAll(Arrays.asList(widgets));
        }
        return column;
    }

    public static FlexColumn of(List<Widget> widgets) {
        FlexColumn column = new FlexColumn();
        if (widgets != null) {
            column.children.addAll(widgets);
        }
        return column;
    }

    public FlexColumn add(Widget widget) {
        if (widget != null) {
            this.children.add(widget);
        }
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div id=\"").append(id).append("\" ")
          .append("class=\"jettra-flex-column ").append(modifier != null ? modifier.getClasses() : "").append("\" ")
          .append("style=\"display:flex; flex-direction:column; width:100%; box-sizing:border-box; ")
          .append(modifier != null ? modifier.getStyles() : "")
          .append("\">\n");

        for (Widget child : children) {
            if (child != null) {
                sb.append(child.render(theme)).append("\n");
            }
        }

        sb.append("</div>\n");
        return sb.toString();
    }
}
