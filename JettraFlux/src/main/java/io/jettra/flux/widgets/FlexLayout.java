package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Flexible layout container with responsive fluid presets in JettraFlux.
 */
public class FlexLayout extends Widget {

    private final List<Widget> children = new ArrayList<>();
    private String direction = "column";
    private boolean fullWidth = true;

    public FlexLayout() {
        super();
        this.id = "jettra-flex-layout-" + System.identityHashCode(this);
    }

    public static FlexLayout fullWidth(Widget... widgets) {
        FlexLayout layout = new FlexLayout();
        layout.fullWidth = true;
        if (widgets != null) {
            layout.children.addAll(Arrays.asList(widgets));
        }
        return layout;
    }

    public static FlexLayout row(Widget... widgets) {
        FlexLayout layout = new FlexLayout();
        layout.direction = "row";
        if (widgets != null) {
            layout.children.addAll(Arrays.asList(widgets));
        }
        return layout;
    }

    public static FlexLayout column(Widget... widgets) {
        FlexLayout layout = new FlexLayout();
        layout.direction = "column";
        if (widgets != null) {
            layout.children.addAll(Arrays.asList(widgets));
        }
        return layout;
    }

    public FlexLayout direction(String direction) {
        this.direction = direction;
        return this;
    }

    public FlexLayout add(Widget child) {
        if (child != null) {
            this.children.add(child);
        }
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        String widthStyle = fullWidth ? "width:100%; min-width:100%; box-sizing:border-box; flex:1; " : "";

        sb.append("<div id=\"").append(id).append("\" ")
          .append("class=\"jettra-flex-layout ").append(modifier != null ? modifier.getClasses() : "").append("\" ")
          .append("style=\"display:flex; flex-direction:").append(direction).append("; ")
          .append(widthStyle)
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
