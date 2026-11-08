package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

public class Scaffold extends Widget {
    private Widget topBar;
    private Widget body;
    private Widget floatingActionButton;

    public Scaffold() {}

    public static Scaffold of() {
        return new Scaffold();
    }

    public Scaffold topBar(Widget topBar) {
        this.topBar = topBar;
        return this;
    }

    public Scaffold body(Widget body) {
        this.body = body;
        return this;
    }

    public Scaffold floatingActionButton(Widget fab) {
        this.floatingActionButton = fab;
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        sb.append(theme.generateGlobalCss());
        
        // CSS for hamburger menu
        sb.append("<style>\n");
        sb.append(".jettra-scaffold-layout { display: flex; flex-direction: column; min-height: 100vh; position: relative; }\n");
        sb.append(".jettra-topbar { width: 100%; box-sizing: border-box; display: flex; align-items: center; padding: 0 16px; }\n");
        sb.append(".jettra-main-viewport { flex: 1; overflow: auto; padding: 16px; margin-top: -10px; box-sizing: border-box; display: flex; justify-content: center; }\n"); // Elevated position for visual balance
        sb.append(".jettra-hamburger { display: none; cursor: pointer; font-size: 24px; margin-right: 16px; }\n");
        sb.append(".jettra-drawer { display: none; flex-direction: column; position: absolute; top: 60px; left: 0; background: white; border: 1px solid #ccc; width: 100%; max-width: 250px; box-sizing: border-box; z-index: 1001; padding: 16px; box-shadow: 2px 0 5px rgba(0,0,0,0.1); }\n");
        sb.append(".jettra-drawer.open { display: flex; }\n");
        sb.append("@media (max-width: 768px) {\n");
        sb.append("  .jettra-hamburger { display: block; }\n");
        sb.append("  .jettra-topbar-content { display: none; }\n"); // Hide default topbar content on mobile
        sb.append("}\n");
        sb.append("</style>\n");
        
        sb.append("<script>\n");
        sb.append("function toggleJettraMenu() {\n");
        sb.append("  var drawer = document.getElementById('jettra-drawer-menu');\n");
        sb.append("  if(drawer.classList.contains('open')) { drawer.classList.remove('open'); }\n");
        sb.append("  else { drawer.classList.add('open'); }\n");
        sb.append("}\n");
        sb.append("</script>\n");

        sb.append("<div ").append(renderCommonAttributes(theme, "espresso-scaffold jettra-scaffold-layout")).append(" ").append(modifier.getStyles()).append(">\n");
        
        if (topBar != null) {
            sb.append("<header class=\"jettra-topbar\">\n");
            sb.append("<div class=\"jettra-hamburger\" onclick=\"toggleJettraMenu()\">&#9776;</div>\n");
            sb.append("<div class=\"jettra-topbar-content\" style=\"width: 100%;\">\n");
            sb.append(topBar.render(theme));
            sb.append("</div>\n");
            sb.append("</header>\n");
            
            // The drawer menu for mobile
            sb.append("<div id=\"jettra-drawer-menu\" class=\"jettra-drawer\">\n");
            sb.append(topBar.render(theme));
            sb.append("</div>\n");
        }
        
        if (body != null) {
            sb.append("<main class=\"jettra-main-viewport\">\n");
            sb.append("<div style=\"width: 100%;\">\n");
            sb.append(body.render(theme));
            sb.append("</div>\n");
            sb.append("</main>\n");
        }
        
        if (floatingActionButton != null) {
            sb.append("<div style=\"position: fixed; bottom: 24px; right: 24px; z-index: 1000;\">\n");
            sb.append(floatingActionButton.render(theme));
            sb.append("</div>\n");
        }
        
        sb.append("</div>\n");
        return sb.toString();
    }
}
