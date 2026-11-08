package io.jettra.collections.api;

@FunctionalInterface
public interface LongIntProcedure {
    void value(long key, int value);
}
