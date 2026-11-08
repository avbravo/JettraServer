package io.jettra.flux.widgets;

import io.jettra.flux.core.Modifier;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * UserSelectionTable - Composite, reactive multi-selection list component for JettraFlux.
 * Specifically engineered for mass authorization and assignment of identities (e.g. system users)
 * to target resources (e.g. databases).
 *
 * Key Capabilities:
 * - Real-time client-side search filtering across username, email, role, and authorization status.
 * - Live selection counter badge ("X of Y Authorized").
 * - Quick action bulk controls ("Select All" / "Clear All").
 * - Seamless hidden input form synchronization transmitting comma-delimited selected usernames.
 * - Dynamic runtime hydration via `window.UserSelectionTable.syncForDatabase(tableId, targetDb)`
 *   allowing the modal to immediately reflect authorized vs unauthorized state when opened.
 */
public class UserSelectionTable extends Widget {

    private String name = "assigned_users";
    private String searchPlaceholder = "Filter users by username, role, or email...";
    private String emptyMessage = "No matching system users found";
    private String maxHeight = "230px";
    private boolean quickActions = true;
    private String onSelectionChangeJs;
    private final List<ToggleSelectionItem> items = new ArrayList<>();

    protected UserSelectionTable(String name) {
        this.name = (name != null && !name.isBlank()) ? name : "assigned_users";
        this.id = "userSelTable_" + Integer.toHexString(System.identityHashCode(this));
    }

    public static UserSelectionTable of(String name) {
        return new UserSelectionTable(name);
    }

    public static UserSelectionTable of(String id, String name) {
        UserSelectionTable ust = new UserSelectionTable(name);
        ust.id = id;
        return ust;
    }

    public UserSelectionTable name(String name) {
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        return this;
    }

    public UserSelectionTable searchPlaceholder(String placeholder) {
        this.searchPlaceholder = placeholder;
        return this;
    }

    public UserSelectionTable emptyMessage(String emptyMessage) {
        this.emptyMessage = emptyMessage;
        return this;
    }

    public UserSelectionTable maxHeight(String maxHeight) {
        this.maxHeight = maxHeight;
        return this;
    }

    public UserSelectionTable quickActions(boolean quickActions) {
        this.quickActions = quickActions;
        return this;
    }

    public UserSelectionTable onSelectionChange(String onSelectionChangeJs) {
        this.onSelectionChangeJs = onSelectionChangeJs;
        return this;
    }

    public UserSelectionTable addItem(ToggleSelectionItem item) {
        if (item != null) {
            this.items.add(item);
        }
        return this;
    }

    public UserSelectionTable addItems(Collection<ToggleSelectionItem> items) {
        if (items != null) {
            this.items.addAll(items);
        }
        return this;
    }

    public UserSelectionTable items(List<ToggleSelectionItem> items) {
        this.items.clear();
        if (items != null) {
            this.items.addAll(items);
        }
        return this;
    }

    public List<ToggleSelectionItem> items() {
        return Collections.unmodifiableList(items);
    }

    public String name() {
        return name;
    }

    @Override
    public UserSelectionTable id(String id) {
        super.id(id);
        return this;
    }

