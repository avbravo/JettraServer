package io.jettra.aura.model;

import java.util.Objects;

/**
 * Implementación básica y directa de {@link IModel} que contiene un valor en memoria.
 */
public class Model<T> implements IModel<T> {

    private T object;

    public Model() {
        this(null);
    }

    public Model(T object) {
        this.object = object;
    }

    public static <T> Model<T> of(T object) {
        return new Model<>(object);
    }

    public static <T> Model<T> empty() {
        return new Model<>(null);
    }

    @Override
    public T getObject() {
        return object;
    }

    @Override
    public void setObject(T object) {
        this.object = object;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Model<?> model)) return false;
        return Objects.equals(object, model.object);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(object);
    }

    @Override
    public String toString() {
        return String.valueOf(object);
    }
}

