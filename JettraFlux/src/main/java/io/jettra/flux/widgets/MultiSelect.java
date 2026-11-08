package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * MultiSelect - Type-safe, reactive multi-selection component for JettraFlux.
 * Designed for multi-tenant / multi-database assignments, RBAC role groups, and tag selection.
 * Features:
 * - Dynamic option modeling with icons and descriptions
 * - Quick-action toolbar (Select All / Clear All)
 * - Accessible checkbox bindings and automatic comma-separated value synchronization
 * - Modern dark-mode compliant aesthetic with active accent glow
 */
public class MultiSelect extends Widget {

    public record Option(
        String value,
        String label,
        boolean selected,
        String icon,
        String description
    ) {
        public Option(String value, String label, boolean selected) {
            this(value, label, selected, "fas fa-database", "");
        }

        public Option(String value, String label, boolean selected, String icon) {
            this(value, label, selected, icon, "");
        }
    }

    private String name;
    private String label;
    private String placeholder = "Select one or more options...";
    private boolean quickActions = true;
    private boolean selectAllOption = false;
    private String selectAllLabel = "* (All Databases)";
    private final List<Option> options = new ArrayList<>();
    private final Set<String> selectedValues = new HashSet<>();
    private String badgeSeverity = "info";

    protected MultiSelect(String name) {
        this.name = name != null ? name : "multiselect";
        this.id(this.name + "_" + Integer.toHexString(System.identityHashCode(this)));
    }

    public static MultiSelect of(String name) {
        return new MultiSelect(name);
    }

    public static MultiSelect of(String id, String name) {
        MultiSelect ms = new MultiSelect(name);
        ms.id(id);
        return ms;
    }

    @Override
    public MultiSelect id(String id) {
        super.id(id);
        return this;
    }

    public MultiSelect name(String name) {
        this.name = name;
        return this;
    }

    public MultiSelect label(String label) {
        this.label = label;
        return this;
    }

    public MultiSelect placeholder(String placeholder) {
        this.placeholder = placeholder;
        return this;
    }

    public MultiSelect quickActions(boolean enable) {
        this.quickActions = enable;
        return this;
    }

    public MultiSelect selectAllOption(boolean enable) {
        this.selectAllOption = enable;
        return this;
    }

    public MultiSelect selectAllOption(boolean enable, String label) {
        this.selectAllOption = enable;
        this.selectAllLabel = label != null ? label : "* (All Databases)";
        return this;
    }

    public MultiSelect badgeSeverity(String severity) {
        this.badgeSeverity = severity != null ? severity : "info";
        return this;
    }

    public MultiSelect options(Collection<String> values) {
        if (values != null) {
            for (String val : values) {
                if (val != null && !val.isBlank()) {
                    addOption(val.trim(), val.trim());
                }
            }
        }
        return this;
    }

    public MultiSelect options(String... values) {
        if (values != null) {
            return options(Arrays.asList(values));
        }
        return this;
    }

    public MultiSelect addOption(Option option) {
        if (option != null) {
            this.options.add(option);
            if (option.selected()) {
                this.selectedValues.add(option.value());
            }
        }
        return this;
    }

    public MultiSelect addOption(String value, String label) {
        boolean isSelected = this.selectedValues.contains(value);
        return addOption(new Option(value, label, isSelected));
    }

    public MultiSelect addOption(String value, String label, boolean selected) {
        if (selected) {
            this.selectedValues.add(value);
        }
        return addOption(new Option(value, label, selected));
    }

    public MultiSelect addOption(String value, String label, boolean selected, String icon) {
        if (selected) {
            this.selectedValues.add(value);
        }
        return addOption(new Option(value, label, selected, icon, ""));
    }

    public MultiSelect selectedValues(Collection<String> selected) {
        if (selected != null) {
            this.selectedValues.clear();
            this.selectedValues.addAll(selected);
        }
        return this;
    }

    public MultiSelect selectedValues(String... selected) {
        if (selected != null) {
            return selectedValues(Arrays.asList(selected));
        }
        return this;
    }

    public MultiSelect select(String value) {
        if (value != null) {
            this.selectedValues.add(value);
        }
        return this;
    }

    public List<Option> getOptions() {
        return Collections.unmodifiableList(options);
    }

