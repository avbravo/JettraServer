package io.jettra.flux.theme;

/**
 * DarkTheme provides an authentic, high-contrast dark theme inspired by Flowbite Dark Mode
 * (the Tailwind CSS component library).
 *
 * Flowbite Dark Color Architecture:
 * - Background Canvas: Flowbite Gray 900 (#111827)
 * - Surfaces, Cards & Sidebars: Flowbite Gray 800 (#1f2937)
 * - Secondary Surfaces & Form Inputs: Flowbite Gray 700 (#374151)
 * - Borders & Dividers: Flowbite Gray 700 (#374151) and Gray 600 (#4b5563)
 * - Primary Action Brand: Flowbite Blue 600 (#2563eb / hover #1d4ed8)
 * - Text: High-contrast white headers (#ffffff), body text (#e5e7eb), muted text (#9ca3af)
 */
public class DarkTheme implements ThemeDefinition {

    private static final DarkTheme INSTANCE = new DarkTheme();

    public static DarkTheme getInstance() {
        return INSTANCE;
    }

    @Override
    public String getThemeName() {
        return "DarkTheme";
    }

    @Override
    public ThemeTokens tokens(ColorMode mode) {
        return getTokens(mode);
    }

    public static ThemeTokens getTokens(ColorMode mode) {
        if (mode == ColorMode.WHITE) {
            return new ThemeTokens(
                "#f9fafb",                  // surfaceBackground: Flowbite Gray 50
                "#ffffff",                  // cardBackground: Pure White
                "#111827",                  // textPrimary: Flowbite Gray 900 (WCAG contrast > 17:1)
                "#4b5563",                  // textSecondary: Flowbite Gray 600 (WCAG contrast > 7:1)
                "#e5e7eb",                  // border: Flowbite Gray 200
                "#2563eb",                  // accentPrimary: Flowbite Blue 600 (WCAG contrast > 4.6:1)
                "#1d4ed8",                  // accentSecondary: Flowbite Blue 700
                "rgba(37, 99, 235, 0.35)",  // focusRing
                "#111827"                   // iconColor
            );
        } else {
            return new ThemeTokens(
                "#111827",                  // surfaceBackground: Flowbite Gray 900
                "#1f2937",                  // cardBackground: Flowbite Gray 800
                "#f9fafb",                  // textPrimary: Crisp white-slate text (WCAG contrast > 16:1)
                "#9ca3af",                  // textSecondary: Flowbite Gray 400 (WCAG contrast > 6.7:1)
                "#374151",                  // border: Flowbite Gray 700
                "#2563eb",                  // accentPrimary: Flowbite Blue 600
                "#3b82f6",                  // accentSecondary: Flowbite Blue 500
                "rgba(37, 99, 235, 0.5)",   // focusRing
                "#f9fafb"                   // iconColor
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
                "border: none; border-radius: 8px; padding: 10px 20px; font-weight: 500; font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; color: #ffffff; background-color: #2563eb; box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05); cursor: pointer; transition: background-color 0.2s ease, box-shadow 0.2s ease;",
                "border: 1px solid #e5e7eb; border-radius: 8px; padding: 24px; background-color: #ffffff; box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.08); color: " + tok.textPrimary() + ";",
                "padding: 20px; border-radius: 8px; border: 1px solid #e5e7eb; background-color: #f9fafb;",
                "font-size: 15px; color: " + tok.textPrimary() + "; font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6;",
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
                "#ffffff",
                tok.textPrimary(),
                "border: none; border-radius: 8px; padding: 10px 20px; font-weight: 500; font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; color: #ffffff; background-color: #2563eb; box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05); cursor: pointer; transition: background-color 0.2s ease, box-shadow 0.2s ease;",
                "border: 1px solid #374151; border-radius: 8px; padding: 24px; background-color: #1f2937; box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.3); color: #f9fafb;",
                "padding: 20px; border-radius: 8px; border: 1px solid #374151; background-color: #1f2937;",
                "font-size: 15px; color: #e5e7eb; font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6;",
                Template.CustomCSS,
                Template.CustomJS,
                tok,
                mode
            );
        }
    }

