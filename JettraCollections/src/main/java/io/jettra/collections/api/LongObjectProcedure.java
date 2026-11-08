package io.jettra.collections.api;

@FunctionalInterface
public interface LongObjectProcedure<V> {
    void value(long key, V value);
}
