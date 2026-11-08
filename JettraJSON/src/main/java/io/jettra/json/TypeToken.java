package io.jettra.json;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class TypeToken<T> {
    private final Type type;

    protected TypeToken() {
        Type superclass = getClass().getGenericSuperclass();
        if (superclass instanceof Class) {
            throw new RuntimeException("Missing type parameter.");
        }
        ParameterizedType parameterized = (ParameterizedType) superclass;
        this.type = parameterized.getActualTypeArguments()[0];
    }

    private TypeToken(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public static TypeToken<?> get(Type type) {
        return new TypeToken<>(type);
    }
}
