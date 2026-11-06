package io.jettra.server.core.storage;

/**
 * Métricas de rendimiento y uso de recursos del motor de almacenamiento.
 */
public record StorageMetrics(
    StorageMode mode,
    long totalRecords,
    long memoryUsedBytes,
    long readOperations,
    long writeOperations,
    double hitRatePercentage
) {
    public String humanReadableMemory() {
        if (memoryUsedBytes < 1024) return memoryUsedBytes + " B";
        int exp = (int) (Math.log(memoryUsedBytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "iB";
        return String.format("%.2f %s", memoryUsedBytes / Math.pow(1024, exp), pre);
    }
}
