package io.jettra.collections.api;

import java.util.function.LongFunction;

/**
 * High-performance map from primitive long keys to generic Object values.
 * Eliminates 100% of Long wrapper objects and Map$Node overhead.
 */
public interface LongObjectMap<V> {

    int size();

    default boolean isEmpty() {
        return size() == 0;
    }

    V put(long key, V value);

    V get(long key);

    V getIfAbsent(long key, V defaultValue);

    V getIfAbsentPut(long key, LongFunction<? extends V> factory);

    boolean containsKey(long key);

    boolean containsValue(Object value);

    V removeKey(long key);

    void clear();

    void forEachKeyValue(LongObjectProcedure<? super V> procedure);

    long[] keys();

    Object[] values();
}
