package io.jettra.flux.widgets;

import io.jettra.flux.core.Modifier;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.Objects;

/**
 * An individual interactive menu item within an {@link IconRail}.
 * Implements the State Pattern for active/inactive selection states,
 * supporting accessible tooltips, title attributes, badge indicators,
 * and ARIA page navigation attributes.
 */
public class IconRailItem extends Widget {

    private String key;
    private String href;
    private String title;
    private Widget icon;
    private boolean active;
    private String target = "";
    private String badgeText;

    public IconRailItem() {
        super();
    }

    public static IconRailItem of(String href, Widget icon) {
        IconRailItem item = new IconRailItem();
        item.href = href;
        item.icon = icon;
        return item;
    }

    public static IconRailItem of(String key, String href, Widget icon, String title) {
        IconRailItem item = new IconRailItem();
        item.key = key;
        item.id = key;
        item.href = href;
        item.icon = icon;
        item.title = title;
        return item;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getKey() {
        return key != null ? key : id;
    }

    public IconRailItem key(String key) {
        this.key = key;
        this.id = key;
        return this;
    }

    public String getHref() {
        return href;
    }

    public IconRailItem href(String href) {
        this.href = href;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public IconRailItem title(String title) {
        this.title = title;
        return this;
    }

    public Widget getIcon() {
        return icon;
    }

    public IconRailItem icon(Widget icon) {
        this.icon = icon;
        return this;
    }

    public boolean isActive() {
        return active;
    }

    public IconRailItem active(boolean active) {
        this.active = active;
        return this;
    }

    public IconRailItem active() {
        return active(true);
    }

    public String getTarget() {
        return target;
    }

    public IconRailItem target(String target) {
        this.target = target != null ? target : "";
        return this;
    }

    public String getBadgeText() {
        return badgeText;
    }

    public IconRailItem badgeText(String badgeText) {
        this.badgeText = badgeText;
        return this;
    }

    @Override
    public IconRailItem modifier(Modifier modifier) {
        super.modifier(modifier);
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        String linkHref = href != null ? href : "#";
        sb.append("<a href=\"").append(linkHref).append("\" ");

        if (target != null && !target.isBlank()) {
            sb.append("target=\"").append(target).append("\" ");
        }
        if (title != null && !title.isBlank() && !modifier.getAttributes().containsKey("title")) {
            sb.append("title=\"").append(title).append("\" ");
            sb.append("aria-label=\"").append(title).append("\" ");
        }
        if (active) {
            sb.append("aria-current=\"page\" ");
        }

        String cssClass = "rail-item" + (active ? " active" : "");
        sb.append(renderCommonAttributes(theme, cssClass));
        sb.append(">");

        if (icon != null) {
            sb.append(icon.render(theme));
        }
        if (badgeText != null && !badgeText.isBlank()) {
            sb.append("<span class=\"rail-badge\">").append(badgeText).append("</span>");
        }
        sb.append("</a>");
        return sb.toString();
    }

    public static class Builder {
        private final IconRailItem item = new IconRailItem();

        public Builder key(String key) {
            item.key(key);
            return this;
        }

        public Builder href(String href) {
            item.href(href);
            return this;
        }

        public Builder title(String title) {
            item.title(title);
            return this;
        }

        public Builder icon(Widget icon) {
            item.icon(icon);
            return this;
        }

        public Builder active(boolean active) {
            item.active(active);
            return this;
        }

        public Builder target(String target) {
            item.target(target);
            return this;
        }

        public Builder badgeText(String badgeText) {
            item.badgeText(badgeText);
            return this;
        }

        public Builder modifier(Modifier modifier) {
            item.modifier(modifier);
            return this;
        }

        public IconRailItem build() {
            return item;
        }
    }
}
