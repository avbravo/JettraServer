package io.jettra.flux.theme;

import java.util.Objects;

/**
 * Standardized catalog of the 12 visual themes in JettraFlux (Java 25+ Baseline).
 * Implements ThemeProvider and sealed interface contracts with pattern matching switch.
 */
public enum JettraTheme implements ThemeProvider {
    FLAT_THEME("FlatTheme"),
    THEME_3D("Theme3D"),
    FUTURISTIC_THEME("FuturisticTheme"),
    AST_THEME("AstTheme"),
    ATLANTIS_THEME("AtlantisTheme"),
    OCEAN_THEME("OceanTheme"),
    MATRIX("Matrix"),
    RETRO("Retro"),
    DARK_THEME("DarkTheme"),
    HEROES("Heroes"),
    SL("SL"),
    CORE("Core"),
    POLICE("Police"),
    GAMES("Games"),
    BIT("Bit");

    private final String displayName;

    JettraTheme(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the official human-readable display name of the theme.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the canonical name of the theme in JettraFlux.
     */
    public String getThemeName() {
        return displayName;
    }

    /**
     * Returns the natural default color mode for this theme.
     */
    public ColorMode getDefaultColorMode() {
        return switch (this) {
            case FLAT_THEME, THEME_3D, ATLANTIS_THEME -> ColorMode.WHITE;
            case SL, CORE, HEROES, FUTURISTIC_THEME, AST_THEME, OCEAN_THEME, MATRIX, RETRO, DARK_THEME, POLICE, GAMES, BIT -> ColorMode.DARK;
        };
    }

    @Override
    public ThemeTokens tokens(ColorMode mode) {
        Objects.requireNonNull(mode, "ColorMode cannot be null");
        return switch (this) {
            case SL -> io.jettra.flux.theme.SL.getTokens(mode);
            case CORE -> io.jettra.flux.theme.Core.getTokens(mode);
            case HEROES -> io.jettra.flux.theme.Heroes.getTokens(mode);
            case MATRIX -> io.jettra.flux.theme.MatrixTheme.getTokens(mode);
            case POLICE -> io.jettra.flux.theme.Police.getTokens(mode);
            case GAMES -> io.jettra.flux.theme.Games.getTokens(mode);
            case BIT -> io.jettra.flux.theme.Bit.getTokens(mode);
            case DARK_THEME -> io.jettra.flux.theme.DarkTheme.getTokens(mode);
            case RETRO -> io.jettra.flux.theme.RetroTheme.getTokens(mode);
            case OCEAN_THEME -> io.jettra.flux.theme.OceanTheme.getTokens(mode);
            case FLAT_THEME -> getFlatTokens(mode);
            case THEME_3D -> get3DTokens(mode);
            case FUTURISTIC_THEME -> getFuturisticTokens(mode);
            case AST_THEME -> getAstTokens(mode);
            case ATLANTIS_THEME -> getAtlantisTokens(mode);
        };
    }

    @Override
    public ThemeData createTheme(ColorMode mode) {
        return create(mode);
    }

    public ThemeData create(ColorMode mode) {
        Objects.requireNonNull(mode, "ColorMode cannot be null");
        return switch (this) {
            case SL -> io.jettra.flux.theme.SL.create(mode);
            case CORE -> io.jettra.flux.theme.Core.create(mode);
            case HEROES -> io.jettra.flux.theme.Heroes.create(mode);
            case MATRIX -> io.jettra.flux.theme.MatrixTheme.create(mode);
            case POLICE -> io.jettra.flux.theme.Police.create(mode);
            case GAMES -> io.jettra.flux.theme.Games.create(mode);
            case BIT -> io.jettra.flux.theme.Bit.create(mode);
            case DARK_THEME -> io.jettra.flux.theme.DarkTheme.create(mode);
            case RETRO -> io.jettra.flux.theme.RetroTheme.create(mode);
            case OCEAN_THEME -> io.jettra.flux.theme.OceanTheme.create(mode);
            case FLAT_THEME -> createFlat(mode);
            case THEME_3D -> createTheme3D(mode);
            case FUTURISTIC_THEME -> createFuturistic(mode);
            case AST_THEME -> createAst(mode);
            case ATLANTIS_THEME -> createAtlantis(mode);
        };
    }

    @Override
    public ThemeData createTheme() {
        return create(getDefaultColorMode());
    }

    public ThemeData create() {
        return create(getDefaultColorMode());
    }

    /**
     * Resolves a JettraTheme from a case-insensitive string name, supporting legacy suffixes.
     */
    public static JettraTheme fromName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return MATRIX;
        }
        String clean = name.trim().toLowerCase().replaceAll("[_\\-\\s]", "");
        return switch (clean) {
            case "sl", "sltheme" -> SL;
            case "core", "coretheme" -> CORE;
            case "heroes", "heroestheme" -> HEROES;
            case "flat", "flattheme" -> FLAT_THEME;
            case "3d", "theme3d" -> THEME_3D;
            case "futuristic", "futuristictheme" -> FUTURISTIC_THEME;
            case "ast", "asttheme" -> AST_THEME;
            case "atlantis", "atlantistheme" -> ATLANTIS_THEME;
            case "ocean", "oceantheme" -> OCEAN_THEME;
            case "matrix", "matrixtheme" -> MATRIX;
            case "retro", "retrotheme" -> RETRO;
            case "dark", "darktheme" -> DARK_THEME;
            case "police", "policetheme" -> POLICE;
            case "games", "gamestheme" -> GAMES;
            case "bit", "bittheme" -> BIT;
            default -> MATRIX;
        };
    }

    // --- Semantic token providers for remaining themes ---

    private static ThemeTokens getFlatTokens(ColorMode mode) {
        if (mode == ColorMode.WHITE) {
            return new ThemeTokens(
                "#fafafa", "#ffffff", "#212121", "#616161",
                "#e0e0e0", "#1976d2", "#d97706",
                "rgba(25, 118, 210, 0.35)", "#1976d2"
            );
        } else {
            return new ThemeTokens(
                "#121212", "#1e1e1e", "#ffffff", "#b0bec5",
                "#2c2c2c", "#2196f3", "#ffb74d",
                "rgba(33, 150, 243, 0.4)", "#2196f3"
            );
        }
    }

    private static ThemeData createFlat(ColorMode mode) {
        ThemeTokens tok = getFlatTokens(mode);
        if (mode == ColorMode.WHITE) {
            return new ThemeData(
                tok.accentPrimary(), tok.accentSecondary(), tok.surfaceBackground(), tok.cardBackground(),
                "#ffffff", tok.textPrimary(),
                "border: none; border-radius: 4px; padding: 10px 20px; font-weight: 500; cursor: pointer; color: #ffffff; background-color: #1976d2; transition: background 0.3s;",
                "border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.08); padding: 16px; background-color: #ffffff; color: " + tok.textPrimary() + ";",
                "padding: 16px; border-radius: 4px; background-color: #fafafa;",
                "font-size: 16px; color: " + tok.textPrimary() + ";",
                "", "", tok, mode
            );
        } else {
            return new ThemeData(
                tok.accentPrimary(), tok.accentSecondary(), tok.surfaceBackground(), tok.cardBackground(),
                "#121212", tok.textPrimary(),
                "border: none; border-radius: 4px; padding: 10px 20px; font-weight: 500; cursor: pointer; color: #121212; background-color: #2196f3; transition: background 0.3s;",
                "border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.4); padding: 16px; background-color: #1e1e1e; color: " + tok.textPrimary() + ";",
                "padding: 16px; border-radius: 4px; background-color: #121212;",
                "font-size: 16px; color: " + tok.textPrimary() + ";",
                "", "", tok, mode
            );
        }
    }

    private static ThemeTokens get3DTokens(ColorMode mode) {
        if (mode == ColorMode.WHITE) {
            return new ThemeTokens(
                "#e0e5ec", "#e0e5ec", "#2d3748", "#4a5568",
                "#c8d0db", "#2b6cb0", "#c05621",
                "rgba(43, 108, 176, 0.35)", "#2d3748"
            );
        } else {
            return new ThemeTokens(
                "#1e232a", "#232932", "#f7fafc", "#cbd5e0",
                "#2d3748", "#4299e1", "#ed8936",
                "rgba(66, 153, 225, 0.45)", "#f7fafc"
            );
        }
    }

    private static ThemeData createTheme3D(ColorMode mode) {
        ThemeTokens tok = get3DTokens(mode);
        if (mode == ColorMode.WHITE) {
            return new ThemeData(
                tok.accentPrimary(), tok.accentSecondary(), tok.surfaceBackground(), tok.cardBackground(),
                "#ffffff", tok.textPrimary(),
                "border: none; border-radius: 12px; padding: 12px 24px; font-weight: bold; color: #2d3748; background-color: #e0e5ec; box-shadow: 6px 6px 12px rgba(163,177,198,0.6), -6px -6px 12px rgba(255,255,255, 0.7); cursor: pointer; transition: all 0.2s ease;",
                "border-radius: 20px; padding: 24px; background-color: #e0e5ec; box-shadow: 8px 8px 16px rgba(163,177,198,0.5), -8px -8px 16px rgba(255,255,255, 0.6); color: #2d3748;",
                "padding: 16px; border-radius: 12px; background-color: #e0e5ec;",
                "font-size: 16px; color: #2d3748; text-shadow: 1px 1px 0px #FFF;",
                "", "", tok, mode
            );
        } else {
            return new ThemeData(
                tok.accentPrimary(), tok.accentSecondary(), tok.surfaceBackground(), tok.cardBackground(),
                "#1e232a", tok.textPrimary(),
                "border: none; border-radius: 12px; padding: 12px 24px; font-weight: bold; color: #f7fafc; background-color: #232932; box-shadow: 6px 6px 14px rgba(0,0,0,0.6), -6px -6px 14px rgba(255,255,255,0.05); cursor: pointer; transition: all 0.2s ease;",
                "border-radius: 20px; padding: 24px; background-color: #232932; box-shadow: 8px 8px 18px rgba(0,0,0,0.7), -8px -8px 18px rgba(255,255,255,0.04); color: #f7fafc;",
                "padding: 16px; border-radius: 12px; background-color: #1e232a;",
                "font-size: 16px; color: #f7fafc;",
                "", "", tok, mode
            );
        }
    }

    private static ThemeTokens getFuturisticTokens(ColorMode mode) {
        if (mode == ColorMode.WHITE) {
            return new ThemeTokens(
                "#f8fafc", "#ffffff", "#020617", "#475569",
                "#cbd5e1", "#0284c7", "#c026d3",
                "rgba(2, 132, 199, 0.35)", "#0284c7"
            );
        } else {
            return new ThemeTokens(
                "#090a0f", "#12141d", "#00f3ff", "#ff00e4",
                "rgba(0, 243, 255, 0.4)", "#00f3ff", "#ff00e4",
                "rgba(0, 243, 255, 0.5)", "#00f3ff"
            );
        }
    }

    private static ThemeData createFuturistic(ColorMode mode) {
        ThemeTokens tok = getFuturisticTokens(mode);
        if (mode == ColorMode.WHITE) {
            return new ThemeData(
                tok.accentPrimary(), tok.accentSecondary(), tok.surfaceBackground(), tok.cardBackground(),
                "#ffffff", tok.textPrimary(),
                "border: 2px solid #0284c7; border-radius: 2px; padding: 12px 24px; font-weight: bold; font-family: monospace; color: #0284c7; background: #ffffff; text-transform: uppercase; cursor: pointer;",
                "border: 1px solid #cbd5e1; border-radius: 4px; padding: 20px; background: #ffffff; box-shadow: 0 4px 12px rgba(0,0,0,0.05); color: #020617;",
                "padding: 16px;",
                "font-size: 16px; color: #020617; font-family: monospace;",
                "", "", tok, mode
            );
        } else {
            return new ThemeData(
                tok.accentPrimary(), tok.accentSecondary(), tok.surfaceBackground(), tok.cardBackground(),
                "#090a0f", tok.textPrimary(),
                "border: 1px solid #00f3ff; border-radius: 0px; padding: 12px 24px; font-weight: bold; font-family: monospace; color: #00f3ff; background: transparent; text-transform: uppercase; box-shadow: 0 0 10px rgba(0,243,255,0.5); cursor: pointer; transition: all 0.2s;",
                "border: 1px solid rgba(255,0,228,0.5); border-radius: 4px; padding: 20px; background: rgba(18,20,29,0.8); box-shadow: inset 0 0 20px rgba(255,0,228,0.1);",
                "padding: 16px;",
                "font-size: 16px; color: #00f3ff; font-family: 'Courier New', Courier, monospace;",
                "", "", tok, mode
            );
        }
    }

    private static ThemeTokens getAstTokens(ColorMode mode) {
        if (mode == ColorMode.WHITE) {
            return new ThemeTokens(
                "#faf5ff", "#ffffff", "#1e1b4b", "#4c1d95",
                "#e9d5ff", "#7c3aed", "#0284c7",
                "rgba(124, 58, 237, 0.35)", "#7c3aed"
            );
        } else {
            return new ThemeTokens(
                "#0b0c10", "#1f2833", "#ffffff", "#c5c6c7",
                "rgba(138, 43, 226, 0.35)", "#8a2be2", "#00ced1",
                "rgba(138, 43, 226, 0.5)", "#00ced1"
            );
        }
    }

    private static ThemeData createAst(ColorMode mode) {
        ThemeTokens tok = getAstTokens(mode);
        if (mode == ColorMode.WHITE) {
            return new ThemeData(
                tok.accentPrimary(), tok.accentSecondary(), tok.surfaceBackground(), tok.cardBackground(),
                "#ffffff", tok.textPrimary(),
                "border: none; border-radius: 8px; padding: 12px 24px; font-weight: 600; color: #ffffff; background: linear-gradient(135deg, #7c3aed 0%, #6d28d9 100%); box-shadow: 0 4px 12px rgba(124,58,237,0.3); cursor: pointer;",
                "border: 1px solid #e9d5ff; border-radius: 12px; padding: 20px; background: #ffffff; box-shadow: 0 4px 16px rgba(0,0,0,0.05); color: #1e1b4b;",
                "padding: 20px; border-radius: 12px;",
                "font-size: 16px; color: #1e1b4b; font-family: 'Inter', sans-serif;",
                "", "", tok, mode
            );
        } else {
            return new ThemeData(
                tok.accentPrimary(), tok.accentSecondary(), tok.surfaceBackground(), tok.cardBackground(),
                "#ffffff", tok.textPrimary(),
                "border: none; border-radius: 8px; padding: 12px 24px; font-weight: 600; color: #FFFFFF; background: linear-gradient(135deg, #8A2BE2 0%, #4B0082 100%); box-shadow: 0 4px 15px rgba(138,43,226,0.4); cursor: pointer; transition: all 0.3s ease;",
                "border: 1px solid rgba(138,43,226,0.3); border-radius: 12px; padding: 20px; background: #1F2833; box-shadow: 0 8px 32px rgba(0,0,0,0.5); backdrop-filter: blur(10px);",
                "padding: 20px; border-radius: 12px;",
                "font-size: 16px; color: #C5C6C7; font-family: 'Inter', 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;",
                "", "", tok, mode
            );
        }
    }

    private static ThemeTokens getAtlantisTokens(ColorMode mode) {
        if (mode == ColorMode.WHITE) {
            return new ThemeTokens(
                "#f8fafc", "#ffffff", "#0f172a", "#475569",
                "#e2e8f0", "#2563eb", "#6366f1",
                "rgba(37, 99, 235, 0.35)", "#2563eb"
            );
        } else {
            return new ThemeTokens(
                "#0f172a", "#1e293b", "#f8fafc", "#94a3b8",
                "#334155", "#3b82f6", "#818cf8",
                "rgba(59, 130, 246, 0.4)", "#38bdf8"
            );
        }
    }

    private static ThemeData createAtlantis(ColorMode mode) {
        ThemeTokens tok = getAtlantisTokens(mode);
        if (mode == ColorMode.WHITE) {
            return new ThemeData(
                tok.accentPrimary(), tok.accentSecondary(), tok.surfaceBackground(), tok.cardBackground(),
                "#ffffff", tok.textPrimary(),
                "border: none; border-radius: 8px; padding: 12px 24px; font-weight: 600; color: #FFFFFF; background-color: #3B82F6; box-shadow: 0 4px 6px -1px rgba(59, 130, 246, 0.2); cursor: pointer;",
                "border: 1px solid #E2E8F0; border-radius: 16px; padding: 24px; background-color: #FFFFFF; box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.05); color: #334155;",
                "padding: 24px; border-radius: 16px;",
                "font-size: 15px; color: #334155; font-family: 'Inter', sans-serif;",
                "", "", tok, mode
            );
        } else {
            return new ThemeData(
                tok.accentPrimary(), tok.accentSecondary(), tok.surfaceBackground(), tok.cardBackground(),
                "#ffffff", tok.textPrimary(),
                "border: none; border-radius: 8px; padding: 12px 24px; font-weight: 600; color: #FFFFFF; background-color: #3B82F6; box-shadow: 0 4px 6px -1px rgba(59, 130, 246, 0.3); cursor: pointer;",
                "border: 1px solid #334155; border-radius: 16px; padding: 24px; background-color: #1e293b; box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.4); color: #f8fafc;",
                "padding: 24px; border-radius: 16px; background-color: #0f172a;",
                "font-size: 15px; color: #f8fafc; font-family: 'Inter', sans-serif;",
                "", "", tok, mode
            );
        }
    }
}
