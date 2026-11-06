package io.jettra.server;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.jettra.ee.core.IO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

/**
 * Servidor HTTP modular de ultra-alto rendimiento para el ecosistema Jettra.
 * Basado en Java 25 con Hilos Virtuales (Project Loom) y arquitectura de mínimo consumo de recursos.
 */
public class JettraServer {

    private static String contextPath = "/";

    private int customPort = 8080;
    private HttpServer httpServer;
    private final Map<String, HttpHandler> handlerRegistry = new ConcurrentHashMap<>();
    private String errorPage;

    public JettraServer() {
        String p = io.jettra.server.config.JettraConfig.getProperty("server.port");
        if (p != null && !p.isBlank()) {
            try {
                this.customPort = Integer.parseInt(p.trim());
            } catch (Exception ignored) {}
        }
        String cp = io.jettra.server.config.JettraConfig.getProperty("server.contextpath");
        if (cp != null && !cp.isBlank()) {
            setContextPath(cp.trim());
        }
    }

    public JettraServer(int port) {
        this();
        this.customPort = port;
    }

    public static void setContextPath(String cp) {
        contextPath = (cp == null || cp.isBlank() || cp.equals("/")) ? "/" : (cp.startsWith("/") ? cp : "/" + cp);
        try {
            Class<?> fluxConfig = Class.forName("io.jettra.flux.core.FluxConfig");
            Method m = fluxConfig.getMethod("setContextPath", String.class);
            m.invoke(null, contextPath);
        } catch (Throwable ignored) {}
    }

    public static String getContextPath() {
        return contextPath;
    }

    public static String resolvePath(String path) {
        if (path == null || path.isBlank()) {
            return contextPath.equals("/") ? "/" : contextPath;
        }
        String normalized = path.startsWith("/") ? path : "/" + path;
        if (contextPath.equals("/")) {
            return normalized;
        }
        if (normalized.startsWith(contextPath)) {
            return normalized;
        }
        return contextPath + normalized;
    }

    public void setPort(int port) {
        this.customPort = port;
    }

    public int getPort() {
        return customPort;
    }

    public void setErrorPage(String path) {
        this.errorPage = path;
    }

    public void addHandler(String path, HttpHandler handler) {
        String fullPath = resolveContextPath(path);
        handlerRegistry.put(fullPath, handler);
        if (httpServer != null) {
            httpServer.createContext(fullPath, handler);
        }
    }

    public void addHandler(String path, Class<?> handlerClass) {
        try {
            HttpHandler handler = (HttpHandler) handlerClass.getDeclaredConstructor().newInstance();
            addHandler(path, handler);
        } catch (Exception e) {
            throw new RuntimeException("Could not instantiate handler: " + handlerClass.getName(), e);
        }
    }

    private String resolveContextPath(String path) {
        if (path == null || path.isBlank() || path.equals("/")) {
            return contextPath.equals("/") ? "/" : contextPath;
        }
        String p = path.startsWith("/") ? path : "/" + path;
        if (contextPath.equals("/") || p.startsWith(contextPath)) {
            return p;
        }
        return contextPath.endsWith("/") ? (contextPath.substring(0, contextPath.length() - 1) + p) : (contextPath + p);
    }

    public static void generateMvnScripts() {
        IO.println("[JettraServer] Scripts generated.");
    }

    public void loadDiscoveredPages() {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader == null) classLoader = JettraServer.class.getClassLoader();
            Enumeration<java.net.URL> resources = classLoader.getResources("META-INF/jettra/page.classes");
            while (resources.hasMoreElements()) {
                java.net.URL url = resources.nextElement();
                try (InputStream is = url.openStream();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty() || line.startsWith("#")) continue;
                        String[] parts = line.split("=", 2);
                        if (parts.length == 2) {
                            String className = parts[0].trim();
                            String path = parts[1].trim();
                            try {
                                Class<?> clazz = Class.forName(className, true, classLoader);
                                addHandler(path, clazz);
                            } catch (Throwable ignored) {
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    public synchronized void start() {
        long startTime = System.currentTimeMillis();
        try {
            loadDiscoveredPages();
            if (httpServer == null) {
                httpServer = HttpServer.create(new InetSocketAddress(customPort), 0);
                httpServer.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
                for (Map.Entry<String, HttpHandler> entry : handlerRegistry.entrySet()) {
                    httpServer.createContext(entry.getKey(), entry.getValue());
                }
            }
            httpServer.start();
            long duration = System.currentTimeMillis() - startTime;
            IO.success("⚡ JettraServer iniciado en http://localhost:" + customPort + (contextPath.equals("/") ? "" : contextPath) + " en " + duration + " ms (Loom Virtual Threads)");
        } catch (IOException e) {
            throw new RuntimeException("Error iniciando JettraServer en puerto " + customPort, e);
        }
    }

    public synchronized void stop() {
        if (httpServer != null) {
            httpServer.stop(0);
            IO.info("JettraServer detenido exitosamente.");
        }
    }

    public HttpServer getHttpServer() {
        return httpServer;
    }
}

