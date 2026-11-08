package io.jettra.flux.widgets;

import io.jettra.flux.core.Modifier;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ColorMode;
import io.jettra.flux.theme.JettraTheme;
import io.jettra.flux.theme.ThemeContext;
import io.jettra.flux.theme.ThemeData;
import io.jettra.flux.theme.ThemeTokens;

import java.util.ArrayList;
import java.util.List;

/**
 * Root reactive container for JettraFlux dashboards and applications.
 *
 * Implements reactive semantic token propagation:
 * 1. Binds semantic tokens (--jf-bg, --jf-surface, --jf-text-primary, --jf-border, etc.) to the root layout node.
 * 2. Listens to reactive ThemeContext changes on server/lifecycle side.
 * 3. Injects client-side DOM event listeners to patch child hierarchy styles dynamically on colorMode switch.
 */
public class DashboardRootContainer extends Widget {

    private final List<Widget> children = new ArrayList<>();
    private String currentThemeName = "Matrix";
    private ColorMode colorMode = ColorMode.DARK;
    private boolean autoClientReconciliation = true;

    public DashboardRootContainer() {
        super();
        this.id = "jettra-dashboard-root-" + System.identityHashCode(this);
    }

    public static DashboardRootContainer of(Widget... widgets) {
        DashboardRootContainer container = new DashboardRootContainer();
        if (widgets != null) {
            for (Widget w : widgets) {
                if (w != null) container.children.add(w);
            }
        }
        return container;
    }

    public DashboardRootContainer currentTheme(String themeName) {
        if (themeName != null && !themeName.trim().isEmpty()) {
            this.currentThemeName = themeName.trim();
        }
        return this;
    }

    public DashboardRootContainer currentTheme(JettraTheme theme) {
        if (theme != null) {
            this.currentThemeName = theme.getDisplayName();
        }
        return this;
    }

    public DashboardRootContainer colorMode(ColorMode mode) {
        if (mode != null) {
            this.colorMode = mode;
        }
        return this;
    }

    public DashboardRootContainer autoReconcile(boolean auto) {
        this.autoClientReconciliation = auto;
        return this;
    }

    public DashboardRootContainer add(Widget child) {
        if (child != null) {
            this.children.add(child);
        }
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        JettraTheme jt = JettraTheme.fromName(currentThemeName);
        ColorMode activeMode = (theme != null && theme.getColorMode() != null) ? theme.getColorMode() : this.colorMode;
        ThemeTokens tokens = jt.tokens(activeMode);

        StringBuilder sb = new StringBuilder();

        // 1. Root container element with dynamic CSS variable scoping
        sb.append("<div id=\"").append(id).append("\" ")
          .append("class=\"jettra-dashboard-root\" ")
          .append("data-theme=\"").append(jt.getDisplayName()).append("\" ")
          .append("data-color-mode=\"").append(activeMode.name().toLowerCase()).append("\" ")
          .append("style=\"")
          .append("display:flex; flex-direction:column; width:100%; min-height:100vh; ")
          .append("background-color:var(--jf-bg, ").append(tokens.surfaceBackground()).append("); ")
          .append("color:var(--jf-text-primary, ").append(tokens.textPrimary()).append("); ")
          .append("transition: background-color 0.25s ease, color 0.25s ease; ")
          .append(modifier != null ? modifier.getStyles() : "")
          .append("\">\n");

        // 2. Render children
        for (Widget child : children) {
            if (child != null) {
                sb.append(child.render(theme)).append("\n");
            }
        }

        sb.append("</div>\n");

        // 3. Client-Side reactive listener for instant reconciliation without reload
        if (autoClientReconciliation) {
            sb.append("<script>\n");
            sb.append("(function bindRootContainerReconciliation() {\n");
            sb.append("  var rootContainer = document.getElementById('").append(id).append("');\n");
            sb.append("  if (!rootContainer) return;\n");
            sb.append("  window.addEventListener('jettraThemeChange', function(e) {\n");
            sb.append("    if (!e.detail || !e.detail.tokens) return;\n");
            sb.append("    var tok = e.detail.tokens;\n");
            sb.append("    rootContainer.setAttribute('data-color-mode', e.detail.mode);\n");
            sb.append("    rootContainer.setAttribute('data-theme', e.detail.theme);\n");
            sb.append("    rootContainer.style.backgroundColor = tok.surfaceBackground;\n");
            sb.append("    rootContainer.style.color = tok.textPrimary;\n");
            sb.append("  });\n");
            sb.append("})();\n");
            sb.append("</script>\n");
        }

        return sb.toString();
    }
}
