package io.jettra.studio.model;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * PropertyModel resolves a property expression (e.g., "name", "email", "address.city")
 * against a target model or object using reflection (supporting Java Beans and Java 25 Records).
 *
 * @param <T> Property value type
 */
public class PropertyModel<T> implements IModel<T> {

    private final Object target;
    private final String propertyExpression;

    public PropertyModel(Object target, String propertyExpression) {
        this.target = Objects.requireNonNull(target, "target cannot be null");
        this.propertyExpression = Objects.requireNonNull(propertyExpression, "propertyExpression cannot be null");
    }

    public static <T> PropertyModel<T> of(Object target, String propertyExpression) {
        return new PropertyModel<>(target, propertyExpression);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getObject() {
        Object current = (target instanceof IModel<?> m) ? m.getObject() : target;
        if (current == null) return null;

        String[] parts = propertyExpression.split("\\.");
        for (String part : parts) {
            if (current == null) return null;
            current = resolveProperty(current, part);
        }
        return (T) current;
    }

    @Override
    public void setObject(T value) {
        Object current = (target instanceof IModel<?> m) ? m.getObject() : target;
        if (current == null) return;

        String[] parts = propertyExpression.split("\\.");
        for (int i = 0; i < parts.length - 1; i++) {
            current = resolveProperty(current, parts[i]);
            if (current == null) return;
        }

        setProperty(current, parts[parts.length - 1], value);
    }

    private Object resolveProperty(Object obj, String prop) {
        Class<?> clazz = obj.getClass();
        String capitalized = Character.toUpperCase(prop.charAt(0)) + prop.substring(1);

        // 1. Try getter: getProp()
        try {
            Method m = clazz.getMethod("get" + capitalized);
            return m.invoke(obj);
        } catch (Exception ignored) {}

        // 2. Try boolean getter: isProp()
        try {
            Method m = clazz.getMethod("is" + capitalized);
            return m.invoke(obj);
        } catch (Exception ignored) {}

        // 3. Try record component / direct method: prop()
        try {
            Method m = clazz.getMethod(prop);
            return m.invoke(obj);
        } catch (Exception ignored) {}

        // 4. Try direct field
        try {
            Field f = clazz.getDeclaredField(prop);
            f.setAccessible(true);
            return f.get(obj);
        } catch (Exception ignored) {}

        return null;
    }

    private void setProperty(Object obj, String prop, Object value) {
        Class<?> clazz = obj.getClass();
        String capitalized = Character.toUpperCase(prop.charAt(0)) + prop.substring(1);

        // 1. Try setter: setProp(val)
        for (Method m : clazz.getMethods()) {
            if (m.getName().equals("set" + capitalized) && m.getParameterCount() == 1) {
                try {
                    m.invoke(obj, value);
                    return;
                } catch (Exception ignored) {}
            }
        }

        // 2. Try direct field
        try {
            Field f = clazz.getDeclaredField(prop);
            f.setAccessible(true);
            f.set(obj, value);
        } catch (Exception ignored) {}
    }
}
