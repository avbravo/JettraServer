package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * BadgeList - Encapsulated JettraFlux component that renders collections of tags or database scopes
 * as styled badges instead of raw unformatted text.
 */
public class BadgeList extends Widget {

    private final List<String> items = new ArrayList<>();
    private String defaultSeverity = "info";
    private String wildcardSeverity = "active";
    private String defaultIcon = "fas fa-database";
    private String wildcardIcon = "fas fa-globe";
    private String emptyText = "None";
    private String gap = "4px";

    protected BadgeList(Collection<String> items) {
        if (items != null) {
            for (String item : items) {
                if (item != null && !item.isBlank()) {
                    this.items.add(item.trim());
                }
            }
        }
    }

    public static BadgeList of(Collection<String> items) {
        return new BadgeList(items);
    }

    public static BadgeList of(String... items) {
        return new BadgeList(items != null ? Arrays.asList(items) : List.of());
    }

    public static BadgeList ofCsv(String csv) {
        if (csv == null || csv.isBlank()) {
            return new BadgeList(List.of());
        }
        List<String> list = new ArrayList<>();
        for (String part : csv.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                list.add(trimmed);
            }
        }
        return new BadgeList(list);
    }

    public BadgeList defaultSeverity(String severity) {
        this.defaultSeverity = severity != null ? severity : "info";
        return this;
    }

    public BadgeList wildcardSeverity(String severity) {
        this.wildcardSeverity = severity != null ? severity : "active";
        return this;
    }

    public BadgeList defaultIcon(String icon) {
        this.defaultIcon = icon;
        return this;
    }

    public BadgeList wildcardIcon(String icon) {
        this.wildcardIcon = icon;
        return this;
    }

    public BadgeList emptyText(String text) {
        this.emptyText = text != null ? text : "";
        return this;
    }

    public BadgeList gap(String gap) {
        this.gap = gap != null ? gap : "4px";
        return this;
    }

    public List<String> getItems() {
        return items;
    }

    @Override
    public String render(ThemeData theme) {
        if (items.isEmpty()) {
            return "<span style=\"color:#64748b; font-size:12px; font-style:italic;\">" + emptyText + "</span>";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<div ").append(renderCommonAttributes(theme, "jettra-badge-list"))
          .append(" style=\"display:inline-flex; flex-wrap:wrap; gap:").append(gap).append("; align-items:center;\">\n");

        for (String item : items) {
            boolean isWildcard = "*".equals(item) || item.toUpperCase().contains("ALL");
            String label = isWildcard ? "ALL DATABASES" : item;
            String sev = isWildcard ? wildcardSeverity : defaultSeverity;
            String icon = isWildcard ? wildcardIcon : defaultIcon;

            Badge b = Badge.of(label, sev).icon(icon);
            sb.append("  ").append(b.render(theme)).append("\n");
        }

        sb.append("</div>\n");
        return sb.toString();
    }
}
