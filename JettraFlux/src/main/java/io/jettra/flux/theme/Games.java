package io.jettra.flux.theme;

/**
 * Games provides a 3D arcade / gaming HUD visual theme for JettraFlux,
 * inspired 100% by the interactive interface of JettraStore 3D:
 * - Deep space dark command canvas (#040610 / #0e1220)
 * - Games Gold (#ffd700) primary actions, JETTRA CORE header, modal outlines and event logs
 * - Cyber Sky Blue (#00d4ff) secondary accents, JAVA 25 EDITION subtitle and node connections
 * - Neon Tactical Lime (#22c55e) and Alert Red (#ef4444) for status, actions and alerts
 * - 3D perspective cyber grid, HUD glass panels and high-legibility monospace metrics
 * - Pure HTML, CSS, JavaScript (zero external UI dependencies)
 *
 * Implements ThemeDefinition with full White and Dark color mode support.
 */
public class Games implements ThemeDefinition {

    private static final Games INSTANCE = new Games();

    public static Games getInstance() {
        return INSTANCE;
    }

    @Override
    public String getThemeName() {
        return "Games";
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
                "#f8fafc",                  // surfaceBackground: crisp gaming slate white
                "#ffffff",                  // cardBackground: pure elevated surface card
                "#040610",                  // textPrimary: deep game contrast black (WCAG > 16:1)
                "#475569",                  // textSecondary: slate-600
                "#cbd5e1",                  // border: crisp tactical border
                "#d97706",                  // accentPrimary: arcade gold-amber
                "#0284c7",                  // accentSecondary: tactical sky blue
                "rgba(217, 119, 6, 0.45)",  // focusRing
                "#d97706"                   // iconColor
            );
        } else {
            return new ThemeTokens(
                "#040610",                  // surfaceBackground: Deep Space JettraStore Canvas
                "#0e1220",                  // cardBackground: HUD Surface Card
                "#f8fafc",                  // textPrimary: Crisp White
                "#94a3b8",                  // textSecondary: Tactical Slate
                "#ffd700",                  // border: Games Gold Border
                "#ffd700",                  // accentPrimary: Games Gold
                "#00d4ff",                  // accentSecondary: Cyber Sky Blue
                "rgba(255, 215, 0, 0.5)",   // focusRing
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
                "border: 1px solid #d97706; border-radius: 6px; padding: 10px 22px; font-weight: 800; font-family: 'JetBrains Mono', 'Orbitron', monospace; color: #ffffff; background-color: #d97706; box-shadow: 0 2px 8px rgba(217, 119, 6, 0.3); cursor: pointer; text-transform: uppercase; letter-spacing: 0.08em; transition: all 0.2s ease;",
                "border: 1px solid #cbd5e1; border-radius: 8px; padding: 22px; background: #ffffff; box-shadow: 0 4px 18px rgba(0, 0, 0, 0.08); color: " + tok.textPrimary() + ";",
                "padding: 20px; border-radius: 8px; border: 1px solid #cbd5e1; background-color: #f8fafc;",
                "font-size: 15px; color: " + tok.textPrimary() + "; font-family: 'JetBrains Mono', 'Orbitron', monospace; line-height: 1.6; letter-spacing: 0.02em;",
                GamesTheme.Template.CustomCSS,
                GamesTheme.Template.CustomJS,
                tok,
                mode
            );
        } else {
            return new ThemeData(
                tok.accentPrimary(),
                tok.accentSecondary(),
                tok.surfaceBackground(),
                tok.cardBackground(),
                "#040610",
                tok.textPrimary(),
                "border: 1px solid #ffd700; border-radius: 6px; padding: 10px 22px; font-weight: 800; font-family: 'JetBrains Mono', 'Orbitron', monospace; color: #040610; background-color: #ffd700; box-shadow: 0 0 16px rgba(255, 215, 0, 0.5); cursor: pointer; text-transform: uppercase; letter-spacing: 0.08em; transition: all 0.2s ease;",
                "border: 1px solid #ffd700; border-radius: 8px; padding: 22px; background: linear-gradient(180deg, rgba(16, 22, 38, 0.95) 0%, rgba(9, 13, 22, 0.98) 100%); box-shadow: 0 4px 24px rgba(0, 0, 0, 0.8), inset 0 1px 0 rgba(255, 215, 0, 0.2); color: #f8fafc;",
                "padding: 20px; border-radius: 8px; border: 1px solid rgba(0, 212, 255, 0.35); background-color: rgba(14, 18, 32, 0.95); box-shadow: 0 4px 24px rgba(0, 0, 0, 0.7);",
                "font-size: 15px; color: #f8fafc; font-family: 'JetBrains Mono', 'Orbitron', monospace; line-height: 1.6; letter-spacing: 0.02em;",
                GamesTheme.Template.CustomCSS,
                GamesTheme.Template.CustomJS,
                tok,
                mode
            );
        }
    }
}