    public static class Template {
        public static final String CustomCSS = "<style>\n"
            + "/* Flowbite Dark Mode Comprehensive Design System */\n"
            + "@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap');\n"
            + "\n"
            + ":root {\n"
            + "  --fb-gray-900: #111827;\n"
            + "  --fb-gray-800: #1f2937;\n"
            + "  --fb-gray-700: #374151;\n"
            + "  --fb-gray-600: #4b5563;\n"
            + "  --fb-gray-400: #9ca3af;\n"
            + "  --fb-gray-200: #e5e7eb;\n"
            + "  --fb-gray-50: #f9fafb;\n"
            + "  --fb-blue-600: #2563eb;\n"
            + "  --fb-blue-700: #1d4ed8;\n"
            + "  --background-color: #111827 !important;\n"
            + "  --surface-color: #1f2937 !important;\n"
            + "  --on-surface-color: #f9fafb !important;\n"
            + "  --on-primary-color: #ffffff !important;\n"
            + "  --primary-color: #2563eb !important;\n"
            + "}\n"
            + "\n"
            + "* {\n"
            + "  box-sizing: border-box;\n"
            + "}\n"
            + "\n"
            + "/* Preserve icon font families */\n"
            + "i, .fa, .fas, .far, .fab, .bi, .material-icons, .material-symbols, [class*=\"fa-\"], [class*=\"bi-\"] {\n"
            + "  font-family: \"Font Awesome 5 Free\", \"FontAwesome\", \"bootstrap-icons\", \"Material Symbols Outlined\" !important;\n"
            + "  font-style: normal !important;\n"
            + "  display: inline-block !important;\n"
            + "}\n"
            + "\n"
            + "/* Flowbite Body & Root Canvas */\n"
            + "html, body {\n"
            + "  background-color: #111827 !important;\n"
            + "  color: #f9fafb !important;\n"
            + "  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif !important;\n"
            + "  margin: 0;\n"
            + "  padding: 0;\n"
            + "}\n"
            + "\n"
            + "/* Flowbite Dark Scrollbar */\n"
            + "::-webkit-scrollbar { width: 8px; height: 8px; }\n"
            + "::-webkit-scrollbar-track { background: #111827; }\n"
            + "::-webkit-scrollbar-thumb { background: #374151; border-radius: 4px; border: 1px solid rgba(255, 255, 255, 0.05); }\n"
            + "::-webkit-scrollbar-thumb:hover { background: #4b5563; }\n"
            + "\n"
            + "/* Flowbite Layout Areas */\n"
            + ".espresso-dashboard, .espresso-center, .espresso-main, .jettra-main-viewport, .professional-center {\n"
            + "  background-color: #111827 !important;\n"
            + "  color: #f9fafb !important;\n"
            + "}\n"
            + ".espresso-top, .jettra-topbar {\n"
            + "  background-color: #1f2937 !important;\n"
            + "  border-bottom: 1px solid #374151 !important;\n"
            + "  color: #ffffff !important;\n"
            + "  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.2) !important;\n"
            + "  z-index: 10;\n"
            + "}\n"
            + ".espresso-left, .jettra-drawer {\n"
            + "  background-color: #1f2937 !important;\n"
            + "  border-right: 1px solid #374151 !important;\n"
            + "  color: #f9fafb !important;\n"
            + "  z-index: 5;\n"
            + "}\n"
            + ".espresso-footer {\n"
            + "  background-color: #1f2937 !important;\n"
            + "  border-top: 1px solid #374151 !important;\n"
            + "  color: #9ca3af !important;\n"
            + "  display: flex;\n"
            + "  justify-content: center;\n"
            + "  align-items: center;\n"
            + "}\n"
            + "\n"
            + "/* Sidebar Navigation (Flowbite Sidebar) */\n"
            + ".sidebar-logo {\n"
            + "  color: #ffffff !important;\n"
            + "  font-weight: 700;\n"
            + "}\n"
            + ".sidebar-category {\n"
            + "  color: #9ca3af !important;\n"
            + "  font-size: 0.75rem;\n"
            + "  font-weight: 700;\n"
            + "  text-transform: uppercase;\n"
            + "  letter-spacing: 0.05em;\n"
            + "}\n"
            + ".professional-left a, .professional-left p, .espresso-left a, .espresso-left p, .widgetlet-item, .widgetlet-header {\n"
            + "  color: #9ca3af !important;\n"
            + "  background-color: transparent !important;\n"
            + "  border-radius: 8px !important;\n"
            + "  padding: 10px 14px !important;\n"
            + "  margin-bottom: 4px;\n"
            + "  transition: background-color 0.2s, color 0.2s;\n"
            + "  font-weight: 500;\n"
            + "}\n"
            + ".professional-left a:hover, .professional-left p:hover, .espresso-left a:hover, .espresso-left p:hover, .widgetlet-item:hover {\n"
            + "  background-color: #374151 !important;\n"
            + "  color: #ffffff !important;\n"
            + "}\n"
            + ".professional-left a.active, .espresso-left a.active {\n"
            + "  background-color: #2563eb !important;\n"
            + "  color: #ffffff !important;\n"
            + "  font-weight: 600;\n"
            + "}\n"
            + ".professional-left i, .espresso-left i, .top-right-section i, .jettra-hamburger {\n"
            + "  color: #9ca3af !important;\n"
            + "}\n"
            + ".professional-left a.active i, .espresso-left a.active i {\n"
            + "  color: #ffffff !important;\n"
            + "}\n"
            + "\n"
            + "/* Flowbite Cards & Containers (Gray 800) */\n"
            + ".espresso-card, .card, .espresso-panel, .panel, .stat-card, .chart-card, .transaction-item,\n"
            + ".oceantheme-main-grid > div, .oceantheme-dashboard-grid > div,\n"
            + ".darktheme-main-grid > div, .darktheme-dashboard-grid > div,\n"
            + "[class*=\"bg-white\"], [class*=\"bg-light\"], .bg-white, .bg-light {\n"
            + "  background-color: #1f2937 !important;\n"
            + "  color: #f9fafb !important;\n"
            + "  border: 1px solid #374151 !important;\n"
            + "  border-radius: 8px !important;\n"
            + "  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.3) !important;\n"
            + "}\n"
            + ".espresso-panel-header {\n"
            + "  background-color: #111827 !important;\n"
            + "  color: #ffffff !important;\n"
            + "  border-bottom: 1px solid #374151 !important;\n"
            + "  font-weight: 600;\n"
            + "  padding: 14px 20px;\n"
            + "}\n"
            + "\n"
            + "/* Typography */\n"
            + "h1, h2, h3, h4, h5, h6, .top-dashboard-title, .stat-value, .chart-card-header, .tx-title, strong, b {\n"
            + "  color: #ffffff !important;\n"
            + "  font-weight: 700 !important;\n"
            + "}\n"
            + "p, span, label, td, li, .tx-details, .stat-header, .tx-date {\n"
            + "  color: #e5e7eb !important;\n"
            + "}\n"
            + "small, .text-muted {\n"
            + "  color: #9ca3af !important;\n"
            + "}\n"
            + ".text-dark, .text-black, [class*=\"text-dark\"], [class*=\"text-black\"] {\n"
            + "  color: #f9fafb !important;\n"
            + "}\n"
            + "\n"
            + "/* Flowbite Form Inputs (Gray 700 with Gray 600 border) */\n"
            + ".espresso-textfield, .espresso-textarea, .espresso-select, .espresso-input, .espresso-inputnumber,\n"
            + ".form-control, .form-select, input, textarea, select {\n"
            + "  background-color: #374151 !important;\n"
            + "  color: #ffffff !important;\n"
            + "  border: 1px solid #4b5563 !important;\n"
            + "  border-radius: 8px !important;\n"
            + "  padding: 10px 14px;\n"
            + "  font-size: 14px;\n"
            + "  transition: border-color 0.2s, box-shadow 0.2s;\n"
            + "}\n"
            + ".espresso-textfield:focus, .espresso-textarea:focus, .espresso-select:focus, .espresso-input:focus,\n"
            + ".form-control:focus, input:focus, textarea:focus, select:focus {\n"
            + "  border-color: #3b82f6 !important;\n"
            + "  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.3) !important;\n"
            + "  outline: none !important;\n"
            + "}\n"
            + "input::placeholder, textarea::placeholder {\n"
            + "  color: #9ca3af !important;\n"
            + "}\n"
            + "option, select option {\n"
            + "  background-color: #374151 !important;\n"
            + "  color: #ffffff !important;\n"
            + "}\n"
            + "\n"
            + "/* Flowbite Buttons (Blue 600 primary) */\n"
            + ".espresso-button, .espresso-btn, .btn, .btn-primary {\n"
            + "  background-color: #2563eb !important;\n"
            + "  color: #ffffff !important;\n"
            + "  border: none !important;\n"
            + "  border-radius: 8px !important;\n"
            + "  padding: 10px 20px !important;\n"
            + "  font-weight: 500 !important;\n"
            + "  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05) !important;\n"
            + "  cursor: pointer;\n"
            + "  transition: background-color 0.2s ease;\n"
            + "}\n"
            + ".espresso-button:hover, .espresso-btn:hover, .btn:hover, .btn-primary:hover {\n"
            + "  background-color: #1d4ed8 !important;\n"
            + "}\n"
            + ".espresso-btn-secondary {\n"
            + "  background-color: #374151 !important;\n"
            + "  color: #e5e7eb !important;\n"
            + "  border: 1px solid #4b5563 !important;\n"
            + "  border-radius: 8px !important;\n"
            + "}\n"
            + ".espresso-btn-secondary:hover {\n"
            + "  background-color: #4b5563 !important;\n"
            + "  color: #ffffff !important;\n"
            + "}\n"
            + ".espresso-btn-outlined, .btn-outline-primary {\n"
            + "  background-color: transparent !important;\n"
            + "  color: #60a5fa !important;\n"
            + "  border: 1px solid #2563eb !important;\n"
            + "  border-radius: 8px !important;\n"
            + "  padding: 10px 20px !important;\n"
            + "  font-weight: 500 !important;\n"
            + "  transition: all 0.2s ease;\n"
            + "}\n"
            + ".espresso-btn-outlined:hover, .btn-outline-primary:hover {\n"
            + "  background-color: #2563eb !important;\n"
            + "  color: #ffffff !important;\n"
            + "}\n"
            + "\n"
            + "/* Flowbite Tables (Gray 800 with Gray 700 header) */\n"
            + ".espresso-datatable, .espresso-table, .table, table {\n"
            + "  background-color: #1f2937 !important;\n"
            + "  color: #f9fafb !important;\n"
            + "  border: 1px solid #374151 !important;\n"
            + "  border-radius: 8px !important;\n"
            + "  overflow: hidden;\n"
            + "}\n"
            + ".espresso-datatable th, .espresso-table th, .table th, thead th {\n"
            + "  background-color: #374151 !important;\n"
            + "  color: #9ca3af !important;\n"
            + "  border-bottom: 1px solid #4b5563 !important;\n"
            + "  font-weight: 600;\n"
            + "  text-transform: uppercase;\n"
            + "  font-size: 0.75rem;\n"
            + "  letter-spacing: 0.05em;\n"
            + "  padding: 12px 16px;\n"
            + "}\n"
            + ".espresso-datatable td, .espresso-table td, .table td, tbody td {\n"
            + "  border-bottom: 1px solid #374151 !important;\n"
            + "  color: #e5e7eb !important;\n"
            + "  background-color: transparent !important;\n"
            + "  padding: 14px 16px;\n"
            + "}\n"
            + ".espresso-datatable tr:hover td, .espresso-table tr:hover td, .table tr:hover td {\n"
            + "  background-color: #2e3b4e !important;\n"
            + "}\n"
            + "\n"
            + "/* Flowbite Badges */\n"
            + ".espresso-chip, .espresso-badge, .badge, .espresso-tag {\n"
            + "  background-color: #1e3a8a !important;\n"
            + "  color: #93c5fd !important;\n"
            + "  border: 1px solid #1d4ed8 !important;\n"
            + "  border-radius: 6px !important;\n"
            + "  padding: 3px 8px;\n"
            + "  font-weight: 500;\n"
            + "  font-size: 0.75rem;\n"
            + "}\n"
            + ".badge.qualified {\n"
            + "  background-color: #064e3b !important;\n"
            + "  color: #6ee7b7 !important;\n"
            + "  border: 1px solid #059669 !important;\n"
            + "}\n"
            + ".badge.unqualified {\n"
            + "  background-color: #7f1d1d !important;\n"
            + "  color: #fca5a5 !important;\n"
            + "  border: 1px solid #dc2626 !important;\n"
            + "}\n"
            + "\n"
            + "/* Flowbite Modals & Dialogs */\n"
            + ".espresso-modal, .espresso-dialog, .modal-content, .modal, .j-sync-content {\n"
            + "  background-color: #1f2937 !important;\n"
            + "  border: 1px solid #374151 !important;\n"
            + "  border-radius: 12px !important;\n"
            + "  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.5) !important;\n"
            + "  color: #f9fafb !important;\n"
            + "}\n"
            + "\n"
            + "/* Flowbite Dropdowns & Menus */\n"
            + ".espresso-overlaymenu, .espresso-dropdown-panel, .dropdown-menu {\n"
            + "  background-color: #1f2937 !important;\n"
            + "  border: 1px solid #374151 !important;\n"
            + "  border-radius: 8px !important;\n"
            + "  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.4) !important;\n"
            + "  color: #e5e7eb !important;\n"
            + "}\n"
            + ".espresso-overlaymenu a, .espresso-dropdown-panel a, .dropdown-item {\n"
            + "  color: #e5e7eb !important;\n"
            + "  padding: 10px 16px;\n"
            + "  border-radius: 6px;\n"
            + "  transition: background-color 0.15s ease;\n"
            + "}\n"
            + ".espresso-overlaymenu a:hover, .espresso-dropdown-panel a:hover, .dropdown-item:hover {\n"
            + "  background-color: #374151 !important;\n"
            + "  color: #ffffff !important;\n"
            + "}\n"
            + "\n"
            + "/* Tabs, Accordions, Dividers */\n"
            + ".espresso-tabview-nav { border-bottom: 2px solid #374151 !important; }\n"
            + ".espresso-tabview-nav-link { color: #9ca3af !important; }\n"
            + ".espresso-tabview-nav-link:hover { color: #ffffff !important; }\n"
            + ".espresso-tabview-nav-active .espresso-tabview-nav-link { color: #2563eb !important; border-bottom: 2px solid #2563eb !important; }\n"
            + ".espresso-accordion-header { background-color: #111827 !important; color: #f9fafb !important; border: 1px solid #374151 !important; }\n"
            + ".espresso-accordion-content { background-color: #1f2937 !important; color: #f9fafb !important; border: 1px solid #374151 !important; }\n"
            + ".espresso-divider-content { background-color: #1f2937 !important; color: #9ca3af !important; }\n"
            + ".espresso-divider { border-color: #374151 !important; }\n"
            + ".progress-track, .espresso-skeleton { background-color: #374151 !important; }\n"
            + "</style>";

