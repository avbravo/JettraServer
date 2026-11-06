package io.jettra.server.flux;

import io.jettra.flux.core.FluxView;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import io.jettra.flux.theme.ThemeRegistry;

/**
 * Motor de renderizado reactivo para componentes JettraFlux en el lado del servidor.
 * Genera documentos HTML5 semánticos completos sin necesidad de plantillas manuales HTML o JS.
 */
public class FluxRenderer {

    private final ThemeData defaultTheme;

    public FluxRenderer() {
        this(resolveDefaultTheme());
    }

    public FluxRenderer(ThemeData theme) {
        this.defaultTheme = theme != null ? theme : resolveDefaultTheme();
    }

    private static ThemeData resolveDefaultTheme() {
        try {
            ThemeData t = ThemeRegistry.getTheme("admin");
            if (t != null) return t;
        } catch (Exception ignored) {}
        return new ThemeData("#2563eb", "#1e40af", "#f8fafc", "#ffffff", "#ffffff", "#0f172a", "", "", "", "");
    }

    public String render(Widget widget) {
        return render(widget, "JettraFlux Application", defaultTheme);
    }

    public String render(Widget widget, String title) {
        return render(widget, title, defaultTheme);
    }

    public String render(Widget widget, String title, ThemeData theme) {
        String bodyContent = widget != null ? widget.render(theme) : "";
        String globalCss = theme.generateGlobalCss();

        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s</title>
                <style>
                    body {
                        margin: 0;
                        padding: 0;
                        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
                        background-color: %s;
                        color: %s;
                    }
                    %s
                </style>
            </head>
            <body>
                <div id="jettra-flux-root">
                    %s
                </div>
            </body>
            </html>
            """.formatted(
                escapeHtml(title),
                theme.backgroundColor != null ? theme.backgroundColor : "#ffffff",
                theme.onSurfaceColor != null ? theme.onSurfaceColor : "#1e293b",
                globalCss != null ? globalCss : "",
                bodyContent
            );
    }

    public String renderView(FluxView view, String title) {
        return render(view, title, defaultTheme);
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
