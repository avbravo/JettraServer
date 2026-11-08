package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

/**
 * Reusable dashboard metric card component for JettraFlux.
 * Displays title labels, high-visibility primary values, auxiliary subtext,
 * iconography, accent highlights, and configurable status badges.
 */
public class MetricCard extends Widget {

    /**
     * Java 25 Record encapsulating the metric state model.
     */
    public record MetricData(
        String title,
        String value,
        String subtext,
        String icon,
        String color,
        Badge badge
    ) {
        public MetricData {
            if (title == null) title = "";
            if (value == null) value = "";
            if (subtext == null) subtext = "";
            if (icon == null) icon = "";
            if (color == null || color.isBlank()) color = "#3b82f6";
        }
    }

    private MetricData data;

    public MetricCard() {
        this.data = new MetricData("", "", "", "", "#3b82f6", null);
    }

    public MetricCard(MetricData data) {
        this.data = data != null ? data : new MetricData("", "", "", "", "#3b82f6", null);
    }

    public static MetricCard of(MetricData data) {
        return new MetricCard(data);
    }

    public static MetricCard of(String title, String value) {
        return new MetricCard(new MetricData(title, value, "", "", "#3b82f6", null));
    }

    public static MetricCard of(String title, String value, String subtext) {
        return new MetricCard(new MetricData(title, value, subtext, "", "#3b82f6", null));
    }

    public static MetricCard of(String icon, String color, String title, String value, String subtext, Badge badge) {
        return new MetricCard(new MetricData(title, value, subtext, icon, color, badge));
    }

    public static MetricCardBuilder builder() {
        return new MetricCardBuilder();
    }

    public MetricData getData() {
        return data;
    }

    public MetricCard title(String title) {
        this.data = new MetricData(title, data.value(), data.subtext(), data.icon(), data.color(), data.badge());
        return this;
    }

    public MetricCard value(String value) {
        this.data = new MetricData(data.title(), value, data.subtext(), data.icon(), data.color(), data.badge());
        return this;
    }

    public MetricCard subtext(String subtext) {
        this.data = new MetricData(data.title(), data.value(), subtext, data.icon(), data.color(), data.badge());
        return this;
    }

    public MetricCard icon(String icon) {
        this.data = new MetricData(data.title(), data.value(), data.subtext(), icon, data.color(), data.badge());
        return this;
    }

    public MetricCard color(String color) {
        this.data = new MetricData(data.title(), data.value(), data.subtext(), data.icon(), color, data.badge());
        return this;
    }

    public MetricCard badge(Badge badge) {
        this.data = new MetricData(data.title(), data.value(), data.subtext(), data.icon(), data.color(), badge);
        return this;
    }

    public MetricCard badge(String text, String severity) {
        return badge(Badge.of(text, severity));
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        String color = data.color();

        sb.append("<div class=\"jettra-metric-card store-card ")
          .append(modifier != null ? modifier.getClasses() : "").append("\" ");
        sb.append("style=\"position: relative; overflow: hidden; background: rgba(30, 41, 59, 0.7); ")
          .append("border: 1px solid rgba(255, 255, 255, 0.08); border-radius: 12px; padding: 20px 22px; ")
          .append("display: flex; flex-direction: column; justify-content: space-between; gap: 12px; ")
          .append("box-shadow: 0 4px 16px rgba(0, 0, 0, 0.25); min-width: 220px; ")
          .append(modifier != null ? modifier.getStyles() : "").append("\">\n");

        // Top accent line
        sb.append("  <div style=\"position: absolute; top: 0; left: 0; right: 0; height: 3px; background: ")
          .append(color).append(";\"></div>\n");

        // Top Row: Icon + Title Header
        sb.append("  <div style=\"display: flex; align-items: center; gap: 12px;\">\n");
        if (!data.icon().isBlank()) {
            sb.append("    <div style=\"width: 36px; height: 36px; border-radius: 9px; background: ")
              .append(color).append("20; display: flex; align-items: center; justify-content: center; color: ")
              .append(color).append("; font-size: 16px; border: 1px solid ").append(color).append("33; flex-shrink: 0;\">\n");
            sb.append("      <i class=\"").append(data.icon()).append("\"></i>\n");
            sb.append("    </div>\n");
        }
        sb.append("    <span style=\"font-size: 12.5px; font-weight: 600; color: #94a3b8; text-transform: uppercase; letter-spacing: 0.5px;\">")
          .append(data.title()).append("</span>\n");
        sb.append("  </div>\n");

        // Center: Primary Value
        sb.append("  <div style=\"font-size: 24px; font-weight: 800; color: #f8fafc; letter-spacing: -0.5px; line-height: 1.2;\">")
          .append(data.value()).append("</div>\n");

        // Bottom Row: Auxiliary Subtext & Status Badge
        sb.append("  <div style=\"display: flex; justify-content: space-between; align-items: center; gap: 8px; border-top: 1px solid rgba(255, 255, 255, 0.06); padding-top: 10px; margin-top: 2px;\">\n");
        sb.append("    <span style=\"font-size: 12px; color: #cbd5e1; font-weight: 500;\">").append(data.subtext()).append("</span>\n");
        if (data.badge() != null) {
            sb.append("    ").append(data.badge().render(theme)).append("\n");
        }
        sb.append("  </div>\n");

        sb.append("</div>\n");
        return sb.toString();
    }

    public static class MetricCardBuilder {
        private String title = "";
        private String value = "";
        private String subtext = "";
        private String icon = "";
        private String color = "#3b82f6";
        private Badge badge = null;

        public MetricCardBuilder title(String title) { this.title = title; return this; }
        public MetricCardBuilder value(String value) { this.value = value; return this; }
        public MetricCardBuilder subtext(String subtext) { this.subtext = subtext; return this; }
        public MetricCardBuilder detail(String detail) { this.subtext = detail; return this; }
        public MetricCardBuilder icon(String icon) { this.icon = icon; return this; }
        public MetricCardBuilder icon(String icon, String color) { this.icon = icon; this.color = color; return this; }
        public MetricCardBuilder color(String color) { this.color = color; return this; }
        public MetricCardBuilder badge(Badge badge) { this.badge = badge; return this; }
        public MetricCardBuilder badge(String text, String severity) { this.badge = Badge.of(text, severity); return this; }

        public MetricCard build() {
            return new MetricCard(new MetricData(title, value, subtext, icon, color, badge));
        }
    }
}
