package io.jettra.flux.theme;

public class OceanTheme implements ThemeDefinition {

    private static final OceanTheme INSTANCE = new OceanTheme();

    public static OceanTheme getInstance() {
        return INSTANCE;
    }

    @Override
    public String getThemeName() {
        return "OceanTheme";
    }

    @Override
    public ThemeTokens tokens(ColorMode mode) {
        return getTokens(mode);
    }

    public static ThemeTokens getTokens(ColorMode mode) {
        if (mode == ColorMode.WHITE) {
            return new ThemeTokens(
                "#f0fdfa",                  // surfaceBackground: light sea foam
                "#ffffff",                  // cardBackground: pure white
                "#0f172a",                  // textPrimary: deep slate-900 (WCAG contrast > 15:1)
                "#334155",                  // textSecondary: slate-700 (WCAG contrast > 9:1)
                "#ccfbf1",                  // border: teal border
                "#0d9488",                  // accentPrimary: deep teal (WCAG contrast > 4.8:1)
                "#0891b2",                  // accentSecondary: ocean cyan
                "rgba(13, 148, 136, 0.35)", // focusRing
                "#0d9488"                   // iconColor
            );
        } else {
            return new ThemeTokens(
                "#121212",                  // surfaceBackground: very dark grey
                "#1e1e1e",                  // cardBackground: dark grey
                "#e2e8f0",                  // textPrimary: crisp raywhite (WCAG contrast > 13:1)
                "#94a3b8",                  // textSecondary: slate-400 (WCAG contrast > 6.8:1)
                "rgba(32, 208, 119, 0.2)",  // border: mint border tint
                "#20d077",                  // accentPrimary: mint green / teal
                "#06b6d4",                  // accentSecondary: cyan
                "rgba(32, 208, 119, 0.4)",  // focusRing
                "#20d077"                   // iconColor
            );
        }
    }

    public static ThemeData create() {
        return create(ColorMode.DARK);
    }

