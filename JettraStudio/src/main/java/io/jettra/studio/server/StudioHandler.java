package io.jettra.studio.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.jettra.flux.theme.ColorMode;
import io.jettra.studio.core.BasePage;
import io.jettra.studio.core.Component;
import io.jettra.studio.core.Form;
import io.jettra.studio.core.PageParameters;
import io.jettra.studio.core.WebPage;
import io.jettra.studio.i18n.LanguageStudio;
import io.jettra.studio.i18n.LocalizationManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Standard HTTP Handler bridging JettraStudio WebPages with JettraServer and Java 25 Loom Virtual Threads.
 */
public class StudioHandler implements HttpHandler {

    private final Class<? extends WebPage> pageClass;
    private final Supplier<? extends WebPage> pageSupplier;

    public StudioHandler(Class<? extends WebPage> pageClass) {
        this.pageClass = pageClass;
        this.pageSupplier = null;
    }

    public StudioHandler(Supplier<? extends WebPage> pageSupplier) {
        this.pageSupplier = pageSupplier;
        this.pageClass = null;
    }

    public static StudioHandler of(Class<? extends WebPage> pageClass) {
        return new StudioHandler(pageClass);
    }

    public static StudioHandler of(Supplier<? extends WebPage> pageSupplier) {
        return new StudioHandler(pageSupplier);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod().toUpperCase();
        Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getRawQuery());
        Map<String, String> cookies = parseCookies(exchange);

        // 1. Resolve active theme from cookie or param
        String themeCookie = cookies.get("jettra_theme");
        String modeCookie = cookies.get("jettra_color_mode");
        ColorMode colorMode = (modeCookie != null && modeCookie.equalsIgnoreCase("white")) ? ColorMode.WHITE : ColorMode.DARK;

        // 2. Resolve active language from cookie or param
        String langCookie = cookies.get("jettra_language");
        if (langCookie != null) {
            LocalizationManager.getInstance().setCurrentLanguage(LanguageStudio.fromCode(langCookie));
        }

        // 3. Build PageParameters
        PageParameters params = new PageParameters(queryParams);

        // 3.5 Check Page Security Constraints
        Class<? extends WebPage> targetClass = (pageClass != null) ? pageClass : null;
        if (targetClass != null && !checkSecurity(exchange, targetClass, cookies)) {
            return;
        }

        // 4. Instantiate WebPage
        WebPage page = createPageInstance(params);
        if (themeCookie != null && !themeCookie.isBlank()) {
            page.setCurrentThemeName(themeCookie);
        }
        page.setCurrentColorMode(colorMode);

        // 5. Handle POST vs GET
        if ("POST".equalsIgnoreCase(method)) {
            Map<String, String> postData = parsePostData(exchange);
            // Process forms and submit actions
            processFormSubmissions(page, postData);
        } else {
            // Check for link action param: ?_action=componentId
            String actionId = queryParams.get("_action");
            if (actionId != null && !actionId.isBlank()) {
                Component c = page.get(actionId);
                if (c instanceof io.jettra.studio.components.Link link) {
                    link.onClick();
                } else if (c instanceof io.jettra.studio.components.Button btn) {
                    btn.onClick();
                }
            }
        }

        // Process response cookies
        for (String cookie : page.getResponseCookies()) {
            exchange.getResponseHeaders().add("Set-Cookie", cookie);
        }

        // Process programmatic redirect
        if (page.getRedirectUrl() != null && !page.getRedirectUrl().isBlank()) {
            exchange.getResponseHeaders().set("Location", page.getRedirectUrl());
            exchange.sendResponseHeaders(302, 0);
            try (OutputStream os = exchange.getResponseBody()) {}
            return;
        }

