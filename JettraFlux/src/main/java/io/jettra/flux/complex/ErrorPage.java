package io.jettra.flux.complex;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.jettra.flux.core.Modifier;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import io.jettra.flux.theme.Themes;
import io.jettra.flux.widgets.Button;
import io.jettra.flux.widgets.Div;
import io.jettra.flux.widgets.Header;
import io.jettra.flux.widgets.Paragraph;
import io.jettra.flux.widgets.Scaffold;
import io.jettra.flux.core.FluxConfig;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ErrorPage implements HttpHandler {

    private String errorTitle = "Error Petición";
    private String errorDetail = "Se ha producido un error inesperado al procesar la solicitud.";
    private String errorOrigin = "Servidor";
    public static String path = "/login";

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
        if (params.containsKey("title")) {
            this.errorTitle = params.get("title");
        }
        if (params.containsKey("detail")) {
            this.errorDetail = params.get("detail");
        }
        if (params.containsKey("origin")) {
            this.errorOrigin = params.get("origin");
        }

        Widget ui = buildUI();
        ThemeData theme = Themes.AstTheme();
        
        String html = "<!DOCTYPE html>\n<html lang=\"es\">\n<head>\n" +
                "<meta charset=\"UTF-8\">\n" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "<title>🚨 Jettra - Error</title>\n" +
                theme.generateGlobalCss() + "\n" +
                "<style>.btn-error:hover { transform: translateY(-3px) translateZ(30px); box-shadow: 0 15px 25px rgba(204, 0, 0, 0.6), inset 0 2px 0 rgba(255,255,255,0.3); }</style>\n" +
                "</head>\n<body style=\"margin: 0; padding: 0; box-sizing: border-box;\">\n" +
                ui.render(theme) + "\n" +
                "</body>\n</html>";
                
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.getResponseBody().close();
    }

    private Widget buildUI() {
        Widget icon = io.jettra.flux.widgets.RawHtml.of("<div style='font-size: 80px; margin-bottom: 20px; animation: float 3s ease-in-out infinite;'>⚠️</div>");

        Widget title = Header.of(2, this.errorTitle)
                .modifier(new Modifier()
                        .style("color: #ff4444; margin: 0 0 15px 0; font-weight: 800; letter-spacing: 2px;"));

        Widget detail = Paragraph.of(this.errorDetail)
                .modifier(new Modifier()
                        .style("color: #94a3b8; font-size: 18px; margin-bottom: 30px; line-height: 1.6;"));

        Widget originBadge = Div.of(io.jettra.flux.widgets.Text.of("Origen del error: " + this.errorOrigin))
                .modifier(new Modifier()
                        .style("display: inline-block; background: rgba(255, 68, 68, 0.1); border: 1px dashed rgba(255, 68, 68, 0.4); color: #ff8888; padding: 8px 15px; border-radius: 8px; font-family: monospace; font-size: 14px; margin-bottom: 40px;"));

        Widget btnBack = io.jettra.flux.widgets.RawHtml.of("<a href='" + FluxConfig.resolvePath(path) + "' style='text-decoration:none; display:inline-block; background: linear-gradient(135deg, #ff4444, #cc0000); color: white; border: none; padding: 12px 30px; border-radius: 8px; font-size: 16px; font-weight: 700; cursor: pointer; box-shadow: 0 10px 20px rgba(204, 0, 0, 0.4), inset 0 2px 0 rgba(255,255,255,0.2); transition: all 0.2s; position: relative; z-index: 1000;'>Volver al Login</a>");

        Widget btnContainer = Div.of(btnBack);

        Widget card = Div.of(icon, title, detail, originBadge, btnContainer)
                .modifier(new Modifier()
                        .style("background: linear-gradient(145deg, rgba(30,41,59,0.95), rgba(15,23,42,0.95)); backdrop-filter: blur(10px); padding: 40px; border-radius: 20px; border: 1px solid rgba(255,255,255,0.1); box-shadow: 0 25px 50px rgba(0,0,0,0.5), inset 0 1px 0 rgba(255,255,255,0.1); width: 100%; max-width: 600px; text-align: center;"));

        Widget container = Div.of(card)
                .modifier(new Modifier()
                        .style("display: flex; align-items: center; justify-content: center; height: 100vh; background: radial-gradient(circle at center, #1e293b 0%, #0f172a 100%);"));

        return Scaffold.of().body(container);
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=");
            try {
                if (kv.length == 2) {
                    map.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8), URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
                }
            } catch (Exception e) {}
        }
        return map;
    }
}
