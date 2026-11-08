package io.jettra.flux.widgets;

import io.jettra.flux.core.Modifier;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.JettraTheme;
import io.jettra.flux.theme.ThemeData;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Standardized Theme Selection Menu widget for JettraFlux.
 * Strictly presents the official catalog of 12 themes (excluding obsolete identifiers):
 * FlatTheme, Theme3D, FuturisticTheme, AstTheme, AtlantisTheme, OceanTheme, Matrix, Retro, DarkTheme, Heroes, SL, Core.
 */
public class ThemeSelectorMenu extends Widget {

    private String currentTheme = "Matrix";

    private static final Map<String, String> THEME_ICONS = new LinkedHashMap<>();

    static {
        THEME_ICONS.put("flattheme", "🟦");
        THEME_ICONS.put("theme3d", "🧊");
        THEME_ICONS.put("futuristictheme", "🚀");
        THEME_ICONS.put("asttheme", "🪐");
        THEME_ICONS.put("atlantistheme", "🔱");
        THEME_ICONS.put("oceantheme", "🌊");
        THEME_ICONS.put("matrix", "🟢");
        THEME_ICONS.put("retro", "🕹️");
        THEME_ICONS.put("darktheme", "🌙");
        THEME_ICONS.put("heroes", "⚡");
        THEME_ICONS.put("sl", "🌐");
        THEME_ICONS.put("core", "⚛️");
        THEME_ICONS.put("police", "🚔");
        THEME_ICONS.put("games", "🎮");
        THEME_ICONS.put("bit", "💎");
    }

    public ThemeSelectorMenu() {
        super();
    }

    public static ThemeSelectorMenu of() {
        return new ThemeSelectorMenu();
    }

    public ThemeSelectorMenu current(String theme) {
        if (theme != null && !theme.trim().isEmpty()) {
            this.currentTheme = theme.trim();
        }
        return this;
    }

    public ThemeSelectorMenu current(JettraTheme theme) {
        if (theme != null) {
            this.currentTheme = theme.getDisplayName();
        }
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"jettra-theme-selector-menu\" style=\"display: inline-block;\">\n");

        // Trigger Themes registration
        try {
            Class.forName("io.jettra.flux.theme.Themes");
        } catch (Exception ignored) {}

        // Resolve clean active icon
        String normCurrent = currentTheme != null ? currentTheme.toLowerCase().replaceAll("[_\\-\\s]", "") : "";
        // Map any legacy names to canonical for display
        if (normCurrent.equals("coretheme")) normCurrent = "core";
        if (normCurrent.equals("sltheme")) normCurrent = "sl";
        if (normCurrent.equals("heroestheme")) normCurrent = "heroes";

        String currentIcon = THEME_ICONS.getOrDefault(normCurrent, "🎨");

        Widget trigger = Span.of(currentIcon)
                .modifier(new Modifier()
                        .attribute("title", "Select Theme (Current: " + currentTheme + ")")
                        .attribute("aria-label", "Theme selector menu")
                        .style("cursor: pointer; font-size: 1.4rem; padding: 4px 8px; border-radius: 6px; display: inline-flex; align-items: center; transition: transform 0.2s ease;"));

        // Strictly iterate over the 12 official JettraTheme values
        JettraTheme[] officialThemes = JettraTheme.values();
        WidgetLet[] items = new WidgetLet[officialThemes.length];

        for (int i = 0; i < officialThemes.length; i++) {
            JettraTheme jt = officialThemes[i];
            String displayName = jt.getDisplayName();
            String icon = THEME_ICONS.getOrDefault(displayName.toLowerCase(), "🎨");
            boolean isActive = displayName.equalsIgnoreCase(currentTheme) || (normCurrent.equalsIgnoreCase(displayName.toLowerCase()));

            String label = displayName + " " + icon + (isActive ? " ✓" : "");
            items[i] = WidgetLet.of(label)
                    .url("javascript:changeJettraTheme('" + displayName + "')");
        }

        Widget menu = ((OverlayMenu) OverlayMenu.of(items).trigger(trigger)).alignRight();
        sb.append(menu.render(theme));

        sb.append("  <script>\n");
        sb.append("    if (typeof changeJettraTheme === 'undefined') {\n");
        sb.append("      function changeJettraTheme(themeName) {\n");
        sb.append("        document.cookie = 'jettra_theme=' + themeName + '; path=/; max-age=31536000; SameSite=Lax';\n");
        sb.append("        try { localStorage.setItem('jettra_theme', themeName); } catch(e) {}\n");
        sb.append("        window.location.reload();\n");
        sb.append("      }\n");
        sb.append("    }\n");
        sb.append("  </script>\n");
        sb.append("</div>\n");

        return sb.toString();
    }
}