        // 6. Render HTML response
        String htmlResponse = page.renderPage();
        byte[] bytes = htmlResponse.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private WebPage createPageInstance(PageParameters params) {
        if (pageSupplier != null) {
            WebPage p = pageSupplier.get();
            p.setPageParameters(params);
            return p;
        }
        if (pageClass != null) {
            try {
                // Try PageParameters constructor
                try {
                    Constructor<? extends WebPage> ctor = pageClass.getConstructor(PageParameters.class);
                    return ctor.newInstance(params);
                } catch (NoSuchMethodException ignored) {}

                // Try default constructor
                Constructor<? extends WebPage> defCtor = pageClass.getConstructor();
                WebPage page = defCtor.newInstance();
                page.setPageParameters(params);
                return page;
            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate WebPage: " + pageClass.getName(), e);
            }
        }
        throw new IllegalStateException("No page class or supplier configured");
    }

    private void processFormSubmissions(io.jettra.studio.core.MarkupContainer container, Map<String, String> postData) {
        for (Component child : container) {
            if (child instanceof Form<?> form) {
                form.processSubmit(postData);
            } else if (child instanceof io.jettra.studio.core.MarkupContainer mc) {
                processFormSubmissions(mc, postData);
            }
        }
    }

    private Map<String, String> parseQueryParams(String rawQuery) {
        Map<String, String> map = new HashMap<>();
        if (rawQuery == null || rawQuery.isBlank()) return map;
        String[] pairs = rawQuery.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String val = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                map.put(key, val);
            } else if (!pair.isBlank()) {
                map.put(URLDecoder.decode(pair, StandardCharsets.UTF_8), "");
            }
        }
        return map;
    }

    private Map<String, String> parseCookies(HttpExchange exchange) {
        Map<String, String> cookies = new HashMap<>();
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookieHeader != null) {
            String[] parts = cookieHeader.split(";");
            for (String part : parts) {
                int eq = part.indexOf("=");
                if (eq > 0) {
                    String name = part.substring(0, eq).trim();
                    String val = part.substring(eq + 1).trim();
                    cookies.put(name, val);
                }
            }
        }
        return cookies;
    }

    
    private boolean checkSecurity(HttpExchange exchange, Class<? extends WebPage> targetClass, Map<String, String> cookies) throws IOException {
        if (targetClass == null) return true;

        if (targetClass.isAnnotationPresent(io.jettra.studio.security.NoLoginRequired.class)) {
            return true;
        }

        if (targetClass.isAnnotationPresent(io.jettra.studio.security.Secured.class)) {
            io.jettra.studio.security.Secured secured = targetClass.getAnnotation(io.jettra.studio.security.Secured.class);
            String user = cookies.get("jettra_user");
            if (user == null || user.isBlank()) {
                exchange.getResponseHeaders().set("Location", secured.loginUrl());
                exchange.sendResponseHeaders(302, 0);
                try (OutputStream os = exchange.getResponseBody()) {}
                return false;
            }
            if (secured.roles().length > 0) {
                String role = cookies.get("jettra_role");
                boolean authorized = false;
                for (String r : secured.roles()) {
                    if (r != null && r.equalsIgnoreCase(role)) {
                        authorized = true;
                        break;
                    }
                }
                if (!authorized) {
                    byte[] msg = "<!DOCTYPE html><html><body style=\"font-family:sans-serif;text-align:center;padding:50px;background:#0f172a;color:#fff;\"><h1 style=\"color:#ef4444;\">403 - Acceso Denegado</h1><p>Su rol no tiene autorización para acceder a esta página.</p><a href=\"/dashboard\" style=\"color:#38bdf8;\">Volver al Dashboard</a></body></html>".getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                    exchange.sendResponseHeaders(403, msg.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(msg);
                    }
                    return false;
                }
            }
        }
        return true;
    }

    private Map<String, String> parsePostData(HttpExchange exchange) throws IOException {
        Map<String, String> map = new HashMap<>();
        InputStream is = exchange.getRequestBody();
        String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        if (body.isBlank()) return map;

        String[] pairs = body.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String val = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                map.put(key, val);
            }
        }
        return map;
    }
}
