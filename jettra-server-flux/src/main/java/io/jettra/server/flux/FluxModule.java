package io.jettra.server.flux;

import io.jettra.flux.core.FluxView;
import io.jettra.flux.core.Widget;
import io.jettra.server.core.JettraServer;
import java.util.function.Supplier;

/**
 * Módulo de integración principal para habilitar páginas y vistas JettraFlux dentro de JettraServer.
 */
public class FluxModule {

    private final JettraServer server;
    private final FluxRenderer renderer;
    private final FluxWebSocketBridge webSocketBridge;

    public FluxModule(JettraServer server) {
        this.server = server;
        this.renderer = new FluxRenderer();
        this.webSocketBridge = new FluxWebSocketBridge();
    }

    public static FluxModule attach(JettraServer server) {
        return new FluxModule(server);
    }

    public FluxModule page(String path, Supplier<Widget> widgetSupplier) {
        return page(path, "JettraFlux Page", widgetSupplier);
    }

    public FluxModule page(String path, String pageTitle, Supplier<Widget> widgetSupplier) {
        server.get(path, new FluxPageHandler(widgetSupplier, pageTitle));
        return this;
    }

    public FluxModule page(String path, Class<? extends FluxView> viewClass) {
        return page(path, viewClass.getSimpleName(), viewClass);
    }

    public FluxModule page(String path, String pageTitle, Class<? extends FluxView> viewClass) {
        server.get(path, new FluxPageHandler(viewClass, pageTitle));
        return this;
    }

    public FluxRenderer getRenderer() {
        return renderer;
    }

    public FluxWebSocketBridge getWebSocketBridge() {
        return webSocketBridge;
    }
}
