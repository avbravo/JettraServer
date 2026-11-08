package io.jettra.collections.api;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;

/**
 * Common interface for primitive int-based collections, eliminating wrapper boxing.
 */
public interface IntCollection {

    int size();

    default boolean isEmpty() {
        return size() == 0;
    }

    default boolean notEmpty() {
        return size() > 0;
    }

    boolean contains(int value);

    boolean add(int value);

    boolean remove(int value);

    void clear();

    int[] toArray();

    IntIterator intIterator();

    void forEach(IntConsumer action);

    long sum();

    OptionalInt min();

    OptionalInt max();

    OptionalDouble average();

    boolean anySatisfy(IntPredicate predicate);

    boolean allSatisfy(IntPredicate predicate);

    boolean noneSatisfy(IntPredicate predicate);

    int count(IntPredicate predicate);

    IntStream toStream();
}
