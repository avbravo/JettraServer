package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

public class StatCard extends Widget {
    private final String header;
    private final String badgeText;
    private final String value;
    private final boolean isUp;

    private StatCard(String header, String badgeText, String value, boolean isUp) {
        this.header = header;
        this.badgeText = badgeText;
        this.value = value;
        this.isUp = isUp;
    }

    public static StatCard of(String header, String badgeText, String value, boolean isUp) {
        return new StatCard(header, badgeText, value, isUp);
    }

    @Override
    public String render(ThemeData theme) {
        String badgeClass = isUp ? "stat-badge up" : "stat-badge down";
        String icon = isUp ? "fas fa-arrow-up" : "fas fa-arrow-down";
        StringBuilder sb = new StringBuilder();
        sb.append("<div class='stat-card'>");
        sb.append("<div class='stat-header'>").append(header).append("</div>");
        sb.append("<div style='display: flex; align-items: center; gap: 15px;'>");
        sb.append("  <div class='").append(badgeClass).append("'><i class='").append(icon).append("'></i> ").append(badgeText).append("</div>");
        sb.append("  <div class='stat-value'>").append(value).append("</div>");
        sb.append("</div>");
        sb.append("</div>");
        
        return Card.of(Paragraph.of(sb.toString())).render(theme);
    }
}
