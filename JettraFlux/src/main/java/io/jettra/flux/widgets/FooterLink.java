package io.jettra.flux.widgets;

import java.util.Objects;

/**
 * Immutable Java 25 record representing a hyperlinked navigation or support item in the AppFooter.
 */
public record FooterLink(
    String text,
    String href,
    String icon,
    String target
) {
    public FooterLink {
        Objects.requireNonNull(text, "text must not be null");
        href = (href == null || href.isBlank()) ? "#" : href;
        icon = (icon == null) ? "" : icon.trim();
        target = (target == null) ? "" : target.trim();
    }

    public static FooterLink of(String text, String href) {
        return new FooterLink(text, href, "", "");
    }

    public static FooterLink of(String text, String href, String icon) {
        return new FooterLink(text, href, icon, "");
    }

    public static FooterLink of(String text, String href, String icon, String target) {
        return new FooterLink(text, href, icon, target);
    }
}
