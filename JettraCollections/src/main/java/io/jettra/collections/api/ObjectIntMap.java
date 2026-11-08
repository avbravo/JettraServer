package io.jettra.collections.api;

/**
 * High-performance map from Object keys to primitive int values.
 * Zero boxing for values and zero entry wrapper nodes.
 */
public interface ObjectIntMap<K> {

    int size();

    default boolean isEmpty() {
        return size() == 0;
    }

    int put(K key, int value);

    int get(K key);

    int getIfAbsent(K key, int defaultValue);

    boolean containsKey(Object key);

    int removeKey(K key);

    int removeKeyIfAbsent(K key, int defaultValue);

    void clear();

    void forEachKeyValue(ObjectIntProcedure<? super K> procedure);
}
