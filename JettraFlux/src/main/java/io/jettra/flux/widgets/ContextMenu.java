package io.jettra.flux.widgets;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.Arrays;
import java.util.List;
public class ContextMenu extends Widget {
    private final List<WidgetLet> items;
    private Widget target;
    private final String id;
    
    private ContextMenu(List<WidgetLet> items) {
        this.items = items;
        this.id = "ctx_" + java.util.UUID.randomUUID().toString().replace("-", "");
    }
    public static ContextMenu of(WidgetLet... items) { return new ContextMenu(Arrays.asList(items)); }
    public ContextMenu target(Widget target) { this.target = target; return this; }
    
    @Override public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div style=\"position: relative; display: inline-block;\" oncontextmenu=\"event.preventDefault(); var p=document.getElementById('").append(id).append("'); p.style.display='block'; p.style.left=event.offsetX+'px'; p.style.top=event.offsetY+'px';\" onmouseleave=\"document.getElementById('").append(id).append("').style.display='none';\">");
        if (target != null) sb.append(target.render(theme));
        
        sb.append("<div id=\"").append(id).append("\" class=\"jettra-contextmenu\" ").append(renderCommonAttributes(theme, "espresso-contextmenu")).append(" style=\"display:none; position:absolute; background:var(--surface-color); padding:10px; border-radius:8px; box-shadow:0 4px 6px rgba(0,0,0,0.1); border:1px solid rgba(0,0,0,0.1); z-index:1000; min-width: 150px;\">");
        for (WidgetLet item : items) {
            item.popup(true);
            sb.append("<div style=\"margin-bottom: 5px;\">");
            sb.append(item.render(theme));
            sb.append("</div>");
        }
        sb.append("</div></div>");
        return sb.toString();
    }
}
