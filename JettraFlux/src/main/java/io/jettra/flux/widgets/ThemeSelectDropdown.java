package io.jettra.flux.widgets;

import io.jettra.flux.core.Modifier;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.JettraTheme;
import io.jettra.flux.theme.ThemeData;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Encapsulated Theme Selection Dropdown component for JettraFlux.
 * Lists strictly the 12 normalized themes in the standardized order:
 * FlatTheme, Theme3D, FuturisticTheme, AstTheme, AtlantisTheme, OceanTheme, Matrix, Retro, DarkTheme, Heroes, SL, Core.
 * Completely excludes obsolete names (CoreTheme, SLTheme, HeroesTheme).
 */
public class ThemeSelectDropdown extends Widget {

    private String currentTheme = "Matrix";
    private boolean nativeSelect = true;
    private Consumer<JettraTheme> themeChangeListener;
    private Consumer<String> changeListener;

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

    public ThemeSelectDropdown() {
        super();
    }

    public static ThemeSelectDropdown of() {
        return new ThemeSelectDropdown();
    }

    public ThemeSelectDropdown current(String theme) {
        if (theme != null && !theme.trim().isEmpty()) {
            this.currentTheme = theme.trim();
        }
        return this;
    }

    public ThemeSelectDropdown current(JettraTheme theme) {
        if (theme != null) {
            this.currentTheme = theme.getDisplayName();
        }
        return this;
    }

    public ThemeSelectDropdown asNativeSelect(boolean nativeSelect) {
        this.nativeSelect = nativeSelect;
        return this;
    }

    public ThemeSelectDropdown onThemeChange(Consumer<JettraTheme> listener) {
        this.themeChangeListener = listener;
        return this;
    }

    public ThemeSelectDropdown onChange(Consumer<String> listener) {
        this.changeListener = listener;
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();

        // Trigger Themes class loading
        try {
            Class.forName("io.jettra.flux.theme.Themes");
        } catch (Exception ignored) {}

        String normCurrent = currentTheme != null ? currentTheme.toLowerCase().replaceAll("[_\\-\\s]", "") : "";
        if (normCurrent.equals("coretheme")) normCurrent = "core";
        if (normCurrent.equals("sltheme")) normCurrent = "sl";
        if (normCurrent.equals("heroestheme")) normCurrent = "heroes";

        JettraTheme[] officialThemes = JettraTheme.values();

        if (nativeSelect) {
            sb.append("<div class=\"jettra-theme-dropdown-container\" style=\"display: inline-flex; align-items: center; position: relative;\">\n");
            sb.append("  <select id=\"").append(id).append("\" class=\"jettra-theme-select-native\" ");
            sb.append("style=\"appearance: none; -webkit-appearance: none; background: var(--j-bg-subsurface, var(--surface, var(--surface-color))); color: var(--j-text-primary, var(--text-primary, var(--on-surface-color))); ");
            sb.append("border: 1px solid var(--j-border, var(--border, rgba(128,128,128,0.25))); border-radius: 6px; padding: 5px 26px 5px 10px; font-size: 11.5px; font-weight: 600; cursor: pointer; outline: none; transition: all 0.15s;\" ");
            sb.append("onchange=\"changeJettraTheme(this.value)\">\n");

            for (JettraTheme jt : officialThemes) {
                String displayName = jt.getDisplayName();
                String icon = THEME_ICONS.getOrDefault(displayName.toLowerCase(), "");
                boolean isSelected = displayName.equalsIgnoreCase(currentTheme) || normCurrent.equalsIgnoreCase(displayName.toLowerCase());
                sb.append("    <option value=\"").append(displayName).append("\"").append(isSelected ? " selected" : "").append(">");
                sb.append(displayName).append(" ").append(icon);
                sb.append("</option>\n");
            }
            sb.append("  </select>\n");
            // Caret icon
            sb.append("  <span style=\"position: absolute; right: 8px; pointer-events: none; color: var(--j-text-muted, var(--icon-color, var(--text-secondary))); font-size: 0.7rem;\">▼</span>\n");
            sb.append("</div>\n");
        } else {
            // Sleek interactive overlay trigger
            String currentIcon = THEME_ICONS.getOrDefault(normCurrent, "🎨");
            String triggerStyles = "display: inline-flex; align-items: center; gap: 8px; background: var(--surface, var(--surface-color)); "
                + "color: var(--text-primary, var(--on-surface-color)); border: 1px solid var(--border, rgba(128, 128, 128, 0.25)); "
                + "border-radius: 8px; padding: 6px 12px; font-size: 0.875rem; font-weight: 500; cursor: pointer; transition: all 0.2s ease;";

            Widget trigger = Row.of(
                Span.of(currentIcon).modifier(new Modifier().style("font-size: 1.15rem; line-height: 1;")),
                Span.of(currentTheme).modifier(new Modifier().style("font-weight: 600;")),
                Span.of("▾").modifier(new Modifier().style("font-size: 0.75rem; opacity: 0.7; margin-left: 2px;"))
            ).modifier(new Modifier().style(triggerStyles).attribute("title", "Select Visual Theme"));

            WidgetLet[] items = new WidgetLet[officialThemes.length];
            for (int i = 0; i < officialThemes.length; i++) {
                JettraTheme jt = officialThemes[i];
                String displayName = jt.getDisplayName();
                String icon = THEME_ICONS.getOrDefault(displayName.toLowerCase(), "🎨");
                boolean isActive = displayName.equalsIgnoreCase(currentTheme) || normCurrent.equalsIgnoreCase(displayName.toLowerCase());
                String label = displayName + " " + icon + (isActive ? " ✓" : "");
                items[i] = WidgetLet.of(label).url("javascript:changeJettraTheme('" + displayName + "')");
            }

            Widget menu = ((OverlayMenu) OverlayMenu.of(items).trigger(trigger)).alignRight();
            sb.append("<div class=\"jettra-theme-select-dropdown\" style=\"display: inline-block;\">\n");
            sb.append(menu.render(theme));
            sb.append("</div>\n");
        }

        // Script for theme selection persistence and reactive refresh
        sb.append("<script>\n");
        sb.append("if (typeof changeJettraTheme === 'undefined') {\n");
        sb.append("  function changeJettraTheme(themeName) {\n");
        sb.append("    var curMode = document.documentElement.getAttribute('data-color-mode') || 'dark';\n");
        sb.append("    document.cookie = 'jettra_theme=' + themeName + '; path=/; max-age=31536000; SameSite=Lax';\n");
        sb.append("    document.cookie = 'jettra_color_mode=' + curMode + '; path=/; max-age=31536000; SameSite=Lax';\n");
        sb.append("    try { localStorage.setItem('jettra_theme', themeName); } catch(e) {}\n");
        sb.append("    try { localStorage.setItem('jettra_color_mode', curMode); } catch(e) {}\n");
        sb.append("    if (typeof applyJettraStylePatch === 'function') {\n");
        sb.append("      applyJettraStylePatch(themeName, curMode);\n");
        sb.append("    }\n");
        sb.append("    window.location.reload();\n");
        sb.append("  }\n");
        sb.append("}\n");
        sb.append("</script>\n");

        return sb.toString();
    }
}
