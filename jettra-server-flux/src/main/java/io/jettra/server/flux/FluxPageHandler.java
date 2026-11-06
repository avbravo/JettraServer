package io.jettra.server.flux;

import io.jettra.flux.core.FluxView;
import io.jettra.flux.core.Widget;
import io.jettra.server.core.http.HttpHandler;
import io.jettra.server.core.http.HttpRequest;
import io.jettra.server.core.http.HttpResponse;
import java.util.function.Supplier;

/**
 * Manejador HTTP que genera y despacha páginas web tipadas en Java mediante JettraFlux.
 */
public class FluxPageHandler implements HttpHandler {

    private final Supplier<Widget> widgetSupplier;
    private final String pageTitle;
    private final FluxRenderer renderer;

    public FluxPageHandler(Supplier<Widget> widgetSupplier, String pageTitle) {
        this.widgetSupplier = widgetSupplier;
        this.pageTitle = pageTitle != null ? pageTitle : "JettraFlux";
        this.renderer = new FluxRenderer();
    }

    public FluxPageHandler(Class<? extends FluxView> viewClass, String pageTitle) {
        this(() -> {
            try {
                return viewClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Error instanciando FluxView: " + viewClass.getName(), e);
            }
        }, pageTitle);
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws Exception {
        Widget root = widgetSupplier.get();
        String html = renderer.render(root, pageTitle);
        response.html(html);
    }
}
