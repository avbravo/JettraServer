package io.jettra.flux.theme;

public class RetroTheme implements ThemeDefinition {

    private static final RetroTheme INSTANCE = new RetroTheme();

    public static RetroTheme getInstance() {
        return INSTANCE;
    }

    @Override
    public String getThemeName() {
        return "Retro";
    }

    @Override
    public ThemeTokens tokens(ColorMode mode) {
        return getTokens(mode);
    }

    public static ThemeTokens getTokens(ColorMode mode) {
        if (mode == ColorMode.WHITE) {
            return new ThemeTokens(
                "#fef3c7",                  // surfaceBackground: warm light pixel parchment
                "#ffffff",                  // cardBackground: pure white
                "#451a03",                  // textPrimary: deep pixel brown (WCAG contrast > 13:1)
                "#78350f",                  // textSecondary: warm amber (WCAG contrast > 8:1)
                "#d97706",                  // border: pixel amber border
                "#3f6212",                  // accentPrimary: deep retro forest green (WCAG contrast > 5:1)
                "#b45309",                  // accentSecondary: pixel gold
                "rgba(63, 98, 18, 0.35)",   // focusRing
                "#3f6212"                   // iconColor
            );
        } else {
            return new ThemeTokens(
                "#242220",                  // surfaceBackground: Stone/Dirt dark background
                "#343236",                  // cardBackground: Cobblestone/Furnace surface
                "#f0f0f0",                  // textPrimary: Light text (WCAG contrast > 13:1)
                "#d49a3d",                  // textSecondary: Gold (WCAG contrast > 7:1)
                "#18171a",                  // border: dark pixel outline
                "#5c8e32",                  // accentPrimary: Minecraft Grass Green
                "#d49a3d",                  // accentSecondary: Gold
                "rgba(92, 142, 50, 0.4)",   // focusRing
                "#5c8e32"                   // iconColor
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
                "border: 2px solid #451a03; border-radius: 0px; padding: 10px 20px; font-weight: bold; font-family: 'Pixelify Sans', 'Silkscreen', 'VT323', monospace; color: #ffffff; background-color: #3f6212; box-shadow: 2px 2px 0px #451a03; cursor: pointer; text-transform: uppercase;",
                "border: 3px solid #d97706; border-radius: 0px; padding: 20px; background-color: #ffffff; box-shadow: 3px 3px 0px rgba(0,0,0,0.1); color: " + tok.textPrimary() + ";",
                "padding: 16px; border-radius: 0px; border: 2px solid #d97706; background-color: #fef3c7;",
                "font-size: 16px; color: " + tok.textPrimary() + "; font-family: 'Pixelify Sans', 'Silkscreen', 'VT323', monospace;",
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
                "border: 2px solid #000; border-radius: 0px; padding: 10px 20px; font-weight: bold; font-family: 'Pixelify Sans', 'Silkscreen', 'VT323', monospace; color: #ffffff; background-color: #706e71; box-shadow: inset 2px 2px 0px #9c9a9d, inset -2px -2px 0px #3c3a3d; cursor: pointer; text-transform: uppercase;",
                "border: 4px solid #18171a; border-radius: 0px; padding: 20px; background-color: #38363c; box-shadow: inset 3px 3px 0px #5c5963, inset -3px -3px 0px #201f23; color: #f0f0f0;",
                "padding: 16px; border-radius: 0px; border: 2px solid #18171a; background-color: #242220;",
                "font-size: 16px; color: #f0f0f0; font-family: 'Pixelify Sans', 'Silkscreen', 'VT323', monospace; text-shadow: 1px 1px 0px #000;",
                Template.CustomCSS,
                Template.CustomJS,
                tok,
                mode
            );
        }
    }

    public static final String FONT_IMPORT = "@import url('https://fonts.googleapis.com/css2?family=Pixelify+Sans:wght@400;500;600;700&family=Silkscreen:wght@400;700&family=VT323&display=swap');\n";

