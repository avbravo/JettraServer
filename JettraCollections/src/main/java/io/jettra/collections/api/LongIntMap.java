package io.jettra.collections.api;

/**
 * High-performance map from primitive long keys to primitive int values.
 * Zero boxing for keys and values, and zero node entry objects.
 */
public interface LongIntMap {

    int size();

    default boolean isEmpty() {
        return size() == 0;
    }

    int put(long key, int value);

    int get(long key);

    int getIfAbsent(long key, int defaultValue);

    boolean containsKey(long key);

    boolean containsValue(int value);

    int removeKey(long key);

    int removeKeyIfAbsent(long key, int defaultValue);

    void clear();

    void forEachKeyValue(LongIntProcedure procedure);

    long[] keys();

    int[] values();
}
