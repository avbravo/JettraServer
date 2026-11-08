package io.jettra.flux.theme;

/**
 * Bit provides an authentic neon-cyan cyberpunk / dark-navy visual theme for JettraFlux.
 * Standardized native theme integrated into JettraFlux.
 *
 * Visual Signature:
 * - Electric Cyan (#00f2fe) primary actions & headers
 * - Sky Blue (#38bdf8) secondary accents
 * - Deep Space Dark Navy (#080d1a) background canvas
 * - Slate Glass Surface (#111827 / rgba(17, 24, 39, 0.85))
 * - High-contrast crisp white typography (#f3f4f6 / #ffffff)
 *
 * Implements ThemeDefinition with full White and Dark color mode support.
 */
public class Bit implements ThemeDefinition {

    private static final Bit INSTANCE = new Bit();

    public static Bit getInstance() {
        return INSTANCE;
    }

    @Override
    public String getThemeName() {
        return "Bit";
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
                "#f0f9ff",                  // surfaceBackground: light cyan canvas
                "#ffffff",                  // cardBackground: pure white
                "#080d1a",                  // textPrimary: dark navy (WCAG contrast > 16:1)
                "#475569",                  // textSecondary: slate
                "#bae6fd",                  // border: light cyan border
                "#0284c7",                  // accentPrimary: crisp cyan blue (WCAG > 4.5:1)
                "#0369a1",                  // accentSecondary: deeper blue
                "rgba(2, 132, 199, 0.35)",  // focusRing
                "#0284c7"                   // iconColor
            );
        } else {
            return new ThemeTokens(
                "#080d1a",                  // surfaceBackground: Deep Dark Navy Canvas
                "#111827",                  // cardBackground: Dark Slate Glass
                "#f3f4f6",                  // textPrimary: Crisp White
                "#9ca3af",                  // textSecondary: Slate Secondary
                "rgba(0, 242, 254, 0.2)",   // border: Neon Cyan Border
                "#00f2fe",                  // accentPrimary: Neon Cyan
                "#38bdf8",                  // accentSecondary: Sky Blue
                "rgba(0, 242, 254, 0.4)",   // focusRing
                "#00f2fe"                   // iconColor
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
                "border: none; border-radius: 8px; padding: 10px 22px; font-weight: 600; cursor: pointer; transition: all 0.25s ease; background: linear-gradient(135deg, #0284c7 0%, #0369a1 100%); color: #ffffff; box-shadow: 0 4px 15px rgba(2, 132, 199, 0.25);",
                "border-radius: 12px; box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08); padding: 24px; background: #ffffff; border: 1px solid #bae6fd; color: #080d1a;",
                "padding: 24px; border-radius: 12px; background: #f0f9ff; color: #080d1a;",
                "font-family: 'Inter', -apple-system, sans-serif; font-size: 15px; color: #080d1a; line-height: 1.6;",
                BitTheme.Template.CustomCSS,
                BitTheme.Template.CustomJS,
                tok,
                mode
            );
        } else {
            return new ThemeData(
                tok.accentPrimary(),
                tok.accentSecondary(),
                tok.surfaceBackground(),
                tok.cardBackground(),
                "#080d1a",
                tok.textPrimary(),
                "border: none; border-radius: 8px; padding: 10px 22px; font-weight: 600; cursor: pointer; transition: all 0.25s ease; background: linear-gradient(135deg, #00f2fe 0%, #4facfe 100%); color: #080d1a; box-shadow: 0 4px 15px rgba(0, 242, 254, 0.3);",
                "border-radius: 12px; box-shadow: 0 10px 30px rgba(0, 0, 0, 0.4); padding: 24px; background: rgba(17, 24, 39, 0.85); backdrop-filter: blur(16px); border: 1px solid rgba(0, 242, 254, 0.2); color: #f3f4f6;",
                "padding: 24px; border-radius: 12px; background: #080d1a; color: #f3f4f6;",
                "font-family: 'Inter', -apple-system, sans-serif; font-size: 15px; color: #f3f4f6; line-height: 1.6;",
                BitTheme.Template.CustomCSS,
                BitTheme.Template.CustomJS,
                tok,
                mode
            );
        }
    }
}
