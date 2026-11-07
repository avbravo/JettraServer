package io.jettra.server.example;

import io.jettra.server.core.JettraServer;
import io.jettra.server.core.config.ServerConfig;
import io.jettra.server.core.storage.StorageMode;
import io.jettra.server.example.faces.FacesRendererServlet;
import io.jettra.server.example.flux.CatalogoFlux;
import io.jettra.server.example.flux.InteractiveCounterFluxView;
import io.jettra.server.example.flux.StorageMonitorFluxView;
import io.jettra.server.example.flux.TelemetryFluxView;
import io.jettra.server.example.rest.ProductResource;
import io.jettra.server.example.rest.ServerInfoResource;
import io.jettra.server.example.service.ProductService;
import io.jettra.server.example.service.SecurityService;
import io.jettra.server.flux.FluxModule;
import io.jettra.server.jakarta.JakartaContainer;

/**
 * Clase principal de arranque de JettraServerExample.
 * Despliega la aplicación web compatible con Jakarta EE 11, PrimeFaces 14 y JettraFlux.
 */
public class App {

    public static void main(String[] args) {
        ServerConfig config = ServerConfig.load();

        String portProp = System.getProperty("server.port", System.getenv("PORT"));
        if (portProp != null && !portProp.isBlank()) {
            try { config.setPort(Integer.parseInt(portProp.trim())); } catch (Exception ignored) {}
        }

        String storageProp = System.getProperty("jettra.storage.mode", System.getenv("JETTRA_STORAGE_MODE"));
        if (storageProp != null && !storageProp.isBlank()) {
            try { config.setStorageMode(StorageMode.valueOf(storageProp.trim().toUpperCase())); } catch (Exception ignored) {}
        }

        JettraServer server = startApplication(config);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[App] Solicitud de apagado recibida. Deteniendo JettraServer...");
            server.stop();
        }));

        try {
            System.out.println("[App] Proceso en ejecución continua (escuchando conexiones). Presione Ctrl+C para detener.");
            server.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("[App] Hilo principal interrumpido. Saliendo...");
        }
    }

    public static JettraServer startApplication(int port, StorageMode mode) {
        ServerConfig config = ServerConfig.load()
            .setPort(port)
            .setStorageMode(mode);
        return startApplication(config);
    }

    public static JettraServer startApplication(ServerConfig config) {
        int port = config.getPort();
        JettraServer server = JettraServer.create(config);

        // 1. Inicializar Contenedor Maestro Jakarta EE
        JakartaContainer jakarta = JakartaContainer.attach(server);

        // 2. Inicializar Servicios de Negocio y Datos
        SecurityService securityService = new SecurityService(jakarta.getEntityManagerFactory());
        securityService.initDefaultUsers();
        jakarta.getCdiContainer().registerSingleton(SecurityService.class, securityService);

        ProductService productService = new ProductService(jakarta.getEntityManagerFactory());
        productService.initSampleCatalog();
        jakarta.getCdiContainer().registerSingleton(ProductService.class, productService);

        // 3. Registrar Servlet de PrimeFaces / Jakarta Faces (Login y Dashboard con plantilla)
        FacesRendererServlet facesServlet = new FacesRendererServlet(
            jakarta.getCdiContainer(), securityService, productService, server
        );
        jakarta.getServletBridge().registerServlet("/", facesServlet);
        jakarta.getServletBridge().registerServlet("/login.xhtml", facesServlet);
        jakarta.getServletBridge().registerServlet("/dashboard.xhtml", facesServlet);
        jakarta.getServletBridge().registerServlet("/productos.xhtml", facesServlet);
        jakarta.getServletBridge().registerServlet("/usuarios.xhtml", facesServlet);
        jakarta.getServletBridge().registerServlet("/flux/storage-monitor", facesServlet);
        jakarta.getServletBridge().registerServlet("/flux/telemetry", facesServlet);
        jakarta.getServletBridge().registerServlet("/flux/counter", facesServlet);
        jakarta.getServletBridge().registerServlet("/flux/catalogo", facesServlet);
        jakarta.getServletBridge().registerServlet("/flux/catalogo-flux", facesServlet);
        jakarta.getServletBridge().registerServlet("/logout", facesServlet);

        // 4. Registrar Recursos RESTful (JAX-RS)
        jakarta.getRestDispatcher().registerResource(new ProductResource(productService));
        jakarta.getRestDispatcher().registerResource(new ServerInfoResource(server));

        // 5. Conectar Módulo JettraFlux y Registrar Páginas Reactivas invocadas desde el menú
        FluxModule flux = FluxModule.attach(server);
        flux.page("/flux/storage-monitor", "JettraFlux - Monitor de Almacenamiento", () -> 
            new StorageMonitorFluxView(server.storage().getMode().name(), server.storage().getMode().getDescription())
        );
        flux.page("/flux/telemetry", "JettraFlux - Telemetría del Servidor", TelemetryFluxView.class);
        flux.page("/flux/counter", "JettraFlux - Contador Reactivo", InteractiveCounterFluxView.class);
        flux.page("/flux/catalogo", "JettraFlux - Catálogo de Productos", () -> new CatalogoFlux(productService));

        // 6. Registrar Swagger-UI con Acreditación de Seguridad usando JettraFlux
        flux.swagger("/swagger-ui", "JettraServerExample - Catálogo & Microservicios API", java.util.List.of(
            ProductResource.class,
            ServerInfoResource.class
        ));

        // Iniciar el servidor
        try {
            server.start();
        } catch (java.net.BindException e) {
            System.err.println("❌ ERROR: El puerto " + port + " ya está ocupado por otro proceso o contenedor (ej. Docker).");
            System.err.println("   -> Puedes cambiar el puerto en 'microprofile-config.properties' (server.port=8080) o pasar '-Dserver.port=8085'.");
            throw new RuntimeException("El puerto " + port + " ya está en uso (BindException).", e);
        } catch (Exception e) {
            throw new RuntimeException("Error iniciando JettraServer en puerto " + port, e);
        }

        System.out.println("==================================================================================");
        System.out.println("🚀 [JettraServerExample] Aplicación Web Jakarta EE + PrimeFaces + JettraFlux LISTA");
        System.out.println(" -> URL Login PrimeFaces : http://localhost:" + port + "/login.xhtml");
        System.out.println(" -> URL Dashboard        : http://localhost:" + port + "/dashboard.xhtml");
        System.out.println(" -> URL Catálogo JPA     : http://localhost:" + port + "/productos.xhtml");
        System.out.println(" -> Swagger-UI (JettraFlux): http://localhost:" + port + "/swagger-ui");
        System.out.println(" -> Vistas JettraFlux    : http://localhost:" + port + "/flux/storage-monitor");
        System.out.println("                         : http://localhost:" + port + "/flux/telemetry");
        System.out.println("                         : http://localhost:" + port + "/flux/counter");
        System.out.println(" -> Endpoints REST       : http://localhost:" + port + "/api/productos");
        System.out.println("                         : http://localhost:" + port + "/api/info");
        System.out.println("==================================================================================");

        return server;
    }
}
