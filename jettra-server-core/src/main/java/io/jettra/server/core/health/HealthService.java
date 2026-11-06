package io.jettra.server.core.health;

import io.jettra.server.core.http.HttpRequest;
import io.jettra.server.core.http.HttpResponse;
import io.jettra.server.core.http.HttpStatus;
import io.jettra.server.core.storage.JettraStorageEngine;
import io.jettra.server.core.storage.StorageEngineFactory;
import io.jettra.server.core.storage.StorageMetrics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Servicio de monitorización de salud del servidor y del motor de almacenamiento.
 */
public class HealthService {

    private final List<HealthCheck> liveChecks = new CopyOnWriteArrayList<>();
    private final List<HealthCheck> readyChecks = new CopyOnWriteArrayList<>();

    public HealthService() {
        // Chequeo por defecto: Memoria de la JVM
        liveChecks.add(() -> {
            Runtime rt = Runtime.getRuntime();
            long free = rt.freeMemory();
            long total = rt.totalMemory();
            long max = rt.maxMemory();
            Map<String, Object> memData = Map.of(
                "freeMemoryBytes", free,
                "totalMemoryBytes", total,
                "maxMemoryBytes", max,
                "availableProcessors", rt.availableProcessors()
            );
            return HealthStatus.up("jvm-memory", memData);
        });

        // Chequeo por defecto: Motor de persistencia
        readyChecks.add(() -> {
            try {
                JettraStorageEngine<String, Object> engine = StorageEngineFactory.getDefaultEngine();
                StorageMetrics metrics = engine.getMetrics();
                Map<String, Object> sData = Map.of(
                    "storageMode", metrics.mode().name(),
                    "totalRecords", metrics.totalRecords(),
                    "memoryUsedBytes", metrics.memoryUsedBytes(),
                    "hitRatePercent", metrics.hitRatePercentage()
                );
                return HealthStatus.up("jettra-storage", sData);
            } catch (Exception e) {
                return HealthStatus.down("jettra-storage", e.getMessage());
            }
        });
    }

    public void addLiveCheck(HealthCheck check) {
        if (check != null) liveChecks.add(check);
    }

    public void addReadyCheck(HealthCheck check) {
        if (check != null) readyChecks.add(check);
    }

    public void handleHealth(HttpRequest req, HttpResponse res) {
        List<HealthStatus> results = new ArrayList<>();
        boolean allUp = evaluate(liveChecks, results) & evaluate(readyChecks, results);

        Map<String, Object> body = new HashMap<>();
        body.put("status", allUp ? "UP" : "DOWN");
        body.put("checks", results);

        res.status(allUp ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE).json(body);
    }

    public void handleLive(HttpRequest req, HttpResponse res) {
        List<HealthStatus> results = new ArrayList<>();
        boolean up = evaluate(liveChecks, results);

        Map<String, Object> body = Map.of(
            "status", up ? "UP" : "DOWN",
            "checks", results
        );
        res.status(up ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE).json(body);
    }

    public void handleReady(HttpRequest req, HttpResponse res) {
        List<HealthStatus> results = new ArrayList<>();
        boolean up = evaluate(readyChecks, results);

        Map<String, Object> body = Map.of(
            "status", up ? "UP" : "DOWN",
            "checks", results
        );
        res.status(up ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE).json(body);
    }

    private boolean evaluate(List<HealthCheck> checks, List<HealthStatus> out) {
        boolean allUp = true;
        for (HealthCheck check : checks) {
            HealthStatus status = check.check();
            out.add(status);
            if (status.state() != HealthStatus.State.UP) {
                allUp = false;
            }
        }
        return allUp;
    }
}
