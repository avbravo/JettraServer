package io.jettra.flux.widgets;

import io.jettra.flux.core.Modifier;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * SelectFilter - Fluent, live-searchable option selector component in JettraFlux.
 * Designed for intuitive selection of existing entities (e.g. system users) with
 * client-side instant filtering, rich option metadata (label, description, role badges),
 * hidden form binding, and accessible keyboard/mouse interactions.
 */
public class SelectFilter extends Widget {

    public record FilterOption(String value, String label, String description, String badge, boolean selected) {
        public FilterOption {
            Objects.requireNonNull(value, "Option value cannot be null");
            Objects.requireNonNull(label, "Option label cannot be null");
        }

        public static FilterOption of(String value, String label) {
            return new FilterOption(value, label, null, null, false);
        }

        public static FilterOption of(String value, String label, String description, String badge) {
            return new FilterOption(value, label, description, badge, false);
        }
    }

    private String name;
    private String placeholder = "Search or select...";
    private String emptyMessage = "No matching records found";
    private String selectedValue;
    private boolean required = false;
    private String onSelectJs;
    private final List<FilterOption> options = new ArrayList<>();

    private SelectFilter(String name) {
        this.name = name;
    }

    public static SelectFilter of(String name) {
        return new SelectFilter(name);
    }

    public static SelectFilter of(String id, String name) {
        SelectFilter sf = new SelectFilter(name);
        sf.id = id;
        return sf;
    }

    public SelectFilter name(String name) {
        this.name = name;
        return this;
    }

    public SelectFilter placeholder(String placeholder) {
        this.placeholder = placeholder;
        return this;
    }

    public SelectFilter emptyMessage(String emptyMessage) {
        this.emptyMessage = emptyMessage;
        return this;
    }

    public SelectFilter selectedValue(String selectedValue) {
        this.selectedValue = selectedValue;
        return this;
    }

    public SelectFilter required(boolean required) {
        this.required = required;
        return this;
    }

    public SelectFilter onSelect(String onSelectJs) {
        this.onSelectJs = onSelectJs;
        return this;
    }

    public SelectFilter addOption(String value, String label) {
        return addOption(value, label, null, null, false);
    }

    public SelectFilter addOption(String value, String label, String description, String badge) {
        return addOption(value, label, description, badge, false);
    }

    public SelectFilter addOption(String value, String label, String description, String badge, boolean selected) {
        if (selected) {
            this.selectedValue = value;
        }
        options.add(new FilterOption(value, label, description, badge, selected));
        return this;
    }

    public List<FilterOption> options() {
        return Collections.unmodifiableList(options);
    }

    public String selectedValue() {
        return selectedValue;
    }

