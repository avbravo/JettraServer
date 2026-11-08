package io.jettra.collections.api;

@FunctionalInterface
public interface ObjectIntProcedure<K> {
    void value(K key, int value);
}
