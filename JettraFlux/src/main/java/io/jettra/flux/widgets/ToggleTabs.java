package io.jettra.flux.widgets;

import io.jettra.flux.core.Modifier;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * ToggleTabs - Segmented tab toggle control component for JettraFlux.
 * Built with modern Java 25 standards, supporting accessible ARIA roles,
 * fluid builder API, custom icons, active state indication, and client-side callbacks.
 */
public class ToggleTabs extends Widget {

    public record Tab(String id, String label, String icon, boolean active) {
        public Tab {
            Objects.requireNonNull(id, "Tab id cannot be null");
            Objects.requireNonNull(label, "Tab label cannot be null");
        }
    }

    private String activeTabId;
    private String onTabChangeJs;
    private final List<Tab> tabs = new ArrayList<>();

    private ToggleTabs(String id) {
        this.id = id;
    }

    public static ToggleTabs of(String id) {
        return new ToggleTabs(id);
    }

    public ToggleTabs addTab(String tabId, String label) {
        return addTab(tabId, label, null, tabs.isEmpty());
    }

    public ToggleTabs addTab(String tabId, String label, String icon) {
        return addTab(tabId, label, icon, tabs.isEmpty());
    }

    public ToggleTabs addTab(String tabId, String label, String icon, boolean active) {
        boolean effectiveActive = active;
        if (effectiveActive) {
            this.activeTabId = tabId;
            // Unmark previously active tabs
            for (int i = 0; i < tabs.size(); i++) {
                Tab t = tabs.get(i);
                if (t.active()) {
                    tabs.set(i, new Tab(t.id(), t.label(), t.icon(), false));
                }
            }
        }
        tabs.add(new Tab(tabId, label, icon, effectiveActive));
        return this;
    }

    public ToggleTabs activeTab(String tabId) {
        this.activeTabId = tabId;
        for (int i = 0; i < tabs.size(); i++) {
            Tab t = tabs.get(i);
            boolean match = t.id().equals(tabId);
            tabs.set(i, new Tab(t.id(), t.label(), t.icon(), match));
        }
        return this;
    }

    public ToggleTabs onTabChange(String onTabChangeJs) {
        this.onTabChangeJs = onTabChangeJs;
        return this;
    }

    public List<Tab> tabs() {
        return Collections.unmodifiableList(tabs);
    }

    public String activeTabId() {
        return activeTabId;
    }

    @Override
    public ToggleTabs modifier(Modifier modifier) {
        super.modifier(modifier);
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        String containerId = (id != null && !id.isBlank()) ? id : "toggleTabs_" + System.identityHashCode(this);

        sb.append("<div id=\"").append(containerId).append("\" ");
        sb.append("role=\"tablist\" class=\"jettra-toggle-tabs");
        if (modifier != null && modifier.getClasses() != null && !modifier.getClasses().isBlank()) {
            sb.append(" ").append(modifier.getClasses().trim());
        }
        sb.append("\" style=\"display:inline-flex; align-items:center; gap:4px; padding:4px; background:rgba(15,23,42,0.7); ");
        sb.append("border:1px solid rgba(255,255,255,0.12); border-radius:10px; box-shadow:inset 0 1px 3px rgba(0,0,0,0.3);");
        if (modifier != null && modifier.getStyles() != null && !modifier.getStyles().isBlank()) {
            sb.append(" ").append(modifier.getStyles().trim());
        }
        sb.append("\">\n");

        for (Tab tab : tabs) {
            boolean isActive = (activeTabId != null) ? tab.id().equals(activeTabId) : tab.active();

            sb.append("  <button type=\"button\" role=\"tab\" ");
            sb.append("id=\"").append(containerId).append("_tab_").append(tab.id()).append("\" ");
            sb.append("data-tab-id=\"").append(tab.id()).append("\" ");
            sb.append("aria-selected=\"").append(isActive ? "true" : "false").append("\" ");
            sb.append("class=\"jettra-tab-btn").append(isActive ? " active" : "").append("\" ");

            // Inline styles for high-fidelity appearance in dark theme
            sb.append("style=\"display:inline-flex; align-items:center; gap:8px; padding:7px 14px; border-radius:7px; font-size:13px; font-weight:");
            sb.append(isActive ? "600" : "500").append("; cursor:pointer; transition:all 0.2s ease; outline:none; ");
            if (isActive) {
                sb.append("background:rgba(56,189,248,0.2); color:#38bdf8; border:1px solid rgba(56,189,248,0.45); box-shadow:0 0 10px rgba(56,189,248,0.25);");
            } else {
                sb.append("background:transparent; color:#94a3b8; border:1px solid transparent;");
            }
            sb.append("\" ");

            // Click handling
            StringBuilder clickHandler = new StringBuilder();
            clickHandler.append("(function(btn){");
            clickHandler.append("var p=btn.closest('[role=tablist]');");
            clickHandler.append("p.querySelectorAll('button[role=tab]').forEach(function(b){");
            clickHandler.append("b.setAttribute('aria-selected','false');");
            clickHandler.append("b.style.background='transparent';");
            clickHandler.append("b.style.color='#94a3b8';");
            clickHandler.append("b.style.borderColor='transparent';");
            clickHandler.append("b.style.boxShadow='none';");
            clickHandler.append("b.style.fontWeight='500';");
            clickHandler.append("b.classList.remove('active');");
            clickHandler.append("});");
            clickHandler.append("btn.setAttribute('aria-selected','true');");
            clickHandler.append("btn.style.background='rgba(56,189,248,0.2)';");
            clickHandler.append("btn.style.color='#38bdf8';");
            clickHandler.append("btn.style.borderColor='rgba(56,189,248,0.45)';");
            clickHandler.append("btn.style.boxShadow='0 0 10px rgba(56,189,248,0.25)';");
            clickHandler.append("btn.style.fontWeight='600';");
            clickHandler.append("btn.classList.add('active');");
            if (onTabChangeJs != null && !onTabChangeJs.isBlank()) {
                clickHandler.append(onTabChangeJs.replace("{tabId}", tab.id())).append(";");
            }
            clickHandler.append("})(this);");

            sb.append("onclick=\"").append(clickHandler.toString()).append("\">\n");

            if (tab.icon() != null && !tab.icon().isBlank()) {
                sb.append("    <i class=\"").append(tab.icon()).append("\"></i>\n");
            }
            sb.append("    <span>").append(tab.label()).append("</span>\n");
            sb.append("  </button>\n");
        }

        sb.append("</div>\n");
        return sb.toString();
    }
}
