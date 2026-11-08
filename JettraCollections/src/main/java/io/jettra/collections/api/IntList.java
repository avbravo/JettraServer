package io.jettra.collections.api;

import java.util.function.IntPredicate;

/**
 * Ordered sequence of primitive int elements with zero object boxing.
 */
public interface IntList extends IntCollection {

    int get(int index);

    int set(int index, int element);

    void addAtIndex(int index, int element);

    int removeAtIndex(int index);

    int indexOf(int value);

    int lastIndexOf(int value);

    IntList sortThis();

    IntList reverseThis();

    int binarySearch(int key);

    IntList select(IntPredicate predicate);

    IntList reject(IntPredicate predicate);

    IntList collect(IntToIntFunction function);

    IntList toImmutable();

    default int getFirst() {
        if (isEmpty()) {
            throw new IndexOutOfBoundsException("List is empty");
        }
        return get(0);
    }

    default int getLast() {
        if (isEmpty()) {
            throw new IndexOutOfBoundsException("List is empty");
        }
        return get(size() - 1);
    }
}
