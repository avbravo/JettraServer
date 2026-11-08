package io.jettra.flux.widgets;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
public class Drawer extends Widget {
    private final List<Widget> children;
    private String position = "left";
    private Widget trigger;
    private final String id;
    
    private Drawer(List<Widget> children) {
        this.children = children;
        this.id = "drawer_" + UUID.randomUUID().toString().replace("-", "");
    }
    public static Drawer of(Widget... children) { return new Drawer(Arrays.asList(children)); }
    public Drawer position(String position) { this.position = position; return this; }
    public Drawer trigger(Widget trigger) { this.trigger = trigger; return this; }
    
    @Override public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        if (trigger != null) {
            sb.append("<div onclick=\"document.getElementById('").append(id).append("').classList.add('open');\">");
            sb.append(trigger.render(theme));
            sb.append("</div>");
        }
        
        String posStyle = "top:0; bottom:0; width:100%; max-width:300px; " + position + ":0; transform: translateX(" + (position.equals("right") ? "100%" : "-100%") + ");";
        if(position.equals("top")) posStyle = "top:0; left:0; right:0; height:100%; max-height:300px; transform: translateY(-100%);";
        else if(position.equals("bottom")) posStyle = "bottom:0; left:0; right:0; height:100%; max-height:300px; transform: translateY(100%);";
        
        sb.append("<div id=\"").append(id).append("\" class=\"jettra-drawer-comp\" style=\"position:fixed; background:var(--surface-color, #fff); z-index:2000; transition: transform 0.3s ease; box-shadow:0 0 15px rgba(0,0,0,0.2); ").append(posStyle).append("\">");
        sb.append("<div style=\"padding:15px; display:flex; justify-content:space-between; align-items:center; border-bottom:1px solid rgba(0,0,0,0.1);\">");
        sb.append("<span style=\"font-weight:bold;\">Drawer</span>");
        sb.append("<button onclick=\"document.getElementById('").append(id).append("').classList.remove('open');\" style=\"background:none; border:none; font-size:1.5rem; cursor:pointer;\">&times;</button>");
        sb.append("</div>");
        sb.append("<div style=\"padding:15px; overflow-y:auto; height:calc(100% - 60px);\">");
        for (Widget child : children) sb.append(child.render(theme));
        sb.append("</div></div>");
        
        sb.append("<style>#").append(id).append(".open { transform: translate(0,0) !important; }</style>");
        
        return sb.toString();
    }
}
