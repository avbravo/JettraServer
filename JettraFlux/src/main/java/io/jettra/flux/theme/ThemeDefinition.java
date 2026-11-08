package io.jettra.flux.theme;

/**
 * Non-sealed theme definition contract enabling extensible implementation of themes
 * in JettraFlux while remaining part of the sealed ThemeProvider hierarchy.
 */
public non-sealed interface ThemeDefinition extends ThemeProvider {

    /**
     * Canonical name of the theme (e.g., "SL", "Matrix", "DarkTheme").
     */
    String getThemeName();
}