    @Override
    public SelectFilter modifier(Modifier modifier) {
        super.modifier(modifier);
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        String containerId = (id != null && !id.isBlank()) ? id : "selectFilter_" + System.identityHashCode(this);
        String inputName = (name != null && !name.isBlank()) ? name : containerId;
        String val = (selectedValue != null) ? selectedValue : "";

        sb.append("<div id=\"").append(containerId).append("\" class=\"jettra-select-filter");
        if (modifier != null && modifier.getClasses() != null && !modifier.getClasses().isBlank()) {
            sb.append(" ").append(modifier.getClasses().trim());
        }
        sb.append("\" style=\"position:relative; width:100%;");
        if (modifier != null && modifier.getStyles() != null && !modifier.getStyles().isBlank()) {
            sb.append(" ").append(modifier.getStyles().trim());
        }
        sb.append("\">\n");

        // Hidden input storing actual form value
        sb.append("  <input type=\"hidden\" id=\"").append(containerId).append("_value\" name=\"")
          .append(inputName).append("\" value=\"").append(val).append("\"");
        if (required) {
            sb.append(" required=\"required\"");
        }
        sb.append("/>\n");

        // Filter search input
        sb.append("  <div style=\"display:flex; align-items:center; gap:8px; background:#0f172a; border:1px solid rgba(255,255,255,0.15); border-radius:8px; padding:9px 12px; margin-bottom:8px;\">\n");
        sb.append("    <i class=\"fas fa-search\" style=\"color:#64748b; font-size:13px;\"></i>\n");
        sb.append("    <input type=\"text\" id=\"").append(containerId).append("_search\" class=\"select-filter-search\" ")
          .append("placeholder=\"").append(placeholder).append("\" ")
          .append("style=\"width:100%; background:transparent; border:none; color:#f8fafc; font-size:13px; outline:none;\" ")
          .append("oninput=\"(function(inp){")
          .append("var q=inp.value.toLowerCase().trim();")
          .append("var cont=document.getElementById('").append(containerId).append("_options');")
          .append("var items=cont.querySelectorAll('.select-filter-item');")
          .append("var matched=0;")
          .append("items.forEach(function(el){")
          .append("var hay=(el.getAttribute('data-search')||'').toLowerCase();")
          .append("if(!q||hay.indexOf(q)!==-1){el.style.display='flex';matched++;}else{el.style.display='none';}")
          .append("});")
          .append("var emp=document.getElementById('").append(containerId).append("_empty');")
          .append("if(emp){emp.style.display=(matched===0)?'block':'none';}")
          .append("})(this)\"/>\n");
        sb.append("  </div>\n");

        // Scrollable Options List
        sb.append("  <div id=\"").append(containerId).append("_options\" class=\"select-filter-options\" ")
          .append("style=\"max-height:190px; overflow-y:auto; display:flex; flex-direction:column; gap:4px; padding-right:2px;\">\n");

        for (FilterOption opt : options) {
            boolean isSelected = opt.value().equals(val) || (val.isEmpty() && opt.selected());
            String searchableText = (opt.label() + " " + (opt.description() != null ? opt.description() : "") + " " + (opt.badge() != null ? opt.badge() : "")).toLowerCase();

            sb.append("    <div class=\"select-filter-item").append(isSelected ? " selected" : "").append("\" ")
              .append("data-value=\"").append(opt.value()).append("\" ")
              .append("data-label=\"").append(opt.label()).append("\" ")
              .append("data-search=\"").append(searchableText).append("\" ")
              .append("style=\"display:flex; justify-content:space-between; align-items:center; padding:8px 12px; border-radius:6px; cursor:pointer; transition:all 0.15s ease; ")
              .append(isSelected
                  ? "background:rgba(56,189,248,0.15); border:1px solid rgba(56,189,248,0.4); color:#f8fafc;"
                  : "background:rgba(15,23,42,0.6); border:1px solid rgba(255,255,255,0.06); color:#cbd5e1;")
              .append("\" ")
              .append("onclick=\"(function(item){")
              .append("var p=item.closest('.jettra-select-filter');")
              .append("p.querySelectorAll('.select-filter-item').forEach(function(i){")
              .append("i.classList.remove('selected');")
              .append("i.style.background='rgba(15,23,42,0.6)';")
              .append("i.style.borderColor='rgba(255,255,255,0.06)';")
              .append("});")
              .append("item.classList.add('selected');")
              .append("item.style.background='rgba(56,189,248,0.15)';")
              .append("item.style.borderColor='rgba(56,189,248,0.4)';")
              .append("var hid=document.getElementById('").append(containerId).append("_value');")
              .append("if(hid){hid.value=item.getAttribute('data-value');}")
              .append("var searchInp=document.getElementById('").append(containerId).append("_search');")
              .append("if(searchInp){searchInp.value=item.getAttribute('data-label');}");
            if (onSelectJs != null && !onSelectJs.isBlank()) {
                sb.append(onSelectJs.replace("{value}", opt.value())).append(";");
            }
            sb.append("})(this)\">\n");

            // Option details: Label and Description
            sb.append("      <div style=\"display:flex; flex-direction:column; gap:2px;\">\n");
            sb.append("        <span style=\"font-weight:600; font-size:13px; color:#f8fafc;\"><i class=\"fas fa-user\" style=\"color:#38bdf8; font-size:11px; margin-right:6px;\"></i>").append(opt.label()).append("</span>\n");
            if (opt.description() != null && !opt.description().isBlank()) {
                sb.append("        <span style=\"font-size:11px; color:#94a3b8;\">").append(opt.description()).append("</span>\n");
            }
            sb.append("      </div>\n");

            // Badge / Indicator
            if (opt.badge() != null && !opt.badge().isBlank()) {
                String badgeColor = opt.badge().contains("ADMIN") ? "background:rgba(244,63,94,0.15); color:#f43f5e; border:1px solid rgba(244,63,94,0.3);"
                        : "background:rgba(56,189,248,0.15); color:#38bdf8; border:1px solid rgba(56,189,248,0.3);";
                sb.append("      <span style=\"font-size:10.5px; font-weight:700; padding:2px 8px; border-radius:4px; ").append(badgeColor).append("\">")
                  .append(opt.badge()).append("</span>\n");
            }

            sb.append("    </div>\n");
        }

        // Empty message state
        sb.append("    <div id=\"").append(containerId).append("_empty\" style=\"display:none; padding:14px; text-align:center; color:#94a3b8; font-size:12px; background:rgba(15,23,42,0.4); border-radius:6px;\">\n");
        sb.append("      <i class=\"fas fa-info-circle\" style=\"margin-right:4px;\"></i>").append(emptyMessage).append("\n");
        sb.append("    </div>\n");

        sb.append("  </div>\n");
        sb.append("</div>\n");
        return sb.toString();
    }
}
