package io.jettra.flux.widgets;

import io.jettra.flux.core.Modifier;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Slim vertical Icon Rail navigation widget.
 * Implements Composite and Builder patterns, providing declarative state management
 * for item selection (State pattern) and dynamic route matching.
 */
public class IconRail extends Widget {

    private Widget logo;
    private final List<IconRailItem> topItems = new ArrayList<>();
    private final List<IconRailItem> bottomItems = new ArrayList<>();
    private String activeKey;

    public IconRail() {
        super();
        this.id = "jettra-icon-rail-" + System.identityHashCode(this);
    }

    public static IconRail of() {
        return new IconRail();
    }

    public static Builder builder() {
        return new Builder();
    }

    public IconRail logo(Widget logo) {
        this.logo = logo;
        return this;
    }

    public Widget getLogo() {
        return logo;
    }

    public IconRail addTopItem(IconRailItem item) {
        if (item != null) {
            topItems.add(item);
        }
        return this;
    }

    public IconRail addTopItems(IconRailItem... items) {
        if (items != null) {
            for (IconRailItem it : items) {
                addTopItem(it);
            }
        }
        return this;
    }

    public IconRail addBottomItem(IconRailItem item) {
        if (item != null) {
            bottomItems.add(item);
        }
        return this;
    }

    public IconRail addBottomItems(IconRailItem... items) {
        if (items != null) {
            for (IconRailItem it : items) {
                addBottomItem(it);
            }
        }
        return this;
    }

    public List<IconRailItem> getTopItems() {
        return Collections.unmodifiableList(topItems);
    }

    public List<IconRailItem> getBottomItems() {
        return Collections.unmodifiableList(bottomItems);
    }

    public List<IconRailItem> getAllItems() {
        List<IconRailItem> all = new ArrayList<>(topItems.size() + bottomItems.size());
        all.addAll(topItems);
        all.addAll(bottomItems);
        return Collections.unmodifiableList(all);
    }

    public String getActiveKey() {
        return activeKey;
    }

    /**
     * Sets active selection state by key or ID, dynamically updating all managed items.
     * Implements the State pattern for menu selection.
     */
    public IconRail selectItem(String key) {
        this.activeKey = key;
        for (IconRailItem item : getAllItems()) {
            boolean matches = key != null && (key.equalsIgnoreCase(item.getKey()) || key.equalsIgnoreCase(item.getId()));
            item.active(matches);
        }
        return this;
    }

    public IconRail activeItem(String key) {
        return selectItem(key);
    }

    /**
     * Sets active selection state dynamically by evaluating current route or path.
     */
    public IconRail selectByRoute(String currentRoute) {
        if (currentRoute == null || currentRoute.isBlank()) {
            return this;
        }
        String cleanRoute = currentRoute.trim();

        // 1. Direct key match (e.g. "databases", "components", "users", "settings")
        for (IconRailItem item : getAllItems()) {
            if (cleanRoute.equalsIgnoreCase(item.getKey()) || cleanRoute.equalsIgnoreCase("/" + item.getKey())) {
                return selectItem(item.getKey());
            }
        }

        // 2. Exact or best URI prefix match
        IconRailItem bestMatch = null;
        int longestMatchLength = -1;

        String routePath = cleanRoute.contains("?") ? cleanRoute.substring(0, cleanRoute.indexOf("?")) : cleanRoute;
        String routeQuery = cleanRoute.contains("?") ? cleanRoute.substring(cleanRoute.indexOf("?") + 1) : "";

        for (IconRailItem item : getAllItems()) {
            String href = item.getHref();
            if (href == null || href.isBlank() || href.equals("#") || href.contains("logout")) {
                continue;
            }
            String cleanHref = href.trim();
            String hrefPath = cleanHref.contains("?") ? cleanHref.substring(0, cleanHref.indexOf("?")) : cleanHref;
            String hrefQuery = cleanHref.contains("?") ? cleanHref.substring(cleanHref.indexOf("?") + 1) : "";

            // Check if query specifies special tabs like tab=settings or tab=schema
            if (!hrefQuery.isEmpty() && !routeQuery.isEmpty()) {
                if (cleanHref.equalsIgnoreCase(cleanRoute) || (hrefPath.equalsIgnoreCase(routePath) && routeQuery.contains(hrefQuery))) {
                    bestMatch = item;
                    break;
                }
            }

            if (hrefPath.equalsIgnoreCase(routePath) && hrefPath.length() > longestMatchLength) {
                // If item href has specific tab query (e.g. tab=settings), only match if route also has it
                if (!hrefQuery.isEmpty() && !routeQuery.contains(hrefQuery)) {
                    continue;
                }
                bestMatch = item;
                longestMatchLength = hrefPath.length();
            }
        }

        if (bestMatch != null) {
            selectItem(bestMatch.getKey());
        }
        return this;
    }

    public IconRail activeRoute(String currentRoute) {
        return selectByRoute(currentRoute);
    }

    public Optional<IconRailItem> getActiveItem() {
        return getAllItems().stream().filter(IconRailItem::isActive).findFirst();
    }

    @Override
    public IconRail modifier(Modifier modifier) {
        super.modifier(modifier);
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div ").append(renderCommonAttributes(theme, "jettra-icon-rail")).append(">\n");

        // Top Section
        sb.append("  <div class=\"rail-top-section\">\n");
        if (logo != null) {
            sb.append("    ").append(logo.render(theme)).append("\n");
        }
        for (IconRailItem item : topItems) {
            sb.append("    ").append(item.render(theme)).append("\n");
        }
        sb.append("  </div>\n");

        // Bottom Section
        sb.append("  <div class=\"rail-bottom-section\">\n");
        for (IconRailItem item : bottomItems) {
            sb.append("    ").append(item.render(theme)).append("\n");
        }
        sb.append("  </div>\n");

        sb.append("</div>\n");
        return sb.toString();
    }

    public static class Builder {
        private final IconRail rail = new IconRail();

        public Builder logo(Widget logo) {
            rail.logo(logo);
            return this;
        }

        public Builder addTopItem(IconRailItem item) {
            rail.addTopItem(item);
            return this;
        }

        public Builder addTopItems(IconRailItem... items) {
            rail.addTopItems(items);
            return this;
        }

        public Builder addBottomItem(IconRailItem item) {
            rail.addBottomItem(item);
            return this;
        }

        public Builder addBottomItems(IconRailItem... items) {
            rail.addBottomItems(items);
            return this;
        }

        public Builder activeKey(String key) {
            rail.selectItem(key);
            return this;
        }

        public Builder activeRoute(String route) {
            rail.selectByRoute(route);
            return this;
        }

        public Builder modifier(Modifier modifier) {
            rail.modifier(modifier);
            return this;
        }

        public IconRail build() {
            return rail;
        }
    }
}