    public Set<String> getSelectedValues() {
        return Collections.unmodifiableSet(selectedValues);
    }

    @Override
    public String render(ThemeData theme) {
        String containerId = getId();
        if (containerId == null || containerId.isBlank()) {
            containerId = "ms_" + Math.abs(name.hashCode());
        }

        // Aggregate full options list including optional wildcard
        List<Option> fullOptions = new ArrayList<>();
        if (selectAllOption) {
            boolean isWildcardSelected = selectedValues.contains("*");
            fullOptions.add(new Option("*", selectAllLabel, isWildcardSelected, "fas fa-globe", "Unrestricted access across all databases"));
        }
        for (Option opt : options) {
            boolean isSelected = selectedValues.contains(opt.value()) || opt.selected();
            fullOptions.add(new Option(opt.value(), opt.label(), isSelected, opt.icon(), opt.description()));
        }

        // Determine CSV initial value
        List<String> initialSelectedList = new ArrayList<>();
        for (Option opt : fullOptions) {
            if (opt.selected()) {
                initialSelectedList.add(opt.value());
            }
        }
        String initialCsv = String.join(",", initialSelectedList);

        StringBuilder sb = new StringBuilder();
        sb.append("<div id=\"").append(containerId).append("\" ")
          .append(renderCommonAttributes(theme, "jettra-multiselect-root"))
          .append(" style=\"display:flex; flex-direction:column; gap:8px; width:100%; box-sizing:border-box;\">\n");

        // Label and Quick Actions Header
        if ((label != null && !label.isBlank()) || quickActions) {
            sb.append("  <div style=\"display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:6px;\">\n");
            if (label != null && !label.isBlank()) {
                sb.append("    <label style=\"font-size:12px; color:#94a3b8; font-weight:600; text-transform:uppercase; letter-spacing:0.4px;\">")
                  .append(label).append("</label>\n");
            } else {
                sb.append("    <div></div>\n");
            }

            if (quickActions) {
                sb.append("    <div style=\"display:inline-flex; align-items:center; gap:6px;\">\n");
                sb.append("      <button type=\"button\" onclick=\"window.JettraMultiSelect.selectAll('").append(containerId).append("')\" ")
                  .append("style=\"background:rgba(56,189,248,0.12); color:#38bdf8; border:1px solid rgba(56,189,248,0.25); border-radius:5px; padding:2px 8px; font-size:11px; font-weight:600; cursor:pointer; transition:all 0.15s;\">Select All</button>\n");
                sb.append("      <button type=\"button\" onclick=\"window.JettraMultiSelect.clearAll('").append(containerId).append("')\" ")
                  .append("style=\"background:rgba(100,116,139,0.12); color:#94a3b8; border:1px solid rgba(148,163,184,0.2); border-radius:5px; padding:2px 8px; font-size:11px; font-weight:600; cursor:pointer; transition:all 0.15s;\">Clear</button>\n");
                sb.append("    </div>\n");
            }
            sb.append("  </div>\n");
        }

        // Hidden CSV fields for both [name]_csv and standard [name] / legacy target_db
        sb.append("  <input type=\"hidden\" name=\"").append(name).append("_csv\" id=\"").append(containerId).append("_csv\" value=\"")
          .append(initialCsv).append("\"/>\n");
        if ("target_dbs".equals(name) || "assigned_databases".equals(name)) {
            sb.append("  <input type=\"hidden\" name=\"target_db\" id=\"").append(containerId).append("_legacy_csv\" value=\"")
              .append(initialCsv).append("\"/>\n");
        }

        // Option Pills Container
        sb.append("  <div class=\"jettra-multiselect-pill-grid\" style=\"display:flex; flex-wrap:wrap; gap:8px; padding:8px 10px; background:rgba(15,23,42,0.7); border:1px solid rgba(255,255,255,0.12); border-radius:8px; min-height:42px; align-items:center;\">\n");

        if (fullOptions.isEmpty()) {
            sb.append("    <span style=\"font-size:12px; color:#64748b; font-style:italic;\">")
              .append(placeholder).append("</span>\n");
        } else {
            int idx = 0;
            for (Option opt : fullOptions) {
                String optId = containerId + "_opt_" + idx;
                boolean isChecked = opt.selected();
                String pillBg = isChecked ? "background:rgba(56,189,248,0.18); border-color:rgba(56,189,248,0.5); color:#38bdf8;"
                                          : "background:rgba(30,41,59,0.6); border-color:rgba(255,255,255,0.08); color:#cbd5e1;";

                sb.append("    <label id=\"").append(optId).append("_lbl\" ")
                  .append("class=\"jettra-multiselect-pill")
                  .append(isChecked ? " selected" : "")
                  .append("\" style=\"display:inline-flex; align-items:center; gap:6px; padding:5px 12px; border-radius:9999px; border:1px solid; font-size:12px; font-weight:600; cursor:pointer; user-select:none; transition:all 0.15s; ")
                  .append(pillBg).append("\">\n");

                sb.append("      <input type=\"checkbox\" name=\"").append(name).append("\" value=\"").append(opt.value()).append("\" ")
                  .append("id=\"").append(optId).append("\" ")
                  .append(isChecked ? "checked " : "")
                  .append("onchange=\"window.JettraMultiSelect.toggle('").append(containerId).append("', this)\" ")
                  .append("style=\"position:absolute; opacity:0; width:0; height:0; margin:0; pointer-events:none;\"/>\n");

                if (opt.icon() != null && !opt.icon().isBlank()) {
                    sb.append("      <i class=\"").append(opt.icon()).append("\" style=\"font-size:11px;\"></i>\n");
                }

                sb.append("      <span>").append(opt.label()).append("</span>\n");

                // Checkmark indicator
                String checkDisplay = isChecked ? "inline-block" : "none";
                sb.append("      <i id=\"").append(optId).append("_chk\" class=\"fas fa-check-circle\" style=\"display:").append(checkDisplay).append("; font-size:11px; margin-left:2px;\"></i>\n");

                sb.append("    </label>\n");
                idx++;
            }
        }

        sb.append("  </div>\n");

        // Helper client script (injected once or scoped)
        sb.append("  <script>\n");
        sb.append("    if (!window.JettraMultiSelect) {\n");
        sb.append("      window.JettraMultiSelect = {\n");
        sb.append("        toggle: function(containerId, checkbox) {\n");
        sb.append("          var lbl = document.getElementById(checkbox.id + '_lbl');\n");
        sb.append("          var chk = document.getElementById(checkbox.id + '_chk');\n");
        sb.append("          if (checkbox.checked) {\n");
        sb.append("            if (lbl) { lbl.classList.add('selected'); lbl.style.background='rgba(56,189,248,0.18)'; lbl.style.borderColor='rgba(56,189,248,0.5)'; lbl.style.color='#38bdf8'; }\n");
        sb.append("            if (chk) { chk.style.display='inline-block'; }\n");
        sb.append("          } else {\n");
        sb.append("            if (lbl) { lbl.classList.remove('selected'); lbl.style.background='rgba(30,41,59,0.6)'; lbl.style.borderColor='rgba(255,255,255,0.08)'; lbl.style.color='#cbd5e1'; }\n");
        sb.append("            if (chk) { chk.style.display='none'; }\n");
        sb.append("          }\n");
        sb.append("          this.sync(containerId);\n");
        sb.append("        },\n");
        sb.append("        sync: function(containerId) {\n");
        sb.append("          var root = document.getElementById(containerId);\n");
        sb.append("          if (!root) return;\n");
        sb.append("          var cbs = root.querySelectorAll('input[type=\"checkbox\"]');\n");
        sb.append("          var vals = [];\n");
        sb.append("          for (var i = 0; i < cbs.length; i++) {\n");
        sb.append("            if (cbs[i].checked) vals.push(cbs[i].value);\n");
        sb.append("          }\n");
        sb.append("          var csv = vals.join(',');\n");
        sb.append("          var csvInput = document.getElementById(containerId + '_csv');\n");
        sb.append("          if (csvInput) csvInput.value = csv;\n");
        sb.append("          var legInput = document.getElementById(containerId + '_legacy_csv');\n");
        sb.append("          if (legInput) legInput.value = csv;\n");
        sb.append("        },\n");
        sb.append("        selectAll: function(containerId) {\n");
        sb.append("          var root = document.getElementById(containerId);\n");
        sb.append("          if (!root) return;\n");
        sb.append("          var cbs = root.querySelectorAll('input[type=\"checkbox\"]');\n");
        sb.append("          for (var i = 0; i < cbs.length; i++) {\n");
        sb.append("            cbs[i].checked = true;\n");
        sb.append("            var lbl = document.getElementById(cbs[i].id + '_lbl');\n");
        sb.append("            var chk = document.getElementById(cbs[i].id + '_chk');\n");
        sb.append("            if (lbl) { lbl.classList.add('selected'); lbl.style.background='rgba(56,189,248,0.18)'; lbl.style.borderColor='rgba(56,189,248,0.5)'; lbl.style.color='#38bdf8'; }\n");
        sb.append("            if (chk) { chk.style.display='inline-block'; }\n");
        sb.append("          }\n");
        sb.append("          this.sync(containerId);\n");
        sb.append("        },\n");
        sb.append("        clearAll: function(containerId) {\n");
        sb.append("          var root = document.getElementById(containerId);\n");
        sb.append("          if (!root) return;\n");
        sb.append("          var cbs = root.querySelectorAll('input[type=\"checkbox\"]');\n");
        sb.append("          for (var i = 0; i < cbs.length; i++) {\n");
        sb.append("            cbs[i].checked = false;\n");
        sb.append("            var lbl = document.getElementById(cbs[i].id + '_lbl');\n");
        sb.append("            var chk = document.getElementById(cbs[i].id + '_chk');\n");
        sb.append("            if (lbl) { lbl.classList.remove('selected'); lbl.style.background='rgba(30,41,59,0.6)'; lbl.style.borderColor='rgba(255,255,255,0.08)'; lbl.style.color='#cbd5e1'; }\n");
        sb.append("            if (chk) { chk.style.display='none'; }\n");
        sb.append("          }\n");
        sb.append("          this.sync(containerId);\n");
        sb.append("        },\n");
        sb.append("        setSelectedValues: function(containerId, values) {\n");
        sb.append("          var root = document.getElementById(containerId);\n");
        sb.append("          if (!root) return;\n");
        sb.append("          var valMap = {};\n");
        sb.append("          if (Array.isArray(values)) {\n");
        sb.append("            for (var k = 0; k < values.length; k++) {\n");
        sb.append("              if (values[k]) valMap[String(values[k]).trim().toLowerCase()] = true;\n");
        sb.append("            }\n");
        sb.append("          } else if (typeof values === 'string') {\n");
        sb.append("            var parts = values.split(',');\n");
        sb.append("            for (var k = 0; k < parts.length; k++) {\n");
        sb.append("              if (parts[k]) valMap[parts[k].trim().toLowerCase()] = true;\n");
        sb.append("            }\n");
        sb.append("          }\n");
        sb.append("          var cbs = root.querySelectorAll('input[type=\"checkbox\"]');\n");
        sb.append("          for (var i = 0; i < cbs.length; i++) {\n");
        sb.append("            var isMatch = !!valMap[String(cbs[i].value).trim().toLowerCase()];\n");
        sb.append("            cbs[i].checked = isMatch;\n");
        sb.append("            var lbl = document.getElementById(cbs[i].id + '_lbl');\n");
        sb.append("            var chk = document.getElementById(cbs[i].id + '_chk');\n");
        sb.append("            if (isMatch) {\n");
        sb.append("              if (lbl) { lbl.classList.add('selected'); lbl.style.background='rgba(56,189,248,0.18)'; lbl.style.borderColor='rgba(56,189,248,0.5)'; lbl.style.color='#38bdf8'; }\n");
        sb.append("              if (chk) { chk.style.display='inline-block'; }\n");
        sb.append("            } else {\n");
        sb.append("              if (lbl) { lbl.classList.remove('selected'); lbl.style.background='rgba(30,41,59,0.6)'; lbl.style.borderColor='rgba(255,255,255,0.08)'; lbl.style.color='#cbd5e1'; }\n");
        sb.append("              if (chk) { chk.style.display='none'; }\n");
        sb.append("            }\n");
        sb.append("          }\n");
        sb.append("          this.sync(containerId);\n");
        sb.append("        }\n");
        sb.append("      };\n");
        sb.append("    }\n");
        sb.append("  </script>\n");
        sb.append("</div>\n");

        return sb.toString();
    }
}
