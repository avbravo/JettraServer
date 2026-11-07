package io.jettra.server.core.config;

import io.jettra.server.core.storage.StorageMode;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Configuración central del servidor JettraServer.
 * Permite resolución jerárquica: Argumentos CLI > System Properties > Variables de Entorno > Archivo de propiedades.
 */
public class ServerConfig {

    public static final String DEFAULT_HOST = "0.0.0.0";
    public static final int DEFAULT_PORT = 8080;
    public static final int DEFAULT_BACKLOG = 1024;
    public static final StorageMode DEFAULT_STORAGE_MODE = StorageMode.JVM_RAM;
    public static final String DEFAULT_CONTEXT_PATH = "/";
    public static final long DEFAULT_MAX_BODY_BYTES = 10 * 1024 * 1024; // 10MB
    public static final String DEFAULT_DATA_DIR = "./data/jettra-server";

    private String host = DEFAULT_HOST;
    private int port = DEFAULT_PORT;
    private int backlog = DEFAULT_BACKLOG;
    private StorageMode storageMode = DEFAULT_STORAGE_MODE;
    private String contextPath = DEFAULT_CONTEXT_PATH;
    private long maxRequestBodyBytes = DEFAULT_MAX_BODY_BYTES;
    private String dataDir = DEFAULT_DATA_DIR;
    private String securityDbPath = null;
    private boolean corsEnabled = true;
    private String jwtSecret = "jettra_server_super_secure_jwt_key_2026";
    private long jwtExpirationMillis = 86400000L; // 24 horas
    private boolean virtualThreads = true;
    private int offHeapPageSizeBytes = 64 * 1024; // 64KB

    public ServerConfig() {
        loadDefaults();
    }

    public static ServerConfig load() {
        ServerConfig config = new ServerConfig();
        config.loadDefaults();
        return config;
    }

    private void loadDefaults() {
        Properties props = new Properties();

        // 1. Cargar desde classpath jettraserver.config
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("jettraserver.config")) {
            if (is != null) {
                props.load(is);
            }
        } catch (Exception ignored) {}

        // Fallback classpath jettra-server.properties
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("jettra-server.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (Exception ignored) {}

        // 2. Archivo personalizado especificado por propiedad de sistema o variable de entorno
        String customConfigPath = resolveString("jettraserver.config", "JETTRASERVER_CONFIG", null);
        if (customConfigPath != null) {
            Path customFile = Path.of(customConfigPath);
            if (Files.exists(customFile)) {
                try (InputStream is = Files.newInputStream(customFile)) {
                    props.load(is);
                } catch (Exception ignored) {}
            }
        }

        // 3. Buscar en el directorio de ejecución actual o ./config
        String[] candidatePaths = {"./jettraserver.config", "./config/jettraserver.config", "./jettra-server.properties", "./config/jettra-server.properties"};
        for (String candidate : candidatePaths) {
            Path file = Path.of(candidate);
            if (Files.exists(file)) {
                try (InputStream is = Files.newInputStream(file)) {
                    props.load(is);
                } catch (Exception ignored) {}
            }
        }

        this.host = resolveProp(props, new String[]{"server.host", "host", "jettra.server.host"}, "JETTRA_SERVER_HOST", this.host);
        this.port = resolveIntProp(props, new String[]{"server.port", "port", "jettra.server.port"}, "JETTRA_SERVER_PORT", this.port);
        this.contextPath = resolveProp(props, new String[]{"contextpath", "server.contextpath", "server.context-path", "server.path", "path", "contextPath", "jettra.server.context-path", "jettra.server.contextpath"}, "JETTRA_SERVER_CONTEXT_PATH", this.contextPath);
        this.dataDir = resolveProp(props, new String[]{"data.dir", "server.data-dir", "jettra.server.data-dir"}, "JETTRA_SERVER_DATA_DIR", this.dataDir);
        this.securityDbPath = resolveProp(props, new String[]{"security.db.path", "server.security.db.path", "securitydb.path", "jettra.securitydb.path"}, "JETTRA_SECURITYDB_PATH", this.dataDir + "/securitydb");
        this.jwtSecret = resolveProp(props, new String[]{"jwt.secret", "server.jwt.secret", "jettra.server.jwt.secret"}, "JETTRA_SERVER_JWT_SECRET", this.jwtSecret);
        this.corsEnabled = Boolean.parseBoolean(resolveProp(props, new String[]{"cors.enabled", "cors", "jettra.server.cors.enabled"}, "JETTRA_SERVER_CORS_ENABLED", String.valueOf(this.corsEnabled)));
        this.virtualThreads = Boolean.parseBoolean(resolveProp(props, new String[]{"server.virtual-threads", "virtual.threads", "jettra.server.virtual-threads"}, "JETTRA_SERVER_VIRTUAL_THREADS", String.valueOf(this.virtualThreads)));

        String modeStr = resolveProp(props, new String[]{"storage.mode", "storageMode", "jettra.storage.mode", "mode"}, "JETTRA_STORAGE_MODE", this.storageMode.name());
        this.storageMode = StorageMode.fromString(modeStr);

        this.offHeapPageSizeBytes = resolveIntProp(props, new String[]{"storage.offheap.page-size", "offheap.page-size"}, "JETTRA_OFFHEAP_PAGE_SIZE", this.offHeapPageSizeBytes);
    }

