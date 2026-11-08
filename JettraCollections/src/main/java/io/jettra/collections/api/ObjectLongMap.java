package io.jettra.collections.api;

/**
 * High-performance map from generic Object keys to primitive long values.
 * Zero boxing for values and zero entry wrapper nodes.
 */
public interface ObjectLongMap<K> {

    int size();

    default boolean isEmpty() {
        return size() == 0;
    }

    long put(K key, long value);

    long get(K key);

    long getIfAbsent(K key, long defaultValue);

    boolean containsKey(Object key);

    long removeKey(K key);

    long removeKeyIfAbsent(K key, long defaultValue);

    void clear();

    void forEachKeyValue(ObjectLongProcedure<? super K> procedure);
}
