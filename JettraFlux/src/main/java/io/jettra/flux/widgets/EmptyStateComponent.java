package io.jettra.flux.widgets;

import io.jettra.flux.core.Modifier;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

/**
 * EmptyStateComponent - Reusable JettraFlux widget for displaying informative,
 * aesthetic empty states when collections, filtered items, or security-scoped entities are empty.
 */
public class EmptyStateComponent extends Widget {

    private String icon = "fas fa-database";
    private String title = "No Databases Found";
    private String description = "No items match your active authorization or filter criteria.";
    private String actionText = "";
    private String actionOnClick = "";

    private EmptyStateComponent() {}

    public static EmptyStateComponent of() {
        return new EmptyStateComponent();
    }

    public static EmptyStateComponent of(String title, String description) {
        EmptyStateComponent c = new EmptyStateComponent();
        c.title = title;
        c.description = description;
        return c;
    }

    public EmptyStateComponent icon(String icon) {
        this.icon = icon;
        return this;
    }

    public EmptyStateComponent title(String title) {
        this.title = title;
        return this;
    }

    public EmptyStateComponent description(String description) {
        this.description = description;
        return this;
    }

    public EmptyStateComponent action(String text, String onClick) {
        this.actionText = text;
        this.actionOnClick = onClick;
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();

        sb.append("<div class=\"jettra-empty-state ")
          .append(modifier != null ? modifier.getClasses() : "").append("\" ");
        sb.append("style=\"display:flex; flex-direction:column; align-items:center; justify-content:center; ")
          .append("padding:48px 24px; text-align:center; background:rgba(15,23,42,0.6); border:1px dashed rgba(255,255,255,0.12); ")
          .append("border-radius:12px; margin:16px 0; ").append(modifier != null ? modifier.getStyles() : "").append("\">\n");

        sb.append("  <div style=\"width:60px; height:60px; border-radius:50%; background:rgba(56,189,248,0.1); ")
          .append("display:flex; align-items:center; justify-content:center; color:#38bdf8; font-size:26px; margin-bottom:16px;\">\n");
        sb.append("    <i class=\"").append(icon).append("\"></i>\n");
        sb.append("  </div>\n");

        sb.append("  <h4 style=\"margin:0 0 8px 0; font-size:16px; font-weight:700; color:#f8fafc;\">")
          .append(title).append("</h4>\n");

        sb.append("  <p style=\"margin:0; font-size:13px; color:#94a3b8; max-width:440px; line-height:1.5;\">")
          .append(description).append("</p>\n");

        if (actionText != null && !actionText.isBlank()) {
            sb.append("  <button type=\"button\" onclick=\"").append(actionOnClick).append("\" ")
              .append("style=\"margin-top:20px; padding:8px 18px; border-radius:8px; background:linear-gradient(135deg, #38bdf8, #2563eb); ")
              .append("color:#fff; border:none; font-size:13px; font-weight:600; cursor:pointer; display:flex; align-items:center; gap:8px; ")
              .append("box-shadow:0 4px 12px rgba(37,99,235,0.3); transition:all 0.15s;\" ")
              .append("onmouseover=\"this.style.transform='translateY(-1px)';\" onmouseout=\"this.style.transform='none';\">\n");
            sb.append("    ").append(actionText).append("\n");
            sb.append("  </button>\n");
        }

        sb.append("</div>\n");
        return sb.toString();
    }
}