        public static final String CustomJS = "<script>\n"
            + "function toggleSidebar() {\n"
            + "  var sidebar = document.querySelector('.espresso-left');\n"
            + "  if(sidebar) sidebar.classList.toggle('open');\n"
            + "}\n"
            + "</script>";
    }

    public static class DashboardPage {
        public static final String CustomCSS = "<style>\n"
            + ".darktheme-dashboard-grid, .oceantheme-dashboard-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 1.5rem; width: 100%; }\n"
            + ".darktheme-main-grid, .oceantheme-main-grid { display: grid; grid-template-columns: 2fr 1fr; gap: 1.5rem; width: 100%; margin-top: 1.5rem; }\n"
            + "@media (max-width: 992px) { .darktheme-dashboard-grid, .darktheme-main-grid, .oceantheme-dashboard-grid, .oceantheme-main-grid { grid-template-columns: 1fr; } }\n"
            + ".stat-card { display: flex; flex-direction: column; gap: 10px; background: #1f2937 !important; border: 1px solid #374151 !important; border-radius: 8px !important; box-shadow: 0 1px 3px 0 rgba(0,0,0,0.3) !important; padding: 22px; }\n"
            + ".stat-header { font-size: 0.85rem; color: #9ca3af !important; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; }\n"
            + ".stat-value { font-size: 2rem; font-weight: 700; color: #ffffff !important; }\n"
            + ".stat-badge { padding: 4px 10px; border-radius: 6px; font-size: 0.75rem; font-weight: 500; display: inline-flex; align-items: center; gap: 4px; }\n"
            + ".stat-badge.down { background-color: #7f1d1d !important; color: #fca5a5 !important; border: 1px solid #dc2626 !important; }\n"
            + ".stat-badge.up { background-color: #064e3b !important; color: #6ee7b7 !important; border: 1px solid #059669 !important; }\n"
            + "</style>";
    }
}
