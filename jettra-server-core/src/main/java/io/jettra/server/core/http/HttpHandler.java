package io.jettra.server.core.http;

/**
 * Interfaz funcional para el manejo de una petición HTTP.
 */
@FunctionalInterface
public interface HttpHandler {
    void handle(HttpRequest request, HttpResponse response) throws Exception;
}
