package io.jettra.studio.model;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Functional model backed by lambda expressions for getter and setter.
 *
 * @param <T> Model object type
 */
public class LambdaModel<T> implements IModel<T> {

    private final Supplier<T> getter;
    private final Consumer<T> setter;

    public LambdaModel(Supplier<T> getter, Consumer<T> setter) {
        this.getter = Objects.requireNonNull(getter, "getter cannot be null");
        this.setter = setter;
    }

    public static <T> LambdaModel<T> of(Supplier<T> getter, Consumer<T> setter) {
        return new LambdaModel<>(getter, setter);
    }

    @Override
    public T getObject() {
        return getter.get();
    }

    @Override
    public void setObject(T object) {
        if (setter != null) {
            setter.accept(object);
        } else {
            IModel.super.setObject(object);
        }
    }
}
