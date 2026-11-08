package io.jettra.themes.bit;

import io.jettra.flux.theme.ColorMode;
import io.jettra.flux.theme.ThemeData;
import io.jettra.flux.theme.ThemeTokens;

/**
 * Backwards-compatibility wrapper for io.jettra.themes.bit.BitTheme.
 * Migrated and natively integrated into JettraFlux.
 */
public class BitTheme {

    public static ThemeData create() {
        return io.jettra.flux.theme.Bit.create();
    }

    public static ThemeData create(ColorMode mode) {
        return io.jettra.flux.theme.Bit.create(mode);
    }

    public static ThemeTokens getTokens(ColorMode mode) {
        return io.jettra.flux.theme.Bit.getTokens(mode);
    }

    public static class Template {
        public static final String CustomCSS = io.jettra.flux.theme.BitTheme.Template.CustomCSS;
        public static final String CustomJS = io.jettra.flux.theme.BitTheme.Template.CustomJS;
    }
}
