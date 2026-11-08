package io.jettra.flux.widgets;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.Arrays;
import java.util.List;
public class OverlayMenu extends Widget {
    private final List<WidgetLet> items;
    private Widget trigger;
    private final String id;
    private boolean alignRight = false;
    
    private OverlayMenu(List<WidgetLet> items) {
        this.items = items;
        this.id = "om_" + java.util.UUID.randomUUID().toString().replace("-", "");
    }
    public static OverlayMenu of(WidgetLet... items) { return new OverlayMenu(Arrays.asList(items)); }
    public OverlayMenu trigger(Widget trigger) { this.trigger = trigger; return this; }
    
    public OverlayMenu alignRight() {
        this.alignRight = true;
        return this;
    }
    
    @Override public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div style=\"position: relative; display: inline-block;\">");
        sb.append("<div onclick=\"var p=document.getElementById('").append(id).append("'); p.style.display=(p.style.display==='none'?'block':'none');\">");
        if (trigger != null) sb.append(trigger.render(theme));
        sb.append("</div>");
        
        String alignStyle = alignRight ? "right:0;" : "left:0;";
        sb.append("<div id=\"").append(id).append("\" class=\"jettra-overlaymenu\" ").append(renderCommonAttributes(theme, "espresso-overlaymenu")).append(" style=\"display:none; position:absolute; top:100%; ").append(alignStyle).append(" margin-top:5px; background:var(--surface-color); padding:10px; border-radius:8px; box-shadow:0 4px 6px rgba(0,0,0,0.1); border:1px solid rgba(0,0,0,0.1); z-index:1000; min-width: 200px;\">");
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
