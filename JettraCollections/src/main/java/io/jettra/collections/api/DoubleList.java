package io.jettra.collections.api;

import java.util.OptionalDouble;
import java.util.function.DoubleConsumer;
import java.util.function.DoublePredicate;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.DoubleStream;

/**
 * Ordered sequence of primitive double elements with zero object boxing.
 */
public interface DoubleList {

    int size();

    default boolean isEmpty() {
        return size() == 0;
    }

    double get(int index);

    double set(int index, double element);

    boolean add(double element);

    void addAtIndex(int index, double element);

    double removeAtIndex(int index);

    boolean contains(double value);

    int indexOf(double value);

    void clear();

    double[] toArray();

    DoubleIterator doubleIterator();

    void forEach(DoubleConsumer action);

    double sum();

    OptionalDouble min();

    OptionalDouble max();

    OptionalDouble average();

    DoubleList sortThis();

    DoubleList reverseThis();

    DoubleList select(DoublePredicate predicate);

    DoubleList reject(DoublePredicate predicate);

    DoubleList collect(DoubleUnaryOperator operator);

    DoubleList toImmutable();

    DoubleStream toStream();
}
