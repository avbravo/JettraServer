package io.jettra.flux.widgets;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.Arrays;
import java.util.List;
public class PlainMenu extends Widget {
    private final List<WidgetLet> items;
    private PlainMenu(List<WidgetLet> items) { this.items = items; }
    public static PlainMenu of(WidgetLet... items) { return new PlainMenu(Arrays.asList(items)); }
    @Override public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder("<div ").append(renderCommonAttributes(theme, "menu-PlainMenu")).append(" style=\"display:flex; gap:15px; background:var(--surface-color); padding:10px; border-radius:8px; border:1px solid rgba(128,128,128,0.2);\">");
        for (WidgetLet item : items) {
            item.popup(true);
            sb.append(item.render(theme));
        }
        sb.append("<script>document.addEventListener('click', function(e) { if(!e.target.closest('.widgetlet-item')) { document.querySelectorAll('.widgetlet-popup').forEach(function(p){ p.style.display = 'none'; }); } });</script>");
        sb.append("</div>");
        return sb.toString();
    }
}
