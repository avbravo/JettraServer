package io.jettra.collections.api;

/**
 * High-performance map from primitive int keys to primitive long values.
 * Zero boxing for keys and values, and zero node entry objects.
 */
public interface IntLongMap {

    int size();

    default boolean isEmpty() {
        return size() == 0;
    }

    long put(int key, long value);

    long get(int key);

    long getIfAbsent(int key, long defaultValue);

    boolean containsKey(int key);

    boolean containsValue(long value);

    long removeKey(int key);

    long removeKeyIfAbsent(int key, long defaultValue);

    void clear();

    void forEachKeyValue(IntLongProcedure procedure);

    int[] keys();

    long[] values();
}
