package io.jettra.server.backend.rest;

import io.jettra.server.openapi.annotations.ApiResponse;
import io.jettra.server.openapi.annotations.Operation;
import io.jettra.server.openapi.annotations.Tag;
import io.jettra.rest.annotations.GET;
import io.jettra.rest.annotations.Path;
import io.jettra.server.core.http.HttpRequest;
import io.jettra.server.core.http.HttpResponse;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Microservicio de telemetría y estado del sistema.
 */
@Path("/api/v1/system")
@Tag(name = "Sistema y Telemetría", description = "Endpoints de verificación y métricas de servidor")
public class SystemInfoResource {

    @GET
    @Path("/ping")
    @Operation(summary = "Verificación rápida (Ping)", description = "Endpoint público sin autenticación para liveness.")
    @ApiResponse(responseCode = "200", description = "Servidor en línea")
    public void ping(HttpRequest req, HttpResponse res) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("status", "UP");
        map.put("service", "JettraServerBackEndExample");
        map.put("timestamp", Instant.now().toString());
        res.json(map);
    }

    @GET
    @Path("/info")
    @Operation(summary = "Información del entorno de ejecución", description = "Detalles de Java 25, Project Loom y memoria.")
    @ApiResponse(responseCode = "200", description = "Información de sistema")
    public void info(HttpRequest req, HttpResponse res) {
        Runtime rt = Runtime.getRuntime();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("server", "JettraServer 1.0.0");
        map.put("javaVersion", System.getProperty("java.version"));
        map.put("virtualThreadsActive", true);
        map.put("availableProcessors", rt.availableProcessors());
        map.put("freeMemoryBytes", rt.freeMemory());
        map.put("totalMemoryBytes", rt.totalMemory());
        map.put("uptimeMillis", ManagementFactory.getRuntimeMXBean().getUptime());
        res.json(map);
    }
}

