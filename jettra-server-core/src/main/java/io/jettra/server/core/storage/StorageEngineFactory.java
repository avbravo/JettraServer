package io.jettra.server.core.storage;

import io.jettra.server.core.config.ServerConfig;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fábrica central para aprovisionamiento y gestión de motores de almacenamiento.
 */
public final class StorageEngineFactory {

    private static volatile JettraStorageEngine<String, Object> defaultEngine;
    private static final Map<String, JettraStorageEngine<?, ?>> namedEngines = new ConcurrentHashMap<>();

    private StorageEngineFactory() {}

    /**
     * Crea un motor tipado según el modo y configuración especificados.
     */
    @SuppressWarnings("unchecked")
    public static <K, V> JettraStorageEngine<K, V> createEngine(StorageMode mode, ServerConfig config, Class<V> valueType) {
        return switch (mode) {
            case JVM_RAM -> new JvmRamStorageEngine<>();
            case DISK_MEMORY -> new DiskMemoryStorageEngine<>(Path.of(config.getDataDir()), valueType);
            case OFF_HEAP -> new OffHeapSlottedPageEngine<>(config.getOffHeapPageSizeBytes(), valueType);
        };
    }

    /**
     * Inicializa el motor por defecto del servidor.
     */
    public static synchronized JettraStorageEngine<String, Object> initDefaultEngine(ServerConfig config) {
        if (defaultEngine != null) {
            defaultEngine.close();
        }
        defaultEngine = createEngine(config.getStorageMode(), config, Object.class);
        return defaultEngine;
    }

    /**
     * Retorna el motor por defecto activo.
     */
    public static JettraStorageEngine<String, Object> getDefaultEngine() {
        if (defaultEngine == null) {
            synchronized (StorageEngineFactory.class) {
                if (defaultEngine == null) {
                    defaultEngine = new JvmRamStorageEngine<>();
                }
            }
        }
        return defaultEngine;
    }

    /**
     * Permite conmutar dinámicamente el motor por defecto en tiempo de ejecución.
     */
    public static synchronized void switchDefaultEngine(StorageMode newMode, ServerConfig config) {
        JettraStorageEngine<String, Object> old = defaultEngine;
        JettraStorageEngine<String, Object> updated = createEngine(newMode, config, Object.class);
        if (old != null) {
            // Migración opcional en caliente de las claves existentes
            for (String key : old.keySet()) {
                Object val = old.get(key);
                if (val != null) updated.put(key, val);
            }
            old.close();
        }
        defaultEngine = updated;
    }

    /**
     * Cierra todos los motores activos.
     */
    public static synchronized void closeAll() {
        if (defaultEngine != null) {
            defaultEngine.close();
            defaultEngine = null;
        }
        for (JettraStorageEngine<?, ?> engine : namedEngines.values()) {
            engine.close();
        }
        namedEngines.clear();
    }
}
