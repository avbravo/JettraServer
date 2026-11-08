package io.jettra.collections.api;

@FunctionalInterface
public interface IntLongProcedure {
    void value(int key, long value);
}
