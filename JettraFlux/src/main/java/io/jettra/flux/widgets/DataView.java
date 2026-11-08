package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.List;

public class DataView extends Widget {

    public enum Layout {
        LIST, GRID
    }

    private final List<Widget> items;
    private Layout layout = Layout.LIST;
    private Widget header;

    private DataView(List<Widget> items) {
        this.items = items;
    }

    public static DataView of(List<Widget> items) {
        return new DataView(items);
    }

    public DataView layout(Layout layout) {
        this.layout = layout;
        return this;
    }

    public DataView header(Widget header) {
        this.header = header;
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div ").append(renderCommonAttributes(theme, "espresso-dataview")).append(">\n");
        
        if (header != null) {
            sb.append("  <div class=\"espresso-dataview-header\">\n");
            sb.append("    ").append(header.render(theme)).append("\n");
            sb.append("  </div>\n");
        }

        sb.append("  <div class=\"espresso-dataview-content\">\n");
        
        String containerClass = layout == Layout.LIST ? "espresso-dataview-list" : "espresso-dataview-grid";
        String itemClass = layout == Layout.LIST ? "dataview-list-item" : "dataview-grid-item";
        
        sb.append("    <div class=\"").append(containerClass).append("\">\n");
        
        for (Widget item : items) {
            sb.append("      <div class=\"").append(itemClass).append("\">\n");
            sb.append("        ").append(item.render(theme)).append("\n");
            sb.append("      </div>\n");
        }
        
        sb.append("    </div>\n"); // End container
        sb.append("  </div>\n"); // End content
        
        sb.append("</div>\n"); // End dataview
        return sb.toString();
    }
}
