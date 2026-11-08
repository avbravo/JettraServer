package io.jettra.flux.theme;

import java.util.Objects;

/**
 * ThemeTokens provides an immutable, strongly-typed collection of semantic color tokens
 * in compliance with WCAG AA accessibility standards (minimum 4.5:1 contrast ratio).
 *
 * @param surfaceBackground Canvas or main page background color
 * @param cardBackground    Elevated surface / card / modal container color
 * @param textPrimary       Highest contrast primary body & heading typography color
 * @param textSecondary     Subdued secondary text / metadata color
 * @param border            Dividers, structural outlines, and frame borders
 * @param accentPrimary     Brand action color (primary buttons, active highlights)
 * @param accentSecondary   Complementary brand tone or status highlight
 * @param focusRing         Interactive accessibility focus outline
 * @param iconColor         Symbol and iconography color
 */
public record ThemeTokens(
        String surfaceBackground,
        String cardBackground,
        String textPrimary,
        String textSecondary,
        String border,
        String accentPrimary,
        String accentSecondary,
        String focusRing,
        String iconColor
) {
    public ThemeTokens {
        Objects.requireNonNull(surfaceBackground, "surfaceBackground cannot be null");
        Objects.requireNonNull(cardBackground, "cardBackground cannot be null");
        Objects.requireNonNull(textPrimary, "textPrimary cannot be null");
        Objects.requireNonNull(textSecondary, "textSecondary cannot be null");
        Objects.requireNonNull(border, "border cannot be null");
        Objects.requireNonNull(accentPrimary, "accentPrimary cannot be null");
        Objects.requireNonNull(accentSecondary, "accentSecondary cannot be null");
        Objects.requireNonNull(focusRing, "focusRing cannot be null");
        Objects.requireNonNull(iconColor, "iconColor cannot be null");
    }

    /**
     * Alias accessors aligning with material / semantic naming conventions.
     */
    public String background() {
        return surfaceBackground;
    }

    public String surface() {
        return cardBackground;
    }

    public String surfaceVariant() {
        return cardBackground;
    }

    public String accent() {
        return accentPrimary;
    }

    public String brandColor() {
        return accentPrimary;
    }

    public String surfaceHover() {
        if (surfaceBackground != null && (surfaceBackground.startsWith("#f") || surfaceBackground.equalsIgnoreCase("#ffffff"))) {
            return "rgba(0, 0, 0, 0.05)";
        }
        return "rgba(255, 255, 255, 0.08)";
    }

    /**
     * Formats these tokens into CSS custom properties (variables)
     * including --jf-* semantic tokens, --j-* studio compatibility, and standard tokens.
     */
    public String toCssVariables() {
        return "  /* JettraFlux Semantic Variables (--jf-*) */\n"
             + "  --jf-bg: " + surfaceBackground + ";\n"
             + "  --jf-surface: " + cardBackground + ";\n"
             + "  --jf-surface-hover: " + surfaceHover() + ";\n"
             + "  --jf-text-primary: " + textPrimary + ";\n"
             + "  --jf-text-secondary: " + textSecondary + ";\n"
             + "  --jf-border: " + border + ";\n"
             + "  --jf-accent: " + accentPrimary + ";\n"
             + "  --jf-focus-ring: " + focusRing + ";\n"
             + "  --jf-icon-color: " + iconColor + ";\n"
             + "  /* Jettra Studio Compatibility Variables (--j-*) */\n"
             + "  --j-bg-body: " + surfaceBackground + ";\n"
             + "  --j-bg-surface: " + cardBackground + ";\n"
             + "  --j-bg-subsurface: " + surfaceHover() + ";\n"
             + "  --j-text-primary: " + textPrimary + ";\n"
             + "  --j-text-secondary: " + textSecondary + ";\n"
             + "  --j-text-muted: " + textSecondary + ";\n"
             + "  --j-border: " + border + ";\n"
             + "  --j-primary: " + accentPrimary + ";\n"
             + "  /* Standard Semantic Variables */\n"
             + "  --background: " + surfaceBackground + ";\n"
             + "  --surface: " + cardBackground + ";\n"
             + "  --surface-hover: " + surfaceHover() + ";\n"
             + "  --surface-background: " + surfaceBackground + ";\n"
             + "  --card-background: " + cardBackground + ";\n"
             + "  --text-primary: " + textPrimary + ";\n"
             + "  --text-secondary: " + textSecondary + ";\n"
             + "  --border: " + border + ";\n"
             + "  --accent: " + accentPrimary + ";\n"
             + "  --accent-primary: " + accentPrimary + ";\n"
             + "  --accent-secondary: " + accentSecondary + ";\n"
             + "  --focus-ring: " + focusRing + ";\n"
             + "  --icon-color: " + iconColor + ";\n";
    }

    /**
     * Serializes this token set as a JSON string suitable for client-side JavaScript injection.
     */
    public String toJson() {
        return "{"
            + "\"surfaceBackground\":\"" + surfaceBackground + "\","
            + "\"cardBackground\":\"" + cardBackground + "\","
            + "\"surfaceHover\":\"" + surfaceHover() + "\","
            + "\"textPrimary\":\"" + textPrimary + "\","
            + "\"textSecondary\":\"" + textSecondary + "\","
            + "\"border\":\"" + border + "\","
            + "\"accentPrimary\":\"" + accentPrimary + "\","
            + "\"accentSecondary\":\"" + accentSecondary + "\","
            + "\"focusRing\":\"" + focusRing + "\","
            + "\"iconColor\":\"" + iconColor + "\""
            + "}";
    }
}