    private String resolveProp(Properties props, String[] keys, String envVar, String defaultVal) {
        for (String key : keys) {
            String sys = System.getProperty(key);
            if (sys != null && !sys.isBlank()) return sys.trim();
        }
        if (envVar != null) {
            String env = System.getenv(envVar);
            if (env != null && !env.isBlank()) return env.trim();
        }
        for (String key : keys) {
            String p = props.getProperty(key);
            if (p != null && !p.isBlank()) return p.trim();
        }
        return defaultVal;
    }

    private int resolveIntProp(Properties props, String[] keys, String envVar, int defaultVal) {
        String val = resolveProp(props, keys, envVar, null);
        if (val != null) {
            try { return Integer.parseInt(val); } catch (NumberFormatException ignored) {}
        }
        return defaultVal;
    }

    private static String resolveString(String sysProp, String envVar, String defaultVal) {
        String val = System.getProperty(sysProp);
        if (val != null && !val.isBlank()) return val.trim();
        val = System.getenv(envVar);
        if (val != null && !val.isBlank()) return val.trim();
        return defaultVal;
    }

    private static int resolveInt(String sysProp, String envVar, int defaultVal) {
        String val = resolveString(sysProp, envVar, null);
        if (val != null) {
            try { return Integer.parseInt(val); } catch (NumberFormatException ignored) {}
        }
        return defaultVal;
    }

    // Getters y Setters fluidos
    public String getHost() { return host; }
    public ServerConfig setHost(String host) { this.host = host; return this; }

    public int getPort() { return port; }
    public ServerConfig setPort(int port) { this.port = port; return this; }

    public int getBacklog() { return backlog; }
    public ServerConfig setBacklog(int backlog) { this.backlog = backlog; return this; }

    public StorageMode getStorageMode() { return storageMode; }
    public ServerConfig setStorageMode(StorageMode storageMode) { this.storageMode = storageMode; return this; }

    public String getContextPath() { return contextPath; }
    public ServerConfig setContextPath(String contextPath) {
        if (!contextPath.startsWith("/")) contextPath = "/" + contextPath;
        if (contextPath.length() > 1 && contextPath.endsWith("/")) {
            contextPath = contextPath.substring(0, contextPath.length() - 1);
        }
        this.contextPath = contextPath;
        return this;
    }

    public long getMaxRequestBodyBytes() { return maxRequestBodyBytes; }
    public ServerConfig setMaxRequestBodyBytes(long maxRequestBodyBytes) { this.maxRequestBodyBytes = maxRequestBodyBytes; return this; }

    public String getDataDir() { return dataDir; }
    public ServerConfig setDataDir(String dataDir) {
        this.dataDir = dataDir;
        if (this.securityDbPath == null || this.securityDbPath.endsWith("/securitydb")) {
            this.securityDbPath = dataDir + "/securitydb";
        }
        return this;
    }

    public String getSecurityDbPath() {
        if (securityDbPath == null) {
            return dataDir + "/securitydb";
        }
        return securityDbPath;
    }
    public ServerConfig setSecurityDbPath(String securityDbPath) { this.securityDbPath = securityDbPath; return this; }

    public boolean isCorsEnabled() { return corsEnabled; }
    public ServerConfig setCorsEnabled(boolean corsEnabled) { this.corsEnabled = corsEnabled; return this; }

    public String getJwtSecret() { return jwtSecret; }
    public ServerConfig setJwtSecret(String jwtSecret) { this.jwtSecret = jwtSecret; return this; }

    public long getJwtExpirationMillis() { return jwtExpirationMillis; }
    public ServerConfig setJwtExpirationMillis(long jwtExpirationMillis) { this.jwtExpirationMillis = jwtExpirationMillis; return this; }

    public boolean isVirtualThreads() { return virtualThreads; }
    public ServerConfig setVirtualThreads(boolean virtualThreads) { this.virtualThreads = virtualThreads; return this; }

    public int getOffHeapPageSizeBytes() { return offHeapPageSizeBytes; }
    public ServerConfig setOffHeapPageSizeBytes(int offHeapPageSizeBytes) { this.offHeapPageSizeBytes = offHeapPageSizeBytes; return this; }
}
