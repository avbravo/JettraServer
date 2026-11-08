package io.jettra.server.core;

import io.jettra.server.core.config.ServerConfig;
import io.jettra.server.core.health.HealthService;
import io.jettra.server.core.http.HttpFilter;
import io.jettra.server.core.http.HttpHandler;
import io.jettra.server.core.http.HttpMethod;
import io.jettra.server.core.http.HttpRouter;
import io.jettra.server.core.nio.NioHttpServer;
import io.jettra.server.core.security.JettraJwtAuthenticator;
import io.jettra.server.core.security.SecurityContext;
import io.jettra.server.core.security.db.JettraSecurityDB;
import io.jettra.server.core.security.entity.JRole;
import io.jettra.server.core.security.entity.JUser;
import io.jettra.server.core.security.repository.JettraSecurityDBInitializer;
import io.jettra.server.core.security.service.JettraSecurityService;
import io.jettra.server.core.storage.JettraStorageEngine;
import io.jettra.server.core.storage.StorageEngineFactory;
import io.jettra.server.core.storage.StorageMetrics;
import io.jettra.server.core.storage.StorageMode;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Map;

/**
 * Servidor modular de alto rendimiento JettraServer.
 * Administra el ciclo de vida del servidor NIO, Loom Virtual Threads, persistencia tri-modal y seguridad.
 */
public class JettraServer {

    private final ServerConfig config;
    private final HttpRouter router;
    private final NioHttpServer nioServer;
    private final HealthService healthService;
    private final JettraJwtAuthenticator authenticator;
    private final JettraSecurityDB securityDB;
    private final JettraSecurityService securityService;
    private JettraStorageEngine<String, Object> storageEngine;
    private long startupTimeMillis;

    public JettraServer() {
        this(new ServerConfig());
    }

    public JettraServer(ServerConfig config) {
        this.config = config;
        this.router = new HttpRouter();
        this.nioServer = new NioHttpServer(config, router);
        this.healthService = new HealthService();
        this.authenticator = new JettraJwtAuthenticator(config.getJwtSecret(), config.getJwtExpirationMillis());

        // Inicializar motor de persistencia según configuración
        this.storageEngine = StorageEngineFactory.initDefaultEngine(config);

        // Inicializar base de datos de seguridad de la instancia JettraSecurityDB
        this.securityDB = new JettraSecurityDB(config.getSecurityDbPath());
        JettraSecurityDBInitializer.initializeIfEmpty(this.securityDB);
        this.securityService = new JettraSecurityService(this.securityDB);
        this.securityService.configureJwt(config.getJwtSecret(), config.getJwtExpirationMillis());

        // Registrar rutas de monitoreo y seguridad por defecto
        setupDefaultEndpoints();
    }

    public static JettraServer create() {
        return new JettraServer();
    }

    public static JettraServer create(ServerConfig config) {
        return new JettraServer(config);
    }

    public static String resolvePath(String path) {
        if (path == null || path.isBlank()) return "/";
        return path.startsWith("/") ? path : "/" + path;
    }

