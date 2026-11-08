package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

import java.util.ArrayList;
import java.util.List;

/**
 * High-level composable panel container in JettraFlux.
 * Organizes dashboard sections with rich headers, badges, footers, and responsive grid layouts.
 * Backward compatible with legacy Panel.of(String header, Widget child).
 */
public class Panel extends Widget {

    private String headerText;
    private PanelHeader headerWidget;
    private PanelBody bodyWidget;
    private Widget child;
    private final List<Widget> children = new ArrayList<>();
    private Widget footerWidget;
    private boolean toggleable = false;

    public Panel() {}

    private Panel(String header, Widget child) {
        this.headerText = header;
        this.child = child;
    }

    public static Panel of(String header, Widget child) {
        return new Panel(header, child);
    }

    public static Panel of(PanelHeader header, PanelBody body) {
        Panel p = new Panel();
        p.headerWidget = header;
        p.bodyWidget = body;
        return p;
    }

    public static Panel of(String title) {
        Panel p = new Panel();
        p.headerWidget = PanelHeader.of(title);
        return p;
    }

    public static Panel of(Widget... children) {
        Panel p = new Panel();
        if (children != null) {
            for (Widget w : children) {
                if (w != null) p.children.add(w);
            }
        }
        return p;
    }

    public static PanelBuilder builder() {
        return new PanelBuilder();
    }

    public Panel header(PanelHeader header) {
        this.headerWidget = header;
        return this;
    }

    public Panel header(String title) {
        if (this.headerWidget == null) {
            this.headerWidget = PanelHeader.of(title);
        } else {
            this.headerWidget.title(title);
        }
        return this;
    }

    public Panel subtitle(String subtitle) {
        if (this.headerWidget == null) {
            this.headerWidget = new PanelHeader();
        }
        this.headerWidget.subtitle(subtitle);
        return this;
    }

    public Panel icon(String icon) {
        if (this.headerWidget == null) {
            this.headerWidget = new PanelHeader();
        }
        this.headerWidget.icon(icon);
        return this;
    }

    public Panel icon(String icon, String color) {
        if (this.headerWidget == null) {
            this.headerWidget = new PanelHeader();
        }
        this.headerWidget.icon(icon, color);
        return this;
    }

    public Panel badge(Badge badge) {
        if (this.headerWidget == null) {
            this.headerWidget = new PanelHeader();
        }
        this.headerWidget.badge(badge);
        return this;
    }

    public Panel badge(String text, String severity) {
        return badge(Badge.of(text, severity));
    }

    public Panel addAction(Widget action) {
        if (this.headerWidget == null) {
            this.headerWidget = new PanelHeader();
        }
        this.headerWidget.addAction(action);
        return this;
    }

    public Panel body(PanelBody body) {
        this.bodyWidget = body;
        return this;
    }

    public Panel grid(int columns, Widget... items) {
        this.bodyWidget = PanelBody.grid(columns, items);
        return this;
    }

    public Panel add(Widget childWidget) {
        if (this.bodyWidget != null) {
            this.bodyWidget.add(childWidget);
        } else {
            if (childWidget != null) this.children.add(childWidget);
        }
        return this;
    }

    public Panel addAll(List<Widget> childrenList) {
        if (this.bodyWidget != null) {
            this.bodyWidget.addAll(childrenList);
        } else {
            if (childrenList != null) this.children.addAll(childrenList);
        }
        return this;
    }

    public Panel footer(Widget footer) {
        this.footerWidget = footer;
        return this;
    }

    public Panel toggleable(boolean toggleable) {
        this.toggleable = toggleable;
        return this;
    }

    public PanelHeader getHeaderWidget() {
        return headerWidget;
    }

    public PanelBody getBodyWidget() {
        return bodyWidget;
    }

    public Widget getFooterWidget() {
        return footerWidget;
    }

    public boolean isToggleable() {
        return toggleable;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div ").append(renderCommonAttributes(theme, "espresso-panel jettra-panel store-card")).append(" ");
        sb.append("style=\"position: relative; overflow: hidden; display: flex; flex-direction: column; ")
          .append("background: rgba(30, 41, 59, 0.6); border: 1px solid rgba(255, 255, 255, 0.08); ")
          .append("border-radius: 14px; margin-bottom: 24px; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2); backdrop-filter: blur(8px); ")
          .append(modifier != null ? modifier.getStyles() : "").append("\">\n");

        // Top aesthetic accent bar
        sb.append("  <div style=\"position: absolute; top: 0; left: 0; right: 0; height: 3px; background: linear-gradient(90deg, #38bdf8, #818cf8, #f43f5e);\"></div>\n");

        // Render Header
        if (headerWidget != null) {
            sb.append(headerWidget.render(theme));
        } else if (headerText != null && !headerText.isEmpty()) {
            sb.append("  <div class=\"espresso-panel-header\">\n");
            sb.append("    <span>").append(headerText).append("</span>\n");
            if (toggleable) {
                sb.append("    <span class=\"espresso-panel-icons\"><i class=\"fas fa-chevron-down\"></i></span>\n");
            }
            sb.append("  </div>\n");
        }

        // Render Body
        if (bodyWidget != null) {
            sb.append(bodyWidget.render(theme));
        } else if (child != null || !children.isEmpty()) {
            sb.append("  <div class=\"espresso-panel-content\">\n");
            if (child != null) {
                sb.append(child.render(theme));
            }
            for (Widget c : children) {
                sb.append(c.render(theme));
            }
            sb.append("  </div>\n");
        }

        // Render Footer
        if (footerWidget != null) {
            sb.append("  <div class=\"espresso-panel-footer\" style=\"padding: 14px 24px; border-top: 1px solid rgba(255, 255, 255, 0.06); background: rgba(15, 23, 42, 0.3);\">\n");
            sb.append("    ").append(footerWidget.render(theme)).append("\n");
            sb.append("  </div>\n");
        }

        sb.append("</div>\n");
        return sb.toString();
    }
}
