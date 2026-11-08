package io.jettra.collections.api;

@FunctionalInterface
public interface ObjectLongProcedure<K> {
    void value(K key, long value);
}