    private void setupDefaultEndpoints() {
        router.get("/health", healthService::handleHealth);
        router.get("/health/live", healthService::handleLive);
        router.get("/health/ready", healthService::handleReady);
        router.get("/q/health", healthService::handleHealth);
        router.get("/q/health/live", healthService::handleLive);
        router.get("/q/health/ready", healthService::handleReady);

        // Endpoint de métricas de persistencia y servidor
        router.get("/metrics", (req, res) -> {
            StorageMetrics sm = storageEngine.getMetrics();
            Runtime rt = Runtime.getRuntime();
            Map<String, Object> metricsMap = new java.util.LinkedHashMap<>();
            metricsMap.put("server", "JettraServer 1.0.0");
            metricsMap.put("storageMode", sm.mode().name());
            metricsMap.put("storageModeDescription", sm.mode().getDescription());
            metricsMap.put("storageRecords", sm.totalRecords());
            metricsMap.put("storageMemoryUsedBytes", sm.memoryUsedBytes());
            metricsMap.put("storageMemoryReadable", sm.humanReadableMemory());
            metricsMap.put("storageHitRatePercent", sm.hitRatePercentage());
            metricsMap.put("storageReadOps", sm.readOperations());
            metricsMap.put("storageWriteOps", sm.writeOperations());
            metricsMap.put("jvmFreeMemoryBytes", rt.freeMemory());
            metricsMap.put("jvmTotalMemoryBytes", rt.totalMemory());
            metricsMap.put("jvmAvailableProcessors", rt.availableProcessors());
            metricsMap.put("jvmUptimeMillis", ManagementFactory.getRuntimeMXBean().getUptime());
            res.json(metricsMap);
        });

        // Endpoints de Seguridad y Autenticación con JettraSecurityDB
        HttpHandler loginHandler = (req, res) -> {
            String user = req.getQueryParam("username");
            String pass = req.getQueryParam("password");
            if ((user == null || pass == null) && req.getBody().length > 0) {
                try {
                    Map<?, ?> body = req.getBodyAsJson(Map.class);
                    if (body != null) {
                        if (user == null && body.get("username") != null) user = body.get("username").toString();
                        if (pass == null && body.get("password") != null) pass = body.get("password").toString();
                    }
                } catch (Exception ignored) {}
            }

            if (user == null || pass == null || user.isBlank() || pass.isBlank()) {
                res.status(401).json(Map.of("error", "Usuario y contraseña son requeridos"));
                return;
            }

            var tokenOpt = securityService.authenticate(user, pass);
            if (tokenOpt.isEmpty()) {
                res.status(401).json(Map.of("error", "Credenciales incorrectas o usuario inactivo"));
                return;
            }

            String token = tokenOpt.get();
            JUser jUser = securityService.findUser(user).orElse(null);
            java.util.List<String> roles = new java.util.ArrayList<>();
            if (jUser != null && jUser.jRoles() != null) {
                for (JRole r : jUser.jRoles()) {
                    if (r != null && r.name() != null) roles.add(r.name());
                }
            }

            Map<String, Object> authResp = new java.util.LinkedHashMap<>();
            authResp.put("token", "Bearer " + token);
            authResp.put("rawToken", token);
            authResp.put("tokenType", "Bearer");
            authResp.put("username", user);
            authResp.put("roles", roles);
            authResp.put("expiresInMs", securityService.getJwtExpirationMs());
            res.status(200).json(authResp);
        };

        router.post("/api/auth/login", loginHandler);
        router.get("/api/auth/login", loginHandler);

        // Perfil autenticado
        router.get("/api/auth/me", (req, res) -> {
            String authHeader = req.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                res.status(401).json(Map.of("error", "Token de autorización requerido"));
                return;
            }
            var userOpt = securityService.validateTokenAndGetUser(authHeader);
            if (userOpt.isEmpty()) {
                res.status(401).json(Map.of("error", "Token inválido o expirado"));
                return;
            }
            JUser u = userOpt.get();
            java.util.List<String> roles = (u.jRoles() != null)
                    ? u.jRoles().stream().map(JRole::name).toList()
                    : java.util.List.of();

            Map<String, Object> profile = new java.util.LinkedHashMap<>();
            profile.put("id", u.id().toString());
            profile.put("username", u.username());
            profile.put("email", u.email());
            profile.put("phone", u.phone());
            profile.put("active", u.active());
            profile.put("roles", roles);
            res.json(profile);
        });

        // Gestión Administrativa de JettraSecurityDB (Usuarios y Roles)
        io.jettra.server.core.security.rest.SecurityManagementResource secResource =
                new io.jettra.server.core.security.rest.SecurityManagementResource(securityService);

