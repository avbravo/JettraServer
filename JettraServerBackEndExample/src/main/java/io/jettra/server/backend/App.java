package io.jettra.server.backend;

import io.jettra.server.backend.rest.CustomerResource;
import io.jettra.server.backend.rest.OrderResource;
import io.jettra.server.backend.rest.SystemInfoResource;
import io.jettra.server.backend.service.CustomerService;
import io.jettra.server.backend.service.OrderService;
import io.jettra.server.core.JettraServer;
import io.jettra.server.core.config.ServerConfig;
import io.jettra.server.core.http.HttpFilter;
import io.jettra.server.core.security.SecurityContext;
import io.jettra.server.core.security.entity.JUser;
import io.jettra.server.flux.FluxModule;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.Map;

/**
 * Aplicación Principal que inicializa JettraServerBackEndExample.
 * Microservicios de backend de alto rendimiento con Project Loom, autenticación JWT Bearer,
 * JettraSecurityDB y Swagger-UI interactivo generado por JettraFlux.
 */
public class App {

    private JettraServer server;
    private final CustomerService customerService = new CustomerService();
    private final OrderService orderService = new OrderService();
    private final CustomerResource customerResource = new CustomerResource(customerService);
    private final OrderResource orderResource = new OrderResource(orderService);
    private final SystemInfoResource systemInfoResource = new SystemInfoResource();

    public static void main(String[] args) {
        App app = new App();
        app.startApplication(args);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[App] Solicitud de apagado recibida. Deteniendo JettraServerBackEndExample...");
            app.stop();
        }));

        try {
            System.out.println("[App] Proceso en ejecución continua (escuchando microservicios). Presione Ctrl+C para detener.");
            app.getServer().await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("[App] Hilo principal interrumpido. Saliendo...");
        }
    }

    public synchronized void startApplication(String[] args) {
        ServerConfig config = ServerConfig.load();

        // Resolución dinámica de puerto si está ocupado
        int configuredPort = config.getPort();
        int activePort = findAvailablePort(configuredPort);
        config.setPort(activePort);

        this.server = JettraServer.create(config);

        // 1. Filtro de Seguridad JWT para endpoints protegidos de microservicios
        setupSecurityFilter();

        // 2. Registro de rutas REST de microservicios
        registerMicroservicesRoutes();

        // 3. Swagger-UI con Acreditación de Seguridad usando JettraFlux
        List<Class<?>> controllers = List.of(
                CustomerResource.class,
                OrderResource.class,
                SystemInfoResource.class
        );

        FluxModule.attach(server)
                .swagger("/swagger-ui", "JettraServer Backend Microservices API", controllers);

        // Redirección de conveniencia
        server.get("/", (req, res) -> res.header("Location", "/swagger-ui").status(302).body(""));
        server.get("/docs", (req, res) -> res.header("Location", "/swagger-ui").status(302).body(""));
        server.get("/swagger", (req, res) -> res.header("Location", "/swagger-ui").status(302).body(""));

        try {
            server.start();
            printSuccessBanner(activePort);
        } catch (IOException e) {
            throw new RuntimeException("Error iniciando JettraServer en puerto " + activePort, e);
        }
    }

    private void setupSecurityFilter() {
        HttpFilter authFilter = (req, res, chain) -> {
            String path = req.getPath();

            // Endpoints que requieren autenticación por Token JWT
            boolean isProtected = (path.startsWith("/api/v1/customers") || path.startsWith("/api/v1/orders"))
                    && !path.equals("/api/v1/system/ping");

            if (!isProtected) {
                chain.doFilter(req, res);
                return;
            }

            String authHeader = req.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                res.status(401).json(Map.of(
                        "error", "Acreditación requerida",
                        "message", "Debe proveer un token Bearer en el encabezado 'Authorization' o autenticarse en /swagger-ui con usuario 'admin' y clave 'admin'",
                        "status", 401
                ));
                return;
            }

            var userOpt = server.getSecurityService().validateTokenAndGetUser(authHeader);
            if (userOpt.isEmpty()) {
                res.status(401).json(Map.of(
                        "error", "Token inválido o expirado",
                        "message", "El token JWT provisto no es válido o ha expirado. Por favor acredítese nuevamente.",
                        "status", 401
                ));
                return;
            }

            JUser user = userOpt.get();
            req.setAttribute("currentUser", user);
            chain.doFilter(req, res);
        };

        server.filter(authFilter);
    }

    private void registerMicroservicesRoutes() {
        // Microservicio de Clientes
        server.get("/api/v1/customers", customerResource::listCustomers);
        server.get("/api/v1/customers/{id}", (req, res) -> customerResource.getCustomer(req.getPathParam("id"), req, res));
        server.post("/api/v1/customers", customerResource::createCustomer);
        server.delete("/api/v1/customers/{id}", (req, res) -> customerResource.deleteCustomer(req.getPathParam("id"), req, res));

        // Microservicio de Órdenes
        server.get("/api/v1/orders", orderResource::listOrders);
        server.get("/api/v1/orders/{id}", (req, res) -> orderResource.getOrder(req.getPathParam("id"), req, res));
        server.post("/api/v1/orders", orderResource::createOrder);
        server.put("/api/v1/orders/{id}/cancel", (req, res) -> orderResource.cancelOrder(req.getPathParam("id"), req, res));

        // Telemetría y Salud
        server.get("/api/v1/system/ping", systemInfoResource::ping);
        server.get("/api/v1/system/info", systemInfoResource::info);
    }

    private void printSuccessBanner(int port) {
        System.out.println("==================================================================================");
        System.out.println("🚀 [JettraServerBackEndExample] Microservicios Backend en Java 25 & Project Loom");
        System.out.println("==================================================================================");
        System.out.println(" -> Swagger-UI (JettraFlux) : http://localhost:" + port + "/swagger-ui");
        System.out.println(" -> Especificación OpenAPI  : http://localhost:" + port + "/openapi.json");
        System.out.println(" -> Acreditación JWT (Auth) : http://localhost:" + port + "/api/auth/login");
        System.out.println(" -> Microservicio Clientes  : http://localhost:" + port + "/api/v1/customers");
        System.out.println(" -> Microservicio Órdenes   : http://localhost:" + port + "/api/v1/orders");
        System.out.println(" -> Telemetría de Sistema   : http://localhost:" + port + "/api/v1/system/info");
        System.out.println("==================================================================================");
    }

    public synchronized void stop() {
        if (server != null) {
            server.stop();
        }
    }

    public JettraServer getServer() {
        return server;
    }

    private int findAvailablePort(int preferredPort) {
        if (isPortAvailable(preferredPort)) {
            return preferredPort;
        }
        for (int p = preferredPort + 1; p < preferredPort + 50; p++) {
            if (isPortAvailable(p)) {
                return p;
            }
        }
        return preferredPort;
    }

    private boolean isPortAvailable(int port) {
        try (ServerSocket ss = new ServerSocket(port)) {
            ss.setReuseAddress(true);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

