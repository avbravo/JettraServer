package io.jettra.flux.widgets;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.Arrays;
import java.util.List;
public class MegaMenu extends Widget {
    private final List<WidgetLet> items;
    private String orientation = "horizontal";
    
    private MegaMenu(List<WidgetLet> items) { this.items = items; }
    public static MegaMenu of(WidgetLet... items) { return new MegaMenu(Arrays.asList(items)); }
    public MegaMenu orientation(String orientation) { this.orientation = orientation; return this; }
    
    @Override public String render(ThemeData theme) {
        String flexDir = "vertical".equalsIgnoreCase(orientation) ? "column" : "row";
        StringBuilder sb = new StringBuilder("<div ").append(renderCommonAttributes(theme, "menu-MegaMenu")).append(" style=\"display:flex; flex-direction:").append(flexDir).append("; gap:15px; background:var(--surface-color); padding:10px; border-radius:8px; border:1px solid rgba(128,128,128,0.2); width:100%;\">");
        for (WidgetLet item : items) {
            item.popup(true);
            sb.append("<div style=\"flex:1;\">");
            sb.append(item.render(theme));
            sb.append("</div>");
        }
        sb.append("<script>document.addEventListener('click', function(e) { if(!e.target.closest('.widgetlet-item')) { document.querySelectorAll('.widgetlet-popup').forEach(function(p){ p.style.display = 'none'; }); } });</script>");
        sb.append("</div>");
        return sb.toString();
    }
}
