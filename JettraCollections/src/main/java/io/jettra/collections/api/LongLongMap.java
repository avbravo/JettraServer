package io.jettra.collections.api;

/**
 * High-performance map from primitive long keys to primitive long values.
 * Zero boxing for keys and values, and zero node entry objects.
 */
public interface LongLongMap {

    int size();

    default boolean isEmpty() {
        return size() == 0;
    }

    long put(long key, long value);

    long get(long key);

    long getIfAbsent(long key, long defaultValue);

    boolean containsKey(long key);

    boolean containsValue(long value);

    long removeKey(long key);

    long removeKeyIfAbsent(long key, long defaultValue);

    void clear();

    void forEachKeyValue(LongLongProcedure procedure);

    long[] keys();

    long[] values();
}