    @Override
    public ThemeData createTheme(ColorMode mode) {
        return create(mode);
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
                "border: none; border-radius: 8px; padding: 12px 24px; font-weight: 600; color: #ffffff; background-color: #0d9488; box-shadow: 0 4px 6px -1px rgba(13, 148, 136, 0.25); cursor: pointer; transition: all 0.2s ease-in-out;",
                "border: 1px solid #ccfbf1; border-radius: 16px; padding: 24px; background-color: #ffffff; box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.05); color: " + tok.textPrimary() + ";",
                "padding: 24px; border-radius: 16px; border: 1px solid #ccfbf1; background-color: #f0fdfa;",
                "font-size: 15px; color: " + tok.textPrimary() + "; font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;",
                Template.CustomCSS,
                Template.CustomJS,
                tok,
                mode
            );
        } else {
            return new ThemeData(
                tok.accentPrimary(),
                tok.accentSecondary(),
                tok.surfaceBackground(),
                tok.cardBackground(),
                "#121212",
                tok.textPrimary(),
                "border: none; border-radius: 8px; padding: 12px 24px; font-weight: 600; color: #121212; background-color: #20d077; box-shadow: 0 4px 6px -1px rgba(32, 208, 119, 0.3); cursor: pointer; transition: all 0.2s ease-in-out;",
                "border: 1px solid rgba(255, 255, 255, 0.1); border-radius: 16px; padding: 24px; background-color: #1e1e1e; box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.5);",
                "padding: 24px; border-radius: 16px;",
                "font-size: 15px; color: #e2e8f0; font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;",
                Template.CustomCSS,
                Template.CustomJS,
                tok,
                mode
            );
        }
    }
    public static class Template {
        public static final String CustomCSS = "<style>\n"
            + "/* OceanTheme Layout Adjustments */\n"
            + ".espresso-left { border-right: none !important; box-shadow: 2px 0 10px rgba(0,0,0,0.2) !important; z-index: 100; }\n"
            + ".espresso-top { border-bottom: 1px solid rgba(128,128,128,0.1); justify-content: space-between; }\n"
            + ".top-left-section { display: flex; align-items: center; gap: 20px; }\n"
            + ".top-right-section { display: flex; align-items: center; gap: 15px; color: var(--on-surface-color); }\n"
            + ".top-right-section i { font-size: 1.2rem; cursor: pointer; transition: color 0.2s; }\n"
            + ".top-right-section i:hover { color: var(--primary-color); }\n"
            + ".top-btn-today { background-color: #6366F1; color: white; border: none; padding: 8px 16px; border-radius: 6px; font-weight: 600; cursor: pointer; display: flex; align-items: center; gap: 8px; }\n"
            + ".top-avatar { width: 32px; height: 32px; border-radius: 50%; background-color: #ccc; display: flex; justify-content: center; align-items: center; font-weight: bold; color: #333; }\n"
            + ".sidebar-logo { font-size: 1.5rem; font-weight: 700; color: var(--on-surface-color); padding: 10px 15px; margin-bottom: 20px; display: flex; align-items: center; gap: 10px; }\n"
            + ".sidebar-category { margin-top: 20px; font-size: 0.75rem; text-transform: uppercase; letter-spacing: 1px; color: #64748b; margin-bottom: 10px; padding-left: 15px; font-weight: 600; }\n"
            + ".professional-left a, .professional-left p { color: var(--on-surface-color); opacity: 0.8; text-decoration: none; display: flex; align-items: center; padding: 10px 15px; border-radius: 8px; transition: all 0.2s ease; margin-bottom: 4px; cursor: pointer; font-weight: 500; font-size: 0.95rem; }\n"
            + ".professional-left a:hover, .professional-left p:hover { background-color: rgba(128,128,128,0.1); opacity: 1; }\n"
            + ".professional-left a.active { background-color: var(--primary-color); color: var(--on-primary-color) !important; opacity: 1; }\n"
            + ".professional-left a.active i { color: var(--on-primary-color) !important; }\n"
            + ".professional-left i { margin-right: 12px; font-size: 1.1rem; width: 20px; text-align: center; color: #94a3b8; }\n"
            + "/* Card Styling */\n"
            + ".espresso-card { background: var(--surface-color); border-radius: 12px; padding: 20px; border: 1px solid rgba(128, 128, 128, 0.1); }\n"
            + ".professional-center { height: 100%; display: flex; flex-direction: column; gap: 1.5rem; }\n"
            + ".top-bars-icon { font-size: 1.2rem; cursor: pointer; color: var(--on-surface-color); opacity: 0.7; margin-right: 15px; }\n"
            + ".top-dashboard-title { margin: 0; font-weight: 600; font-size: 1.1rem; }\n"
            + "</style>\n";

        public static final String CustomJS = "<script>\n"
            + "function toggleSidebar() {\n"
            + "  var sidebar = document.querySelector('.espresso-left');\n"
            + "  if(sidebar) sidebar.classList.toggle('open');\n"
            + "}\n"
            + "function changeLang(lang) {\n"
            + "  document.cookie = 'jettra_lang=' + lang + '; path=/';\n"
            + "  window.location.reload();\n"
            + "}\n"
            + "function toggleProfileMenu() {\n"
            + "  var pm = document.getElementById('profile-menu');\n"
            + "  if(pm) pm.style.display = (pm.style.display === 'none') ? 'block' : 'none';\n"
            + "}\n"
            + "function restoreMenus() {\n"
            + "  document.querySelectorAll('.widgetlet-children').forEach(function(c) {\n"
            + "    var key = c.getAttribute('data-exp-key');\n"
            + "    if(key && localStorage.getItem(key) === 'open') {\n"
            + "      c.style.display = 'block';\n"
            + "      var iconId = c.id.replace('_children', '_icon');\n"
            + "      var icon = document.getElementById(iconId);\n"
            + "      if(icon) icon.className = 'fas fa-chevron-down';\n"
            + "    }\n"
            + "  });\n"
            + "}\n"
            + "function initMenuObserver() {\n"
            + "  restoreMenus();\n"
            + "  if (typeof window.MutationObserver !== 'undefined' && document.body) {\n"
            + "    var observer = new MutationObserver(function(mutations) {\n"
            + "      restoreMenus();\n"
            + "    });\n"
            + "    observer.observe(document.body, { childList: true, subtree: true });\n"
            + "  }\n"
            + "}\n"
            + "if (document.readyState === 'loading') {\n"
            + "  document.addEventListener('DOMContentLoaded', initMenuObserver);\n"
            + "} else {\n"
            + "  initMenuObserver();\n"
            + "}\n"
            + "setTimeout(restoreMenus, 100);\n"
            + "</script>";
    }

    public static class DashboardPage {
        public static final String CustomCSS = "<style>\n"
            + ".oceantheme-dashboard-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 1.5rem; width: 100%; }\n"
            + ".oceantheme-main-grid { display: grid; grid-template-columns: 2fr 1fr; gap: 1.5rem; width: 100%; margin-top: 1.5rem; }\n"
            + "@media (max-width: 992px) { .oceantheme-dashboard-grid, .oceantheme-main-grid { grid-template-columns: 1fr; } }\n"
            + ".stat-card { display: flex; flex-direction: column; gap: 10px; }\n"
            + ".stat-header { font-size: 0.85rem; color: #94a3b8; font-weight: 600; }\n"
            + ".stat-value { font-size: 1.8rem; font-weight: 700; color: var(--on-surface-color); }\n"
            + ".stat-badge { padding: 4px 8px; border-radius: 4px; font-size: 0.75rem; font-weight: bold; display: inline-flex; align-items: center; gap: 4px; }\n"
            + ".stat-badge.down { background-color: rgba(239, 68, 68, 0.15); color: #ef4444; }\n"
            + ".stat-badge.up { background-color: rgba(34, 197, 94, 0.15); color: #22c55e; }\n"
            + ".chart-card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }\n"
            + ".transaction-item { display: flex; align-items: center; justify-content: space-between; padding: 12px 0; border-bottom: 1px solid rgba(128,128,128,0.1); }\n"
            + ".transaction-item:last-child { border-bottom: none; }\n"
            + ".tx-icon { width: 36px; height: 36px; border-radius: 50%; display: flex; justify-content: center; align-items: center; color: white; font-size: 0.9rem; }\n"
            + ".tx-details { flex: 1; margin-left: 15px; }\n"
            + ".tx-title { font-size: 0.9rem; font-weight: 600; color: var(--on-surface-color); margin: 0 0 4px 0; }\n"
            + ".tx-date { font-size: 0.75rem; color: #94a3b8; margin: 0; }\n"
            + ".tx-amount { font-size: 0.9rem; font-weight: 700; }\n"
            + ".tx-amount.positive { color: #22c55e; }\n"
            + ".tx-amount.negative { color: #ef4444; }\n"
            + "</style>";
    }
}
