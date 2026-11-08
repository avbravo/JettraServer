package io.jettra.flux.theme;

/**
 * ColorMode represents the visual lighting scheme of the application:
 * WHITE (daytime / light mode) or DARK (nighttime / dark mode).
 */
public enum ColorMode {
    WHITE,
    DARK;

    /**
     * Toggles between WHITE and DARK.
     *
     * @return DARK if this is WHITE, WHITE if this is DARK.
     */
    public ColorMode toggle() {
        return this == WHITE ? DARK : WHITE;
    }

    public boolean isDark() {
        return this == DARK;
    }

    public boolean isWhite() {
        return this == WHITE;
    }

    /**
     * Resolves a ColorMode from a string (cookie value, query param, or header),
     * defaulting to the provided defaultMode if null or unrecognized.
     */
    public static ColorMode fromString(String value, ColorMode defaultMode) {
        if (value == null) {
            return defaultMode;
        }
        String v = value.trim().toLowerCase();
        return switch (v) {
            case "dark", "night", "black" -> DARK;
            case "white", "light", "day" -> WHITE;
            default -> defaultMode;
        };
    }
}
