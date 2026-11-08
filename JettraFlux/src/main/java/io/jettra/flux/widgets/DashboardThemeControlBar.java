package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ColorMode;
import io.jettra.flux.theme.JettraTheme;
import io.jettra.flux.theme.ThemeData;
import java.util.function.Consumer;

/**
 * DashboardThemeControlBar is an integrated, adjacent control bar combining:
 * 1. The ThemeSelectDropdown (listing strictly the 12 normalized themes)
 * 2. The ThemeModeToggle (sun / moon interactive luminance toggle)
 *
 * Placed side-by-side in horizontal alignment with harmonic spacing and pure Java UI encapsulation.
 */
public class DashboardThemeControlBar extends Widget {

    private String currentTheme = "Matrix";
    private ColorMode colorMode = ColorMode.DARK;
    private int toggleSize = 18;
    private boolean nativeSelect = false;
    private Consumer<ColorMode> modeListener;
    private Consumer<JettraTheme> themeListener;

    public DashboardThemeControlBar() {
        super();
    }

    public static DashboardThemeControlBar of() {
        return new DashboardThemeControlBar();
    }

    public static DashboardThemeControlBar of(String currentTheme) {
        return new DashboardThemeControlBar().currentTheme(currentTheme);
    }

    public static DashboardThemeControlBar of(String currentTheme, ColorMode colorMode) {
        return new DashboardThemeControlBar().currentTheme(currentTheme).colorMode(colorMode);
    }

    public DashboardThemeControlBar currentTheme(String currentTheme) {
        if (currentTheme != null && !currentTheme.trim().isEmpty()) {
            this.currentTheme = currentTheme.trim();
        }
        return this;
    }

    public DashboardThemeControlBar currentTheme(JettraTheme theme) {
        if (theme != null) {
            this.currentTheme = theme.getDisplayName();
        }
        return this;
    }

    public DashboardThemeControlBar colorMode(ColorMode colorMode) {
        this.colorMode = colorMode;
        return this;
    }

    public DashboardThemeControlBar toggleSize(int toggleSize) {
        this.toggleSize = toggleSize;
        return this;
    }

    public DashboardThemeControlBar asNativeSelect(boolean nativeSelect) {
        this.nativeSelect = nativeSelect;
        return this;
    }

    public DashboardThemeControlBar onModeChange(Consumer<ColorMode> listener) {
        this.modeListener = listener;
        return this;
    }

    public DashboardThemeControlBar onThemeChange(Consumer<JettraTheme> listener) {
        this.themeListener = listener;
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        ThemeSelectDropdown dropdown = ThemeSelectDropdown.of()
                .current(currentTheme)
                .asNativeSelect(nativeSelect)
                .onThemeChange(themeListener);

        ThemeModeToggle toggle = ThemeModeToggle.of()
                .size(toggleSize)
                .colorMode(colorMode)
                .onModeChange(modeListener);

        String containerStyles = "display: inline-flex; align-items: center; gap: 8px; vertical-align: middle; "
                + modifier.getStyles();

        StringBuilder sb = new StringBuilder();
        sb.append("<div id=\"").append(id).append("\" class=\"jettra-dashboard-theme-control-bar\" ");
        sb.append("style=\"").append(containerStyles).append("\">\n");
        sb.append("  ").append(dropdown.render(theme)).append("\n");
        sb.append("  ").append(toggle.render(theme)).append("\n");
        sb.append("</div>\n");

        return sb.toString();
    }
}
