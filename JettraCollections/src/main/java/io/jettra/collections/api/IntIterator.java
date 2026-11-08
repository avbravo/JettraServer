package io.jettra.collections.api;

import java.util.PrimitiveIterator;

/**
 * Primitive iterator for int values, avoiding boxing.
 */
public interface IntIterator extends PrimitiveIterator.OfInt {
    @Override
    int nextInt();

    @Override
    boolean hasNext();
}
