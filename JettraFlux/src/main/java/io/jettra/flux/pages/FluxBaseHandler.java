package io.jettra.flux.pages;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.jettra.flux.core.Widget;
import io.jettra.flux.core.FluxConfig;
import io.jettra.flux.core.FluxContext;

import io.jettra.flux.security.PageSecurityGuard;
import io.jettra.flux.security.SecurityContext;
import io.jettra.flux.security.SecurityContextHolder;
import io.jettra.flux.security.SecurityDecision;
import io.jettra.flux.security.SecurityPrincipal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class FluxBaseHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String username = getLoggedUser(exchange);
        SecurityContext securityCtx;
        if (username != null && !username.isBlank()) {
            String role = getLoggedRole(exchange);
            String department = getLoggedDepartment(exchange);
            SecurityPrincipal principal = SecurityPrincipal.of(username, role, department);
            securityCtx = SecurityContext.authenticated(principal);
        } else {
            securityCtx = SecurityContext.unauthenticated();
        }
        SecurityContextHolder.setContext(securityCtx);

        try {
            // --- Declarative Security Guard ---
            SecurityDecision decision = PageSecurityGuard.evaluate(this.getClass(), securityCtx);
            switch (decision) {
                case SecurityDecision.Granted _ -> {
                    // Authorized, proceed with lifecycle
                }
                case SecurityDecision.RedirectToLogin(String loginPath) -> {
                    redirect(exchange, loginPath);
                    return;
                }
                case SecurityDecision.Denied(int statusCode, String reason, Set<String> requiredRoles) -> {
                    renderAccessDenied(exchange, statusCode, reason, requiredRoles);
                    return;
                }
            }
            // --- End Security Guard ---
        
        Map<String, String> params = new HashMap<>(parseQueryParams(exchange.getRequestURI().getQuery()));
        
        if ("true".equals(params.get("_jtSyncCheck"))) {
            handleSyncCheck(exchange, params);
            return;
        }
        
        if (params.containsKey("change_lang")) {
            io.jettra.flux.core.LanguageFlux.changeLanguage(exchange, params.get("change_lang"));
            String path = exchange.getRequestURI().getPath();
            String cPath = FluxConfig.getContextPath();
            String relPath = path;
            if (cPath != null && path.startsWith(cPath)) {
                relPath = path.substring(cPath.length());
            }
            if (relPath.isEmpty()) relPath = "/";
            redirect(exchange, relPath);
            return;
        }

        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            params.putAll(parseRequestBody(exchange));
            
            // Verificación de @ActionWidgetAllow (Invocación de Método)
            if (params.containsKey("_action_method")) {
                String methodName = params.get("_action_method");
                try {
                    java.lang.reflect.Method method = this.getClass().getDeclaredMethod(methodName, HttpExchange.class, Map.class);
                    if (method.isAnnotationPresent(io.jettra.core.security.widget.ActionWidgetAllow.class)) {
                        io.jettra.core.security.widget.ActionWidgetAllow actionAllow = method.getAnnotation(io.jettra.core.security.widget.ActionWidgetAllow.class);
                        String userRole = getLoggedRole(exchange);
                        String userDept = getLoggedDepartment(exchange);
                        boolean hasAccess = false;
                        java.util.List<String> actionRoles = new java.util.ArrayList<>();
                        try {
                            Object[] actionRolesArr = (Object[]) actionAllow.role();
                            for (Object r : actionRolesArr) {
                                actionRoles.add((r instanceof Enum) ? ((Enum<?>) r).name() : String.valueOf(r));
                            }
                        } catch (java.lang.annotation.AnnotationTypeMismatchException e) {
                            java.util.regex.Matcher m = java.util.regex.Pattern.compile("AppRole\\.([A-Z0-9_]+)").matcher(e.getMessage());
                            while (m.find()) {
                                actionRoles.add(m.group(1));
                            }
                        }

                        if (actionRoles.isEmpty()) {
                            hasAccess = true;
                        } else {
                            for (String rName : actionRoles) {
                                if (rName.equalsIgnoreCase(userRole) || checkSynonym(rName, userRole)) {
                                    hasAccess = true;
                                    break;
                                }
                            }
                        }
                        if (hasAccess && !actionAllow.department().isEmpty()) {
                            if (!actionAllow.department().equalsIgnoreCase(userDept)) {
                                hasAccess = false;
                            }
                        }
                        if (!hasAccess) {
                            renderAccessDenied(exchange, 403, "Acceso Denegado al Método: " + methodName, new HashSet<>(actionRoles));
                            return;
                        }
                    }
                    method.setAccessible(true);
                    method.invoke(this, exchange, params);
                    return; // Si el método se invoca con éxito, asume que manejó la redirección.
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (onPost(exchange, params)) {
                return; // If onPost returns true, it handled the response (e.g. redirect)
            }

            // If client expected JSON and onPost did not handle it, return structured JSON error instead of HTML UI
            String accept = exchange.getRequestHeaders() != null ? exchange.getRequestHeaders().getFirst("Accept") : null;
            String reqWith = exchange.getRequestHeaders() != null ? exchange.getRequestHeaders().getFirst("X-Requested-With") : null;
            if ((accept != null && accept.contains("application/json")) || "XMLHttpRequest".equalsIgnoreCase(reqWith)) {
                String path = exchange.getRequestURI() != null ? exchange.getRequestURI().getPath() : "";
                String jsonErr = "{\"status\":400,\"error\":\"Bad Request\",\"message\":\"Unhandled action or missing operation parameters for POST " + path + "\",\"timestamp\":" + System.currentTimeMillis() + "}";
                byte[] bytes = jsonErr.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                exchange.sendResponseHeaders(400, bytes.length);
                try (java.io.OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                    os.flush();
                }
                return;
            }
        } else {
            if (onGet(exchange, params)) {
                return; // If onGet returns true, it handled the response
            }
        }

        // Render UI
        String themeName = getThemeCookie(exchange);
        if (themeName == null || themeName.isEmpty()) {
            themeName = "Matrix"; // Default theme
        }
        io.jettra.flux.theme.ColorMode colorMode = getColorModeCookie(exchange);
        
        // Synchronize ThemeManager
        io.jettra.flux.theme.ThemeManager.synchronizeFromRequest(colorMode.name().toLowerCase(), themeName);

        // Ensure HTTP response preserves cookies across navigation
        exchange.getResponseHeaders().add("Set-Cookie", "jettra_color_mode=" + colorMode.name().toLowerCase() + "; Path=/; Max-Age=31536000; SameSite=Lax");
        exchange.getResponseHeaders().add("Set-Cookie", "jettra_theme=" + themeName + "; Path=/; Max-Age=31536000; SameSite=Lax");

        io.jettra.flux.theme.ThemeData theme = getThemeByName(themeName, colorMode);
        Widget ui = buildUI(exchange, params, themeName);
        if (ui != null) {
            StringBuilder syncJs = new StringBuilder();
            injectSyncLogic(syncJs);
            injectSecurityHeartbeat(syncJs);
            
            String html = "<!DOCTYPE html>\n<html lang=\"en\" data-color-mode=\"" + colorMode.name().toLowerCase() + "\" data-theme-mode=\"" + colorMode.name().toLowerCase() + "\" data-theme=\"" + themeName + "\">\n<head>\n" +
                          "<meta charset=\"UTF-8\">\n" +
                          "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                          "<title>" + getTitle() + "</title>\n" +
                          "<link rel=\"stylesheet\" href=\"" + FluxConfig.resolvePath("/static/font-awesome/css/all.min.css") + "\">\n" +
                          "<link rel=\"stylesheet\" href=\"" + FluxConfig.resolvePath("/static/bootstrap-icons/font/bootstrap-icons.css") + "\">\n" +
                          "<link rel=\"stylesheet\" href=\"" + FluxConfig.resolvePath("/static/material-icons/material-symbols.css") + "\">\n" +
                          theme.generateGlobalCss() + "\n" +
                          io.jettra.flux.theme.ThemeContext.getInstance().generateClientScript() + "\n" +
                          syncJs.toString() + "\n" +
                          "</head>\n<body style=\"margin: 0; padding: 0; box-sizing: border-box;\">\n" +
                          ui.render(theme) + "\n" +
                          "<div id='j-sync-popup-container'></div>\n" +
                          "</body>\n</html>";
            renderResponse(exchange, html, 200);
        }
        } finally {
            SecurityContextHolder.clear();
        }
    }

    protected abstract Widget buildUI(HttpExchange exchange, Map<String, String> params, String currentTheme);

    protected String getTitle() {
        return "JettraFlux App";
    }

    protected boolean onGet(HttpExchange exchange, Map<String, String> params) throws IOException {
        return false;
    }

    protected boolean onPost(HttpExchange exchange, Map<String, String> params) throws IOException {
        return false;
    }

    protected void redirect(HttpExchange exchange, String path) throws IOException {
        exchange.getResponseHeaders().set("Location", FluxConfig.resolvePath(path));
        exchange.sendResponseHeaders(302, -1);
        exchange.getResponseBody().close();
    }

    protected void renderResponse(HttpExchange exchange, String html, int statusCode) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.getResponseBody().close();
    }

    protected void renderAccessDenied(HttpExchange exchange, int statusCode, String message, Set<String> requiredRoles) throws IOException {
        String accept = exchange.getRequestHeaders() != null ? exchange.getRequestHeaders().getFirst("Accept") : null;
        String reqWith = exchange.getRequestHeaders() != null ? exchange.getRequestHeaders().getFirst("X-Requested-With") : null;
        if ((accept != null && accept.contains("application/json")) || "XMLHttpRequest".equalsIgnoreCase(reqWith)) {
            String rolesJson = (requiredRoles == null || requiredRoles.isEmpty()) ? "[]" : "[\"" + String.join("\",\"", requiredRoles) + "\"]";
            String jsonErr = "{\"status\":" + statusCode + ",\"error\":\"Forbidden\",\"message\":\"" + (message != null ? message.replace("\"", "\\\"") : "Access Denied") + "\",\"requiredRoles\":" + rolesJson + ",\"timestamp\":" + System.currentTimeMillis() + "}";
            byte[] bytes = jsonErr.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(statusCode, bytes.length);
            try (java.io.OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
                os.flush();
            }
            return;
        }

        String themeName = getThemeCookie(exchange);
        if (themeName == null || themeName.isEmpty()) themeName = "Matrix";
        io.jettra.flux.theme.ColorMode colorMode = getColorModeCookie(exchange);
        io.jettra.flux.theme.ThemeData theme = getThemeByName(themeName, colorMode);

        String allowedRolesStr = (requiredRoles == null || requiredRoles.isEmpty())
                ? ""
                : "Roles requeridos: " + String.join(", ", requiredRoles);

        Widget errorWidget = io.jettra.flux.widgets.Scaffold.of().body(
            io.jettra.flux.widgets.Center.of(
                io.jettra.flux.widgets.Card.of(
                    io.jettra.flux.widgets.Column.of(
                        io.jettra.flux.widgets.Icon.of("fas fa-shield-alt").modifier(new io.jettra.flux.core.Modifier().style("font-size: 48px; color: #ef4444; margin-bottom: 16px;")),
                        io.jettra.flux.widgets.Label.of("Acceso Denegado (" + statusCode + ")").modifier(new io.jettra.flux.core.Modifier().style("font-size: 22px; font-weight: 700; margin-bottom: 8px;")),
                        io.jettra.flux.widgets.Paragraph.of(message == null || message.isEmpty() ? "No cuenta con los privilegios suficientes para acceder a esta página." : message)
                            .modifier(new io.jettra.flux.core.Modifier().style("font-size: 14px; margin-bottom: 12px; opacity: 0.85;")),
                        allowedRolesStr.isEmpty() ? io.jettra.flux.widgets.Div.of() : io.jettra.flux.widgets.Paragraph.of(allowedRolesStr)
                            .modifier(new io.jettra.flux.core.Modifier().style("font-size: 12px; font-weight: 600; margin-bottom: 20px; opacity: 0.7;")),
                        io.jettra.flux.widgets.Link.of(FluxConfig.resolvePath("/login"), "Volver al Inicio")
                            .modifier(new io.jettra.flux.core.Modifier().style("display: inline-block; padding: 10px 20px; background: #0284c7; color: white; border-radius: 6px; text-decoration: none; font-weight: 600;"))
                    ).modifier(new io.jettra.flux.core.Modifier().style("display: flex; flex-direction: column; align-items: center; text-align: center; padding: 32px 24px;"))
                ).modifier(new io.jettra.flux.core.Modifier().style("max-width: 480px; width: 90%; margin: 60px auto;"))
            )
        );

        String html = "<!DOCTYPE html>\n<html lang=\"en\" data-color-mode=\"" + colorMode.name().toLowerCase() + "\">\n<head>\n"
                + "<meta charset=\"UTF-8\">\n"
                + "<title>Acceso Denegado - " + statusCode + "</title>\n"
                + "<link rel=\"stylesheet\" href=\"" + FluxConfig.resolvePath("/static/font-awesome/css/all.min.css") + "\">\n"
                + "<link rel=\"stylesheet\" href=\"" + FluxConfig.resolvePath("/static/bootstrap-icons/font/bootstrap-icons.css") + "\">\n"
                + "<link rel=\"stylesheet\" href=\"" + FluxConfig.resolvePath("/static/material-icons/material-symbols.css") + "\">\n"
                + theme.generateGlobalCss() + "\n"
                + "</head>\n<body style=\"margin:0; padding:0; box-sizing: border-box;\">\n"
                + errorWidget.render(theme) + "\n"
                + "</body>\n</html>";

        renderResponse(exchange, html, statusCode);
    }

    protected void setSessionCookie(HttpExchange exchange, String username, String role, String department) {
        String cPath = FluxConfig.getContextPath();
        if (cPath == null || cPath.isEmpty()) cPath = "/";
        exchange.getResponseHeaders().add("Set-Cookie", "username=" + username + "; Path=" + cPath);
        exchange.getResponseHeaders().add("Set-Cookie", "role=" + role + "; Path=" + cPath);
        exchange.getResponseHeaders().add("Set-Cookie", "department=" + department + "; Path=" + cPath);
        if (FluxContext.getCurrent() != null) {
            FluxContext.getCurrent().set(FluxContext.Scope.SESSION, "username", username);
            FluxContext.getCurrent().set(FluxContext.Scope.SESSION, "role", role);
            FluxContext.getCurrent().set(FluxContext.Scope.SESSION, "department", department);
        }
    }

    protected void setSessionCookie(HttpExchange exchange, String username, String role) {
        setSessionCookie(exchange, username, role, "");
    }

    protected void setSessionCookie(HttpExchange exchange, String username) {
        setSessionCookie(exchange, username, "USER", "");
    }

    protected void clearSessionCookie(HttpExchange exchange) {
        String cPath = FluxConfig.getContextPath();
        if (cPath == null || cPath.isEmpty()) cPath = "/";
        exchange.getResponseHeaders().add("Set-Cookie", "username=; Path=" + cPath + "; Max-Age=0");
        exchange.getResponseHeaders().add("Set-Cookie", "role=; Path=" + cPath + "; Max-Age=0");
        exchange.getResponseHeaders().add("Set-Cookie", "department=; Path=" + cPath + "; Max-Age=0");
        if (FluxContext.getCurrent() != null) {
            FluxContext.getCurrent().set(FluxContext.Scope.SESSION, "username", "");
            FluxContext.getCurrent().set(FluxContext.Scope.SESSION, "role", "");
            FluxContext.getCurrent().set(FluxContext.Scope.SESSION, "department", "");
            FluxContext.getCurrent().set(FluxContext.Scope.SESSION, "credentialFlux", "");
        }
    }

    protected String getLoggedUser(HttpExchange exchange) {
        if (FluxContext.getCurrent() != null) {
            Object userObj = FluxContext.getCurrent().get(FluxContext.Scope.SESSION, "username");
            if (userObj != null && !userObj.toString().trim().isEmpty()) {
                return userObj.toString();
            }
        }
        // Simple cookie parse
        String cookies = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookies != null) {
            for (String c : cookies.split(";")) {
                c = c.trim();
                if (c.startsWith("username=")) {
                    return c.substring("username=".length());
                }
                if (c.startsWith("jettra_user=")) {
                    return c.substring("jettra_user=".length());
                }
            }
        }
        return null;
    }

    protected String getLoggedRole(HttpExchange exchange) {
        if (FluxContext.getCurrent() != null) {
            Object roleObj = FluxContext.getCurrent().get(FluxContext.Scope.SESSION, "role");
            if (roleObj != null && !roleObj.toString().trim().isEmpty()) {
                return roleObj.toString();
            }
        }
        // Simple cookie parse for role
        String cookies = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookies != null) {
            for (String c : cookies.split(";")) {
                c = c.trim();
                if (c.startsWith("role=")) {
                    return c.substring("role=".length());
                }
            }
        }
        return "USER";
    }

    protected String getLoggedDepartment(HttpExchange exchange) {
        if (FluxContext.getCurrent() != null) {
            Object deptObj = FluxContext.getCurrent().get(FluxContext.Scope.SESSION, "department");
            if (deptObj != null) {
                return deptObj.toString();
            }
        }
        String cookies = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookies != null) {
            for (String c : cookies.split(";")) {
                c = c.trim();
                if (c.startsWith("department=")) {
                    return c.substring("department=".length());
                }
            }
        }
        return "";
    }

    protected String getThemeCookie(HttpExchange exchange) {
        String cookies = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookies != null) {
            for (String c : cookies.split(";")) {
                c = c.trim();
                if (c.startsWith("jettra_theme=")) {
                    String val = c.substring("jettra_theme=".length()).trim();
                    if (!val.isEmpty()) return val;
                }
            }
        }
        if (io.jettra.flux.theme.ThemeManager.getCurrentTheme() != null) {
            return io.jettra.flux.theme.ThemeManager.getCurrentTheme().getDisplayName();
        }
        return "Matrix";
    }

    protected io.jettra.flux.theme.ColorMode getColorModeCookie(HttpExchange exchange) {
        String cookies = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookies != null) {
            for (String c : cookies.split(";")) {
                c = c.trim();
                if (c.startsWith("jettra_color_mode=")) {
                    String val = c.substring("jettra_color_mode=".length()).trim();
                    return io.jettra.flux.theme.ColorMode.fromString(val, io.jettra.flux.theme.ColorMode.DARK);
                }
            }
        }
        if (io.jettra.flux.theme.ThemeManager.getCurrentMode() != null) {
            return io.jettra.flux.theme.ThemeManager.getCurrentMode();
        }
        return io.jettra.flux.theme.ColorMode.DARK;
    }

    protected io.jettra.flux.theme.ThemeData getThemeByName(String name) {
        return getThemeByName(name, io.jettra.flux.theme.ColorMode.DARK);
    }

    protected io.jettra.flux.theme.ThemeData getThemeByName(String name, io.jettra.flux.theme.ColorMode mode) {
        // Trigger initialization
        try { Class.forName("io.jettra.flux.theme.Themes"); } catch (Exception ignored) {}
        
        io.jettra.flux.theme.ThemeData theme = io.jettra.flux.theme.ThemeRegistry.getTheme(name, mode);
        if (theme != null) {
            return theme;
        }
        return io.jettra.flux.theme.Themes.AstTheme(mode);
    }

    protected Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        for (String pair : query.split("&")) {
            int eqIdx = pair.indexOf('=');
            try {
                if (eqIdx >= 0) {
                    map.put(URLDecoder.decode(pair.substring(0, eqIdx), StandardCharsets.UTF_8),
                            URLDecoder.decode(pair.substring(eqIdx + 1), StandardCharsets.UTF_8));
                } else if (!pair.isBlank()) {
                    map.put(URLDecoder.decode(pair, StandardCharsets.UTF_8), "");
                }
            } catch (Exception ignored) {}
        }
        return map;
    }

    protected Map<String, String> parseRequestBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        StringBuilder sb = new StringBuilder();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) != -1) {
            sb.append(new String(buffer, 0, len, StandardCharsets.UTF_8));
        }
        String body = sb.toString();
        Map<String, String> map = new HashMap<>();
        if (body.isEmpty()) return map;
        map.put("_raw_body", body);

        String contentType = exchange.getRequestHeaders() != null ? exchange.getRequestHeaders().getFirst("Content-Type") : null;
        boolean isJson = (contentType != null && contentType.toLowerCase().contains("application/json"))
                || (body.trim().startsWith("{") && body.trim().endsWith("}"));

        if (isJson) {
            try {
                io.jettra.json.JettraJson json = new io.jettra.json.JettraJson();
                io.jettra.json.JsonObject jsonObj = json.fromJson(body, io.jettra.json.JsonObject.class);
                if (jsonObj != null) {
                    for (String key : jsonObj.keySet()) {
                        Object val = jsonObj.get(key);
                        if (val == null) {
                            map.put(key, "");
                        } else if (val instanceof io.jettra.json.JsonObject || val instanceof io.jettra.json.JsonArray) {
                            map.put(key, json.toJson(val));
                        } else {
                            map.put(key, String.valueOf(val));
                        }
                    }
                    return map;
                }
            } catch (Exception ignored) {}

            // Robust fallback: if full object parsing encountered issues (e.g. unescaped chars in payload),
            // extract top-level string/primitive keys so action, target_id, etc. are preserved.
            try {
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"([a-zA-Z0-9_]+)\"\\s*:\\s*(\"[^\"]*\"|true|false|[0-9.-]+)").matcher(body);
                while (m.find()) {
                    String k = m.group(1);
                    String v = m.group(2);
                    if (v.startsWith("\"") && v.endsWith("\"")) {
                        v = v.substring(1, v.length() - 1);
                    }
                    if (!map.containsKey(k)) {
                        map.put(k, v);
                    }
                }
                if (map.containsKey("action") || map.containsKey("target_id")) {
                    return map;
                }
            } catch (Exception ignored) {}
        }

        if (contentType != null && contentType.toLowerCase().contains("multipart/form-data")) {
            String boundary = null;
            int bIdx = contentType.toLowerCase().indexOf("boundary=");
            if (bIdx != -1) {
                boundary = contentType.substring(bIdx + 9).trim();
                int scIdx = boundary.indexOf(';');
                if (scIdx != -1) boundary = boundary.substring(0, scIdx).trim();
                if (boundary.startsWith("\"") && boundary.endsWith("\"") && boundary.length() > 1) {
                    boundary = boundary.substring(1, boundary.length() - 1);
                }
            }
            if (boundary != null && !boundary.isEmpty()) {
                String delimiter = "--" + boundary;
                String[] parts = body.split(java.util.regex.Pattern.quote(delimiter));
                for (String part : parts) {
                    if (part.isBlank() || part.equals("--\r\n") || part.equals("--") || part.startsWith("--")) continue;
                    int headerEnd = part.indexOf("\r\n\r\n");
                    int sepLen = 4;
                    if (headerEnd == -1) {
                        headerEnd = part.indexOf("\n\n");
                        sepLen = 2;
                    }
                    if (headerEnd != -1) {
                        String header = part.substring(0, headerEnd);
                        String content = part.substring(headerEnd + sepLen);
                        if (content.endsWith("\r\n")) content = content.substring(0, content.length() - 2);
                        else if (content.endsWith("\n")) content = content.substring(0, content.length() - 1);

                        java.util.regex.Matcher m = java.util.regex.Pattern.compile("name=\"([^\"]+)\"").matcher(header);
                        if (m.find()) {
                            String name = m.group(1);
                            map.put(name, content);
                        }
                    }
                }
                return map;
            }
        }

        for (String pair : body.split("&")) {
            int eqIdx = pair.indexOf('=');
            try {
                if (eqIdx >= 0) {
                    String k = URLDecoder.decode(pair.substring(0, eqIdx), StandardCharsets.UTF_8);
                    String v = URLDecoder.decode(pair.substring(eqIdx + 1), StandardCharsets.UTF_8);
                    map.merge(k, v, (oldVal, newVal) -> oldVal.isEmpty() ? newVal : oldVal + "," + newVal);
                } else if (!pair.isBlank()) {
                    String k = URLDecoder.decode(pair, StandardCharsets.UTF_8);
                    map.putIfAbsent(k, "");
                }
            } catch (Exception ignored) {}
        }
        return map;
    }

    private void handleSyncCheck(HttpExchange exchange, Map<String, String> params) throws IOException {
        long startTime = io.jettra.flux.sync.JettraSyncManager.SERVER_START_TIME;
        io.jettra.flux.sync.JettraPageSincronized syncAnno = this.getClass().getAnnotation(io.jettra.flux.sync.JettraPageSincronized.class);
        
        if (syncAnno == null) {
            renderResponse(exchange, String.format("{\"serverStartTime\": %d}", startTime), 200);
            return;
        }
        String entity = syncAnno.entity().isEmpty() ? this.getClass().getSimpleName().replace("Page", "Model") : syncAnno.entity();
        io.jettra.flux.sync.JettraSyncManager.SyncInfo info = io.jettra.flux.sync.JettraSyncManager.getLastChange(entity);
        
        if (info == null) {
            renderResponse(exchange, String.format("{\"serverStartTime\": %d}", startTime), 200);
        } else {
            String json = String.format("{\"type\":\"%s\", \"user\":\"%s\", \"ts\":%d, \"serverStartTime\": %d}", 
                info.type, info.user, info.timestamp, startTime);
            renderResponse(exchange, json, 200);
        }
    }

    private void injectSecurityHeartbeat(StringBuilder builder) {
        long startTime = io.jettra.flux.sync.JettraSyncManager.SERVER_START_TIME;
        String loginPath = FluxConfig.resolvePath("/login");
        
        builder.append("<script>\n")
               .append("  const J_SERVER_START_TIME = ").append(startTime).append(";\n")
               .append("  const J_LOGIN_PATH = '").append(loginPath).append("';\n")
               .append("  \n")
               .append("  function checkJettraHeartbeat() {\n")
               .append("    if (window.location.pathname === J_LOGIN_PATH) return;\n")
               .append("    \n")
               .append("    fetch(window.location.pathname + '?_jtSyncCheck=true')\n")
               .append("      .then(r => {\n")
               .append("        if (!r.ok) throw new Error('Server down');\n")
               .append("        return r.json();\n")
               .append("      })\n")
               .append("      .then(data => {\n")
               .append("        if (data.serverStartTime && data.serverStartTime !== J_SERVER_START_TIME) {\n")
               .append("           console.warn('Server restarted, redirecting to login...');\n")
               .append("           window.location.href = J_LOGIN_PATH;\n")
               .append("        }\n")
               .append("      })\n")
               .append("      .catch(err => {\n")
               .append("        console.error('JettraHeartbeat Error:', err);\n")
               .append("        window.location.href = J_LOGIN_PATH;\n")
               .append("      });\n")
               .append("  }\n")
               .append("  \n")
               .append("  setInterval(checkJettraHeartbeat, 10000);\n")
               .append("</script>\n");
    }

    private void injectSyncLogic(StringBuilder builder) {
        io.jettra.flux.sync.JettraPageSincronized syncAnno = this.getClass().getAnnotation(io.jettra.flux.sync.JettraPageSincronized.class);
        if (syncAnno == null) return;

        String entity = syncAnno.entity().isEmpty() ? this.getClass().getSimpleName().replace("Page", "Model") : syncAnno.entity();
        long loadTime = System.currentTimeMillis();

        builder.append("<script>\n")
               .append("  const J_SYNC_ENTITY = '").append(entity).append("';\n")
               .append("  const J_SYNC_LOAD_TIME = ").append(loadTime).append(";\n")
               .append("  const J_SYNC_TYPE = '").append(syncAnno.value()).append("';\n")
               .append("  \n")
               .append("  function checkJettraSync() {\n")
               .append("    fetch(window.location.pathname + '?_jtSyncCheck=true')\n")
               .append("      .then(r => r.json())\n")
               .append("      .then(data => {\n")
               .append("        if (data.ts && data.ts > J_SYNC_LOAD_TIME) {\n")
               .append("          if (J_SYNC_TYPE === 'ALL' || J_SYNC_TYPE === data.type) {\n")
               .append("            showJettraSyncPopup(data);\n")
               .append("          }\n")
               .append("        }\n")
               .append("      });\n")
               .append("  }\n")
               .append("  \n")
               .append("  function showJettraSyncPopup(data) {\n")
               .append("    const container = document.getElementById('j-sync-popup-container');\n")
               .append("    if (container.innerHTML !== '') return;\n")
               .append("    \n")
               .append("    let actionText = '';\n")
               .append("    switch(data.type) {\n")
               .append("        case 'CREATE': actionText = 'creó un nuevo registro'; break;\n")
               .append("        case 'UPDATE': actionText = 'actualizó los datos'; break;\n")
               .append("        case 'DELETE': actionText = 'eliminó un registro'; break;\n")
               .append("        case 'MOVE': actionText = 'movió un elemento'; break;\n")
               .append("        default: actionText = 'realizó cambios';\n")
               .append("    }\n")
               .append("    \n")
               .append("    const msg = `${data.user} ${actionText}. ¿Desea actualizar la vista?`;\n")
               .append("    const popup = document.createElement('div');\n")
               .append("    popup.className = 'j-sync-popup-3d';\n")
               .append("    popup.innerHTML = `\n")
               .append("      <div class='j-sync-content' style='background: rgba(30, 41, 59, 0.9); backdrop-filter: blur(10px); border: 1px solid rgba(255,255,255,0.1); border-radius: 12px; padding: 20px; box-shadow: 0 10px 30px rgba(0,0,0,0.5); color: white; display: flex; gap: 15px; align-items: center; position: fixed; bottom: 20px; right: 20px; z-index: 9999;'>\n")
               .append("        <div class='j-sync-icon' style='font-size: 24px;'>📡</div>\n")
               .append("        <div class='j-sync-text'>\n")
               .append("          <strong style='display:block; margin-bottom:5px; font-size:16px;'>Sincronización</strong>\n")
               .append("          <p style='margin:0; font-size:14px; color:#cbd5e1;'>${msg}</p>\n")
               .append("        </div>\n")
               .append("        <div class='j-sync-actions' style='display: flex; gap: 10px; margin-left: 10px;'>\n")
               .append("          <button onclick='window.location.reload()' style='background: #3b82f6; color: white; border: none; padding: 8px 12px; border-radius: 6px; cursor: pointer; font-weight: 600;'>Sincronizar</button>\n")
               .append("          <button onclick='this.closest(\".j-sync-popup-3d\").remove()' style='background: rgba(255,255,255,0.1); color: white; border: none; padding: 8px 12px; border-radius: 6px; cursor: pointer;'>Cerrar</button>\n")
               .append("        </div>\n")
               .append("      </div>\n")
               .append("    `;\n")
               .append("    container.appendChild(popup);\n")
               .append("  }\n")
               .append("  \n")
               .append("  setInterval(checkJettraSync, 5000);\n")
               .append("</script>\n");
    }

    private boolean checkSynonym(String pluginRole, String userRole) {
        if (userRole == null || userRole.isEmpty()) return false;
        try {
            java.io.InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("plugin-config.json");
            if (is != null) {
                String content = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                String pRoleStr = "\"plugin-role\"\\s*:\\s*\"" + java.util.regex.Pattern.quote(pluginRole) + "\"";
                String pSecRoleStr = "\"plugin-security-role\"\\s*:\\s*\"" + java.util.regex.Pattern.quote(pluginRole) + "\"";
                String aRoleStr = "\"applicative-role\"\\s*:\\s*\"" + java.util.regex.Pattern.quote(userRole) + "\"";
                String aRoleStrLegacy = "\"application-role\"\\s*:\\s*\"" + java.util.regex.Pattern.quote(userRole) + "\"";
                String aSecRoleStr = "\"applicative-security-role\"\\s*:\\s*\"" + java.util.regex.Pattern.quote(userRole) + "\"";

                String[] blocks = content.split("\\{");
                for (String block : blocks) {
                    boolean matchPlugin = java.util.regex.Pattern.compile(pRoleStr).matcher(block).find() ||
                                          java.util.regex.Pattern.compile(pSecRoleStr).matcher(block).find();
                    boolean matchApp = java.util.regex.Pattern.compile(aRoleStr).matcher(block).find() ||
                                       java.util.regex.Pattern.compile(aRoleStrLegacy).matcher(block).find() ||
                                       java.util.regex.Pattern.compile(aSecRoleStr).matcher(block).find();
                    if (matchPlugin && matchApp) {
                        return true;
                    }
                }
            }
        } catch (Exception ex) {
            // ignore
        }
        return false;
    }
}
