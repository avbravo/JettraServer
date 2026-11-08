package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ColorMode;
import io.jettra.flux.theme.JettraTheme;
import io.jettra.flux.theme.ThemeData;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Dedicated luminance mode switcher component for JettraFlux (White / Dark).
 *
 * Features:
 * - Pure Java UI encapsulation without raw HTML/CSS/JS leaked to application code.
 * - Semantic reactive iconography:
 *     * In WHITE mode: exhibits Moon icon (inviting switch to night/dark mode).
 *     * In DARK mode: exhibits Sun icon (inviting switch to day/light mode).
 * - Client persistence via cookie (`jettra_color_mode`) and localStorage.
 * - OS `prefers-color-scheme` initial detection.
 * - Can operate independently or alongside ThemeSelectorMenu in headers/navbars.
 */
public class ThemeModeToggle extends Widget {

    private ColorMode explicitMode;
    private int size = 20;
    private BiConsumer<JettraTheme, ColorMode> onToggle;

    private static final String SUN_SVG =
        "<svg class=\"jettra-theme-icon-sun\" xmlns=\"http://www.w3.org/2000/svg\" width=\"%d\" height=\"%d\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\">"
        + "<circle cx=\"12\" cy=\"12\" r=\"4\" fill=\"currentColor\" fill-opacity=\"0.15\"/>"
        + "<path d=\"M12 2v2\"/><path d=\"M12 20v2\"/>"
        + "<path d=\"m4.93 4.93 1.41 1.41\"/><path d=\"m17.66 17.66 1.41 1.41\"/>"
        + "<path d=\"M2 12h2\"/><path d=\"M20 12h2\"/>"
        + "<path d=\"m6.34 17.66-1.41 1.41\"/><path d=\"m19.07 4.93-1.41 1.41\"/>"
        + "</svg>";

    private static final String MOON_SVG =
        "<svg class=\"jettra-theme-icon-moon\" xmlns=\"http://www.w3.org/2000/svg\" width=\"%d\" height=\"%d\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\">"
        + "<path d=\"M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z\" fill=\"currentColor\" fill-opacity=\"0.15\"/>"
        + "</svg>";

    public ThemeModeToggle() {
        super();
    }

    public static ThemeModeToggle of() {
        return new ThemeModeToggle();
    }

    public ThemeModeToggle colorMode(ColorMode mode) {
        this.explicitMode = mode;
        return this;
    }

    public ThemeModeToggle size(int size) {
        this.size = size;
        return this;
    }

    public ThemeModeToggle onToggle(BiConsumer<JettraTheme, ColorMode> onToggle) {
        this.onToggle = onToggle;
        return this;
    }

    public ThemeModeToggle onToggle(Consumer<ColorMode> onToggle) {
        if (onToggle != null) {
            this.onToggle = (theme, mode) -> onToggle.accept(mode);
        }
        return this;
    }

    public ThemeModeToggle onModeChange(Consumer<ColorMode> onModeChange) {
        return onToggle(onModeChange);
    }

    @Override
    public String render(ThemeData theme) {
        ColorMode activeMode = explicitMode != null
            ? explicitMode
            : (theme != null && theme.getColorMode() != null ? theme.getColorMode() : ColorMode.DARK);

        boolean isWhite = activeMode == ColorMode.WHITE;
        String iconSvg = isWhite ? String.format(MOON_SVG, size, size) : String.format(SUN_SVG, size, size);
        String tooltip = isWhite ? "Switch to Dark Mode" : "Switch to Light Mode";
        String nextMode = isWhite ? "dark" : "white";

        String defaultStyles = "display: inline-flex; align-items: center; justify-content: center; "
            + "background: transparent; border: 1px solid var(--border, rgba(128, 128, 128, 0.25)); "
            + "border-radius: 8px; padding: 7px 10px; cursor: pointer; color: var(--icon-color, var(--on-surface-color)); "
            + "transition: all 0.2s ease; outline: none; vertical-align: middle;";

        StringBuilder sb = new StringBuilder();
        sb.append("<button type=\"button\" ");
        sb.append("class=\"jettra-theme-mode-toggle\" ");
        sb.append("id=\"").append(id).append("\" ");
        sb.append("title=\"").append(tooltip).append("\" ");
        sb.append("aria-label=\"").append(tooltip).append("\" ");
        sb.append("data-current-mode=\"").append(activeMode.name().toLowerCase()).append("\" ");
        sb.append("data-next-mode=\"").append(nextMode).append("\" ");
        sb.append("onclick=\"event.stopPropagation(); if(event.preventDefault) event.preventDefault(); toggleJettraColorMode(event, '").append(nextMode).append("')\">\n");
        sb.append(iconSvg).append("\n");
        sb.append("</button>\n");

        sb.append(io.jettra.flux.theme.ThemeContext.getInstance().generateClientScript()).append("\n");

        sb.append("<script>\n");
        sb.append("if (typeof toggleJettraColorMode === 'undefined') {\n");
        sb.append("  function toggleJettraColorMode(event, targetMode) {\n");
        sb.append("    if (event) {\n");
        sb.append("      if (typeof event.stopPropagation === 'function') event.stopPropagation();\n");
        sb.append("      if (typeof event.preventDefault === 'function') event.preventDefault();\n");
        sb.append("      event.cancelBubble = true;\n");
        sb.append("    }\n");
        sb.append("    var current = document.documentElement.getAttribute('data-color-mode') || 'dark';\n");
        sb.append("    var mode = targetMode || (current === 'white' ? 'dark' : 'white');\n");
        sb.append("    var currentTheme = document.documentElement.getAttribute('data-theme') || 'Matrix';\n");
        sb.append("    if (typeof applyJettraStylePatch === 'function') {\n");
        sb.append("      applyJettraStylePatch(currentTheme, mode);\n");
        sb.append("    }\n");
        sb.append("  }\n");
        sb.append("  (function initColorModeDetection() {\n");
        sb.append("    var cookies = document.cookie.split(';');\n");
        sb.append("    var found = false;\n");
        sb.append("    for (var i = 0; i < cookies.length; i++) {\n");
        sb.append("      var c = cookies[i].trim();\n");
        sb.append("      if (c.indexOf('jettra_color_mode=') === 0) { found = true; break; }\n");
        sb.append("    }\n");
        sb.append("    if (!found) {\n");
        sb.append("      var stored = null;\n");
        sb.append("      try { stored = localStorage.getItem('jettra_color_mode'); } catch(e) {}\n");
        sb.append("      if (stored) {\n");
        sb.append("        if (typeof applyJettraStylePatch === 'function') applyJettraStylePatch(null, stored);\n");
        sb.append("      }\n");
        sb.append("    }\n");
        sb.append("  })();\n");
        sb.append("}\n");
        sb.append("</script>\n");

        return sb.toString();
    }
}
