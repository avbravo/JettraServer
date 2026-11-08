package io.jettra.flux.widgets;

import io.jettra.flux.core.Modifier;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Native ViewSwitcher Widget for JettraFlux.
 * Provides a high-performance, accessible, and reactive segmented control
 * to toggle between multiple visual perspectives (e.g. List View vs. Tree View vs. Table View).
 *
 * Implements:
 * - Builder Pattern for fluent assembly of perspective views.
 * - Accessible WAI-ARIA Tabs / Panel pattern (role="tablist", role="tab", role="tabpanel").
 * - Clean separation between presentation controls and content panels.
 * - Idempotent client-side switching controller with optional callback hooks.
 */
public class ViewSwitcher extends Widget {

    public record ViewItem(
        String id,
        String label,
        String icon,
        String badge,
        Widget content
    ) {
        public ViewItem {
            Objects.requireNonNull(id, "View id cannot be null");
            Objects.requireNonNull(label, "View label cannot be null");
        }
    }

    private final String switcherId;
    private String activeViewId;
    private String ariaLabel = "Perspective View Switcher";
    private String onViewChangeJs;
    private final List<ViewItem> views = new ArrayList<>();

    public ViewSwitcher() {
        this("viewSwitcher_" + Integer.toHexString(System.identityHashCode(new Object())));
    }

    public ViewSwitcher(String switcherId) {
        this.switcherId = (switcherId != null && !switcherId.isBlank())
            ? switcherId
            : "viewSwitcher_" + Integer.toHexString(System.identityHashCode(this));
        this.id(this.switcherId);
    }

    public static ViewSwitcher of() {
        return new ViewSwitcher();
    }

    public static ViewSwitcher of(String id) {
        return new ViewSwitcher(id);
    }

    public static Builder builder() {
        return new Builder();
    }

    public ViewSwitcher addView(String viewId, String label, String icon, Widget content) {
        return addView(viewId, label, icon, null, content, views.isEmpty());
    }

    public ViewSwitcher addView(String viewId, String label, String icon, Widget content, boolean active) {
        return addView(viewId, label, icon, null, content, active);
    }

    public ViewSwitcher addView(String viewId, String label, String icon, String badge, Widget content, boolean active) {
        ViewItem item = new ViewItem(viewId, label, icon, badge, content);
        this.views.add(item);
        if (active || this.activeViewId == null) {
            this.activeViewId = viewId;
        }
        return this;
    }

    public ViewSwitcher activeView(String viewId) {
        if (viewId != null && !viewId.isBlank()) {
            this.activeViewId = viewId;
        }
        return this;
    }

    public ViewSwitcher ariaLabel(String ariaLabel) {
        this.ariaLabel = ariaLabel;
        return this;
    }

    public ViewSwitcher onViewChange(String onViewChangeJs) {
        this.onViewChangeJs = onViewChangeJs;
        return this;
    }

    public String getSwitcherId() {
        return switcherId;
    }

    public String getActiveViewId() {
        return activeViewId;
    }

    public List<ViewItem> getViews() {
        return Collections.unmodifiableList(views);
    }

    @Override
    public ViewSwitcher id(String id) {
        super.id(id);
        return this;
    }

