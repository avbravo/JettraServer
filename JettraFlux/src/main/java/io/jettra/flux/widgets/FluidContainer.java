package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Responsive full-width fluid container for JettraFlux applications and dashboards.
 * Expands elastically across 100% of the available horizontal canvas right up to the edge,
 * adhering to modern responsive layout principles without fixed max-width bottlenecks.
 */
public class FluidContainer extends Widget {

    private final List<Widget> children = new ArrayList<>();
    private String padding = "16px 20px";

    public FluidContainer() {
        super();
        this.id = "jettra-fluid-container-" + System.identityHashCode(this);
    }

    public static FluidContainer of(Widget... widgets) {
        FluidContainer container = new FluidContainer();
        if (widgets != null) {
            container.children.addAll(Arrays.asList(widgets));
        }
        return container;
    }

    public static FluidContainer of(List<Widget> widgets) {
        FluidContainer container = new FluidContainer();
        if (widgets != null) {
            container.children.addAll(widgets);
        }
        return container;
    }

    public FluidContainer padding(int vertical, int horizontal) {
        this.padding = vertical + "px " + horizontal + "px";
        return this;
    }

    public FluidContainer padding(int all) {
        this.padding = all + "px";
        return this;
    }

    public FluidContainer add(Widget child) {
        if (child != null) {
            this.children.add(child);
        }
        return this;
    }

    public List<Widget> getChildren() {
        return children;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div id=\"").append(id).append("\" ")
          .append("class=\"jettra-fluid-container ").append(modifier != null ? modifier.getClasses() : "").append("\" ")
          .append("style=\"display:flex; flex-direction:column; width:100%; min-width:100%; max-width:100%; flex:1; box-sizing:border-box; ")
          .append("padding:").append(padding).append("; ")
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
