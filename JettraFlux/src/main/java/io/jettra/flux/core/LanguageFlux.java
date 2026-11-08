package io.jettra.flux.core;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.jettra.core.server.Page;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Page(path = "/change-lang")
public class LanguageFlux implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> params = new HashMap<>();
        if (query != null) {
            for (String pair : query.split("&")) {
                String[] kv = pair.split("=");
                if (kv.length == 2) params.put(kv[0], kv[1]);
            }
        }
        
        String lang = params.getOrDefault("lang", "en");
        changeLanguage(exchange, lang);
        
        String referer = exchange.getRequestHeaders().getFirst("Referer");
        String redirectUrl = (referer != null) ? referer : FluxConfig.resolvePath("/dashboard");
        
        exchange.getResponseHeaders().set("Location", redirectUrl);
        exchange.sendResponseHeaders(302, -1);
        exchange.getResponseBody().close();
    }

    public static void changeLanguage(HttpExchange exchange, String lang) {
        String cPath = FluxConfig.getContextPath();
        if (cPath == null || cPath.isEmpty()) cPath = "/";
        exchange.getResponseHeaders().add("Set-Cookie", "jettra_lang=" + lang + "; Path=" + cPath + "; Max-Age=31536000");
        if (FluxContext.getCurrent() != null) {
            FluxContext.getCurrent().set(FluxContext.Scope.SESSION, "jettra_lang", lang);
        }
    }
}
