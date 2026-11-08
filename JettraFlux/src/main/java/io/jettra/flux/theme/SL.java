package io.jettra.flux.theme;

/**
 * SL provides a virtual world aesthetic inspired by Second Life (Linden Lab).
 * Canonical class standardized in JettraFlux (migrated from SLTheme).
 *
 * Implements ThemeDefinition with full White and Dark color mode support.
 */
public class SL implements ThemeDefinition {

    private static final SL INSTANCE = new SL();

    public static SL getInstance() {
        return INSTANCE;
    }

    @Override
    public String getThemeName() {
        return "SL";
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
                "#f8fafc",                  // surfaceBackground: crisp daylight slate
                "#ffffff",                  // cardBackground: pure white
                "#0f172a",                  // textPrimary: slate-900 (WCAG contrast > 15:1)
                "#475569",                  // textSecondary: slate-600 (WCAG contrast > 7:1)
                "#cbd5e1",                  // border: slate-300
                "#4d7c0f",                  // accentPrimary: deep Linden Green (WCAG contrast > 5:1)
                "#0284c7",                  // accentSecondary: virtual sky cyan
                "rgba(77, 124, 15, 0.35)",  // focusRing
                "#4d7c0f"                   // iconColor
            );
        } else {
            return new ThemeTokens(
                "#0e131d",                  // surfaceBackground: virtual midnight slate
                "#1a2233",                  // cardBackground: viewer floater slate
                "#f1f5f9",                  // textPrimary: crisp white-slate (WCAG contrast > 16:1)
                "#cbd5e1",                  // textSecondary: muted slate (WCAG contrast > 11:1)
                "rgba(132, 207, 41, 0.25)", // border: linden green tint
                "#84cf29",                  // accentPrimary: Second Life Linden Green
                "#00bcd4",                  // accentSecondary: virtual sky cyan
                "rgba(132, 207, 41, 0.4)",  // focusRing
                "#84cf29"                   // iconColor
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
                "border: none; border-radius: 6px; padding: 10px 22px; font-weight: 800; font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; color: #ffffff; background: linear-gradient(135deg, #4d7c0f 0%, #3f6212 100%); box-shadow: 0 2px 8px rgba(77, 124, 15, 0.25); cursor: pointer; text-transform: uppercase; letter-spacing: 0.05em; transition: all 0.2s ease;",
                "border: 1px solid #cbd5e1; border-radius: 12px; padding: 24px; background-color: #ffffff; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05); color: " + tok.textPrimary() + ";",
                "padding: 24px; border-radius: 12px; border: 1px solid #cbd5e1; background-color: #f8fafc;",
                "font-size: 15px; color: " + tok.textPrimary() + "; font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6;",
                SLTheme.Template.CustomCSS,
                SLTheme.Template.CustomJS,
                tok,
                mode
            );
        } else {
            return new ThemeData(
                tok.accentPrimary(),
                tok.accentSecondary(),
                tok.surfaceBackground(),
                tok.cardBackground(),
                "#0e131d",
                tok.textPrimary(),
                "border: none; border-radius: 6px; padding: 10px 22px; font-weight: 800; font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; color: #0e131d; background: linear-gradient(135deg, #84cf29 0%, #68a620 100%); box-shadow: 0 0 12px rgba(132, 207, 41, 0.35); cursor: pointer; text-transform: uppercase; letter-spacing: 0.05em; transition: all 0.2s ease;",
                "border: 1px solid rgba(132, 207, 41, 0.2); border-radius: 12px; padding: 24px; background-color: #1a2233; box-shadow: 0 8px 24px rgba(0, 0, 0, 0.5); color: #f1f5f9;",
                "padding: 24px; border-radius: 12px; border: 1px solid rgba(0, 188, 212, 0.25); background-color: #161e2e;",
                "font-size: 15px; color: #cbd5e1; font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6;",
                SLTheme.Template.CustomCSS,
                SLTheme.Template.CustomJS,
                tok,
                mode
            );
        }
    }
}