    public static final String GlobalCSS = FONT_IMPORT
        + "/* --- RETRO MINECRAFT THEME STYLES --- */\n"
        + ":root {\n"
        + "  --primary-color: #5c8e32;\n"
        + "  --secondary-color: #d49a3d;\n"
        + "  --background-color: #242220;\n"
        + "  --surface-color: #343236;\n"
        + "  --on-primary-color: #ffffff;\n"
        + "  --on-surface-color: #f0f0f0;\n"
        + "  --text-color: #f0f0f0;\n"
        + "  --text-color-secondary: #d49a3d;\n"
        + "  --border-color: #1a191a;\n"
        + "  --mc-stone: #706e71;\n"
        + "  --mc-stone-light: #9c9a9d;\n"
        + "  --mc-stone-dark: #3c3a3d;\n"
        + "  --mc-dirt: #866043;\n"
        + "  --mc-grass: #5c8e32;\n"
        + "  --mc-gold: #d49a3d;\n"
        + "  --mc-diamond: #4dedf4;\n"
        + "  --mc-emerald: #55ff55;\n"
        + "  --mc-redstone: #e53935;\n"
        + "  --mc-obsidian: #181424;\n"
        + "}\n"
        + "body, button, input, textarea, select, table, th, td, h1, h2, h3, h4, h5, h6, p, span, a, label, div {\n"
        + "  font-family: 'Pixelify Sans', 'Silkscreen', 'VT323', monospace !important;\n"
        + "  image-rendering: pixelated;\n"
        + "}\n"
        + "body {\n"
        + "  background-color: #242220 !important;\n"
        + "  background-image: radial-gradient(#2d2a27 15%, transparent 16%), radial-gradient(#1c1a18 15%, transparent 16%);\n"
        + "  background-size: 16px 16px;\n"
        + "  background-position: 0 0, 8px 8px;\n"
        + "  color: #f0f0f0 !important;\n"
        + "}\n"
        + "/* Minecraft Pixelated Scrollbars */\n"
        + "* {\n"
        + "  scrollbar-width: thin;\n"
        + "  scrollbar-color: #706e71 #1c1a18;\n"
        + "}\n"
        + "::-webkit-scrollbar { width: 12px; height: 12px; }\n"
        + "::-webkit-scrollbar-track { background: #1c1a18; border: 1px solid #0e0d0c; }\n"
        + "::-webkit-scrollbar-thumb { background: #706e71; border: 2px solid; border-color: #9c9a9d #3c3a3d #3c3a3d #9c9a9d; }\n"
        + "::-webkit-scrollbar-thumb:hover { background: #88858a; border-color: #b5b3b7 #4a474d #4a474d #b5b3b7; }\n"
        + "/* Headings */\n"
        + "h1, h2, h3, h4, h5, h6 {\n"
        + "  color: #55ff55 !important;\n"
        + "  text-shadow: 2px 2px 0px #111111;\n"
        + "  letter-spacing: 0.5px;\n"
        + "}\n"
        + "/* Dashboard Layout (Minecraft UI) */\n"
        + ".espresso-dashboard {\n"
        + "  background-color: #242220 !important;\n"
        + "}\n"
        + ".espresso-top {\n"
        + "  background: #2e2823 !important;\n"
        + "  border-bottom: 4px solid #181512 !important;\n"
        + "  box-shadow: inset 0 -3px 0 #453c35, 0 4px 10px rgba(0,0,0,0.6) !important;\n"
        + "  color: #ffffff !important;\n"
        + "}\n"
        + ".espresso-left {\n"
        + "  background: #252322 !important;\n"
        + "  border-right: 4px solid #141312 !important;\n"
        + "  box-shadow: inset -4px 0 0 #3a3735 !important;\n"
        + "  color: #f0f0f0 !important;\n"
        + "}\n"
        + ".espresso-center {\n"
        + "  background-color: #242220 !important;\n"
        + "  color: #f0f0f0 !important;\n"
        + "}\n"
        + ".espresso-footer {\n"
        + "  background-color: #2e2823 !important;\n"
        + "  border-top: 3px solid #181512 !important;\n"
        + "  color: #d49a3d !important;\n"
        + "  text-shadow: 1px 1px 0px #000;\n"
        + "}\n"
        + "/* Navigation Links */\n"
        + ".professional-left a, .professional-left p {\n"
        + "  border-radius: 0px !important;\n"
        + "  border: 2px solid transparent !important;\n"
        + "  color: #e0e0e0 !important;\n"
        + "  transition: none !important;\n"
        + "  text-shadow: 1px 1px 0px #000;\n"
        + "  margin-bottom: 4px;\n"
        + "}\n"
        + ".professional-left a:hover, .professional-left p:hover {\n"
        + "  background-color: #4a4642 !important;\n"
        + "  border: 2px solid #6c6762 !important;\n"
        + "  color: #ffff55 !important;\n"
        + "  box-shadow: inset 2px 2px 0 #7e7873, inset -2px -2px 0 #282624 !important;\n"
        + "}\n"
        + ".professional-left a.active {\n"
        + "  background-color: #5c8e32 !important;\n"
        + "  border: 2px solid #82bd48 !important;\n"
        + "  color: #ffffff !important;\n"
        + "  box-shadow: inset 2px 2px 0 #9ddb5e, inset -2px -2px 0 #3b5c20 !important;\n"
        + "  text-shadow: 2px 2px 0px #203510;\n"
        + "}\n"
        + ".sidebar-logo {\n"
        + "  color: #55ff55 !important;\n"
        + "  text-shadow: 2px 2px 0px #1a3a1a !important;\n"
        + "  letter-spacing: 1px;\n"
        + "}\n"
        + ".sidebar-category {\n"
        + "  color: #d49a3d !important;\n"
        + "  text-shadow: 1px 1px 0px #000;\n"
        + "}\n"
        + "/* Minecraft Stone & Wood Buttons */\n"
        + ".espresso-button, .espresso-btn, .btn, .btn-primary, .top-btn-today {\n"
        + "  font-family: 'Pixelify Sans', 'Silkscreen', monospace !important;\n"
        + "  font-size: 15px !important;\n"
        + "  font-weight: 700 !important;\n"
        + "  color: #ffffff !important;\n"
        + "  background: #706e71 !important;\n"
        + "  border: 3px solid !important;\n"
        + "  border-color: #9c9a9d #3c3a3d #3c3a3d #9c9a9d !important;\n"
        + "  box-shadow: inset -3px -3px 0px #2a282a, inset 3px 3px 0px #b8b6b9, 2px 2px 0px #000000 !important;\n"
        + "  text-shadow: 2px 2px 0px #222222 !important;\n"
        + "  border-radius: 0px !important;\n"
        + "  padding: 10px 20px !important;\n"
        + "  cursor: pointer !important;\n"
        + "  text-transform: uppercase !important;\n"
        + "  image-rendering: pixelated !important;\n"
        + "  transition: none !important;\n"
        + "}\n"
        + ".espresso-button:hover, .espresso-btn:hover, .btn:hover, .btn-primary:hover, .top-btn-today:hover {\n"
        + "  background: #88858a !important;\n"
        + "  border-color: #b5b3b7 #4a474d #4a474d #b5b3b7 !important;\n"
        + "  color: #ffffa0 !important;\n"
        + "}\n"
        + ".espresso-button:active, .espresso-btn:active, .btn:active, .btn-primary:active, .top-btn-today:active {\n"
        + "  box-shadow: inset 3px 3px 0px #2a282a, inset -3px -3px 0px #b8b6b9 !important;\n"
        + "  transform: translate(1px, 1px) !important;\n"
        + "}\n"
        + "/* Button Severities */\n"
        + ".espresso-btn-primary { background: #5c8e32 !important; border-color: #82bd48 #3b5c20 #3b5c20 #82bd48 !important; box-shadow: inset -3px -3px 0 #284015, inset 3px 3px 0 #a2de62, 2px 2px 0 #000 !important; }\n"
        + ".espresso-btn-secondary { background: #706e71 !important; border-color: #9c9a9d #3c3a3d #3c3a3d #9c9a9d !important; }\n"
        + ".espresso-btn-success { background: #2e7d32 !important; border-color: #4caf50 #1b5e20 #1b5e20 #4caf50 !important; box-shadow: inset -3px -3px 0 #134216, inset 3px 3px 0 #81c784, 2px 2px 0 #000 !important; }\n"
        + ".espresso-btn-danger { background: #b71c1c !important; border-color: #e53935 #7f0000 #7f0000 #e53935 !important; box-shadow: inset -3px -3px 0 #570000, inset 3px 3px 0 #ef5350, 2px 2px 0 #000 !important; }\n"
        + ".espresso-btn-warning { background: #c67d0a !important; border-color: #f59e0b #8c5300 #8c5300 #f59e0b !important; box-shadow: inset -3px -3px 0 #573300, inset 3px 3px 0 #fbbf24, 2px 2px 0 #000 !important; }\n"
        + ".espresso-btn-info { background: #0284c7 !important; border-color: #38bdf8 #0369a1 #0369a1 #38bdf8 !important; box-shadow: inset -3px -3px 0 #024368, inset 3px 3px 0 #7dd3fc, 2px 2px 0 #000 !important; }\n"
        + ".espresso-btn-help { background: #7e22ce !important; border-color: #a855f7 #581c87 #581c87 #a855f7 !important; box-shadow: inset -3px -3px 0 #3b1261, inset 3px 3px 0 #c084fc, 2px 2px 0 #000 !important; }\n"
        + "/* Outlined Buttons */\n"
        + ".espresso-btn-outlined { background: transparent !important; border: 2px solid !important; border-radius: 0px !important; box-shadow: none !important; }\n"
        + "/* Minecraft Crafting Table / Inventory GUI Cards */\n"
        + ".espresso-card, .card, .stat-card {\n"
        + "  background-color: #343236 !important;\n"
        + "  border: 4px solid #181719 !important;\n"
        + "  box-shadow: inset 4px 4px 0px #545158, inset -4px -4px 0px #222124, 4px 4px 0px rgba(0,0,0,0.6) !important;\n"
        + "  border-radius: 0px !important;\n"
        + "  padding: 20px !important;\n"
        + "  color: #f0f0f0 !important;\n"
        + "}\n"
        + ".stat-value {\n"
        + "  color: #55ff55 !important;\n"
        + "  text-shadow: 2px 2px 0px #0e2a0e !important;\n"
        + "}\n"
        + ".stat-header {\n"
        + "  color: #d49a3d !important;\n"
        + "  text-shadow: 1px 1px 0px #000 !important;\n"
        + "}\n"
        + "/* Minecraft Inventory Sunken Input Slots */\n"
        + ".espresso-textfield, .espresso-textarea, .espresso-select, .espresso-input, .espresso-inputnumber, .form-control, .form-select, input[type=\"text\"], input[type=\"number\"], input[type=\"password\"], input[type=\"email\"], textarea, select {\n"
        + "  background-color: #121214 !important;\n"
        + "  color: #55ff55 !important;\n"
        + "  border: 3px solid #000000 !important;\n"
        + "  box-shadow: inset 3px 3px 0px #1e1e22, inset -3px -3px 0px #3a3a42 !important;\n"
        + "  border-radius: 0px !important;\n"
        + "  padding: 10px 14px !important;\n"
        + "  font-size: 15px !important;\n"
        + "  outline: none !important;\n"
        + "  transition: none !important;\n"
        + "}\n"
        + ".espresso-textfield:focus, .espresso-textarea:focus, .espresso-select:focus, .espresso-input:focus, input:focus, textarea:focus, select:focus {\n"
        + "  border-color: #55ff55 !important;\n"
        + "  box-shadow: 0 0 0 2px #55ff55, inset 3px 3px 0px #000000 !important;\n"
        + "}\n"
        + ".espresso-textfield::placeholder, .espresso-textarea::placeholder, input::placeholder, textarea::placeholder {\n"
        + "  color: #706e71 !important;\n"
        + "}\n"
        + "/* Minecraft Inventory Matrix Tables */\n"
        + ".espresso-table, .espresso-datatable, .table {\n"
        + "  background-color: #343236 !important;\n"
        + "  color: #f0f0f0 !important;\n"
        + "  border: 3px solid #181719 !important;\n"
        + "  border-radius: 0px !important;\n"
        + "  border-collapse: separate !important;\n"
        + "  border-spacing: 0 !important;\n"
        + "}\n"
        + ".espresso-table th, .espresso-datatable th, .table th {\n"
        + "  background-color: #262428 !important;\n"
        + "  color: #ffff55 !important;\n"
        + "  border-bottom: 3px solid #181719 !important;\n"
        + "  border-right: 1px solid #181719 !important;\n"
        + "  padding: 12px 16px !important;\n"
        + "  text-shadow: 1px 1px 0px #000;\n"
        + "  text-transform: uppercase;\n"
        + "}\n"
        + ".espresso-table td, .espresso-datatable td, .table td {\n"
        + "  border-bottom: 2px solid #262428 !important;\n"
        + "  border-right: 1px solid #262428 !important;\n"
        + "  color: #e0e0e0 !important;\n"
        + "  background-color: #343236 !important;\n"
        + "  padding: 10px 16px !important;\n"
        + "}\n"
        + ".espresso-table tr:hover td, .espresso-datatable tr:hover td, .table tr:hover td {\n"
        + "  background-color: #454249 !important;\n"
        + "  color: #ffffaa !important;\n"
        + "}\n"
        + "/* Minecraft Chest / GUI Windows (Modals, Dialogs, Menus) */\n"
        + ".espresso-modal, .espresso-dialog, .espresso-overlay-menu, .modal-content, .ui-dialog {\n"
        + "  background-color: #38363c !important;\n"
        + "  border: 5px solid #18171a !important;\n"
        + "  box-shadow: inset 4px 4px 0px #5c5963, inset -4px -4px 0px #201f23, 0 10px 30px rgba(0,0,0,0.8) !important;\n"
        + "  border-radius: 0px !important;\n"
        + "  color: #f0f0f0 !important;\n"
        + "}\n"
        + ".espresso-overlay-menu a, .espresso-overlay-menu div {\n"
        + "  border-radius: 0px !important;\n"
        + "  color: #f0f0f0 !important;\n"
        + "  text-shadow: 1px 1px 0px #000;\n"
        + "}\n"
        + ".espresso-overlay-menu a:hover {\n"
        + "  background-color: #5c8e32 !important;\n"
        + "  color: #ffffff !important;\n"
        + "}\n"
        + "/* Badges & Tags (Minecraft Item EXP Badges) */\n"
        + ".espresso-badge, .espresso-tag, .espresso-chip, .stat-badge {\n"
        + "  border-radius: 0px !important;\n"
        + "  border: 2px solid #000000 !important;\n"
        + "  box-shadow: inset 1px 1px 0px rgba(255,255,255,0.4), inset -1px -1px 0px rgba(0,0,0,0.5) !important;\n"
        + "  font-weight: 700 !important;\n"
        + "  text-shadow: 1px 1px 0px #000 !important;\n"
        + "}\n"
        + ".stat-badge.up { background-color: #5c8e32 !important; color: #ffffff !important; }\n"
        + ".stat-badge.down { background-color: #e53935 !important; color: #ffffff !important; }\n"
        + "/* Minecraft Chat System Alerts */\n"
        + ".espresso-alert {\n"
        + "  border-radius: 0px !important;\n"
        + "  border: 3px solid #000000 !important;\n"
        + "  background-color: rgba(20, 20, 22, 0.95) !important;\n"
        + "  box-shadow: inset 2px 2px 0px #444, 3px 3px 0px rgba(0,0,0,0.6) !important;\n"
        + "}\n"
        + ".espresso-alert-success { border-left: 8px solid #55ff55 !important; color: #55ff55 !important; }\n"
        + ".espresso-alert-danger { border-left: 8px solid #e53935 !important; color: #ff5555 !important; }\n"
        + ".espresso-alert-info { border-left: 8px solid #4dedf4 !important; color: #4dedf4 !important; }\n"
        + "/* Minecraft EXP Level Progress Bar */\n"
        + ".espresso-progressbar, progress {\n"
        + "  border-radius: 0px !important;\n"
        + "  background: #101012 !important;\n"
        + "  border: 2px solid #000000 !important;\n"
        + "  box-shadow: inset 2px 2px 0 #222 !important;\n"
        + "  height: 16px !important;\n"
        + "}\n"
        + ".espresso-progressbar-value {\n"
        + "  background: linear-gradient(180deg, #7fff00 0%, #55ff55 50%, #3db83d 100%) !important;\n"
        + "  box-shadow: inset 0 2px 0 #aaff55, inset 0 -2px 0 #288028 !important;\n"
        + "}\n";

