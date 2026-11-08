package io.jettra.collections.api;

import java.util.function.LongPredicate;

/**
 * Primitive set of long elements with zero object overhead.
 */
public interface LongSet extends LongCollection {

    LongSet select(LongPredicate predicate);

    LongSet reject(LongPredicate predicate);

    LongSet toImmutable();
}