    @Override
    public UserSelectionTable modifier(Modifier modifier) {
        super.modifier(modifier);
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        String tableId = (id != null && !id.isBlank()) ? id : "userSelTable_" + System.identityHashCode(this);
        String inputName = (name != null && !name.isBlank()) ? name : "assigned_users";

        List<String> initialSelectedValues = items.stream()
            .filter(ToggleSelectionItem::isSelected)
            .map(ToggleSelectionItem::value)
            .toList();
        String initialJoinedValues = String.join(",", initialSelectedValues);

        int totalCount = items.size();
        int selectedCount = initialSelectedValues.size();

        sb.append("<div id=\"").append(tableId).append("\" class=\"jettra-user-selection-table");
        if (modifier != null && modifier.getClasses() != null && !modifier.getClasses().isBlank()) {
            sb.append(" ").append(modifier.getClasses().trim());
        }
        sb.append("\" style=\"position:relative; width:100%; box-sizing:border-box;");
        if (modifier != null && modifier.getStyles() != null && !modifier.getStyles().isBlank()) {
            sb.append(" ").append(modifier.getStyles().trim());
        }
        sb.append("\">\n");

        // Hidden input storing actual comma-separated selected usernames
        sb.append("  <input type=\"hidden\" id=\"").append(tableId).append("_value\" name=\"")
          .append(escapeHtml(inputName)).append("\" value=\"").append(escapeHtml(initialJoinedValues)).append("\"/>\n");

        // Header toolbar: Search input + Counter badge + Quick actions
        sb.append("  <div style=\"display:flex; flex-direction:column; gap:8px; margin-bottom:10px;\">\n");
        sb.append("    <div style=\"display:flex; align-items:center; gap:8px;\">\n");
        sb.append("      <div style=\"display:flex; flex:1; align-items:center; gap:8px; background:#0f172a; border:1px solid rgba(255,255,255,0.15); border-radius:8px; padding:8px 12px;\">\n");
        sb.append("        <i class=\"fas fa-search\" style=\"color:#64748b; font-size:13px;\"></i>\n");
        sb.append("        <input type=\"text\" id=\"").append(tableId).append("_search\" class=\"user-selection-search\" ")
          .append("placeholder=\"").append(escapeHtml(searchPlaceholder)).append("\" ")
          .append("style=\"width:100%; background:transparent; border:none; color:#f8fafc; font-size:13px; outline:none;\" ")
          .append("oninput=\"window.UserSelectionTable && window.UserSelectionTable.filter('").append(tableId).append("', this.value)\"/>\n");
        sb.append("      </div>\n");

        // Counter badge
        sb.append("      <span id=\"").append(tableId).append("_counter\" ")
          .append("style=\"background:rgba(56,189,248,0.12); color:#38bdf8; border:1px solid rgba(56,189,248,0.3); font-size:11.5px; font-weight:600; padding:6px 12px; border-radius:8px; white-space:nowrap;\">")
          .append(selectedCount).append(" / ").append(totalCount).append(" Authorized</span>\n");
        sb.append("    </div>\n");

        // Quick action buttons if enabled
        if (quickActions) {
            sb.append("    <div style=\"display:flex; justify-content:space-between; align-items:center; font-size:12px; color:#94a3b8; padding:0 2px;\">\n");
            sb.append("      <span>Click rows or checkboxes to toggle database authorization</span>\n");
            sb.append("      <div style=\"display:flex; gap:8px;\">\n");
            sb.append("        <button type=\"button\" onclick=\"window.UserSelectionTable && window.UserSelectionTable.selectAll('").append(tableId).append("')\" ")
              .append("style=\"background:none; border:none; color:#38bdf8; font-size:11.5px; font-weight:600; cursor:pointer; padding:2px 4px;\">Select All</button>\n");
            sb.append("        <span style=\"color:#475569;\">|</span>\n");
            sb.append("        <button type=\"button\" onclick=\"window.UserSelectionTable && window.UserSelectionTable.deselectAll('").append(tableId).append("')\" ")
              .append("style=\"background:none; border:none; color:#94a3b8; font-size:11.5px; font-weight:600; cursor:pointer; padding:2px 4px;\">Clear All</button>\n");
            sb.append("      </div>\n");
            sb.append("    </div>\n");
        }
        sb.append("  </div>\n");

        // Scrollable Items Container
        sb.append("  <div id=\"").append(tableId).append("_items\" class=\"user-selection-items\" ")
          .append("style=\"max-height:").append(maxHeight).append("; overflow-y:auto; display:flex; flex-direction:column; gap:4px; padding-right:2px;\">\n");

        for (ToggleSelectionItem item : items) {
            sb.append(item.render(theme));
        }

        sb.append("  </div>\n");

        // Empty Search Feedback
        sb.append("  <div id=\"").append(tableId).append("_empty\" style=\"display:none; text-align:center; padding:18px; color:#94a3b8; font-size:12.5px; background:rgba(15,23,42,0.4); border-radius:8px; border:1px dashed rgba(255,255,255,0.1);\">\n");
        sb.append("    <i class=\"fas fa-user-slash\" style=\"font-size:20px; color:#64748b; margin-bottom:6px; display:block;\"></i>\n");
        sb.append("    ").append(escapeHtml(emptyMessage)).append("\n");
        sb.append("  </div>\n");

        // Runtime Client-side JS Script Module (Idempotent Definition)
        sb.append("  <script>\n");
        sb.append("    if (!window.UserSelectionTable) {\n");
        sb.append("      window.UserSelectionTable = {\n");
        sb.append("        syncValue: function(tableId) {\n");
        sb.append("          var table = document.getElementById(tableId);\n");
        sb.append("          if (!table) return;\n");
        sb.append("          var checkboxes = table.querySelectorAll('.toggle-item-checkbox');\n");
        sb.append("          var selectedVals = [];\n");
        sb.append("          checkboxes.forEach(function(cb) {\n");
        sb.append("            var row = cb.closest('.jettra-toggle-selection-item');\n");
        sb.append("            var statusBadge = row ? row.querySelector('.toggle-item-status') : null;\n");
        sb.append("            if (cb.checked) {\n");
        sb.append("              selectedVals.push(cb.value);\n");
        sb.append("              if (row) {\n");
        sb.append("                row.classList.add('selected');\n");
        sb.append("                row.style.background = 'rgba(56,189,248,0.12)';\n");
        sb.append("                row.style.border = '1px solid rgba(56,189,248,0.4)';\n");
        sb.append("              }\n");
        sb.append("              if (statusBadge) {\n");
        sb.append("                statusBadge.innerText = 'Authorized';\n");
        sb.append("                statusBadge.style.background = 'rgba(16,185,129,0.15)';\n");
        sb.append("                statusBadge.style.color = '#34d399';\n");
        sb.append("                statusBadge.style.border = '1px solid rgba(16,185,129,0.3)';\n");
        sb.append("              }\n");
        sb.append("            } else {\n");
        sb.append("              if (row) {\n");
        sb.append("                row.classList.remove('selected');\n");
        sb.append("                row.style.background = 'rgba(15,23,42,0.65)';\n");
        sb.append("                row.style.border = '1px solid rgba(255,255,255,0.07)';\n");
        sb.append("              }\n");
        sb.append("              if (statusBadge) {\n");
        sb.append("                statusBadge.innerText = 'Not Assigned';\n");
        sb.append("                statusBadge.style.background = 'rgba(100,116,139,0.15)';\n");
        sb.append("                statusBadge.style.color = '#94a3b8';\n");
        sb.append("                statusBadge.style.border = '1px solid rgba(100,116,139,0.25)';\n");
        sb.append("              }\n");
        sb.append("            }\n");
        sb.append("          });\n");
        sb.append("          var hiddenVal = document.getElementById(tableId + '_value');\n");
        sb.append("          if (hiddenVal) hiddenVal.value = selectedVals.join(',');\n");
        sb.append("          var counter = document.getElementById(tableId + '_counter');\n");
        sb.append("          if (counter) counter.innerText = selectedVals.length + ' / ' + checkboxes.length + ' Authorized';\n");
        if (onSelectionChangeJs != null && !onSelectionChangeJs.isBlank()) {
            sb.append("          try { (function() { ").append(onSelectionChangeJs).append(" })(); } catch(e) {}\n");
        }
        sb.append("        },\n");
        sb.append("        syncForDatabase: function(tableId, targetDb) {\n");
        sb.append("          var table = document.getElementById(tableId);\n");
        sb.append("          if (!table) return;\n");
        sb.append("          var rows = table.querySelectorAll('.jettra-toggle-selection-item');\n");
        sb.append("          var cleanDb = (targetDb || '').trim().toLowerCase();\n");
        sb.append("          rows.forEach(function(row) {\n");
        sb.append("            var assignedDbsStr = (row.getAttribute('data-assigned-dbs') || '').toLowerCase();\n");
        sb.append("            var dbs = assignedDbsStr.split(',').map(function(s){return s.trim();});\n");
        sb.append("            var cb = row.querySelector('.toggle-item-checkbox');\n");
        sb.append("            if (cb) {\n");
        sb.append("              var isAssigned = (dbs.indexOf(cleanDb) !== -1 || dbs.indexOf('*') !== -1);\n");
        sb.append("              cb.checked = isAssigned;\n");
        sb.append("            }\n");
        sb.append("          });\n");
        sb.append("          window.UserSelectionTable.syncValue(tableId);\n");
        sb.append("        },\n");
        sb.append("        selectAll: function(tableId) {\n");
        sb.append("          var table = document.getElementById(tableId);\n");
        sb.append("          if (!table) return;\n");
        sb.append("          var checkboxes = table.querySelectorAll('.toggle-item-checkbox:not(:disabled)');\n");
        sb.append("          checkboxes.forEach(function(cb) { cb.checked = true; });\n");
        sb.append("          window.UserSelectionTable.syncValue(tableId);\n");
        sb.append("        },\n");
        sb.append("        deselectAll: function(tableId) {\n");
        sb.append("          var table = document.getElementById(tableId);\n");
        sb.append("          if (!table) return;\n");
        sb.append("          var checkboxes = table.querySelectorAll('.toggle-item-checkbox:not(:disabled)');\n");
        sb.append("          checkboxes.forEach(function(cb) { cb.checked = false; });\n");
        sb.append("          window.UserSelectionTable.syncValue(tableId);\n");
        sb.append("        },\n");
        sb.append("        filter: function(tableId, query) {\n");
        sb.append("          var table = document.getElementById(tableId);\n");
        sb.append("          if (!table) return;\n");
        sb.append("          var q = (query || '').toLowerCase().trim();\n");
        sb.append("          var rows = table.querySelectorAll('.jettra-toggle-selection-item');\n");
        sb.append("          var matched = 0;\n");
        sb.append("          rows.forEach(function(row) {\n");
        sb.append("            var text = (row.getAttribute('data-search') || '').toLowerCase();\n");
        sb.append("            if (!q || text.indexOf(q) !== -1) {\n");
        sb.append("              row.style.display = 'flex';\n");
        sb.append("              matched++;\n");
        sb.append("            } else {\n");
        sb.append("              row.style.display = 'none';\n");
        sb.append("            }\n");
        sb.append("          });\n");
        sb.append("          var emptyEl = document.getElementById(tableId + '_empty');\n");
        sb.append("          if (emptyEl) emptyEl.style.display = (matched === 0) ? 'block' : 'none';\n");
        sb.append("        }\n");
        sb.append("      };\n");
        sb.append("    }\n");
        sb.append("    // Attach automatic change listeners to checkboxes in this table\n");
        sb.append("    (function(){\n");
        sb.append("      var tbl = document.getElementById('").append(tableId).append("');\n");
        sb.append("      if (tbl) {\n");
        sb.append("        tbl.addEventListener('change', function(e) {\n");
        sb.append("          if (e.target && e.target.classList.contains('toggle-item-checkbox')) {\n");
        sb.append("            window.UserSelectionTable.syncValue('").append(tableId).append("');\n");
        sb.append("          }\n");
        sb.append("        });\n");
        sb.append("      }\n");
        sb.append("    })();\n");
        sb.append("  </script>\n");

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
