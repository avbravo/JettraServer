package io.jettra.server.core.http;

import java.util.List;

/**
 * Cadena de ejecución recursiva de filtros que finaliza en el handler destino.
 */
public class HttpFilterChain {

    private final List<HttpFilter> filters;
    private final HttpHandler terminalHandler;
    private int index = 0;

    public HttpFilterChain(List<HttpFilter> filters, HttpHandler terminalHandler) {
        this.filters = filters != null ? filters : List.of();
        this.terminalHandler = terminalHandler;
    }

    public void doFilter(HttpRequest request, HttpResponse response) throws Exception {
        if (index < filters.size()) {
            HttpFilter nextFilter = filters.get(index++);
            nextFilter.doFilter(request, response, this);
        } else if (terminalHandler != null) {
            terminalHandler.handle(request, response);
        }
    }
}
