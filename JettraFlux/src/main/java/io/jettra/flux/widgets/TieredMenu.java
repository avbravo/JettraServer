package io.jettra.flux.widgets;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.Arrays;
import java.util.List;
public class TieredMenu extends Widget {
    private final List<WidgetLet> items;
    private TieredMenu(List<WidgetLet> items) { this.items = items; }
    public static TieredMenu of(WidgetLet... items) { return new TieredMenu(Arrays.asList(items)); }
    @Override public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder("<div ").append(renderCommonAttributes(theme, "menu-TieredMenu")).append(" style=\"display:flex; flex-direction:column; gap:5px; background:var(--surface-color); padding:10px; border-radius:8px; border:1px solid rgba(128,128,128,0.2); width: 200px;\">");
        for (WidgetLet item : items) {
            item.popup(true);
            sb.append(item.render(theme));
        }
        sb.append("<script>document.addEventListener('click', function(e) { if(!e.target.closest('.widgetlet-item')) { document.querySelectorAll('.widgetlet-popup').forEach(function(p){ p.style.display = 'none'; }); } });</script>");
        sb.append("</div>");
        return sb.toString();
    }
}
