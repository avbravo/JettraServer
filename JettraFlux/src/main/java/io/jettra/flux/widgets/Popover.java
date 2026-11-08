package io.jettra.flux.widgets;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
public class Popover extends Widget {
    private final List<Widget> children;
    private Widget target;
    private final String id;
    
    private Popover(List<Widget> children) {
        this.children = children;
        this.id = "popover_" + UUID.randomUUID().toString().replace("-", "");
    }
    public static Popover of(Widget... children) { return new Popover(Arrays.asList(children)); }
    public Popover target(Widget target) { this.target = target; return this; }
    
    @Override public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div style=\"position: relative; display: inline-block;\">");
        sb.append("<div onclick=\"var p=document.getElementById('").append(id).append("'); p.style.display=(p.style.display==='none'?'block':'none');\">");
        if (target != null) sb.append(target.render(theme));
        sb.append("</div>");
        
        sb.append("<div id=\"").append(id).append("\" class=\"jettra-popover\" ").append(renderCommonAttributes(theme, "espresso-popover")).append(" style=\"display:none; position:absolute; top:100%; left:0; margin-top:8px; background:var(--surface-color, #fff); padding:10px; border-radius:8px; box-shadow:0 4px 6px rgba(0,0,0,0.1); border:1px solid rgba(0,0,0,0.1); z-index:1000; min-width: 200px;\">");
        for (Widget child : children) sb.append(child.render(theme));
        sb.append("</div></div>");
        return sb.toString();
    }
}
