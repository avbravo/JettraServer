package io.jettra.collections.api;

/**
 * High-performance map from primitive int keys to primitive int values.
 * Zero boxing for keys and values, and zero node entry objects.
 */
public interface IntIntMap {

    int size();

    default boolean isEmpty() {
        return size() == 0;
    }

    int put(int key, int value);

    int get(int key);

    int getIfAbsent(int key, int defaultValue);

    boolean containsKey(int key);

    boolean containsValue(int value);

    int removeKey(int key);

    int removeKeyIfAbsent(int key, int defaultValue);

    void clear();

    void forEachKeyValue(IntIntProcedure procedure);

    int[] keys();

    int[] values();
}
