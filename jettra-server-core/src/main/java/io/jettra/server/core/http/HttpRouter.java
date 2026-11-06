package io.jettra.server.core.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enrutador de alta velocidad con soporte para parámetros de ruta dinámicos, comodines y despacho según método.
 */
public class HttpRouter {

    public record Route(
        HttpMethod method,
        String rawPath,
        Pattern pattern,
        List<String> paramNames,
        HttpHandler handler
    ) {}

    private final List<Route> routes = new ArrayList<>();
    private HttpHandler notFoundHandler = (req, res) -> res.status(HttpStatus.NOT_FOUND).json(Map.of(
        "error", "Not Found",
        "path", req.getPath(),
        "status", 404
    ));

    public HttpRouter add(HttpMethod method, String pathPattern, HttpHandler handler) {
        String cleanPath = sanitizePath(pathPattern);
        List<String> paramNames = new ArrayList<>();
        StringBuilder regex = new StringBuilder("^");

        String[] segments = cleanPath.split("/");
        for (String seg : segments) {
            if (seg.isEmpty()) continue;
            regex.append("/");
            if (seg.startsWith("{") && seg.endsWith("}")) {
                String paramName = seg.substring(1, seg.length() - 1);
                paramNames.add(paramName);
                regex.append("([^/]+)");
            } else if (seg.startsWith(":")) {
                String paramName = seg.substring(1);
                paramNames.add(paramName);
                regex.append("([^/]+)");
            } else if (seg.equals("*") || seg.equals("**")) {
                regex.append(".*");
            } else {
                regex.append(Pattern.quote(seg));
            }
        }
        if (cleanPath.equals("/")) {
            regex.append("/?");
        }
        regex.append("$");

        Pattern compiled = Pattern.compile(regex.toString());
        routes.add(new Route(method, pathPattern, compiled, paramNames, handler));
        return this;
    }

    public HttpRouter get(String path, HttpHandler handler) { return add(HttpMethod.GET, path, handler); }
    public HttpRouter post(String path, HttpHandler handler) { return add(HttpMethod.POST, path, handler); }
    public HttpRouter put(String path, HttpHandler handler) { return add(HttpMethod.PUT, path, handler); }
    public HttpRouter delete(String path, HttpHandler handler) { return add(HttpMethod.DELETE, path, handler); }
    public HttpRouter patch(String path, HttpHandler handler) { return add(HttpMethod.PATCH, path, handler); }
    public HttpRouter options(String path, HttpHandler handler) { return add(HttpMethod.OPTIONS, path, handler); }

    public HttpRouter setNotFoundHandler(HttpHandler handler) {
        if (handler != null) this.notFoundHandler = handler;
        return this;
    }

    public void dispatch(HttpRequest req, HttpResponse res) throws Exception {
        String path = req.getPath();
        HttpMethod method = req.getMethod();

        for (Route route : routes) {
            if (route.method() == null || route.method() == method) {
                Matcher matcher = route.pattern().matcher(path);
                if (matcher.matches()) {
                    // Extraer path variables
                    if (!route.paramNames().isEmpty()) {
                        Map<String, String> extracted = new HashMap<>();
                        for (int i = 0; i < route.paramNames().size(); i++) {
                            extracted.put(route.paramNames().get(i), matcher.group(i + 1));
                        }
                        req.setPathParams(extracted);
                    }
                    route.handler().handle(req, res);
                    return;
                }
            }
        }

        // Si no hubo coincidencia
        notFoundHandler.handle(req, res);
    }

    private static String sanitizePath(String path) {
        if (path == null || path.isBlank()) return "/";
        path = path.trim();
        if (!path.startsWith("/")) path = "/" + path;
        return path;
    }
}