        router.get("/api/security/users", secResource::listUsers);
        router.post("/api/security/users", secResource::createUser);
        router.delete("/api/security/users", secResource::deleteUser);
        router.delete("/api/security/users/{username}", (req, res) -> {
            secResource.deleteUserByUsername(req.getPathParam("username"), res);
        });
        router.post("/api/security/users/role", secResource::updateRole);
        router.post("/api/security/users/status", secResource::updateStatus);
        router.post("/api/security/users/password", secResource::updatePassword);
        router.get("/api/security/roles", secResource::listRoles);
        router.post("/api/security/roles", secResource::createRole);
    }

    public JettraServer get(String path, HttpHandler handler) {
        router.get(path, handler);
        return this;
    }

    public JettraServer post(String path, HttpHandler handler) {
        router.post(path, handler);
        return this;
    }

    public JettraServer put(String path, HttpHandler handler) {
        router.put(path, handler);
        return this;
    }

    public JettraServer delete(String path, HttpHandler handler) {
        router.delete(path, handler);
        return this;
    }

    public JettraServer patch(String path, HttpHandler handler) {
        router.patch(path, handler);
        return this;
    }

    public JettraServer route(HttpMethod method, String path, HttpHandler handler) {
        router.add(method, path, handler);
        return this;
    }

    public JettraServer filter(HttpFilter filter) {
        nioServer.addFilter(filter);
        return this;
    }

    public synchronized JettraServer start() throws IOException {
        long start = System.currentTimeMillis();
        nioServer.start();
        this.startupTimeMillis = System.currentTimeMillis() - start;
        printBanner();
        return this;
    }

    public synchronized void stop() {
        nioServer.stop();
        if (storageEngine != null) {
            storageEngine.flush();
            storageEngine.close();
        }
        System.out.println("[JettraServer] Servidor detenido exitosamente.");
    }

    public void await() throws InterruptedException {
        nioServer.await();
    }

    public boolean isRunning() {
        return nioServer.isRunning();
    }

    public int getPort() {
        return nioServer.getPort();
    }

    public ServerConfig getConfig() {
        return config;
    }

    public HttpRouter getRouter() {
        return router;
    }

    public HealthService getHealthService() {
        return healthService;
    }

    public JettraJwtAuthenticator getAuthenticator() {
        return authenticator;
    }

    public JettraSecurityDB getSecurityDB() {
        return securityDB;
    }

    public JettraSecurityService getSecurityService() {
        return securityService;
    }

    public JettraStorageEngine<String, Object> storage() {
        return storageEngine;
    }

    public void setStorageMode(StorageMode newMode) {
        config.setStorageMode(newMode);
        StorageEngineFactory.switchDefaultEngine(newMode, config);
        this.storageEngine = StorageEngineFactory.getDefaultEngine();
    }

    private void printBanner() {
        System.out.println("==================================================================================");
        System.out.println("      __      __  __               _____                                          ");
        System.out.println("     / /___  / /_/ /__________ _  / ___/___  ______   _____  _____                ");
        System.out.println("    / / _ \\/ __/ __/ ___/ __ `/  \\__ \\/ _ \\/ ___/ | / / _ \\/ ___/                ");
        System.out.println("   / /  __/ /_/ /_/ /  / /_/ /  ___/ /  __/ /   | |/ /  __/ /                     ");
        System.out.println(" _/ /\\___/\\__/\\__/_/   \\__,_/  /____/\\___/_/    |___/\\___/_/                      ");
        System.out.println("/___/   Jettra Enterprise Application Server  ");
        System.out.println("==================================================================================");
        System.out.println(" -> Escuchando en      : http://" + config.getHost() + ":" + getPort() + config.getContextPath());
        System.out.println(" -> Modo Persistencia  : [" + config.getStorageMode().name() + "] - " + config.getStorageMode().getDescription());
        System.out.println(" -> JettraSecurityDB   : " + securityDB.getBaseDir());
        System.out.println(" -> Concurrencia       : Virtual Threads (Project Loom) activados");
        System.out.println(" -> Arranque en frío   : " + startupTimeMillis + " ms");
        System.out.println(" -> Endpoints Activos  : /health, /health/live, /health/ready, /metrics, /api/auth/login, /api/auth/me");
        System.out.println("==================================================================================");
    }
}
