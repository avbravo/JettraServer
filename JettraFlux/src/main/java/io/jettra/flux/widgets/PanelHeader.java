package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

import java.util.ArrayList;
import java.util.List;

/**
 * Composable header component for JettraFlux Panel containers.
 * Supports structured titles, subtitles, status badges, contextual icons, and action toolbars.
 */
public class PanelHeader extends Widget {

    private String title = "";
    private Widget titleWidget;
    private String subtitle = "";
    private String icon = "";
    private String iconColor = "#38bdf8";
    private Badge badge;
    private boolean actionsVisible = true;
    private final List<Widget> actions = new ArrayList<>();

    public PanelHeader() {}

    public PanelHeader(String title) {
        this.title = title != null ? title : "";
    }

    public static PanelHeader of(String title) {
        return new PanelHeader(title);
    }

    public static PanelHeader of(String title, String subtitle) {
        PanelHeader header = new PanelHeader(title);
        header.subtitle = subtitle != null ? subtitle : "";
        return header;
    }

    public static PanelHeaderBuilder builder() {
        return new PanelHeaderBuilder();
    }

    public PanelHeader title(String title) {
        this.title = title != null ? title : "";
        return this;
    }

    public PanelHeader title(Widget titleWidget) {
        this.titleWidget = titleWidget;
        return this;
    }

    public PanelHeader subtitle(String subtitle) {
        this.subtitle = subtitle != null ? subtitle : "";
        return this;
    }

    public PanelHeader icon(String icon) {
        this.icon = icon != null ? icon : "";
        return this;
    }

    public PanelHeader icon(String icon, String iconColor) {
        this.icon = icon != null ? icon : "";
        this.iconColor = iconColor != null ? iconColor : "#38bdf8";
        return this;
    }

    public PanelHeader iconColor(String iconColor) {
        this.iconColor = iconColor != null ? iconColor : "#38bdf8";
        return this;
    }

    public PanelHeader badge(Badge badge) {
        this.badge = badge;
        return this;
    }

    public PanelHeader badge(String text, String severity) {
        this.badge = Badge.of(text, severity);
        return this;
    }

    public PanelHeader withActionsVisible(boolean visible) {
        this.actionsVisible = visible;
        return this;
    }

    public boolean isActionsVisible() {
        return actionsVisible;
    }

    public PanelHeader addAction(Widget action) {
        if (action != null) actions.add(action);
        return this;
    }

    public PanelHeader actions(Widget... actionWidgets) {
        if (actionWidgets != null) {
            for (Widget w : actionWidgets) {
                if (w != null) actions.add(w);
            }
        }
        return this;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public Badge getBadge() {
        return badge;
    }

    public List<Widget> getActions() {
        return List.copyOf(actions);
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"espresso-panel-header jettra-panel-header ")
          .append(modifier != null ? modifier.getClasses() : "").append("\" ");
        sb.append("style=\"display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 14px; padding: 18px 24px; border-bottom: 1px solid rgba(255, 255, 255, 0.08); background: rgba(15, 23, 42, 0.4); ")
          .append(modifier != null ? modifier.getStyles() : "").append("\">\n");

        // Left Section: Icon + Title stack + Badge
        sb.append("  <div style=\"display: flex; align-items: center; gap: 14px;\">\n");
        if (icon != null && !icon.isBlank()) {
            sb.append("    <div style=\"width: 40px; height: 40px; border-radius: 10px; background: ")
              .append(iconColor).append("18; display: flex; align-items: center; justify-content: center; color: ")
              .append(iconColor).append("; font-size: 18px; border: 1px solid ").append(iconColor).append("33; flex-shrink: 0;\">\n");
            sb.append("      <i class=\"").append(icon).append("\"></i>\n");
            sb.append("    </div>\n");
        }
        sb.append("    <div>\n");
        sb.append("      <div style=\"display: flex; align-items: center; gap: 10px; flex-wrap: wrap;\">\n");
        if (titleWidget != null) {
            sb.append(titleWidget.render(theme));
        } else {
            sb.append("        <h3 style=\"margin: 0; font-size: 17px; font-weight: 700; color: #f8fafc; letter-spacing: -0.2px;\">")
              .append(title).append("</h3>\n");
        }
        if (badge != null) {
            sb.append("        ").append(badge.render(theme)).append("\n");
        }
        sb.append("      </div>\n");
        if (subtitle != null && !subtitle.isBlank()) {
            sb.append("      <p style=\"margin: 4px 0 0 0; font-size: 12.5px; color: #94a3b8; line-height: 1.4;\">")
              .append(subtitle).append("</p>\n");
        }
        sb.append("    </div>\n");
        sb.append("  </div>\n");

        // Right Section: Action widgets
        if (!actions.isEmpty() && actionsVisible) {
            sb.append("  <div style=\"display: flex; align-items: center; gap: 10px; flex-wrap: wrap;\">\n");
            for (Widget action : actions) {
                sb.append("    ").append(action.render(theme)).append("\n");
            }
            sb.append("  </div>\n");
        }

        sb.append("</div>\n");
        return sb.toString();
    }

    public static class PanelHeaderBuilder {
        private final PanelHeader header = new PanelHeader();

        public PanelHeaderBuilder title(String title) { header.title(title); return this; }
        public PanelHeaderBuilder title(Widget titleWidget) { header.title(titleWidget); return this; }
        public PanelHeaderBuilder subtitle(String subtitle) { header.subtitle(subtitle); return this; }
        public PanelHeaderBuilder icon(String icon) { header.icon(icon); return this; }
        public PanelHeaderBuilder icon(String icon, String iconColor) { header.icon(icon, iconColor); return this; }
        public PanelHeaderBuilder iconColor(String iconColor) { header.iconColor(iconColor); return this; }
        public PanelHeaderBuilder badge(Badge badge) { header.badge(badge); return this; }
        public PanelHeaderBuilder badge(String text, String severity) { header.badge(text, severity); return this; }
        public PanelHeaderBuilder withActionsVisible(boolean visible) { header.withActionsVisible(visible); return this; }
        public PanelHeaderBuilder addAction(Widget action) { header.addAction(action); return this; }
        public PanelHeaderBuilder actions(Widget... actions) { header.actions(actions); return this; }
        public PanelHeader build() { return header; }
    }
}
