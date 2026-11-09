package io.jettra.aura.model;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Modelo funcional basado en lambdas de Java 25 para acceso declarativo y fluido a datos.
 */
public class LambdaModel<T> implements IModel<T> {

    private final Supplier<T> getter;
    private final Consumer<T> setter;

    public LambdaModel(Supplier<T> getter, Consumer<T> setter) {
        this.getter = getter;
        this.setter = setter;
    }

    public static <T> LambdaModel<T> of(Supplier<T> getter) {
        return new LambdaModel<>(getter, null);
    }

    public static <T> LambdaModel<T> of(Supplier<T> getter, Consumer<T> setter) {
        return new LambdaModel<>(getter, setter);
    }

    @Override
    public T getObject() {
        return getter != null ? getter.get() : null;
    }

    @Override
    public void setObject(T object) {
        if (setter != null) {
            setter.accept(object);
        }
    }
}

