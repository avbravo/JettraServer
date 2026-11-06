package io.jettra.server.core.storage;

import io.jettra.store.offheap.OffHeapSlottedMap;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Motor de almacenamiento de ultra-alta velocidad Off-Heap basado en páginas ranuradas (Slotted Pages)
 * y la API Foreign Function & Memory (FFM - Project Panama) de Java 25.
 *
 * Los datos se asignan en memoria nativa sin supervisión del GC, eliminando tiempos de pausa
 * y reduciendo la huella de memoria en el Heap de la JVM.
 */
public class OffHeapSlottedPageEngine<K, V> implements JettraStorageEngine<K, V> {

    private final OffHeapSlottedMap slottedMap;
    private final Class<V> valueType;
    private final Set<K> knownKeys = ConcurrentHashMap.newKeySet();
    private final int pageSize;
    private final AtomicLong readOps = new AtomicLong(0);
    private final AtomicLong writeOps = new AtomicLong(0);
    private final AtomicLong hits = new AtomicLong(0);
    private final AtomicLong misses = new AtomicLong(0);

    public OffHeapSlottedPageEngine(int pageSize, Class<V> valueType) {
        this.pageSize = pageSize > 0 ? pageSize : 64 * 1024;
        this.valueType = valueType;
        this.slottedMap = new OffHeapSlottedMap(this.pageSize);
    }

    @Override
    public StorageMode getMode() {
        return StorageMode.OFF_HEAP;
    }

    @Override
    public void put(K key, V value) {
        Objects.requireNonNull(key, "key no puede ser nula");
        byte[] bytes = StorageSerializationHelper.serialize(value);
        slottedMap.put(String.valueOf(key), bytes);
        knownKeys.add(key);
        writeOps.incrementAndGet();
    }

    @Override
    public V get(K key) {
        Objects.requireNonNull(key, "key no puede ser nula");
        readOps.incrementAndGet();
        byte[] bytes = slottedMap.get(String.valueOf(key));
        if (bytes != null) {
            hits.incrementAndGet();
            return StorageSerializationHelper.deserialize(bytes, valueType);
        } else {
            misses.incrementAndGet();
            return null;
        }
    }

    @Override
    public V remove(K key) {
        Objects.requireNonNull(key, "key no puede ser nula");
        writeOps.incrementAndGet();
        V prev = get(key);
        slottedMap.remove(String.valueOf(key));
        knownKeys.remove(key);
        return prev;
    }

    @Override
    public boolean containsKey(K key) {
        Objects.requireNonNull(key, "key no puede ser nula");
        return slottedMap.containsKey(String.valueOf(key));
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
        return slottedMap.size();
    }

    @Override
    public void clear() {
        for (K key : knownKeys) {
            slottedMap.remove(String.valueOf(key));
        }
        knownKeys.clear();
    }

    @Override
    public void flush() {
        // Slotted page buffer reside directamente en memoria Off-Heap compartida
    }

    @Override
    public StorageMetrics getMetrics() {
        long totalReads = readOps.get();
        double hitRate = totalReads == 0 ? 100.0 : ((double) hits.get() / totalReads) * 100.0;
        long totalMemory = (long) slottedMap.pageCount() * (long) pageSize;

        return new StorageMetrics(
            StorageMode.OFF_HEAP,
            slottedMap.size(),
            totalMemory,
            totalReads,
            writeOps.get(),
            hitRate
        );
    }

    @Override
    public void close() {
        try {
            slottedMap.close();
        } catch (Exception ignored) {}
    }
}
