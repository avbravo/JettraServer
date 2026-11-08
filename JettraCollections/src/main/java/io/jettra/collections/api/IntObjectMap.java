package io.jettra.collections.api;

import java.util.function.IntFunction;

/**
 * High-performance map from primitive int keys to Object values.
 * Zero boxing for keys and zero entry wrapper nodes.
 */
public interface IntObjectMap<V> {

    int size();

    default boolean isEmpty() {
        return size() == 0;
    }

    V put(int key, V value);

    V get(int key);

    V getIfAbsent(int key, V defaultValue);

    V getIfAbsentPut(int key, IntFunction<? extends V> factory);

    boolean containsKey(int key);

    boolean containsValue(Object value);

    V removeKey(int key);

    void clear();

    void forEachKeyValue(IntObjectProcedure<? super V> procedure);

    int[] keys();

    Object[] values();
}
