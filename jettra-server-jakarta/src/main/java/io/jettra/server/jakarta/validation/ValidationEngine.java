package io.jettra.server.jakarta.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public final class ValidationEngine {

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface NotNull {
        String message() default "El valor no puede ser nulo";
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Size {
        int min() default 0;
        int max() default Integer.MAX_VALUE;
        String message() default "Tamaño fuera de rango";
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Min {
        long value();
        String message() default "El valor debe ser mayor o igual al mínimo";
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Max {
        long value();
        String message() default "El valor debe ser menor o igual al máximo";
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Email {
        String message() default "Formato de correo electrónico inválido";
    }

    public record ConstraintViolation(String propertyPath, String message, Object invalidValue) {}

    public static Set<ConstraintViolation> validate(Object target) {
        Set<ConstraintViolation> violations = new HashSet<>();
        if (target == null) return violations;

        Class<?> clazz = target.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    Object val = field.get(target);

                    if (field.isAnnotationPresent(NotNull.class) && val == null) {
                        violations.add(new ConstraintViolation(field.getName(), field.getAnnotation(NotNull.class).message(), null));
                    }

                    if (val instanceof String s) {
                        Size size = field.getAnnotation(Size.class);
                        if (size != null && (s.length() < size.min() || s.length() > size.max())) {
                            violations.add(new ConstraintViolation(field.getName(), size.message(), val));
                        }

                        Email email = field.getAnnotation(Email.class);
                        if (email != null && !s.contains("@")) {
                            violations.add(new ConstraintViolation(field.getName(), email.message(), val));
                        }
                    }

                    if (val instanceof Number n) {
                        Min min = field.getAnnotation(Min.class);
                        if (min != null && n.longValue() < min.value()) {
                            violations.add(new ConstraintViolation(field.getName(), min.message(), val));
                        }

                        Max max = field.getAnnotation(Max.class);
                        if (max != null && n.longValue() > max.value()) {
                            violations.add(new ConstraintViolation(field.getName(), max.message(), val));
                        }
                    }

                } catch (IllegalAccessException ignored) {}
            }
            clazz = clazz.getSuperclass();
        }
        return violations;
    }
}
