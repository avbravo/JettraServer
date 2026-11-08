package io.jettra.collections.api;

import java.util.PrimitiveIterator;

/**
 * Primitive iterator for long values, avoiding boxing.
 */
public interface LongIterator extends PrimitiveIterator.OfLong {
    @Override
    long nextLong();

    @Override
    boolean hasNext();
}
