package io.jettra.aura.server;

import io.jettra.aura.core.AuraPage;
import io.jettra.server.core.JettraServer;
import io.jettra.server.core.http.HttpHandler;
import java.util.Map;

/**
 * Módulo de integración principal para acoplar el framework JettraAura a una instancia de JettraServer.
 *
 * Uso típico:
 * <pre>{@code
 * JettraServer server = JettraServer.create();
 * AuraModule aura = AuraModule.attach(server);
 * aura.page("/", Index.class);
 * aura.page("/productos", ProductPage.class);
 * server.start();
 * }</pre>
 */
public class AuraModule {

    private final JettraServer server;
    private final AuraApplication application;
    private final AuraHandler handler;

    private AuraModule(JettraServer server) {
        this.server = server;
        this.application = new AuraApplication();
        this.handler = new AuraHandler(application);
    }

    public static AuraModule attach(JettraServer server) {
        if (server == null) {
            throw new IllegalArgumentException("La instancia de JettraServer no puede ser nula");
        }
        return new AuraModule(server);
    }

    public AuraApplication getApplication() {
        return application;
    }

    public AuraModule page(String path, Class<? extends AuraPage> pageClass) {
        application.mountPage(path, pageClass);
        registerRouteOnServer(path);
        return this;
    }

    public AuraModule register(Class<? extends AuraPage> pageClass) {
        application.registerPage(pageClass);
        // Registrar todas las rutas conocidas
        for (String p : application.getPageRoutes().keySet()) {
            registerRouteOnServer(p);
        }
        return this;
    }

    public AuraModule homePage(Class<? extends AuraPage> homePageClass) {
        application.setHomePage(homePageClass);
        registerRouteOnServer("/");
        return this;
    }

    public AuraModule loginPage(Class<? extends AuraPage> loginPageClass) {
        application.setLoginPage(loginPageClass);
        registerRouteOnServer("/login");
        return this;
    }

    private void registerRouteOnServer(String path) {
        HttpHandler h = handler::handle;
        server.get(path, h);
        server.post(path, h);
        if (!path.endsWith(".html") && !path.endsWith(".xhtml") && !"/".equals(path)) {
            server.get(path + ".html", h);
            server.post(path + ".html", h);
            server.get(path + ".xhtml", h);
            server.post(path + ".xhtml", h);
        }
    }
}