    public static class Template {
        public static final String CustomCSS = "<style>\n"
            + GlobalCSS
            + "/* Retro Template Overrides */\n"
            + ".top-left-section { display: flex; align-items: center; gap: 20px; }\n"
            + ".top-right-section { display: flex; align-items: center; gap: 15px; color: var(--on-surface-color); }\n"
            + ".top-right-section i { font-size: 1.2rem; cursor: pointer; transition: color 0.1s; }\n"
            + ".top-right-section i:hover { color: #55ff55; }\n"
            + ".top-avatar { width: 32px; height: 32px; border-radius: 0px !important; border: 2px solid #000; background-color: #5c8e32; display: flex; justify-content: center; align-items: center; font-weight: bold; color: #ffffff; text-shadow: 1px 1px 0 #000; }\n"
            + ".top-bars-icon { font-size: 1.2rem; cursor: pointer; color: #55ff55; margin-right: 15px; }\n"
            + ".top-dashboard-title { margin: 0; font-weight: 700; font-size: 1.2rem; color: #55ff55 !important; text-shadow: 2px 2px 0 #000; }\n"
            + ".professional-center { height: 100%; display: flex; flex-direction: column; gap: 1.5rem; }\n"
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
            + ".oceantheme-dashboard-grid, .retrotheme-dashboard-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 1.5rem; width: 100%; }\n"
            + ".oceantheme-main-grid, .retrotheme-main-grid { display: grid; grid-template-columns: 2fr 1fr; gap: 1.5rem; width: 100%; margin-top: 1.5rem; }\n"
            + "@media (max-width: 992px) { .oceantheme-dashboard-grid, .oceantheme-main-grid, .retrotheme-dashboard-grid, .retrotheme-main-grid { grid-template-columns: 1fr; } }\n"
            + ".stat-card { display: flex; flex-direction: column; gap: 10px; }\n"
            + ".stat-header { font-size: 0.85rem; color: #d49a3d; font-weight: 700; }\n"
            + ".stat-value { font-size: 1.8rem; font-weight: 700; color: #55ff55; text-shadow: 2px 2px 0 #000; }\n"
            + ".stat-badge { padding: 4px 8px; border-radius: 0px !important; font-size: 0.75rem; font-weight: bold; display: inline-flex; align-items: center; gap: 4px; }\n"
            + ".chart-card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }\n"
            + ".transaction-item { display: flex; align-items: center; justify-content: space-between; padding: 12px 0; border-bottom: 2px solid #28262a; }\n"
            + ".transaction-item:last-child { border-bottom: none; }\n"
            + ".tx-icon { width: 36px; height: 36px; border-radius: 0px !important; border: 2px solid #000; display: flex; justify-content: center; align-items: center; color: white; font-size: 0.9rem; }\n"
            + ".tx-details { flex: 1; margin-left: 15px; }\n"
            + ".tx-title { font-size: 0.9rem; font-weight: 700; color: #f0f0f0; margin: 0 0 4px 0; }\n"
            + ".tx-date { font-size: 0.75rem; color: #d49a3d; margin: 0; }\n"
            + ".tx-amount { font-size: 0.9rem; font-weight: 700; }\n"
            + ".tx-amount.positive { color: #55ff55; text-shadow: 1px 1px 0 #000; }\n"
            + ".tx-amount.negative { color: #e53935; text-shadow: 1px 1px 0 #000; }\n"
            + "</style>";
    }
}
