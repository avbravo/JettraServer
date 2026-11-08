package io.jettra.flux.widgets;

import io.jettra.flux.core.Modifier;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * ToggleSelectionItem - Interactive selectable row widget for JettraFlux.
 * Models an individual entity (e.g. system user) within selection lists or tables,
 * featuring interactive checkbox binding, user avatar/icon, title, subtitle (e.g. email),
 * role badge, and dynamic authorization status badge ("Authorized" / "Not Assigned").
 */
public class ToggleSelectionItem extends Widget {

    private String value;
    private String title;
    private String subtitle;
    private String roleBadge = "READ_WRITE";
    private String statusBadge = "Not Assigned";
    private boolean selected = false;
    private boolean disabled = false;
    private final Set<String> assignedDatabases = new LinkedHashSet<>();
    private String onToggleJs;

    protected ToggleSelectionItem(String value, String title) {
        this.value = Objects.requireNonNull(value, "Value cannot be null");
        this.title = (title != null && !title.isBlank()) ? title : value;
        this.id = "toggleItem_" + Integer.toHexString(Objects.hash(value, System.identityHashCode(this)));
    }

    public static ToggleSelectionItem of(String value, String title) {
        return new ToggleSelectionItem(value, title);
    }

    public static ToggleSelectionItem of(String value) {
        return new ToggleSelectionItem(value, value);
    }

    public ToggleSelectionItem value(String value) {
        this.value = Objects.requireNonNull(value, "Value cannot be null");
        return this;
    }

    public ToggleSelectionItem title(String title) {
        this.title = title;
        return this;
    }

    public ToggleSelectionItem subtitle(String subtitle) {
        this.subtitle = subtitle;
        return this;
    }

    public ToggleSelectionItem roleBadge(String roleBadge) {
        this.roleBadge = roleBadge;
        return this;
    }

    public ToggleSelectionItem statusBadge(String statusBadge) {
        this.statusBadge = statusBadge;
        return this;
    }

    public ToggleSelectionItem selected(boolean selected) {
        this.selected = selected;
        return this;
    }

    public ToggleSelectionItem disabled(boolean disabled) {
        this.disabled = disabled;
        return this;
    }

    public ToggleSelectionItem assignedDatabases(Collection<String> dbs) {
        this.assignedDatabases.clear();
        if (dbs != null) {
            this.assignedDatabases.addAll(dbs);
        }
        return this;
    }

    public ToggleSelectionItem addAssignedDatabase(String db) {
        if (db != null && !db.isBlank()) {
            this.assignedDatabases.add(db.trim());
        }
        return this;
    }

    public ToggleSelectionItem onToggle(String onToggleJs) {
        this.onToggleJs = onToggleJs;
        return this;
    }

    public String value() {
        return value;
    }

    public String title() {
        return title;
    }

    public String subtitle() {
        return subtitle;
    }

    public String roleBadge() {
        return roleBadge;
    }

    public String statusBadge() {
        return statusBadge;
    }

