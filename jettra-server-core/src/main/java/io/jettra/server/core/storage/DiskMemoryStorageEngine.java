package io.jettra.server.core.storage;

import com.jettra.memory.api.JettraMemoryConfig;
import com.jettra.memory.api.JettraMemoryEngine;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Motor de almacenamiento basado en el motor LSM persistente Off-Heap de JettraMemory.
 * Garantiza durabilidad en disco combinada con búferes en memoria nativa sin pausas de GC.
 */
public class DiskMemoryStorageEngine<K, V> implements JettraStorageEngine<K, V> {

    private final JettraMemoryEngine engine;
    private final Class<V> valueType;
    private final Set<K> knownKeys = ConcurrentHashMap.newKeySet();
    private final AtomicLong readOps = new AtomicLong(0);
    private final AtomicLong writeOps = new AtomicLong(0);
    private final AtomicLong hits = new AtomicLong(0);
    private final AtomicLong misses = new AtomicLong(0);

    public DiskMemoryStorageEngine(Path storageDir, Class<V> valueType) {
        this.valueType = valueType;
        try {
            Files.createDirectories(storageDir);
            JettraMemoryConfig config = JettraMemoryConfig.builder()
                .storageDirectory(storageDir)
                .storeName("jettra-server-disk")
                .autoGcEnabled(true)
                .build();
            this.engine = new JettraMemoryEngine(config);
        } catch (IOException e) {
            throw new RuntimeException("Error al inicializar DiskMemoryStorageEngine en: " + storageDir, e);
        }
    }

    @Override
    public StorageMode getMode() {
        return StorageMode.DISK_MEMORY;
    }

    @Override
    public void put(K key, V value) {
        Objects.requireNonNull(key, "key no puede ser nula");
        try {
            byte[] bytes = StorageSerializationHelper.serialize(value);
            engine.put(String.valueOf(key), bytes);
            knownKeys.add(key);
            writeOps.incrementAndGet();
        } catch (IOException e) {
            throw new RuntimeException("Error en put(key=" + key + ") en DiskMemory", e);
        }
    }

    @Override
    public V get(K key) {
        Objects.requireNonNull(key, "key no puede ser nula");
        readOps.incrementAndGet();
        try {
            byte[] bytes = engine.get(String.valueOf(key));
            if (bytes != null) {
                hits.incrementAndGet();
                return StorageSerializationHelper.deserialize(bytes, valueType);
            } else {
                misses.incrementAndGet();
                return null;
            }
        } catch (IOException e) {
            throw new RuntimeException("Error en get(key=" + key + ") en DiskMemory", e);
        }
    }

    @Override
    public V remove(K key) {
        Objects.requireNonNull(key, "key no puede ser nula");
        writeOps.incrementAndGet();
        try {
            V prev = get(key);
            engine.delete(String.valueOf(key));
            knownKeys.remove(key);
            return prev;
        } catch (IOException e) {
            throw new RuntimeException("Error en remove(key=" + key + ") en DiskMemory", e);
        }
    }

    @Override
    public boolean containsKey(K key) {
        Objects.requireNonNull(key, "key no puede ser nula");
        return engine.containsKey(String.valueOf(key));
    }

    @Override
    public Set<K> keySet() {
        return Collections.unmodifiableSet(new HashSet<>(knownKeys));
    }

    @Override
    public Collection<V> values() {
        java.util.List<V> list = new java.util.ArrayList<>();
        for (K key : knownKeys) {
            V val = get(key);
            if (val != null) list.add(val);
        }
        return Collections.unmodifiableCollection(list);
    }

    @Override
    public long size() {
        return engine.size();
    }

    @Override
    public void clear() {
        for (K key : knownKeys) {
            try {
                engine.delete(String.valueOf(key));
            } catch (IOException ignored) {}
        }
        knownKeys.clear();
    }

    @Override
    public void flush() {
        try {
            engine.flush();
        } catch (IOException e) {
            throw new RuntimeException("Error al volcar datos en DiskMemory", e);
        }
    }

    @Override
    public StorageMetrics getMetrics() {
        long totalReads = readOps.get();
        double hitRate = totalReads == 0 ? 100.0 : ((double) hits.get() / totalReads) * 100.0;
        long mem = 0L;
        try {
            var m = engine.getMetrics();
            if (m != null) mem = m.totalAllocatedBytes();
        } catch (Exception ignored) {}

        return new StorageMetrics(
            StorageMode.DISK_MEMORY,
            engine.size(),
            mem,
            totalReads,
            writeOps.get(),
            hitRate
        );
    }

    @Override
    public void close() {
        try {
            engine.close();
        } catch (Exception ignored) {}
    }
}
