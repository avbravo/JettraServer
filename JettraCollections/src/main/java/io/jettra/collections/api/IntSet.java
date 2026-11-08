package io.jettra.collections.api;

import java.util.function.IntPredicate;

/**
 * Primitive set of int elements with zero object overhead.
 */
public interface IntSet extends IntCollection {

    IntSet select(IntPredicate predicate);

    IntSet reject(IntPredicate predicate);

    IntSet toImmutable();
}
