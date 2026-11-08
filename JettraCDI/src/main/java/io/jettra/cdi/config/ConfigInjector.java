package io.jettra.cdi.config;

import java.lang.reflect.Field;

/**
 * Inyector de propiedades de configuración (@JettraConfigProperty o @ConfigProperty).
 */
public class ConfigInjector {

    public static void inject(Object target) {
        if (target == null) return;
        Class<?> current = target.getClass();

        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                String propName = null;
                String defaultVal = null;

                // 1. Revisar anotación @JettraConfigProperty
                for (java.lang.annotation.Annotation ann : field.getAnnotations()) {
                    String simpleName = ann.annotationType().getSimpleName();
                    if (simpleName.equals("JettraConfigProperty") || simpleName.equals("ConfigProperty")) {
                        try {
                            java.lang.reflect.Method nameMethod = ann.annotationType().getMethod("name");
                            propName = (String) nameMethod.invoke(ann);
                        } catch (Exception ignored) {}

                        try {
                            java.lang.reflect.Method defMethod = ann.annotationType().getMethod("defaultValue");
                            defaultVal = (String) defMethod.invoke(ann);
                        } catch (Exception ignored) {}
                        break;
                    }
                }

                if (propName != null && !propName.isBlank()) {
                    String rawValue = JettraConfig.getProperty(propName, defaultVal);
                    if (rawValue != null) {
                        try {
                            Object converted = JettraConfig.convertValue(rawValue, field.getType());
                            field.setAccessible(true);
                            field.set(target, converted);
                        } catch (Exception e) {
                            System.err.println("[ConfigInjector] Error asignando propiedad '" + propName + "' al campo " + field.getName() + ": " + e.getMessage());
                        }
                    }
                }
            }
            current = current.getSuperclass();
        }
    }
}
