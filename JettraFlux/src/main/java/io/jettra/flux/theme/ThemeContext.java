package io.jettra.flux.theme;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Reactive ThemeContext / ThemeManager for JettraFlux (Java 25+).
 *
 * Centralizes the reactive combination:
 *   (JettraTheme theme, ColorMode mode) -> ThemeTokens
 *
 * Features:
 * - Observer / Event-Driven State with thread-safe listener dispatch.
 * - Pure functional token resolution across the 12 canonical themes.
 * - Dynamic client-side DOM style patching script generation (zero-page-reload re-injection).
 */
public class ThemeContext {

    private static final ThemeContext INSTANCE = new ThemeContext();

    private JettraTheme currentTheme = JettraTheme.MATRIX;
    private ColorMode currentMode = ColorMode.DARK;
    private ThemeTokens currentTokens = JettraTheme.MATRIX.tokens(ColorMode.DARK);

    private final List<ThemeChangeListener> listeners = new CopyOnWriteArrayList<>();

    @FunctionalInterface
    public interface ThemeChangeListener {
        void onThemeChanged(JettraTheme theme, ColorMode mode, ThemeTokens tokens);
    }

    public ThemeContext() {}

    public static ThemeContext getInstance() {
        return INSTANCE;
    }

    public synchronized JettraTheme getCurrentTheme() {
        return currentTheme;
    }

    public synchronized ColorMode getCurrentMode() {
        return currentMode;
    }

    public synchronized ThemeTokens getCurrentTokens() {
        return currentTokens;
    }

    /**
     * Pure functional token resolution for any combination of theme and mode.
     */
    public ThemeTokens resolveTokens(JettraTheme theme, ColorMode mode) {
        Objects.requireNonNull(theme, "theme cannot be null");
        Objects.requireNonNull(mode, "mode cannot be null");
        return theme.tokens(mode);
    }

    /**
     * Pure functional ThemeData resolution.
     */
    public ThemeData resolveTheme(JettraTheme theme, ColorMode mode) {
        Objects.requireNonNull(theme, "theme cannot be null");
        Objects.requireNonNull(mode, "mode cannot be null");
        return theme.create(mode);
    }

    /**
     * Updates active theme and recalculates tokens.
     */
    public synchronized void setTheme(JettraTheme theme) {
        if (theme == null || theme == this.currentTheme) return;
        this.currentTheme = theme;
        this.currentTokens = resolveTokens(this.currentTheme, this.currentMode);
        notifyListeners();
    }

    /**
     * Updates active color mode (WHITE / DARK) and recalculates tokens.
     */
    public synchronized void setMode(ColorMode mode) {
        if (mode == null || mode == this.currentMode) return;
        this.currentMode = mode;
        this.currentTokens = resolveTokens(this.currentTheme, this.currentMode);
        notifyListeners();
    }

    /**
     * Toggles between WHITE and DARK mode.
     */
    public synchronized ColorMode toggleMode() {
        ColorMode next = this.currentMode.toggle();
        setMode(next);
        return next;
    }

    /**
     * Sets both theme and color mode atomically.
     */
    public synchronized void set(JettraTheme theme, ColorMode mode) {
        if (theme == null) theme = this.currentTheme;
        if (mode == null) mode = this.currentMode;
        this.currentTheme = theme;
        this.currentMode = mode;
        this.currentTokens = resolveTokens(this.currentTheme, this.currentMode);
        notifyListeners();
    }

