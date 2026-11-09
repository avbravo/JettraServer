package io.jettra.aura.model;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Modelo que vincula dinámicamente un componente a una propiedad de un objeto mediante
 * getters, setters, campos o métodos de Java 25 Records.
 *
 * @param <T> Tipo del valor de la propiedad
 */
public class PropertyModel<T> implements IModel<T> {

    private final Object target;
    private final String propertyExpression;

    public PropertyModel(Object target, String propertyExpression) {
        this.target = target;
        this.propertyExpression = propertyExpression;
    }

    public static <T> PropertyModel<T> of(Object target, String property) {
        return new PropertyModel<>(target, property);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getObject() {
        if (target == null || propertyExpression == null) return null;
        return (T) readProperty(target, propertyExpression);
    }

    @Override
    public void setObject(T object) {
        if (target != null && propertyExpression != null) {
            writeProperty(target, propertyExpression, object);
        }
    }

    public Object getTarget() {
        return target;
    }

    public String getPropertyExpression() {
        return propertyExpression;
    }

    public static Object readProperty(Object bean, String propertyName) {
        if (bean == null || propertyName == null) return null;

        Class<?> clazz = bean.getClass();
        String capitalized = Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);

        // 1. Probar getter estándar: getProperty()
        try {
            Method m = clazz.getMethod("get" + capitalized);
            return m.invoke(bean);
        } catch (Exception ignored) {}

        // 2. Probar boolean isProperty()
        try {
            Method m = clazz.getMethod("is" + capitalized);
            return m.invoke(bean);
        } catch (Exception ignored) {}

        // 3. Probar método estilo Record de Java: property()
        try {
            Method m = clazz.getMethod(propertyName);
            return m.invoke(bean);
        } catch (Exception ignored) {}

        // 4. Probar acceso directo por campo
        try {
            Field f = findField(clazz, propertyName);
            if (f != null) {
                f.setAccessible(true);
                return f.get(bean);
            }
        } catch (Exception ignored) {}

        return null;
    }

    public static void writeProperty(Object bean, String propertyName, Object value) {
        if (bean == null || propertyName == null) return;

        Class<?> clazz = bean.getClass();
        String capitalized = Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);

        // 1. Buscar setter: setProperty(...)
        for (Method m : clazz.getMethods()) {
            if (m.getName().equals("set" + capitalized) && m.getParameterCount() == 1) {
                try {
                    Object converted = convertValue(value, m.getParameterTypes()[0]);
                    m.invoke(bean, converted);
                    return;
                } catch (Exception ignored) {}
            }
        }

        // 2. Intentar campo directo
        try {
            Field f = findField(clazz, propertyName);
            if (f != null) {
                f.setAccessible(true);
                Object converted = convertValue(value, f.getType());
                f.set(bean, converted);
            }
        } catch (Exception ignored) {}
    }

    private static Field findField(Class<?> clazz, String name) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    private static Object convertValue(Object val, Class<?> targetType) {
        if (val == null) {
            if (targetType.isPrimitive()) {
                if (targetType == boolean.class) return false;
                if (targetType == int.class) return 0;
                if (targetType == long.class) return 0L;
                if (targetType == double.class) return 0.0;
                if (targetType == float.class) return 0.0f;
            }
            return null;
        }

        if (targetType.isInstance(val)) {
            return val;
        }

        String str = val.toString().trim();
        if (targetType == String.class) return str;
        if (targetType == int.class || targetType == Integer.class) return Integer.parseInt(str);
        if (targetType == long.class || targetType == Long.class) return Long.parseLong(str);
        if (targetType == double.class || targetType == Double.class) return Double.parseDouble(str);
        if (targetType == float.class || targetType == Float.class) return Float.parseFloat(str);
        if (targetType == boolean.class || targetType == Boolean.class) return Boolean.parseBoolean(str);

        return val;
    }
}

