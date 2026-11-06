package io.jettra.server.core.storage;

import io.jettra.collections.map.UnifiedMap;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Motor de almacenamiento en memoria RAM Heap utilizando {@link UnifiedMap} de JettraCollections.
 * Ofrece latencias de lectura/escritura sub-microsegundo con soporte para concurrencia.
 */
public class JvmRamStorageEngine<K, V> implements JettraStorageEngine<K, V> {

    private final Map<K, V> map = UnifiedMap.newMap();
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final AtomicLong readOps = new AtomicLong(0);
    private final AtomicLong writeOps = new AtomicLong(0);
    private final AtomicLong hits = new AtomicLong(0);
    private final AtomicLong misses = new AtomicLong(0);

    @Override
    public StorageMode getMode() {
        return StorageMode.JVM_RAM;
    }

    @Override
    public void put(K key, V value) {
        Objects.requireNonNull(key, "key no puede ser nula");
        rwLock.writeLock().lock();
        try {
            map.put(key, value);
            writeOps.incrementAndGet();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public V get(K key) {
        Objects.requireNonNull(key, "key no puede ser nula");
        readOps.incrementAndGet();
        rwLock.readLock().lock();
        try {
            V val = map.get(key);
            if (val != null) {
                hits.incrementAndGet();
            } else {
                misses.incrementAndGet();
            }
            return val;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public V remove(K key) {
        Objects.requireNonNull(key, "key no puede ser nula");
        writeOps.incrementAndGet();
        rwLock.writeLock().lock();
        try {
            return map.remove(key);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public boolean containsKey(K key) {
        Objects.requireNonNull(key, "key no puede ser nula");
        rwLock.readLock().lock();
        try {
            return map.containsKey(key);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public Set<K> keySet() {
        rwLock.readLock().lock();
        try {
            return new HashSet<>(map.keySet());
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public Collection<V> values() {
        rwLock.readLock().lock();
        try {
            return Collections.unmodifiableCollection(new java.util.ArrayList<>(map.values()));
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public long size() {
        rwLock.readLock().lock();
        try {
            return map.size();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public void clear() {
        rwLock.writeLock().lock();
        try {
            map.clear();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public void flush() {
        // En JVM_RAM el estado ya reside directamente en la memoria viva
    }

    @Override
    public StorageMetrics getMetrics() {
        long totalReads = readOps.get();
        double hitRate = totalReads == 0 ? 100.0 : ((double) hits.get() / totalReads) * 100.0;
        long estimatedMemory = (long) map.size() * 64L; // Estimación aproximada
        return new StorageMetrics(
            StorageMode.JVM_RAM,
            map.size(),
            estimatedMemory,
            totalReads,
            writeOps.get(),
            hitRate
        );
    }

    @Override
    public void close() {
        clear();
    }
}
