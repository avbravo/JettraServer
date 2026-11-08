package io.jettra.flux.widgets;

import io.jettra.flux.core.FluxEscapers;
import io.jettra.flux.core.JettraComponent;
import io.jettra.flux.core.Modifier;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

import java.util.Objects;
import java.util.UUID;

/**
 * First-Class Collapsible/Disclosure component for JettraFlux.
 * Encapsulates:
 * - Header/summary component or label
 * - Nested collapsible details payload
 * - Reactive visibility toggle state (isExpanded, toggle())
 * - Accessible WAI-ARIA disclosure semantics (role="region", aria-expanded, aria-controls)
 * - Builder Pattern for fluent declarative composition
 */
public class JettraCollapsible extends Widget {

    private final String collapsibleId;
    private JettraComponent headerComponent;
    private String headerTitle;
    private JettraComponent detailsComponent;
    private boolean expanded = false;
    private String icon = "fas fa-chevron-right";
    private String iconExpanded = "fas fa-chevron-down";
    private String badge;
    private String badgeClass = "store-badge";

    public JettraCollapsible() {
        this("collapsible_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8));
    }

    public JettraCollapsible(String collapsibleId) {
        this.collapsibleId = collapsibleId != null ? collapsibleId : ("collapsible_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8));
        this.id(this.collapsibleId);
    }

    public static JettraCollapsible of() {
        return new JettraCollapsible();
    }

    public static JettraCollapsible of(String collapsibleId) {
        return new JettraCollapsible(collapsibleId);
    }

    public static JettraCollapsible of(JettraComponent header, JettraComponent details) {
        return new JettraCollapsible().header(header).withDetails(details);
    }

    public static JettraCollapsible of(String headerTitle, JettraComponent details) {
        return new JettraCollapsible().header(headerTitle).withDetails(details);
    }

    public String getCollapsibleId() {
        return collapsibleId;
    }

    public JettraCollapsible header(JettraComponent header) {
        this.headerComponent = header;
        return this;
    }

    public JettraCollapsible header(String title) {
        this.headerTitle = title;
        return this;
    }

    public JettraComponent getHeader() {
        return headerComponent;
    }

    public String getHeaderTitle() {
        return headerTitle;
    }

    public JettraCollapsible withDetails(JettraComponent details) {
        this.detailsComponent = details;
        return this;
    }

    public JettraCollapsible details(JettraComponent details) {
        return withDetails(details);
    }

    public JettraComponent getDetails() {
        return detailsComponent;
    }

    public boolean hasDetails() {
        return detailsComponent != null;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public JettraCollapsible expanded(boolean expanded) {
        this.expanded = expanded;
        return this;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public void toggle() {
        this.expanded = !this.expanded;
    }

    public JettraCollapsible icon(String icon) {
        this.icon = icon;
        return this;
    }

    public JettraCollapsible iconExpanded(String iconExpanded) {
        this.iconExpanded = iconExpanded;
        return this;
    }

    public JettraCollapsible badge(String badge) {
        this.badge = badge;
        return this;
    }

    public JettraCollapsible badge(String badge, String badgeClass) {
        this.badge = badge;
        this.badgeClass = badgeClass;
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        String detailContentId = "detail_" + collapsibleId;
        String toggleBtnId = "btn_toggle_" + collapsibleId;
        String iconId = "icon_" + collapsibleId;
        String currentIcon = expanded ? iconExpanded : icon;
        String contentDisplay = expanded ? "block" : "none";
        String ariaExpanded = String.valueOf(expanded);

        StringBuilder sb = new StringBuilder();
        sb.append("<div id=\"").append(collapsibleId).append("\" class=\"jettra-collapsible\" style=\"display:flex; flex-direction:column; width:100%;\">\n");

        // Header Row
        sb.append("  <div class=\"jettra-collapsible-header\" style=\"display:flex; align-items:center; justify-content:space-between; cursor:pointer; user-select:none;\" ")
          .append("onclick=\"JettraCollapsible.toggle('").append(collapsibleId).append("')\">\n");

        sb.append("    <div style=\"display:inline-flex; align-items:center; gap:6px;\">\n");
        sb.append("      <button id=\"").append(toggleBtnId).append("\" type=\"button\" class=\"jettra-collapsible-btn\" ")
          .append("aria-expanded=\"").append(ariaExpanded).append("\" aria-controls=\"").append(detailContentId).append("\" ")
          .append("onclick=\"event.stopPropagation(); JettraCollapsible.toggle('").append(collapsibleId).append("')\" ")
          .append("style=\"background:none; border:none; padding:2px 4px; cursor:pointer; color:var(--j-primary,#38bdf8); font-size:10px;\">")
          .append("<i id=\"").append(iconId).append("\" class=\"").append(currentIcon).append(" jettra-collapsible-icon\"></i>")
          .append("</button>\n");

        if (headerComponent != null) {
            sb.append("      ").append(headerComponent.render(theme)).append("\n");
        } else if (headerTitle != null) {
            sb.append("      <span style=\"font-size:12px; font-weight:600; color:var(--j-text-primary,#f8fafc);\">")
              .append(escapeHtml(headerTitle))
              .append("</span>\n");
        }

        if (badge != null && !badge.isBlank()) {
            sb.append("      <span class=\"").append(badgeClass)
              .append("\" style=\"font-size:8.5px; padding:1px 5px; border-radius:3px; font-weight:700;\">")
              .append(escapeHtml(badge))
              .append("</span>\n");
        }
        sb.append("    </div>\n");

        sb.append("  </div>\n");

        // Collapsible Content
        sb.append("  <div id=\"").append(detailContentId).append("\" role=\"region\" class=\"jettra-collapsible-content\" ")
          .append("aria-expanded=\"").append(ariaExpanded).append("\" ")
          .append("style=\"display:").append(contentDisplay).append("; margin-top:4px;\">\n");

        if (detailsComponent != null) {
            sb.append(detailsComponent.render(theme)).append("\n");
        }

        sb.append("  </div>\n");
        sb.append("</div>\n");

        // Client Controller Helper
        sb.append("<script>\n")
          .append("  window.JettraCollapsible = window.JettraCollapsible || {};\n")
          .append("  window.JettraCollapsible.toggle = function(id) {\n")
          .append("    var content = document.getElementById('detail_' + id);\n")
          .append("    var btn = document.getElementById('btn_toggle_' + id);\n")
          .append("    var icon = document.getElementById('icon_' + id);\n")
          .append("    if (!content) return;\n")
          .append("    var isExp = content.style.display !== 'none';\n")
          .append("    if (isExp) {\n")
          .append("      content.style.display = 'none';\n")
          .append("      content.setAttribute('aria-expanded', 'false');\n")
          .append("      if (btn) btn.setAttribute('aria-expanded', 'false');\n")
          .append("      if (icon) icon.className = '").append(FluxEscapers.escapeJs(icon)).append(" jettra-collapsible-icon';\n")
          .append("    } else {\n")
          .append("      content.style.display = 'block';\n")
          .append("      content.setAttribute('aria-expanded', 'true');\n")
          .append("      if (btn) btn.setAttribute('aria-expanded', 'true');\n")
          .append("      if (icon) icon.className = '").append(FluxEscapers.escapeJs(iconExpanded)).append(" jettra-collapsible-icon';\n")
          .append("    }\n")
          .append("  };\n")
          .append("</script>\n");

        return sb.toString();
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
