package io.jettra.studio.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Core model interface in JettraStudio, core model interface in JettraStudio.
 * Decouples components from their data source and provides lazy/dynamic value resolution.
 *
 * @param <T> Model object type
 */
@FunctionalInterface
public interface IModel<T> extends Serializable {

    /**
     * Resolves and returns the current model object.
     */
    T getObject();

    /**
     * Sets the model object. Default implementation throws UnsupportedOperationException if read-only.
     */
    default void setObject(T object) {
        throw new UnsupportedOperationException("This model is read-only.");
    }

    /**
     * Called at the end of the request cycle to detach and free transient state.
     */
    default void detach() {}

    /**
     * Creates a static/simple value model.
     */
    static <T> IModel<T> of(T value) {
        return new Model<>(value);
    }

    /**
     * Creates a functional read-only model from a Supplier.
     */
    static <T> IModel<T> of(Supplier<T> getter) {
        Objects.requireNonNull(getter, "getter cannot be null");
        return getter::get;
    }

    /**
     * Creates a functional read-write model from a getter and setter.
     */
    static <T> IModel<T> of(Supplier<T> getter, Consumer<T> setter) {
        return new LambdaModel<>(getter, setter);
    }
}
