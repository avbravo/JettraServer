package io.jettra.flux.theme;

/**
 * Core provides a high-performance HUD cybernetic aesthetic.
 * Canonical class standardized in JettraFlux (migrated from CoreTheme).
 *
 * Implements ThemeDefinition with full White and Dark color mode support.
 */
public class Core implements ThemeDefinition {

    private static final Core INSTANCE = new Core();

    public static Core getInstance() {
        return INSTANCE;
    }

    @Override
    public String getThemeName() {
        return "Core";
    }

    @Override
    public ThemeTokens tokens(ColorMode mode) {
        return getTokens(mode);
    }

    @Override
    public ThemeData createTheme(ColorMode mode) {
        return create(mode);
    }

    public static ThemeTokens getTokens(ColorMode mode) {
        if (mode == ColorMode.WHITE) {
            return new ThemeTokens(
                "#f8fafc",                  // surfaceBackground: clean daylight slate
                "#ffffff",                  // cardBackground: pure white
                "#0f172a",                  // textPrimary: slate-900 (WCAG contrast > 15:1)
                "#475569",                  // textSecondary: slate-600 (WCAG contrast > 7:1)
                "#d97706",                  // border: amber border
                "#b45309",                  // accentPrimary: deep amber gold (WCAG contrast > 5.5:1)
                "#0284c7",                  // accentSecondary: Java 25 sky blue
                "rgba(180, 83, 9, 0.4)",    // focusRing
                "#b45309"                   // iconColor
            );
        } else {
            return new ThemeTokens(
                "#0b0e14",                  // surfaceBackground: Deep 3D Space Black
                "#161f2e",                  // cardBackground: Dark HUD Glass Surface
                "#f1f5f9",                  // textPrimary: Crisp raywhite text (WCAG contrast > 17:1)
                "#94a3b8",                  // textSecondary: muted slate (WCAG contrast > 6.8:1)
                "#f59e0b",                  // border: Jettra Core Amber Gold
                "#f59e0b",                  // accentPrimary: Jettra Core Amber Gold
                "#38bdf8",                  // accentSecondary: Java 25 Sky Blue
                "rgba(245, 158, 11, 0.4)",  // focusRing
                "#f59e0b"                   // iconColor
            );
        }
    }

    public static ThemeData create() {
        return create(ColorMode.DARK);
    }

    public static ThemeData create(ColorMode mode) {
        ThemeTokens tok = getTokens(mode);
        if (mode == ColorMode.WHITE) {
            return new ThemeData(
                tok.accentPrimary(),
                tok.accentSecondary(),
                tok.surfaceBackground(),
                tok.cardBackground(),
                "#ffffff",
                tok.textPrimary(),
                "border: 1px solid #d97706; border-radius: 6px; padding: 10px 22px; font-weight: 700; font-family: 'Rajdhani', 'Share Tech Mono', sans-serif; text-transform: uppercase; letter-spacing: 0.06em; color: #ffffff; background-color: #b45309; box-shadow: 0 2px 8px rgba(180, 83, 9, 0.25); cursor: pointer; transition: all 0.2s ease;",
                "border: 1px solid #d97706; border-radius: 8px; padding: 20px; background-color: #ffffff; box-shadow: 0 4px 14px rgba(0, 0, 0, 0.06); color: " + tok.textPrimary() + ";",
                "padding: 20px; border-radius: 8px; border: 1px solid rgba(180, 83, 9, 0.3); background-color: #f8fafc;",
                "font-size: 15px; color: " + tok.textPrimary() + "; font-family: 'Inter', 'Rajdhani', sans-serif; line-height: 1.6;",
                CoreTheme.Template.CustomCSS,
                CoreTheme.Template.CustomJS,
                tok,
                mode
            );
        } else {
            return new ThemeData(
                tok.accentPrimary(),
                tok.accentSecondary(),
                tok.surfaceBackground(),
                tok.cardBackground(),
                "#0b0e14",
                tok.textPrimary(),
                "border: 1px solid rgba(255, 255, 255, 0.35); border-radius: 6px; padding: 10px 22px; font-weight: 700; font-family: 'Rajdhani', 'Share Tech Mono', sans-serif; text-transform: uppercase; letter-spacing: 0.06em; color: #0b0e14; background-color: #f59e0b; box-shadow: 0 0 12px rgba(245, 158, 11, 0.35); cursor: pointer; transition: all 0.2s ease;",
                "border: 1px solid #f59e0b; border-radius: 8px; padding: 20px; background-color: rgba(16, 22, 33, 0.92); box-shadow: 0 4px 20px rgba(0, 0, 0, 0.8), inset 0 0 15px rgba(245, 158, 11, 0.05); color: #f1f5f9;",
                "padding: 20px; border-radius: 8px; border: 1px solid rgba(245, 158, 11, 0.3); background-color: rgba(14, 18, 27, 0.95);",
                "font-size: 15px; color: #f1f5f9; font-family: 'Inter', 'Rajdhani', sans-serif; line-height: 1.6;",
                CoreTheme.Template.CustomCSS,
                CoreTheme.Template.CustomJS,
                tok,
                mode
            );
        }
    }
}
