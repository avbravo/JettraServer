package io.jettra.flux.theme;

/**
 * Heroes provides a heroic, vibrant aesthetic inspired by Flowbite superhero designs.
 * Canonical class standardized in JettraFlux (migrated from HeroesTheme).
 *
 * Implements ThemeDefinition with full White and Dark color mode support.
 */
public class Heroes implements ThemeDefinition {

    private static final Heroes INSTANCE = new Heroes();

    public static Heroes getInstance() {
        return INSTANCE;
    }

    @Override
    public String getThemeName() {
        return "Heroes";
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
                "#111827",                  // textPrimary: dark gray-900 (WCAG contrast > 17:1)
                "#4b5563",                  // textSecondary: gray-600 (WCAG contrast > 7:1)
                "#e2e8f0",                  // border: slate-200
                "#4338ca",                  // accentPrimary: hero deep indigo (WCAG contrast > 7.5:1)
                "#db2777",                  // accentSecondary: vibrant hero rose
                "rgba(67, 56, 202, 0.35)",  // focusRing
                "#4338ca"                   // iconColor
            );
        } else {
            return new ThemeTokens(
                "#111827",                  // surfaceBackground: Flowbite Gray 900
                "#1f2937",                  // cardBackground: Flowbite Gray 800
                "#f9fafb",                  // textPrimary: Crisp raywhite text (WCAG contrast > 16:1)
                "#9ca3af",                  // textSecondary: Flowbite Gray 400 (WCAG contrast > 6.5:1)
                "#374151",                  // border: Flowbite Gray 700
                "#4f46e5",                  // accentPrimary: Hero Indigo
                "#ff80b5",                  // accentSecondary: Hero Mesh Pink
                "rgba(79, 70, 229, 0.4)",   // focusRing
                "#9089fc"                   // iconColor
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
                "border: none; border-radius: 8px; padding: 10px 20px; font-weight: 600; font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; color: #ffffff; background-color: #4338ca; box-shadow: 0 2px 4px 0 rgba(0, 0, 0, 0.1); cursor: pointer; transition: background-color 0.2s, box-shadow 0.2s;",
                "border: 1px solid #e2e8f0; border-radius: 12px; padding: 24px; background-color: #ffffff; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05); color: " + tok.textPrimary() + ";",
                "padding: 24px; border-radius: 12px; border: 1px solid #e2e8f0; background-color: #f8fafc;",
                "font-size: 15px; color: " + tok.textPrimary() + "; font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6;",
                HeroesTheme.Template.CustomCSS,
                HeroesTheme.Template.CustomJS,
                tok,
                mode
            );
        } else {
            return new ThemeData(
                tok.accentPrimary(),
                tok.accentSecondary(),
                tok.surfaceBackground(),
                tok.cardBackground(),
                "#ffffff",
                tok.textPrimary(),
                "border: none; border-radius: 8px; padding: 10px 20px; font-weight: 600; font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; color: #ffffff; background-color: #4f46e5; box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.2); cursor: pointer; transition: background-color 0.2s, box-shadow 0.2s;",
                "border: 1px solid #374151; border-radius: 12px; padding: 24px; background-color: #1f2937; box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.3); color: #f9fafb;",
                "padding: 24px; border-radius: 12px; border: 1px solid #374151; background-color: #1f2937;",
                "font-size: 15px; color: #e5e7eb; font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6;",
                HeroesTheme.Template.CustomCSS,
                HeroesTheme.Template.CustomJS,
                tok,
                mode
            );
        }
    }
}
