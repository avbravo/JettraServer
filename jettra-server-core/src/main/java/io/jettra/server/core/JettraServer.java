package io.jettra.server.core;

import io.jettra.server.core.config.ServerConfig;
import io.jettra.server.core.health.HealthService;
import io.jettra.server.core.http.HttpFilter;
import io.jettra.server.core.http.HttpHandler;
import io.jettra.server.core.http.HttpMethod;
import io.jettra.server.core.http.HttpRouter;
import io.jettra.server.core.nio.NioHttpServer;
import io.jettra.server.core.security.JettraJwtAuthenticator;
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

        // Registrar rutas de monitoreo por defecto
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
        System.out.println("/___/   Google Antigravity Enterprise Application Server - Java 25 & Project Loom ");
        System.out.println("==================================================================================");
        System.out.println(" -> Escuchando en      : http://" + config.getHost() + ":" + getPort() + config.getContextPath());
        System.out.println(" -> Modo Persistencia  : [" + config.getStorageMode().name() + "] - " + config.getStorageMode().getDescription());
        System.out.println(" -> Concurrencia       : Virtual Threads (Project Loom) activados");
        System.out.println(" -> Arranque en frío   : " + startupTimeMillis + " ms");
        System.out.println(" -> Endpoints Activos  : /health, /health/live, /health/ready, /metrics");
        System.out.println("==================================================================================");
    }
}
