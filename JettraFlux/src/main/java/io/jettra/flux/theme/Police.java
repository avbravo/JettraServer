package io.jettra.flux.theme;

/**
 * Police provides a dark cyber-surveillance command aesthetic inspired by JettraStorePolice3D.
 * Standardized in JettraFlux:
 * - Tactical midnight navy command console (#090d16 / #0f1526)
 * - Police Gold (#ffd700) primary actions, headers and telemetry badges
 * - Cyber Sky Blue (#00d4ff) secondary accents and network stream monitors
 * - Neon Tactical Lime (#22c55e) and Alert Red (#ef4444) node status indicators
 * - Cyber grid layout, HUD glass panels and high-legibility monospace metrics
 * - Pure HTML, CSS, JavaScript (zero external UI dependencies)
 *
 * Implements ThemeDefinition with full White and Dark color mode support.
 */
public class Police implements ThemeDefinition {

    private static final Police INSTANCE = new Police();

    public static Police getInstance() {
        return INSTANCE;
    }

    @Override
    public String getThemeName() {
        return "Police";
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
                "#f1f5f9",                  // surfaceBackground: daylight tactical slate
                "#ffffff",                  // cardBackground: pure white command card
                "#090d16",                  // textPrimary: deep police dark navy (WCAG > 16:1)
                "#475569",                  // textSecondary: slate-600 (WCAG > 7:1)
                "#cbd5e1",                  // border: crisp slate border
                "#b45309",                  // accentPrimary: deep police amber-gold (WCAG > 7:1)
                "#0284c7",                  // accentSecondary: tactical sky blue
                "rgba(180, 83, 9, 0.4)",    // focusRing
                "#b45309"                   // iconColor
            );
        } else {
            return new ThemeTokens(
                "#090d16",                  // surfaceBackground: Tactical Midnight Navy Command
                "#0f1526",                  // cardBackground: Command HUD Surface
                "#f8fafc",                  // textPrimary: Crisp Raywhite
                "#94a3b8",                  // textSecondary: Tactical Slate
                "rgba(0, 212, 255, 0.35)",  // border: Cyber Skyblue Grid Border
                "#ffd700",                  // accentPrimary: Police Gold
                "#00d4ff",                  // accentSecondary: Cyber Sky Blue
                "rgba(255, 215, 0, 0.45)",  // focusRing
                "#ffd700"                   // iconColor
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
                "border: 1px solid #b45309; border-radius: 6px; padding: 10px 22px; font-weight: 700; font-family: 'JetBrains Mono', 'Segoe UI', monospace; color: #ffffff; background-color: #b45309; box-shadow: 0 2px 8px rgba(180, 83, 9, 0.25); cursor: pointer; text-transform: uppercase; letter-spacing: 0.08em; transition: all 0.2s ease;",
                "border: 1px solid #cbd5e1; border-radius: 8px; padding: 24px; background: #ffffff; box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06); color: " + tok.textPrimary() + ";",
                "padding: 20px; border-radius: 8px; border: 1px solid #cbd5e1; background-color: #f1f5f9;",
                "font-size: 15px; color: " + tok.textPrimary() + "; font-family: 'JetBrains Mono', 'Segoe UI', monospace; line-height: 1.6; letter-spacing: 0.02em;",
                PoliceTheme.Template.CustomCSS,
                PoliceTheme.Template.CustomJS,
                tok,
                mode
            );
        } else {
            return new ThemeData(
                tok.accentPrimary(),
                tok.accentSecondary(),
                tok.surfaceBackground(),
                tok.cardBackground(),
                "#090d16",
                tok.textPrimary(),
                "border: 1px solid #ffd700; border-radius: 6px; padding: 10px 22px; font-weight: 700; font-family: 'JetBrains Mono', 'Segoe UI', monospace; color: #090d16; background-color: #ffd700; box-shadow: 0 0 14px rgba(255, 215, 0, 0.45); cursor: pointer; text-transform: uppercase; letter-spacing: 0.08em; transition: all 0.2s ease;",
                "border: 1px solid rgba(0, 212, 255, 0.35); border-radius: 8px; padding: 24px; background: linear-gradient(180deg, #0f1526 0%, #090d16 100%); box-shadow: 0 4px 20px rgba(0, 0, 0, 0.7); color: #f8fafc;",
                "padding: 20px; border-radius: 8px; border: 1px solid rgba(0, 212, 255, 0.35); background-color: rgba(15, 21, 38, 0.95); box-shadow: 0 4px 20px rgba(0, 0, 0, 0.6);",
                "font-size: 15px; color: #f8fafc; font-family: 'JetBrains Mono', 'Segoe UI', monospace; line-height: 1.6; letter-spacing: 0.02em;",
                PoliceTheme.Template.CustomCSS,
                PoliceTheme.Template.CustomJS,
                tok,
                mode
            );
        }
    }
}
