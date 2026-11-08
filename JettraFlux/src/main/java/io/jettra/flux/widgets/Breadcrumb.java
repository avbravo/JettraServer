package io.jettra.flux.widgets;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.Arrays;
import java.util.List;
public class Breadcrumb extends Widget {
    private final List<WidgetLet> items;
    private Breadcrumb(List<WidgetLet> items) { this.items = items; }
    public static Breadcrumb of(WidgetLet... items) { return new Breadcrumb(Arrays.asList(items)); }
    @Override public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder("<nav ").append(renderCommonAttributes(theme, "breadcrumb-nav")).append("><ol style=\"list-style:none; padding:0; display:flex; gap:8px; margin:0; align-items:center;\">");
        for (int i = 0; i < items.size(); i++) {
            WidgetLet item = items.get(i);
            String cursor = (item.getUrl() != null && !item.getUrl().isEmpty()) ? "cursor:pointer;" : "";
            String onClickAttr = (item.getUrl() != null && !item.getUrl().isEmpty()) ? " onclick=\"window.location.href='" + item.getUrl() + "'\"" : "";
            sb.append("<li style=\"color: var(--primary-color); display:flex; align-items:center; gap:5px;").append(cursor).append("\"").append(onClickAttr).append(">");
            if (item.getIcon() != null && !item.getIcon().isEmpty()) {
                if (item.getIcon().trim().startsWith("<svg")) {
                    sb.append("<span style=\"width:1em; height:1em; display:inline-flex;\">").append(item.getIcon()).append("</span>");
                } else {
                    sb.append("<i class=\"").append(item.getIcon()).append("\"></i>");
                }
            }
            if (item.getTitle() != null && !item.getTitle().isEmpty()) {
                sb.append("<span>").append(item.getTitle()).append("</span>");
            }
            sb.append("</li>");
            if (i < items.size() - 1) sb.append("<li style=\"color:var(--text-color);\">/</li>");
        }
        sb.append("</ol></nav>");
        return sb.toString();
    }
}