    @Override
    public ViewSwitcher modifier(Modifier modifier) {
        super.modifier(modifier);
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        String currentActive = (activeViewId != null) ? activeViewId : (views.isEmpty() ? "" : views.getFirst().id());

        sb.append("<div id=\"").append(switcherId).append("\" class=\"jettra-view-switcher");
        if (modifier != null && modifier.getClasses() != null && !modifier.getClasses().isBlank()) {
            sb.append(" ").append(modifier.getClasses().trim());
        }
        sb.append("\" style=\"width:100%; display:flex; flex-direction:column; gap:14px;");
        if (modifier != null && modifier.getStyles() != null && !modifier.getStyles().isBlank()) {
            sb.append(" ").append(modifier.getStyles().trim());
        }
        sb.append("\">\n");

        // Segmented Switcher Header Toolbar
        sb.append("  <div style=\"display:flex; justify-content:flex-end; align-items:center; margin-bottom:2px;\">\n");
        sb.append("    <div role=\"tablist\" aria-label=\"").append(escapeHtml(ariaLabel)).append("\" ")
          .append("class=\"view-switcher-control\" ")
          .append("style=\"display:inline-flex; align-items:center; background:rgba(15,23,42,0.7); padding:3px; border-radius:8px; border:1px solid rgba(255,255,255,0.1); gap:2px;\">\n");

        for (ViewItem view : views) {
            boolean isActive = view.id().equalsIgnoreCase(currentActive);
            String tabBtnId = switcherId + "_tab_" + view.id();
            String panelId = switcherId + "_panel_" + view.id();

            sb.append("      <button id=\"").append(tabBtnId).append("\" ")
              .append("type=\"button\" role=\"tab\" ")
              .append("aria-selected=\"").append(isActive).append("\" ")
              .append("aria-controls=\"").append(panelId).append("\" ")
              .append("class=\"view-switcher-btn").append(isActive ? " active" : "").append("\" ")
              .append("onclick=\"window.ViewSwitcher && window.ViewSwitcher.switchView('").append(switcherId).append("', '").append(view.id()).append("')\" ")
              .append("style=\"display:inline-flex; align-items:center; gap:6px; padding:6px 14px; border-radius:6px; font-size:12.5px; font-weight:600; cursor:pointer; transition:all 0.18s ease; border:none; ");

            if (isActive) {
                sb.append("background:#0284c7; color:#f8fafc; box-shadow:0 1px 4px rgba(0,0,0,0.3);");
            } else {
                sb.append("background:transparent; color:#94a3b8;");
            }
            sb.append("\">\n");

            if (view.icon() != null && !view.icon().isBlank()) {
                sb.append("        <i class=\"").append(view.icon()).append("\"></i>\n");
            }
            sb.append("        <span>").append(escapeHtml(view.label())).append("</span>\n");

            if (view.badge() != null && !view.badge().isBlank()) {
                sb.append("        <span style=\"font-size:10px; padding:1px 5px; border-radius:4px; font-weight:700; background:rgba(0,0,0,0.25);\">")
                  .append(escapeHtml(view.badge())).append("</span>\n");
            }

            sb.append("      </button>\n");
        }

        sb.append("    </div>\n");
        sb.append("  </div>\n");

        // View Panels
        for (ViewItem view : views) {
            boolean isActive = view.id().equalsIgnoreCase(currentActive);
            String panelId = switcherId + "_panel_" + view.id();
            String tabBtnId = switcherId + "_tab_" + view.id();

            sb.append("  <div id=\"").append(panelId).append("\" ")
              .append("role=\"tabpanel\" aria-labelledby=\"").append(tabBtnId).append("\" ")
              .append("class=\"view-switcher-panel\" ")
              .append("style=\"display:").append(isActive ? "block" : "none").append("; width:100%;\">\n");

            if (view.content() != null) {
                sb.append(view.content().render(theme)).append("\n");
            }

            sb.append("  </div>\n");
        }

        // Global Client Controller Script (Idempotent)
        sb.append("  <script>\n")
          .append("    if (!window.ViewSwitcher) {\n")
          .append("      window.ViewSwitcher = {\n")
          .append("        switchView: function(switcherId, targetViewId) {\n")
          .append("          var container = document.getElementById(switcherId);\n")
          .append("          if (!container) return;\n")
          .append("          var btns = container.querySelectorAll('.view-switcher-btn');\n")
          .append("          btns.forEach(function(b) {\n")
          .append("            var isTarget = b.id === (switcherId + '_tab_' + targetViewId);\n")
          .append("            b.setAttribute('aria-selected', isTarget ? 'true' : 'false');\n")
          .append("            if (isTarget) {\n")
          .append("              b.classList.add('active');\n")
          .append("              b.style.background = '#0284c7';\n")
          .append("              b.style.color = '#f8fafc';\n")
          .append("              b.style.boxShadow = '0 1px 4px rgba(0,0,0,0.3)';\n")
          .append("            } else {\n")
          .append("              b.classList.remove('active');\n")
          .append("              b.style.background = 'transparent';\n")
          .append("              b.style.color = '#94a3b8';\n")
          .append("              b.style.boxShadow = 'none';\n")
          .append("            }\n")
          .append("          });\n")
          .append("          var panels = container.querySelectorAll('.view-switcher-panel');\n")
          .append("          panels.forEach(function(p) {\n")
          .append("            var isTarget = p.id === (switcherId + '_panel_' + targetViewId);\n")
          .append("            p.style.display = isTarget ? 'block' : 'none';\n")
          .append("          });\n");

        if (onViewChangeJs != null && !onViewChangeJs.isBlank()) {
            sb.append("          try { (function() { ").append(onViewChangeJs.replace("{viewId}", "targetViewId")).append(" })(); } catch(e) {}\n");
        }

        sb.append("        }\n")
          .append("      };\n")
          .append("    }\n")
          .append("  </script>\n")
          .append("</div>\n");

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

    public static class Builder {
        private String id;
        private String activeViewId;
        private String ariaLabel = "Perspective View Switcher";
        private String onViewChangeJs;
        private final List<ViewItem> views = new ArrayList<>();

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder activeView(String activeViewId) {
            this.activeViewId = activeViewId;
            return this;
        }

        public Builder ariaLabel(String ariaLabel) {
            this.ariaLabel = ariaLabel;
            return this;
        }

        public Builder onViewChange(String onViewChangeJs) {
            this.onViewChangeJs = onViewChangeJs;
            return this;
        }

        public Builder addView(String viewId, String label, String icon, Widget content) {
            return addView(viewId, label, icon, null, content, views.isEmpty());
        }

        public Builder addView(String viewId, String label, String icon, Widget content, boolean active) {
            return addView(viewId, label, icon, null, content, active);
        }

        public Builder addView(String viewId, String label, String icon, String badge, Widget content, boolean active) {
            this.views.add(new ViewItem(viewId, label, icon, badge, content));
            if (active || this.activeViewId == null) {
                this.activeViewId = viewId;
            }
            return this;
        }

        public ViewSwitcher build() {
            ViewSwitcher vs = new ViewSwitcher(this.id);
            if (this.activeViewId != null) vs.activeView(this.activeViewId);
            if (this.ariaLabel != null) vs.ariaLabel(this.ariaLabel);
            if (this.onViewChangeJs != null) vs.onViewChange(this.onViewChangeJs);
            for (ViewItem vi : views) {
                boolean isActive = vi.id().equals(this.activeViewId);
                vs.addView(vi.id(), vi.label(), vi.icon(), vi.badge(), vi.content(), isActive);
            }
            return vs;
        }
    }
}
