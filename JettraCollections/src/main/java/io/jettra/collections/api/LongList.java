package io.jettra.collections.api;

import java.util.function.LongPredicate;
import java.util.function.LongUnaryOperator;

/**
 * Ordered sequence of primitive long elements with zero object boxing.
 */
public interface LongList extends LongCollection {

    long get(int index);

    long set(int index, long element);

    void addAtIndex(int index, long element);

    long removeAtIndex(int index);

    int indexOf(long value);

    int lastIndexOf(long value);

    LongList sortThis();

    LongList reverseThis();

    int binarySearch(long key);

    LongList select(LongPredicate predicate);

    LongList reject(LongPredicate predicate);

    LongList collect(LongUnaryOperator operator);

    LongList toImmutable();

    default long getFirst() {
        if (isEmpty()) {
            throw new IndexOutOfBoundsException("List is empty");
        }
        return get(0);
    }

    default long getLast() {
        if (isEmpty()) {
            throw new IndexOutOfBoundsException("List is empty");
        }
        return get(size() - 1);
    }
}
