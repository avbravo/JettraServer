package io.jettra.flux.widgets;

import io.jettra.flux.core.Modifier;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

import java.util.ArrayList;
import java.util.List;

/**
 * JettraCardPanel - High-level unified panel component in JettraFlux.
 * Organizes complex dashboard sections (such as Multi-Model Components, Active Databases,
 * and Storage Partition Hierarchies) with rich headers, badges, responsive grids, and sleek aesthetics.
 */
public class JettraCardPanel extends Widget {

    private String title = "";
    private String subtitle = "";
    private String icon = "";
    private String iconColor = "#38bdf8";
    private String badgeText = "";
    private String badgeClass = "badge-active";
    private final List<Widget> headerActions = new ArrayList<>();
    private final List<Widget> children = new ArrayList<>();

    private JettraCardPanel() {}

    public static JettraCardPanel of() {
        return new JettraCardPanel();
    }

    public static JettraCardPanel of(String title) {
        JettraCardPanel p = new JettraCardPanel();
        p.title = title;
        return p;
    }

    public JettraCardPanel title(String title) {
        this.title = title;
        return this;
    }

    public JettraCardPanel subtitle(String subtitle) {
        this.subtitle = subtitle;
        return this;
    }

    public JettraCardPanel icon(String icon) {
        this.icon = icon;
        return this;
    }

    public JettraCardPanel iconColor(String iconColor) {
        this.iconColor = iconColor;
        return this;
    }

    public JettraCardPanel badge(String text, String badgeClass) {
        this.badgeText = text;
        this.badgeClass = badgeClass;
        return this;
    }

    public JettraCardPanel addHeaderAction(Widget action) {
        if (action != null) headerActions.add(action);
        return this;
    }

    public JettraCardPanel add(Widget child) {
        if (child != null) children.add(child);
        return this;
    }

    public JettraCardPanel addAll(List<Widget> list) {
        if (list != null) children.addAll(list);
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();

        sb.append("<div class=\"store-card ")
          .append(modifier != null ? modifier.getClasses() : "").append("\" ");
        sb.append("style=\"position:relative; overflow:hidden; display:flex; flex-direction:column; gap:16px; margin-bottom:24px; ")
          .append(modifier != null ? modifier.getStyles() : "").append("\">\n");

        // Top aesthetic accent bar
        sb.append("  <div style=\"position:absolute; top:0; left:0; width:100%; height:3px; background:linear-gradient(90deg, #38bdf8, #818cf8, #f43f5e);\"></div>\n");

        // Panel Header
        if (!title.isEmpty() || !headerActions.isEmpty()) {
            sb.append("  <div style=\"display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:12px; padding-bottom:12px; border-bottom:1px solid rgba(255,255,255,0.06);\">\n");
            sb.append("    <div style=\"display:flex; align-items:center; gap:12px;\">\n");
            if (icon != null && !icon.isBlank()) {
                sb.append("      <div style=\"width:38px; height:38px; border-radius:10px; background:rgba(56,189,248,0.12); display:flex; align-items:center; justify-content:center; color:").append(iconColor).append("; font-size:18px;\">\n");
                sb.append("        <i class=\"").append(icon).append("\"></i>\n");
                sb.append("      </div>\n");
            }
            sb.append("      <div>\n");
            sb.append("        <div style=\"display:flex; align-items:center; gap:8px;\">\n");
            sb.append("          <h3 style=\"margin:0; font-size:17px; font-weight:700; color:#f8fafc;\">").append(title).append("</h3>\n");
            if (badgeText != null && !badgeText.isBlank()) {
                sb.append("          <span class=\"store-badge ").append(badgeClass).append("\" style=\"font-size:11px;\">").append(badgeText).append("</span>\n");
            }
            sb.append("        </div>\n");
            if (subtitle != null && !subtitle.isBlank()) {
                sb.append("        <p style=\"margin:3px 0 0 0; font-size:12.5px; color:#94a3b8;\">").append(subtitle).append("</p>\n");
            }
            sb.append("      </div>\n");
            sb.append("    </div>\n");

            if (!headerActions.isEmpty()) {
                sb.append("    <div style=\"display:flex; align-items:center; gap:8px;\">\n");
                for (Widget w : headerActions) {
                    sb.append(w.render(theme));
                }
                sb.append("    </div>\n");
            }
            sb.append("  </div>\n");
        }

        // Panel Body
        sb.append("  <div style=\"display:flex; flex-direction:column; gap:16px;\">\n");
        for (Widget child : children) {
            sb.append(child.render(theme));
        }
        sb.append("  </div>\n");

        sb.append("</div>\n");
        return sb.toString();
    }
}