    public boolean isSelected() {
        return selected;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public Set<String> assignedDatabases() {
        return Collections.unmodifiableSet(assignedDatabases);
    }

    @Override
    public ToggleSelectionItem id(String id) {
        super.id(id);
        return this;
    }

    @Override
    public ToggleSelectionItem modifier(Modifier modifier) {
        super.modifier(modifier);
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        String itemId = (id != null && !id.isBlank()) ? id : "toggleItem_" + System.identityHashCode(this);
        String safeValue = escapeHtml(value);
        String safeTitle = escapeHtml(title != null ? title : value);
        String safeSubtitle = subtitle != null ? escapeHtml(subtitle) : "";
        String safeRole = roleBadge != null ? escapeHtml(roleBadge) : "USER";
        String safeStatus = statusBadge != null ? escapeHtml(statusBadge) : (selected ? "Authorized" : "Not Assigned");
        String assignedDbsStr = String.join(",", assignedDatabases);

        String searchableText = (safeTitle + " " + safeSubtitle + " " + safeRole + " " + safeStatus).toLowerCase();

        boolean isAuthorized = selected || "Authorized".equalsIgnoreCase(safeStatus);

        sb.append("<div id=\"").append(itemId).append("\" class=\"jettra-toggle-selection-item")
          .append(selected ? " selected" : "")
          .append(disabled ? " disabled" : "");

        if (modifier != null && modifier.getClasses() != null && !modifier.getClasses().isBlank()) {
            sb.append(" ").append(modifier.getClasses().trim());
        }

        sb.append("\" data-value=\"").append(safeValue).append("\" ")
          .append("data-title=\"").append(safeTitle).append("\" ")
          .append("data-search=\"").append(searchableText).append("\" ")
          .append("data-assigned-dbs=\"").append(escapeHtml(assignedDbsStr)).append("\" ")
          .append("style=\"display:flex; justify-content:space-between; align-items:center; padding:10px 14px; ")
          .append("border-radius:8px; margin-bottom:6px; cursor:").append(disabled ? "default" : "pointer").append("; ")
          .append("transition:all 0.2s ease; user-select:none; ");

        if (selected) {
            sb.append("background:rgba(56,189,248,0.12); border:1px solid rgba(56,189,248,0.4); ");
        } else {
            sb.append("background:rgba(15,23,42,0.65); border:1px solid rgba(255,255,255,0.07); ");
        }

        if (modifier != null && modifier.getStyles() != null && !modifier.getStyles().isBlank()) {
            sb.append(modifier.getStyles().trim());
        }
        sb.append("\" ");

        if (!disabled) {
            sb.append("onclick=\"(function(row){")
              .append("var cb=row.querySelector('.toggle-item-checkbox');")
              .append("if(cb && event.target!==cb){cb.checked=!cb.checked; cb.dispatchEvent(new Event('change', {bubbles:true}));}")
              .append("})(this)\"");
        }
        sb.append(">\n");

        // Left section: Checkbox + Avatar + Title & Subtitle
        sb.append("  <div style=\"display:flex; align-items:center; gap:12px; min-width:0;\">\n");
        sb.append("    <input type=\"checkbox\" class=\"toggle-item-checkbox\" ")
          .append("data-username=\"").append(safeValue).append("\" ")
          .append("value=\"").append(safeValue).append("\" ");
        if (selected) {
            sb.append("checked=\"checked\" ");
        }
        if (disabled) {
            sb.append("disabled=\"disabled\" ");
        }
        sb.append("style=\"width:17px; height:17px; accent-color:#38bdf8; cursor:").append(disabled ? "default" : "pointer").append("; margin:0;\" ");
        if (onToggleJs != null && !onToggleJs.isBlank()) {
            sb.append("onchange=\"").append(onToggleJs.replace("{value}", safeValue)).append("\" ");
        }
        sb.append("/>\n");

        sb.append("    <i class=\"fas fa-user-circle\" style=\"font-size:22px; color:").append(selected ? "#38bdf8" : "#94a3b8").append("; flex-shrink:0;\"></i>\n");
        sb.append("    <div style=\"display:flex; flex-direction:column; min-width:0;\">\n");
        sb.append("      <span class=\"toggle-item-title\" style=\"font-size:13px; font-weight:600; color:#f8fafc; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;\">")
          .append(safeTitle).append("</span>\n");
        if (!safeSubtitle.isEmpty()) {
            sb.append("      <span class=\"toggle-item-subtitle\" style=\"font-size:11px; color:#64748b; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;\">")
              .append(safeSubtitle).append("</span>\n");
        }
        sb.append("    </div>\n");
        sb.append("  </div>\n");

        // Right section: Role Badge + Status Badge
        sb.append("  <div style=\"display:flex; align-items:center; gap:8px; flex-shrink:0; margin-left:12px;\">\n");
        sb.append("    <span class=\"badge-role\" style=\"font-size:10.5px; font-weight:600; padding:2px 8px; border-radius:12px; ")
          .append("background:rgba(56,189,248,0.15); color:#38bdf8; border:1px solid rgba(56,189,248,0.3); letter-spacing:0.3px;\">")
          .append(safeRole).append("</span>\n");

        sb.append("    <span class=\"toggle-item-status\" style=\"font-size:10.5px; font-weight:600; padding:2px 8px; border-radius:12px; ");
        if (isAuthorized) {
            sb.append("background:rgba(16,185,129,0.15); color:#34d399; border:1px solid rgba(16,185,129,0.3);\">");
        } else {
            sb.append("background:rgba(100,116,139,0.15); color:#94a3b8; border:1px solid rgba(100,116,139,0.25);\">");
        }
        sb.append(safeStatus).append("</span>\n");
        sb.append("  </div>\n");

        sb.append("</div>\n");
        return sb.toString();
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