    public void addListener(ThemeChangeListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removeListener(ThemeChangeListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    private void notifyListeners() {
        JettraTheme t = this.currentTheme;
        ColorMode m = this.currentMode;
        ThemeTokens tok = this.currentTokens;
        for (ThemeChangeListener listener : listeners) {
            try {
                listener.onThemeChanged(t, m, tok);
            } catch (Exception e) {
                // Ignore observer faults to safeguard core pipeline
            }
        }
    }

    /**
     * Generates a lightweight client-side JavaScript dictionary of tokens
     * for all 12 canonical themes across WHITE and DARK modes, plus the reactive
     * DOM patch function for zero-page-reload style propagation.
     */
    public String generateClientScript() {
        StringBuilder sb = new StringBuilder();
        sb.append("<script id=\"jettra-theme-engine-script\">\n");
        sb.append("window.__jettraThemeCatalog = {\n");

        JettraTheme[] themes = JettraTheme.values();
        for (int i = 0; i < themes.length; i++) {
            JettraTheme t = themes[i];
            ThemeTokens wTokens = t.tokens(ColorMode.WHITE);
            ThemeTokens dTokens = t.tokens(ColorMode.DARK);

            sb.append("  \"").append(t.getDisplayName().toLowerCase()).append("\": {\n");
            sb.append("    \"white\": ").append(wTokens.toJson()).append(",\n");
            sb.append("    \"dark\": ").append(dTokens.toJson()).append("\n");
            sb.append("  }").append(i < themes.length - 1 ? "," : "").append("\n");
        }
        sb.append("};\n\n");

        sb.append("function applyJettraStylePatch(themeName, modeName) {\n");
        sb.append("  var theme = (themeName || document.documentElement.getAttribute('data-theme') || 'Matrix').toLowerCase().trim();\n");
        sb.append("  var mode = (modeName || document.documentElement.getAttribute('data-color-mode') || 'dark').toLowerCase().trim();\n");
        sb.append("  if (mode !== 'white' && mode !== 'dark') mode = 'dark';\n");
        sb.append("  var catalog = window.__jettraThemeCatalog || {};\n");
        sb.append("  var themeTokens = (catalog[theme] && catalog[theme][mode]) ? catalog[theme][mode] : (catalog['matrix'] ? catalog['matrix'][mode] : null);\n");
        sb.append("  if (!themeTokens) return;\n");
        sb.append("  var root = document.documentElement;\n");
        sb.append("  root.setAttribute('data-color-mode', mode);\n");
        sb.append("  root.setAttribute('data-theme-mode', mode);\n");
        sb.append("  root.setAttribute('data-theme', theme);\n");
        sb.append("  if (mode === 'dark') { root.classList.add('dark'); } else { root.classList.remove('dark'); }\n");
        sb.append("  if (document.body) {\n");
        sb.append("    if (mode === 'dark') { document.body.classList.add('dark'); } else { document.body.classList.remove('dark'); }\n");
        sb.append("    document.body.style.backgroundColor = themeTokens.surfaceBackground;\n");
        sb.append("    document.body.style.color = themeTokens.textPrimary;\n");
        sb.append("  }\n");
        sb.append("  // Inject comprehensive CSS Variables on :root for instant update of all components\n");
        sb.append("  var vars = {\n");
        sb.append("    '--jf-bg': themeTokens.surfaceBackground,\n");
        sb.append("    '--jf-surface': themeTokens.cardBackground,\n");
        sb.append("    '--jf-surface-hover': themeTokens.surfaceHover,\n");
        sb.append("    '--jf-text-primary': themeTokens.textPrimary,\n");
        sb.append("    '--jf-text-secondary': themeTokens.textSecondary,\n");
        sb.append("    '--jf-border': themeTokens.border,\n");
        sb.append("    '--jf-accent': themeTokens.accentPrimary,\n");
        sb.append("    '--jf-focus-ring': themeTokens.focusRing,\n");
        sb.append("    '--jf-icon-color': themeTokens.iconColor,\n");
        sb.append("    '--j-bg-body': themeTokens.surfaceBackground,\n");
        sb.append("    '--j-bg-surface': themeTokens.cardBackground,\n");
        sb.append("    '--j-bg-subsurface': themeTokens.surfaceHover,\n");
        sb.append("    '--j-text-primary': themeTokens.textPrimary,\n");
        sb.append("    '--j-text-secondary': themeTokens.textSecondary,\n");
        sb.append("    '--j-text-muted': themeTokens.textSecondary,\n");
        sb.append("    '--j-border': themeTokens.border,\n");
        sb.append("    '--j-primary': themeTokens.accentPrimary,\n");
        sb.append("    '--primary-color': themeTokens.accentPrimary,\n");
        sb.append("    '--secondary-color': themeTokens.accentSecondary,\n");
        sb.append("    '--background-color': themeTokens.surfaceBackground,\n");
        sb.append("    '--surface-color': themeTokens.cardBackground,\n");
        sb.append("    '--on-primary-color': '#ffffff',\n");
        sb.append("    '--on-surface-color': themeTokens.textPrimary,\n");
        sb.append("    '--text-color': themeTokens.textPrimary,\n");
        sb.append("    '--text-color-secondary': themeTokens.textSecondary,\n");
        sb.append("    '--border-color': themeTokens.border,\n");
        sb.append("    '--background': themeTokens.surfaceBackground,\n");
        sb.append("    '--surface': themeTokens.cardBackground,\n");
        sb.append("    '--surface-hover': themeTokens.surfaceHover,\n");
        sb.append("    '--text-primary': themeTokens.textPrimary,\n");
        sb.append("    '--text-secondary': themeTokens.textSecondary,\n");
        sb.append("    '--border': themeTokens.border,\n");
        sb.append("    '--accent': themeTokens.accentPrimary,\n");
        sb.append("    '--icon-color': themeTokens.iconColor\n");
        sb.append("  };\n");
        sb.append("  for (var key in vars) {\n");
        sb.append("    root.style.setProperty(key, vars[key]);\n");
        sb.append("  }\n");
        sb.append("  // Persist cookies and localStorage so navigation preserves them indefinitely\n");
        sb.append("  document.cookie = 'jettra_color_mode=' + mode + '; path=/; max-age=31536000; SameSite=Lax';\n");
        sb.append("  document.cookie = 'jettra_theme=' + theme + '; path=/; max-age=31536000; SameSite=Lax';\n");
        sb.append("  try { localStorage.setItem('jettra_color_mode', mode); } catch(e) {}\n");
        sb.append("  try { localStorage.setItem('jettra_theme', theme); } catch(e) {}\n");
        sb.append("  // Update in-place toggles (SVG, tooltip and attributes)\n");
        sb.append("  var toggles = document.querySelectorAll('.jettra-theme-mode-toggle');\n");
        sb.append("  var sunSvg = '<svg class=\"jettra-theme-icon-sun\" xmlns=\"http://www.w3.org/2000/svg\" width=\"20\" height=\"20\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><circle cx=\"12\" cy=\"12\" r=\"4\" fill=\"currentColor\" fill-opacity=\"0.15\"/><path d=\"M12 2v2\"/><path d=\"M12 20v2\"/><path d=\"m4.93 4.93 1.41 1.41\"/><path d=\"m17.66 17.66 1.41 1.41\"/><path d=\"M2 12h2\"/><path d=\"M20 12h2\"/><path d=\"m6.34 17.66-1.41 1.41\"/><path d=\"m19.07 4.93-1.41 1.41\"/></svg>';\n");
        sb.append("  var moonSvg = '<svg class=\"jettra-theme-icon-moon\" xmlns=\"http://www.w3.org/2000/svg\" width=\"20\" height=\"20\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><path d=\"M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z\" fill=\"currentColor\" fill-opacity=\"0.15\"/></svg>';\n");
        sb.append("  for (var i = 0; i < toggles.length; i++) {\n");
        sb.append("    var btn = toggles[i];\n");
        sb.append("    if (mode === 'white') {\n");
        sb.append("      btn.innerHTML = moonSvg;\n");
        sb.append("      btn.setAttribute('title', 'Switch to Dark Mode');\n");
        sb.append("      btn.setAttribute('aria-label', 'Switch to Dark Mode');\n");
        sb.append("      btn.setAttribute('data-current-mode', 'white');\n");
        sb.append("      btn.setAttribute('data-next-mode', 'dark');\n");
        sb.append("    } else {\n");
        sb.append("      btn.innerHTML = sunSvg;\n");
        sb.append("      btn.setAttribute('title', 'Switch to Light Mode');\n");
        sb.append("      btn.setAttribute('aria-label', 'Switch to Light Mode');\n");
        sb.append("      btn.setAttribute('data-current-mode', 'dark');\n");
        sb.append("      btn.setAttribute('data-next-mode', 'white');\n");
        sb.append("    }\n");
        sb.append("  }\n");
        sb.append("  // Update native theme dropdowns if present\n");
        sb.append("  var dropdowns = document.querySelectorAll('.jettra-theme-select-native');\n");
        sb.append("  for (var j = 0; j < dropdowns.length; j++) {\n");
        sb.append("    var sel = dropdowns[j];\n");
        sb.append("    for (var k = 0; k < sel.options.length; k++) {\n");
        sb.append("      if (sel.options[k].value.toLowerCase() === theme.toLowerCase()) {\n");
        sb.append("        sel.selectedIndex = k;\n");
        sb.append("        break;\n");
        sb.append("      }\n");
        sb.append("    }\n");
        sb.append("  }\n");
        sb.append("  // Dispatch custom reactive event for any components or charts\n");
        sb.append("  window.dispatchEvent(new CustomEvent('jettraThemeChange', { detail: { theme: theme, mode: mode, tokens: themeTokens } }));\n");
        sb.append("}\n");
        sb.append("\n");
        sb.append("// Immediate client hydration: guarantees saved mode & theme persist across page navigation without reverting to default\n");
        sb.append("(function initJettraThemeSync() {\n");
        sb.append("  try {\n");
        sb.append("    var cookies = document.cookie.split(';');\n");
        sb.append("    var m = null;\n");
        sb.append("    var t = null;\n");
        sb.append("    for (var i = 0; i < cookies.length; i++) {\n");
        sb.append("      var c = cookies[i].trim();\n");
        sb.append("      if (c.indexOf('jettra_color_mode=') === 0) m = c.substring(18).trim();\n");
        sb.append("      if (c.indexOf('jettra_theme=') === 0) t = c.substring(13).trim();\n");
        sb.append("    }\n");
        sb.append("    if (!m) {\n");
        sb.append("      try { m = localStorage.getItem('jettra_color_mode'); } catch(e) {}\n");
        sb.append("    }\n");
        sb.append("    if (!t) {\n");
        sb.append("      try { t = localStorage.getItem('jettra_theme'); } catch(e) {}\n");
        sb.append("    }\n");
        sb.append("    // Default is Matrix with Dark\n");
        sb.append("    if (!m || (m !== 'white' && m !== 'dark')) m = 'dark';\n");
        sb.append("    if (!t) t = 'Matrix';\n");
        sb.append("\n");
        sb.append("    var curMode = document.documentElement.getAttribute('data-color-mode');\n");
        sb.append("    var curTheme = document.documentElement.getAttribute('data-theme');\n");
        sb.append("    if (curMode !== m || (curTheme && curTheme.toLowerCase() !== t.toLowerCase())) {\n");
        sb.append("      applyJettraStylePatch(t, m);\n");
        sb.append("    }\n");
        sb.append("  } catch(e) {}\n");
        sb.append("})();\n");
        sb.append("</script>\n");

        return sb.toString();
    }
}
