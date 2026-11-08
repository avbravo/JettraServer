package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

import java.util.ArrayList;
import java.util.List;

/**
 * Composable body container for JettraFlux Panel containers.
 * Supports standard linear flow layouts and responsive CSS grid distributions.
 */
public class PanelBody extends Widget {

    private boolean isGrid = false;
    private int columns = 4;
    private String gap = "16px";
    private String padding = "20px 24px";
    private final List<Widget> children = new ArrayList<>();

    public PanelBody() {}

    public static PanelBody of(Widget... children) {
        PanelBody body = new PanelBody();
        if (children != null) {
            for (Widget w : children) {
                if (w != null) body.children.add(w);
            }
        }
        return body;
    }

    public static PanelBody grid(int columns, Widget... children) {
        PanelBody body = new PanelBody();
        body.isGrid = true;
        body.columns = Math.max(1, columns);
        if (children != null) {
            for (Widget w : children) {
                if (w != null) body.children.add(w);
            }
        }
        return body;
    }

    public static PanelBodyBuilder builder() {
        return new PanelBodyBuilder();
    }

    public PanelBody grid(boolean isGrid) {
        this.isGrid = isGrid;
        return this;
    }

    public PanelBody columns(int columns) {
        this.columns = Math.max(1, columns);
        this.isGrid = true;
        return this;
    }

    public PanelBody gap(String gap) {
        this.gap = gap != null ? gap : "16px";
        return this;
    }

    public PanelBody padding(String padding) {
        this.padding = padding != null ? padding : "20px 24px";
        return this;
    }

    public PanelBody add(Widget child) {
        if (child != null) children.add(child);
        return this;
    }

    public PanelBody addAll(List<Widget> list) {
        if (list != null) {
            for (Widget w : list) {
                if (w != null) children.add(w);
            }
        }
        return this;
    }

    public List<Widget> getChildren() {
        return List.copyOf(children);
    }

    public boolean isGrid() {
        return isGrid;
    }

    public int getColumns() {
        return columns;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"espresso-panel-content jettra-panel-body ")
          .append(modifier != null ? modifier.getClasses() : "").append("\" ");

        String layoutStyle;
        if (isGrid) {
            layoutStyle = "display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: " + gap + "; padding: " + padding + ";";
        } else {
            layoutStyle = "display: flex; flex-direction: column; gap: " + gap + "; padding: " + padding + ";";
        }

        sb.append("style=\"").append(layoutStyle).append(" ")
          .append(modifier != null ? modifier.getStyles() : "").append("\">\n");

        for (Widget child : children) {
            sb.append("  ").append(child.render(theme)).append("\n");
        }

        sb.append("</div>\n");
        return sb.toString();
    }

    public static class PanelBodyBuilder {
        private final PanelBody body = new PanelBody();

        public PanelBodyBuilder grid(int columns) { body.columns(columns); return this; }
        public PanelBodyBuilder gap(String gap) { body.gap(gap); return this; }
        public PanelBodyBuilder padding(String padding) { body.padding(padding); return this; }
        public PanelBodyBuilder add(Widget child) { body.add(child); return this; }
        public PanelBodyBuilder addAll(List<Widget> list) { body.addAll(list); return this; }
        public PanelBody build() { return body; }
    }
}
