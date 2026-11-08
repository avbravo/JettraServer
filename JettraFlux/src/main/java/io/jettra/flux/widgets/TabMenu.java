package io.jettra.flux.widgets;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
public class TabMenu extends Widget {
    private final List<WidgetLet> items;
    private final String id;
    
    private TabMenu(List<WidgetLet> items) {
        this.items = items;
        this.id = "tab_" + UUID.randomUUID().toString().replace("-", "");
    }
    public static TabMenu of(WidgetLet... items) { return new TabMenu(Arrays.asList(items)); }
    
    @Override public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div id=\"").append(id).append("\" class=\"jettra-tabmenu\" ").append(renderCommonAttributes(theme, "espresso-tabmenu")).append(" style=\"display:flex; overflow-x:auto; border-bottom:2px solid var(--surface-border, #e2e8f0); gap:5px;\">");
        
        for (int i = 0; i < items.size(); i++) {
            WidgetLet item = items.get(i);
            String activeStyle = (i == 0) ? "border-bottom: 2px solid var(--primary-color, #3b82f6); color: var(--primary-color, #3b82f6);" : "border-bottom: 2px solid transparent;";
            sb.append("<div class=\"tab-item\" style=\"padding: 10px 20px; cursor: pointer; margin-bottom: -2px; ").append(activeStyle).append("\" onclick=\"var tabs=this.parentElement.children; for(var j=0; j<tabs.length; j++){ tabs[j].style.borderBottomColor='transparent'; tabs[j].style.color=''; } this.style.borderBottomColor='var(--primary-color, #3b82f6)'; this.style.color='var(--primary-color, #3b82f6)';\">");
            sb.append(item.render(theme));
            sb.append("</div>");
        }
        sb.append("</div>");
        return sb.toString();
    }
}
