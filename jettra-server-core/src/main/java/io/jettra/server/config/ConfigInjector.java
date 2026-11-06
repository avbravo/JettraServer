package io.jettra.server.config;

import java.lang.reflect.Field;

public class ConfigInjector {

    public static void inject(Object target) {
        Class<?> clazz = target.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(JettraConfigProperty.class)) {
                JettraConfigProperty annotation = field.getAnnotation(JettraConfigProperty.class);
                String propertyName = annotation.name();
                String value = JettraConfig.getProperty(propertyName);

                if (value != null) {
                    field.setAccessible(true);
                    try {
                        field.set(target, value);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
