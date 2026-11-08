package io.jettra.collections.api;

import java.util.OptionalDouble;
import java.util.OptionalLong;
import java.util.function.LongConsumer;
import java.util.function.LongPredicate;
import java.util.stream.LongStream;

/**
 * Common interface for primitive long-based collections.
 */
public interface LongCollection {

    int size();

    default boolean isEmpty() {
        return size() == 0;
    }

    default boolean notEmpty() {
        return size() > 0;
    }

    boolean contains(long value);

    boolean add(long value);

    boolean remove(long value);

    void clear();

    long[] toArray();

    LongIterator longIterator();

    void forEach(LongConsumer action);

    long sum();

    OptionalLong min();

    OptionalLong max();

    OptionalDouble average();

    boolean anySatisfy(LongPredicate predicate);

    boolean allSatisfy(LongPredicate predicate);

    boolean noneSatisfy(LongPredicate predicate);

    int count(LongPredicate predicate);

    LongStream toStream();
}
