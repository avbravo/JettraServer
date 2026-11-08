package io.jettra.collections.api;

@FunctionalInterface
public interface LongLongProcedure {
    void value(long key, long value);
}
