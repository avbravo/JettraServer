package io.jettra.server.example.rest;

import io.jettra.server.core.JettraServer;
import io.jettra.server.core.storage.StorageMetrics;
import io.jettra.server.jakarta.cdi.CdiAnnotations.Inject;
import io.jettra.server.jakarta.rest.RestAnnotations.GET;
import io.jettra.server.jakarta.rest.RestAnnotations.Path;
import io.jettra.server.jakarta.rest.RestAnnotations.Produces;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Recurso REST para telemetría e información del servidor.
 */
@Path("/api/info")
@Produces("application/json")
public class ServerInfoResource {

    @Inject
    private JettraServer server;

    public ServerInfoResource() {}

    public ServerInfoResource(JettraServer server) {
        this.server = server;
    }

    @GET
    public Map<String, Object> getServerInfo() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("application", "JettraServerExample");
        info.put("version", "1.0.0-SNAPSHOT");
        info.put("framework", "Jakarta EE 11 / JettraServer");
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("vmName", System.getProperty("java.vm.name"));

        if (server != null) {
            StorageMetrics sm = server.storage().getMetrics();
            info.put("storageMode", sm.mode().name());
            info.put("storageDescription", sm.mode().getDescription());
            info.put("storageRecords", sm.totalRecords());
            info.put("storageMemoryUsed", sm.humanReadableMemory());
            info.put("cacheHitRate", sm.hitRatePercentage() + "%");
        }

        return info;
    }
}

