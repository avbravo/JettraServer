package io.jettra.studio.model;

import java.util.Objects;

/**
 * Standard basic implementation of IModel holding a direct object reference.
 *
 * @param <T> Model object type
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
        if (o == null || getClass() != o.getClass()) return false;
        Model<?> model = (Model<?>) o;
        return Objects.equals(object, model.object);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(object);
    }

    @Override
    public String toString() {
        return object != null ? object.toString() : "";
    }
}
