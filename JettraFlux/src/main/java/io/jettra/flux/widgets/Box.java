package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Box container in JettraFlux inspired by modern declarative UI frameworks.
 * Provides fillMaxSize and fillMaxWidth presets for responsive layout nesting.
 */
public class Box extends Widget {

    private final List<Widget> children = new ArrayList<>();
    private boolean fillWidth = false;
    private boolean fillHeight = false;

    public Box() {
        super();
        this.id = "jettra-box-" + System.identityHashCode(this);
    }

    public static Box of(Widget... widgets) {
        Box box = new Box();
        if (widgets != null) {
            box.children.addAll(Arrays.asList(widgets));
        }
        return box;
    }

    public static Box fillMaxSize(Widget... widgets) {
        Box box = new Box();
        box.fillWidth = true;
        box.fillHeight = true;
        if (widgets != null) {
            box.children.addAll(Arrays.asList(widgets));
        }
        return box;
    }

    public static Box fillMaxWidth(Widget... widgets) {
        Box box = new Box();
        box.fillWidth = true;
        if (widgets != null) {
            box.children.addAll(Arrays.asList(widgets));
        }
        return box;
    }

    public Box add(Widget child) {
        if (child != null) {
            this.children.add(child);
        }
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        StringBuilder styles = new StringBuilder("box-sizing:border-box; ");
        if (fillWidth) {
            styles.append("width:100%; min-width:100%; ");
        }
        if (fillHeight) {
            styles.append("height:100%; min-height:100%; flex:1; ");
        }

        sb.append("<div id=\"").append(id).append("\" ")
          .append("class=\"jettra-box ").append(modifier != null ? modifier.getClasses() : "").append("\" ")
          .append("style=\"").append(styles).append(modifier != null ? modifier.getStyles() : "").append("\">\n");

        for (Widget child : children) {
            if (child != null) {
                sb.append(child.render(theme)).append("\n");
            }
        }

        sb.append("</div>\n");
        return sb.toString();
    }
}
