package io.jettra.flux.core;

/**
 * Configuración global del motor JettraFlux.
 * Permite resolver rutas y configurar el contextPath sin depender de un servidor de aplicaciones específico.
 */
public final class FluxConfig {

    private static volatile String contextPath = "/";

    private FluxConfig() {
    }

    public static void setContextPath(String cp) {
        contextPath = (cp == null || cp.isBlank() || cp.equals("/")) ? "/" : (cp.startsWith("/") ? cp : "/" + cp);
    }

    public static String getContextPath() {
        return contextPath;
    }

    public static String resolvePath(String path) {
        if (path == null) return contextPath;
        if ("/".equals(contextPath)) {
            return path.startsWith("/") ? path : "/" + path;
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (path.equals(contextPath) || path.startsWith(contextPath + "/")) {
            return path;
        }
        return contextPath + path;
    }
}
