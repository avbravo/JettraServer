package io.jettra.server.core.http;

/**
 * Métodos HTTP soportados por JettraServer.
 */
public enum HttpMethod {
    GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS, TRACE;

    public static HttpMethod fromString(String method) {
        if (method == null) return GET;
        try {
            return HttpMethod.valueOf(method.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return GET;
        }
    }
}
