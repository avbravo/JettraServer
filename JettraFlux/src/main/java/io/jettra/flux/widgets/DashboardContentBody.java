package io.jettra.flux.widgets;

import io.jettra.flux.core.Modifier;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ColorMode;
import io.jettra.flux.theme.JettraTheme;
import io.jettra.flux.theme.ThemeData;
import io.jettra.flux.theme.ThemeTokens;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulated scrollable body container for JettraFlux dashboards.
 *
 * Provides:
 * 1. Global native vertical scrolling (overflow-y: auto; overflow-x: hidden; flex-grow: 1).
 * 2. Full keyboard accessibility (tabindex="0", role="region") enabling keyboard-driven navigation
 *    (ArrowDown, ArrowUp, PageDown, PageUp, Home, End).
 * 3. Semantic scrollbar integration matching the active JettraFlux theme tokens (--jf-accent, --jf-border).
 */
public class DashboardContentBody extends Widget {

    private final List<Widget> children = new ArrayList<>();
    private String ariaLabel = "Dashboard Content Body";

    public DashboardContentBody() {
        super();
        this.id = "jettra-dashboard-body-" + System.identityHashCode(this);
    }

    public static DashboardContentBody of(Widget... widgets) {
        DashboardContentBody body = new DashboardContentBody();
        if (widgets != null) {
            for (Widget w : widgets) {
                if (w != null) body.children.add(w);
            }
        }
        return body;
    }

    public static DashboardContentBody of(List<Widget> widgets) {
        DashboardContentBody body = new DashboardContentBody();
        if (widgets != null) {
            for (Widget w : widgets) {
                if (w != null) body.children.add(w);
            }
        }
        return body;
    }

    public DashboardContentBody add(Widget widget) {
        if (widget != null) {
            this.children.add(widget);
        }
        return this;
    }

    public DashboardContentBody ariaLabel(String ariaLabel) {
        if (ariaLabel != null && !ariaLabel.isBlank()) {
            this.ariaLabel = ariaLabel;
        }
        return this;
    }

    public List<Widget> getChildren() {
        return children;
    }

    @Override
    public String render(ThemeData theme) {
        ThemeTokens tokens = (theme != null) ? theme.getTokens() : null;
        String accentColor = (tokens != null) ? tokens.accentPrimary() : (theme != null && theme.primaryColor != null ? theme.primaryColor : "#0284c7");
        String hoverAccent = (tokens != null) ? tokens.accentSecondary() : (theme != null && theme.secondaryColor != null ? theme.secondaryColor : "#0369a1");

        StringBuilder sb = new StringBuilder();

        // Scoped inline style for webkit scrollbar harmonization
        sb.append("<style>\n")
          .append("#").append(id).append(" {\n")
          .append("  scrollbar-width: thin;\n")
          .append("  scrollbar-color: var(--jf-accent, ").append(accentColor).append(") transparent;\n")
          .append("  outline: none;\n")
          .append("}\n")
          .append("#").append(id).append("::-webkit-scrollbar {\n")
          .append("  width: 8px;\n")
          .append("  height: 8px;\n")
          .append("}\n")
          .append("#").append(id).append("::-webkit-scrollbar-track {\n")
          .append("  background: transparent;\n")
          .append("}\n")
          .append("#").append(id).append("::-webkit-scrollbar-thumb {\n")
          .append("  background-color: var(--jf-accent, ").append(accentColor).append(");\n")
          .append("  border-radius: 4px;\n")
          .append("}\n")
          .append("#").append(id).append("::-webkit-scrollbar-thumb:hover {\n")
          .append("  background-color: var(--jf-accent-hover, ").append(hoverAccent).append(");\n")
          .append("}\n")
          .append("</style>\n");

        sb.append("<div id=\"").append(id).append("\" ")
          .append("class=\"jettra-dashboard-content-body ")
          .append(modifier != null ? modifier.getClasses() : "").append("\" ")
          .append("tabindex=\"0\" ")
          .append("role=\"region\" ")
          .append("aria-label=\"").append(ariaLabel).append("\" ")
          .append("style=\"")
          .append("flex-grow:1; flex:1; display:flex; flex-direction:column; ")
          .append("overflow-y:auto; overflow-x:hidden; box-sizing:border-box; width:100%; ")
          .append(modifier != null ? modifier.getStyles() : "")
          .append("\">\n");

        for (Widget child : children) {
            if (child != null) {
                sb.append(child.render(theme)).append("\n");
            }
        }

        sb.append("</div>\n");
        return sb.toString();
    }
}
