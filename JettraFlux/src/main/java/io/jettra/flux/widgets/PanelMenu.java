package io.jettra.flux.widgets;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
public class PanelMenu extends Widget {
    private final List<WidgetLet> items;
    private final String id;
    
    private PanelMenu(List<WidgetLet> items) {
        this.items = items;
        this.id = "panel_" + UUID.randomUUID().toString().replace("-", "");
    }
    public static PanelMenu of(WidgetLet... items) { return new PanelMenu(Arrays.asList(items)); }
    
    @Override public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div id=\"").append(id).append("\" class=\"jettra-panelmenu\" ").append(renderCommonAttributes(theme, "espresso-panelmenu")).append(" style=\"display:flex; flex-direction:column; border:1px solid var(--surface-border, #e2e8f0); border-radius:6px; overflow:hidden;\">");
        
        for (WidgetLet item : items) {
            item.popup(false);
            sb.append("<div style=\"border-bottom:1px solid var(--surface-border, #e2e8f0); background:var(--surface-color); padding:10px;\">");
            sb.append(item.render(theme));
            sb.append("</div>");
        }
        sb.append("</div>");
        return sb.toString();
    }
}
