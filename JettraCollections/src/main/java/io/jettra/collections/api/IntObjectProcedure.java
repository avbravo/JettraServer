package io.jettra.collections.api;

@FunctionalInterface
public interface IntObjectProcedure<V> {
    void value(int key, V value);
}
