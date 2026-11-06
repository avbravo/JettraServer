package io.jettra.server.core.http;

/**
 * Filtro de intercepción para pre/post procesamiento de peticiones.
 */
@FunctionalInterface
public interface HttpFilter {
    void doFilter(HttpRequest request, HttpResponse response, HttpFilterChain chain) throws Exception;
}
