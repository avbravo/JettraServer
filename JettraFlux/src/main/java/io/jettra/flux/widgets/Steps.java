package io.jettra.flux.widgets;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.Arrays;
import java.util.List;
public class Steps extends Widget {
    private final List<WidgetLet> items;
    private int activeIndex = 0;
    private Steps(List<WidgetLet> items) { this.items = items; }
    public static Steps of(WidgetLet... items) { return new Steps(Arrays.asList(items)); }
    public Steps activeIndex(int index) { this.activeIndex = index; return this; }
    @Override public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder("<div ").append(renderCommonAttributes(theme, "steps-container")).append(" style=\"display:flex; justify-content:space-between;\">");
        for (int i = 0; i < items.size(); i++) {
            WidgetLet item = items.get(i);
            String color = (i <= activeIndex) ? "var(--primary-color)" : "var(--surface-alt, #ccc)";
            String cursor = (item.getUrl() != null && !item.getUrl().isEmpty()) ? "cursor:pointer;" : "";
            String onClickAttr = (item.getUrl() != null && !item.getUrl().isEmpty()) ? " onclick=\"window.location.href='" + item.getUrl() + "'\"" : "";
            sb.append("<div style=\"display:flex; flex-direction:column; align-items:center; flex:1;").append(cursor).append("\"").append(onClickAttr).append("><div style=\"width:30px; height:30px; border-radius:50%; background:").append(color).append("; color:white; display:flex; align-items:center; justify-content:center;\">").append(i+1).append("</div><span style=\"margin-top:8px; font-weight:bold; color:").append(color).append(";\">").append(item.getTitle()).append("</span></div>");
        }
        sb.append("</div>");
        return sb.toString();
    }
}
