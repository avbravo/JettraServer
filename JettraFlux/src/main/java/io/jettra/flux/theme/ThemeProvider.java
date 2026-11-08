package io.jettra.flux.theme;

/**
 * Sealed contract for theme providers in JettraFlux (Java 25+ Baseline).
 * Returns semantic tokens and constructed ThemeData for a given ColorMode.
 */
public sealed interface ThemeProvider permits JettraTheme, ThemeDefinition {

    /**
     * Returns the semantic WCAG AA compliant color tokens for the specified color mode.
     *
     * @param mode ColorMode.WHITE or ColorMode.DARK
     * @return ThemeTokens with all required semantic color tokens
     */
    ThemeTokens tokens(ColorMode mode);

    /**
     * Creates the complete ThemeData instance configured for the specified color mode.
     *
     * @param mode ColorMode.WHITE or ColorMode.DARK
     * @return ThemeData populated with styles and tokens
     */
    ThemeData createTheme(ColorMode mode);

    /**
     * Default factory method producing ThemeData in its default mode (DARK or canonical).
     */
    default ThemeData createTheme() {
        return createTheme(ColorMode.DARK);
    }
}
