package io.jettra.server.example.bean;

import io.jettra.server.core.storage.StorageMetrics;
import io.jettra.server.core.storage.StorageMode;
import io.jettra.server.example.service.ProductService;
import io.jettra.server.jakarta.cdi.CdiAnnotations.Inject;
import io.jettra.server.jakarta.cdi.CdiAnnotations.Named;
import io.jettra.server.jakarta.cdi.CdiAnnotations.RequestScoped;
import java.io.Serializable;
import java.lang.management.ManagementFactory;

/**
 * Managed Bean para proveer métricas e información del sistema al Dashboard de PrimeFaces.
 */
@Named("dashboardBean")
@RequestScoped
public class DashboardBean implements Serializable {

    @Inject
    private ProductService productService;

    private String storageMode = "JVM_RAM";
    private String storageDescription = "Memoria RAM Heap estándar";
    private long totalProducts = 0;
    private long memoryUsedMb = 0;
    private long uptimeSeconds = 0;
    private int cpuCores = 0;

    public DashboardBean() {}

    public void refreshMetrics(StorageMetrics metrics) {
        if (metrics != null) {
            this.storageMode = metrics.mode().name();
            this.storageDescription = metrics.mode().getDescription();
            this.memoryUsedMb = metrics.memoryUsedBytes() / (1024 * 1024);
        } else {
            this.storageMode = StorageMode.JVM_RAM.name();
            this.storageDescription = StorageMode.JVM_RAM.getDescription();
        }

        Runtime rt = Runtime.getRuntime();
        this.cpuCores = rt.availableProcessors();
        this.uptimeSeconds = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;

        if (productService != null) {
            try {
                this.totalProducts = productService.listAll().size();
            } catch (Exception ignored) {}
        }
    }

    public String getStorageMode() { return storageMode; }
    public String getStorageDescription() { return storageDescription; }
    public long getTotalProducts() { return totalProducts; }
    public long getMemoryUsedMb() { return memoryUsedMb; }
    public long getUptimeSeconds() { return uptimeSeconds; }
    public int getCpuCores() { return cpuCores; }

    public String getUptimeFormatted() {
        long s = uptimeSeconds % 60;
        long m = (uptimeSeconds / 60) % 60;
        long h = uptimeSeconds / 3600;
        return String.format("%02dh %02dm %02ds", h, m, s);
    }
}

