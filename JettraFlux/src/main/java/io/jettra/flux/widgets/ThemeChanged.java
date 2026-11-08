package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

public class ThemeChanged extends Widget {
    
    private String currentTheme = "Matrix"; // default

    private ThemeChanged() {}

    public static ThemeChanged of() {
        return new ThemeChanged();
    }
    
    public ThemeChanged current(String theme) {
        this.currentTheme = theme;
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("<div class=\"espresso-theme-changed\" style=\"display: inline-block;\">\n");
        
        // Trigger ThemeRegistry loading by touching Themes class if not yet loaded
        try { Class.forName("io.jettra.flux.theme.Themes"); } catch (Exception ignored) {}
        
        String[] themes = io.jettra.flux.theme.ThemeRegistry.getAvailableThemeNames();
        
        java.util.Map<String, String> themeIcons = new java.util.HashMap<>();
        themeIcons.put("asttheme", "🪐");
        themeIcons.put("ast", "🪐");
        themeIcons.put("flattheme", "🟦");
        themeIcons.put("flat", "🟦");
        themeIcons.put("theme3d", "🧊");
        themeIcons.put("3d", "🧊");
        themeIcons.put("futuristictheme", "🚀");
        themeIcons.put("futuristic", "🚀");
        themeIcons.put("atlantistheme", "🔱");
        themeIcons.put("atlantis", "🔱");
        themeIcons.put("oceantheme", "🌊");
        themeIcons.put("ocean", "🌊");
        themeIcons.put("matrixtheme", "🟢");
        themeIcons.put("matrix", "🟢");
        themeIcons.put("retrotheme", "🕹️");
        themeIcons.put("retro", "🕹️");
        themeIcons.put("darktheme", "🌙");
        themeIcons.put("dark", "🌙");
        themeIcons.put("heroestheme", "⚡");
        themeIcons.put("heroes", "⚡");
        themeIcons.put("sltheme", "🌐");
        themeIcons.put("sl", "🌐");
        themeIcons.put("coretheme", "⚛️");
        themeIcons.put("core", "⚛️");
        themeIcons.put("policetheme", "🚔");
        themeIcons.put("police", "🚔");
        themeIcons.put("gamestheme", "🎮");
        themeIcons.put("games", "🎮");
        
        String currentLogo = "🎨";
        if (currentTheme != null) {
            String norm = currentTheme.toLowerCase().trim();
            currentLogo = themeIcons.getOrDefault(norm, "🎨");
        }
        
        Widget trigger = Span.of(currentLogo)
                .modifier(new io.jettra.flux.core.Modifier().attribute("title", currentTheme != null ? currentTheme : "Theme").style("cursor:pointer; font-size:1.5rem;"));
        
        WidgetLet[] items = new WidgetLet[themes.length];
        for (int i = 0; i < themes.length; i++) {
            String themeName = themes[i];
            String logo = themeIcons.getOrDefault(themeName.toLowerCase(), "🎨");
            items[i] = WidgetLet.of(themeName + " " + logo)
                    .url("javascript:changeJettraTheme('" + themeName + "')");
        }
        
        Widget menu = ((io.jettra.flux.widgets.OverlayMenu) OverlayMenu.of(items).trigger(trigger)).alignRight();
        
        sb.append(menu.render(theme));
        
        sb.append("  <script>\n");
        sb.append("    function changeJettraTheme(themeName) {\n");
        sb.append("      document.cookie = 'jettra_theme=' + themeName + '; path=/';\n");
        sb.append("      window.location.reload();\n");
        sb.append("    }\n");
        sb.append("  </script>\n");
        sb.append("</div>\n");
        
        return sb.toString();
    }
}
