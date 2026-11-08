package io.jettra.flux.theme;

/**
 * Singleton access and management facade for ThemeContext.
 */
public final class ThemeManager {

    private ThemeManager() {}

    public static ThemeContext getContext() {
        return ThemeContext.getInstance();
    }

    public static ThemeContext getInstance() {
        return ThemeContext.getInstance();
    }

    public static JettraTheme getCurrentTheme() {
        return ThemeContext.getInstance().getCurrentTheme();
    }

    public static ColorMode getCurrentMode() {
        return ThemeContext.getInstance().getCurrentMode();
    }

    public static ThemeTokens getCurrentTokens() {
        return ThemeContext.getInstance().getCurrentTokens();
    }

    public static void setTheme(JettraTheme theme) {
        ThemeContext.getInstance().setTheme(theme);
    }

    public static void setMode(ColorMode mode) {
        ThemeContext.getInstance().setMode(mode);
    }

    public static ColorMode toggleMode() {
        return ThemeContext.getInstance().toggleMode();
    }

    public static void set(JettraTheme theme, ColorMode mode) {
        ThemeContext.getInstance().set(theme, mode);
    }

    public static ThemeData getTheme(String themeName, ColorMode mode) {
        JettraTheme jt = JettraTheme.fromName(themeName);
        return jt.create(mode);
    }

    public static void synchronizeFromRequest(String modeStr, String themeStr) {
        if (themeStr != null && !themeStr.trim().isEmpty()) {
            JettraTheme jt = JettraTheme.fromName(themeStr.trim());
            ThemeContext.getInstance().setTheme(jt);
        }
        if (modeStr != null && !modeStr.trim().isEmpty()) {
            ColorMode cm = ColorMode.fromString(modeStr.trim(), null);
            if (cm != null) {
                ThemeContext.getInstance().setMode(cm);
            }
        }
    }

    public static String generateClientScript() {
        return ThemeContext.getInstance().generateClientScript();
    }
}
