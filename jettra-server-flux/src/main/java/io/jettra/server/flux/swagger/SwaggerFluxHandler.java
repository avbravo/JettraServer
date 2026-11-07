package io.jettra.server.flux.swagger;

import io.jettra.server.core.http.HttpHandler;
import io.jettra.server.core.http.HttpRequest;
import io.jettra.server.core.http.HttpResponse;
import io.jettra.server.flux.FluxRenderer;
import io.jettra.server.openapi.OpenApiGenerator;

import java.util.Collections;
import java.util.List;

/**
 * Manejador HTTP que sirve la interfaz interactiva Swagger-UI generada mediante JettraFlux
 * y expone la especificación OpenAPI 3.0.0 en formato JSON.
 */
public class SwaggerFluxHandler implements HttpHandler {

    private final List<Class<?>> controllers;
    private final String title;
    private final FluxRenderer renderer;

    public SwaggerFluxHandler(List<Class<?>> controllers) {
        this(controllers, "JettraServer API Explorer (Swagger-UI)");
    }

    public SwaggerFluxHandler(List<Class<?>> controllers, String title) {
        this.controllers = controllers != null ? controllers : Collections.emptyList();
        this.title = title;
        this.renderer = new FluxRenderer();
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws Exception {
        SwaggerFluxView view = new SwaggerFluxView(controllers);
        String html = renderer.render(view, title);
        response.html(html);
    }

    public void handleOpenApiJson(HttpRequest request, HttpResponse response) throws Exception {
        String json = OpenApiGenerator.generate(controllers);
        response.contentType("application/json; charset=UTF-8").body(json);
    }
}

