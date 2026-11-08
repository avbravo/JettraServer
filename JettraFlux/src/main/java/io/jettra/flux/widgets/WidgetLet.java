package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WidgetLet extends Widget {
    private String title;
    private String icon;
    private final List<WidgetLet> children = new ArrayList<>();
    private Widget actionWidget;
    private String url;
    private boolean popup;
    private Widget iconWidget;

    public WidgetLet(String title) {
        this.title = title;
    }

    public static WidgetLet of(String title) {
        return new WidgetLet(title);
    }

    public WidgetLet icon(String iconClass) {
        this.icon = iconClass;
        return this;
    }
    public WidgetLet icon(Widget iconWidget) { this.iconWidget = iconWidget; return this; }

    public WidgetLet url(String url) {
        this.url = url;
        return this;
    }

    public WidgetLet add(WidgetLet child) {
        child.popup(this.popup);
        this.children.add(child);
        return this;
    }
    
    public WidgetLet action(Widget widget) {
        this.actionWidget = widget;
        return this;
    }
    
    public WidgetLet popup(boolean popup) {
        this.popup = popup;
        for (WidgetLet child : children) {
            child.popup(popup);
        }
        return this;
    }

    public String getTitle() { return title; }
    public String getUrl() { return url; }
    public String getIcon() { return icon; }
    public List<WidgetLet> getChildren() { return children; }

    @Override
    public String render(ThemeData theme) {
        String id = "wl_" + UUID.randomUUID().toString().replace("-", "");
        StringBuilder sb = new StringBuilder();
        
        String containerStyle = popup ? "padding: 8px; margin-left: 10px; position: relative;" : "padding: 8px; margin-left: 10px;";
        sb.append("<div class=\"widgetlet-item\" style=\"").append(containerStyle).append("\">");
        
        sb.append("<div style=\"display: flex; align-items: center; cursor: pointer; justify-content: space-between;\" ");
        if (url != null && !url.isEmpty()) {
            if (url.startsWith("javascript:")) {
                sb.append("onclick=\"").append(url.substring(11).replace("\"", "&quot;")).append("\" ");
            } else {
                sb.append("onclick=\"window.location.href='").append(url).append("'\" ");
            }
        } else if (!children.isEmpty()) {
            if (popup) {
                sb.append("onclick=\"var el = document.getElementById('").append(id).append("_children'); ")
                  .append("var isHidden = el.style.display === 'none'; ")
                  .append("document.querySelectorAll('.widgetlet-popup').forEach(function(p){ p.style.display = 'none'; }); ")
                  .append("el.style.display = isHidden ? 'block' : 'none'; event.stopPropagation();\" ");
            } else {
                String storageKey = "menu_exp_" + title.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
                sb.append("onclick=\"var el = document.getElementById('").append(id).append("_children'); ")
                  .append("var icon = document.getElementById('").append(id).append("_icon'); ")
                  .append("var isHidden = el.style.display === 'none'; ")
                  .append("el.style.display = isHidden ? 'block' : 'none'; ")
                  .append("icon.className = isHidden ? 'fas fa-chevron-down' : 'fas fa-chevron-right'; ")
                  .append("localStorage.setItem('").append(storageKey).append("', isHidden ? 'open' : 'closed');\" ");
            }
        }
        sb.append(">");
        
        sb.append("<div style=\"display: flex; align-items: center;\">");
        if (iconWidget != null) {
            sb.append("<span style=\"margin-right: 8px; display: flex; align-items: center;\">").append(iconWidget.render(theme)).append("</span>");
        } else if (icon != null && !icon.isEmpty()) {
            sb.append("<span style=\"margin-right: 8px; display: flex; align-items: center;\">").append(io.jettra.flux.widgets.Icon.of(icon).render(theme)).append("</span>");
        }
        sb.append("<span style=\"font-weight: bold; white-space: nowrap;\">").append(title).append("</span>");
        sb.append("</div>");
        
        if (actionWidget != null) {
            sb.append("<div style=\"margin-left: 10px;\">").append(actionWidget.render(theme)).append("</div>");
        } else if (!children.isEmpty()) {
            String chevronClass = popup ? "fas fa-chevron-down" : "fas fa-chevron-right";
            sb.append("<span style=\"margin-left: 10px; color: #888;\"><i id=\"").append(id).append("_icon\" class=\"").append(chevronClass).append("\"></i></span>");
        }
        
        sb.append("</div>");

        if (!children.isEmpty()) {
            if (popup) {
                sb.append("<div id=\"").append(id).append("_children\" class=\"widgetlet-children widgetlet-popup\" style=\"display: none; position: absolute; top: 100%; left: 0; min-width: 200px; background: var(--surface-color); box-shadow: 0 10px 15px -3px rgba(0,0,0,0.1); border-radius: 8px; border: 1px solid rgba(128,128,128,0.2); padding: 10px; z-index: 1000;\">");
                for (WidgetLet child : children) {
                    sb.append(child.render(theme));
                }
                sb.append("</div>");
            } else {
                String storageKey = "menu_exp_" + title.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
                sb.append("<div id=\"").append(id).append("_children\" class=\"widgetlet-children\" data-exp-key=\"").append(storageKey).append("\" style=\"display: none; margin-left: 15px; border-left: 1px solid rgba(0,0,0,0.1); padding-left: 10px; margin-top: 5px;\">");
                for (WidgetLet child : children) {
                    sb.append(child.render(theme));
                }
                sb.append("</div>");
                sb.append("<script>if(localStorage.getItem('").append(storageKey).append("') === 'open') { ")
                  .append("var c = document.getElementById('").append(id).append("_children'); if(c) c.style.display = 'block'; ")
                  .append("var i = document.getElementById('").append(id).append("_icon'); if(i) i.className = 'fas fa-chevron-down'; } </script>");
            }
        }
        
        sb.append("</div>");
        return sb.toString();
    }
}
