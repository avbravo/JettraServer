package io.jettra.flux.widgets;

import io.jettra.flux.core.Modifier;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

/**
 * JettraFluxButton - Modern, highly configurable button component in JettraFlux.
 * Supports fluid builder pattern, color variants, size scale, icons, loading spinner states,
 * and direct integration with dynamic forms.
 */
public class JettraFluxButton extends Widget {

    public enum Variant {
        PRIMARY("var(--j-primary, #38bdf8)", "#0f172a", "rgba(56,189,248,0.3)"),
        SECONDARY("var(--j-bg-subsurface, #1e293b)", "#f8fafc", "var(--j-border, rgba(255,255,255,0.1))"),
        SUCCESS("#10b981", "#ffffff", "rgba(16,185,129,0.3)"),
        DANGER("#ef4444", "#ffffff", "rgba(239,68,68,0.3)"),
        WARNING("#f59e0b", "#0f172a", "rgba(245,158,11,0.3)"),
        INFO("#06b6d4", "#ffffff", "rgba(6,182,212,0.3)"),
        PURPLE("#a855f7", "#ffffff", "rgba(168,85,247,0.3)"),
        EMERALD("#059669", "#ffffff", "rgba(5,150,105,0.3)"),
        OUTLINE("transparent", "var(--j-text-primary, #f8fafc)", "var(--j-border, rgba(255,255,255,0.2))"),
        GHOST("transparent", "var(--j-text-muted, #94a3b8)", "transparent");

        private final String bg;
        private final String text;
        private final String border;

        Variant(String bg, String text, String border) {
            this.bg = bg;
            this.text = text;
            this.border = border;
        }

        public String bg() { return bg; }
        public String text() { return text; }
        public String border() { return border; }
    }

    public enum Size {
        SM("4px 10px", "11px", "12px", "4px"),
        MD("7px 16px", "12.5px", "14px", "6px"),
        LG("10px 22px", "14px", "16px", "8px");

        private final String padding;
        private final String fontSize;
        private final String iconSize;
        private final String gap;

        Size(String padding, String fontSize, String iconSize, String gap) {
            this.padding = padding;
            this.fontSize = fontSize;
            this.iconSize = iconSize;
            this.gap = gap;
        }
    }

    private String label = "";
    private String icon = null;
    private String iconRight = null;
    private Variant variant = Variant.PRIMARY;
    private Size size = Size.MD;
    private String buttonType = "button";
    private boolean disabled = false;
    private boolean loading = false;
    private String tooltip = null;
    private String jsOnClick = null;
    private String badge = null;
    private String formId = null;

    private JettraFluxButton(String label) {
        this.label = label;
    }

    public static JettraFluxButton of(String label) {
        return new JettraFluxButton(label);
    }

    public static JettraFluxButton of(String label, String icon) {
        JettraFluxButton b = new JettraFluxButton(label);
        b.icon = icon;
        return b;
    }

    public static JettraFluxButton iconOnly(String icon) {
        JettraFluxButton b = new JettraFluxButton("");
        b.icon = icon;
        return b;
    }

    @Override
    public JettraFluxButton id(String id) {
        super.id(id);
        return this;
    }

    public JettraFluxButton label(String label) {
        this.label = label;
        return this;
    }

    public JettraFluxButton icon(String icon) {
        this.icon = icon;
        return this;
    }

    public JettraFluxButton iconRight(String iconRight) {
        this.iconRight = iconRight;
        return this;
    }

    public JettraFluxButton variant(Variant variant) {
        if (variant != null) this.variant = variant;
        return this;
    }

    public JettraFluxButton size(Size size) {
        if (size != null) this.size = size;
        return this;
    }

    public JettraFluxButton submit() {
        this.buttonType = "submit";
        return this;
    }

    public JettraFluxButton reset() {
        this.buttonType = "reset";
        return this;
    }

    public JettraFluxButton buttonType(String buttonType) {
        this.buttonType = buttonType;
        return this;
    }

    public JettraFluxButton disabled(boolean disabled) {
        this.disabled = disabled;
        return this;
    }

    public JettraFluxButton loading(boolean loading) {
        this.loading = loading;
        return this;
    }

    public JettraFluxButton tooltip(String tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    public JettraFluxButton onClickJs(String js) {
        this.jsOnClick = js;
        return this;
    }

    public JettraFluxButton badge(String badge) {
        this.badge = badge;
        return this;
    }

    public JettraFluxButton form(String formId) {
        this.formId = formId;
        return this;
    }

    public String form() {
        return this.formId;
    }

    @Override
    public JettraFluxButton onClick(java.util.function.Consumer<Object> onClick) {
        this.onClick = onClick;
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();

        sb.append("<button type=\"").append(buttonType).append("\" ");
        if (id != null) sb.append("id=\"").append(id).append("\" ");
        if (formId != null && !formId.isBlank()) sb.append("form=\"").append(formId).append("\" ");
        if (tooltip != null) sb.append("title=\"").append(tooltip).append("\" ");
        if (disabled) sb.append("disabled=\"disabled\" ");

        StringBuilder clickHandler = new StringBuilder();
        if (jsOnClick != null && !jsOnClick.isBlank()) {
            clickHandler.append(jsOnClick);
            if (!jsOnClick.endsWith(";")) clickHandler.append(";");
        }
        if (onClick != null) {
            clickHandler.append("callWidgetAction('").append(id).append("');");
        }
        if (clickHandler.length() > 0) {
            sb.append("onclick=\"").append(clickHandler).append("\" ");
        }

        String baseStyle = "display:inline-flex; align-items:center; justify-content:center; gap:" + size.gap + "; "
                + "padding:" + size.padding + "; font-size:" + size.fontSize + "; font-weight:600; "
                + "border-radius:6px; cursor:" + (disabled ? "not-allowed" : "pointer") + "; "
                + "background:" + variant.bg + "; color:" + variant.text + "; border:1px solid " + variant.border + "; "
                + "transition:all 0.15s ease-in-out; text-decoration:none; white-space:nowrap; outline:none; "
                + (disabled ? "opacity:0.6;" : "");

        sb.append("class=\"jettra-flux-btn jettra-flux-btn-").append(variant.name().toLowerCase())
          .append(" ").append(modifier != null ? modifier.getClasses() : "").append("\" ");
        sb.append("style=\"").append(baseStyle).append(" ").append(modifier != null ? modifier.getStyles() : "").append("\">\n");

        if (loading) {
            sb.append("  <i class=\"fas fa-spinner fa-spin\" style=\"font-size:").append(size.iconSize).append(";\"></i>\n");
        } else if (icon != null && !icon.isBlank()) {
            sb.append("  <i class=\"").append(icon).append("\" style=\"font-size:").append(size.iconSize).append(";\"></i>\n");
        }

        if (label != null && !label.isBlank()) {
            sb.append("  <span>").append(label).append("</span>\n");
        }

        if (badge != null && !badge.isBlank()) {
            sb.append("  <span style=\"font-size:9.5px; padding:1px 6px; border-radius:10px; background:rgba(255,255,255,0.2); font-weight:700;\">")
              .append(badge).append("</span>\n");
        }

        if (iconRight != null && !iconRight.isBlank()) {
            sb.append("  <i class=\"").append(iconRight).append("\" style=\"font-size:").append(size.iconSize).append(";\"></i>\n");
        }

        sb.append("</button>");
        return sb.toString();
    }
}
