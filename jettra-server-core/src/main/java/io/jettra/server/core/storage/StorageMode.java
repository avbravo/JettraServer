package io.jettra.server.core.storage;

/**
 * Modos de almacenamiento disponibles en JettraServer:
 * - JVM_RAM: Almacenamiento en memoria Heap & Stack utilizando estructuras de JettraCollections (UnifiedMap).
 * - DISK_MEMORY: Motor LSM Off-Heap persistente a disco (JettraMemory).
 * - OFF_HEAP: Motor de páginas ranuradas de acceso directo Off-Heap con Project Panama FFM (Slotted-Page).
 */
public enum StorageMode {
    JVM_RAM("Memoria RAM Heap Estándar (UnifiedMap)"),
    DISK_MEMORY("Motor LSM Off-Heap Persistente en Disco (JettraMemory)"),
    OFF_HEAP("Motor de Páginas Ranuradas Nativo Off-Heap Panama FFM (Slotted-Page)");

    private final String description;

    StorageMode(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static StorageMode fromString(String val) {
        if (val == null || val.isBlank()) {
            return JVM_RAM;
        }
        String clean = val.trim().toUpperCase();
        return switch (clean) {
            case "DISK_MEMORY", "DISK", "LSM" -> DISK_MEMORY;
            case "OFF_HEAP", "OFFHEAP", "PANAMA" -> OFF_HEAP;
            default -> JVM_RAM;
        };
    }
}
