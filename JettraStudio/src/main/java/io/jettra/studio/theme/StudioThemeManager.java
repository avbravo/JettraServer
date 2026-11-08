package io.jettra.studio.theme;

import io.jettra.flux.theme.ColorMode;
import io.jettra.flux.theme.JettraTheme;
import io.jettra.flux.theme.ThemeData;
import io.jettra.flux.theme.ThemeRegistry;
import io.jettra.flux.theme.Themes;

/**
 * Theme manager for JettraStudio, seamlessly integrated with the 14 JettraFlux themes
 * (Games, Police, Core, SL, Heroes, Matrix, Retro, FlatTheme, Theme3D, FuturisticTheme,
 * AstTheme, AtlantisTheme, OceanTheme, DarkTheme).
 */
public class StudioThemeManager {

    private static final StudioThemeManager INSTANCE = new StudioThemeManager();

    private JettraTheme defaultTheme = JettraTheme.GAMES;
    private ColorMode defaultMode = ColorMode.DARK;

    private StudioThemeManager() {
        // Ensure themes are registered
        try {
            Class.forName("io.jettra.flux.theme.Themes");
        } catch (Exception ignored) {}
    }

    public static StudioThemeManager getInstance() {
        return INSTANCE;
    }

    public JettraTheme getDefaultTheme() {
        return defaultTheme;
    }

    public void setDefaultTheme(JettraTheme defaultTheme) {
        if (defaultTheme != null) {
            this.defaultTheme = defaultTheme;
        }
    }

    public ColorMode getDefaultMode() {
        return defaultMode;
    }

    public void setDefaultMode(ColorMode defaultMode) {
        if (defaultMode != null) {
            this.defaultMode = defaultMode;
        }
    }

    /**
     * Resolves ThemeData from theme name and color mode.
     */
    public ThemeData resolveTheme(String themeName, ColorMode mode) {
        ColorMode m = (mode != null) ? mode : defaultMode;
        if (themeName != null && !themeName.trim().isEmpty()) {
            ThemeData data = ThemeRegistry.getTheme(themeName.trim(), m);
            if (data != null) return data;

            JettraTheme jt = JettraTheme.fromName(themeName);
            if (jt != null) return jt.create(m);
        }
        return defaultTheme.create(m);
    }

    /**
     * Injects CSS and JavaScript of the active theme into an HTML document's <head> section.
     */
    public String injectThemeIntoHead(String html, ThemeData theme) {
        if (html == null) return "";
        if (theme == null) theme = defaultTheme.create(defaultMode);

        String globalCss = theme.generateGlobalCss();
        StringBuilder headAdditions = new StringBuilder("\n");
        headAdditions.append("  <!-- JettraStudio Theme: ").append(theme.getColorMode()).append(" -->\n");
        headAdditions.append("  <meta name=\"theme-color\" content=\"").append(theme.primaryColor).append("\" />\n");
        headAdditions.append("  ").append(globalCss).append("\n");

        if (html.contains("</head>")) {
            return html.replace("</head>", headAdditions.toString() + "</head>");
        } else if (html.contains("<body")) {
            return html.replaceFirst("<body", "<head>" + headAdditions.toString() + "</head><body");
        } else {
            return headAdditions.toString() + html;
        }
    }
}
