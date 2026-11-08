package io.jettra.collections.api;

import java.util.PrimitiveIterator;

/**
 * Primitive iterator for double values, avoiding boxing.
 */
public interface DoubleIterator extends PrimitiveIterator.OfDouble {
    @Override
    double nextDouble();

    @Override
    boolean hasNext();
}
