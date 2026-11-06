package io.jettra.server.jakarta.cdi;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contenedor de Inyección de Dependencias y Ciclo de Vida CDI 4.1.
 */
public class CdiContainer {

    private final Map<Class<?>, Object> singletons = new ConcurrentHashMap<>();

    public <T> void registerSingleton(Class<T> type, T instance) {
        singletons.put(type, instance);
        injectFields(instance);
        invokePostConstruct(instance);
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> type) {
        Object existing = singletons.get(type);
        if (existing != null) {
            return (T) existing;
        }

        // Buscar si existe una subclase o implementación registrada
        for (Map.Entry<Class<?>, Object> entry : singletons.entrySet()) {
            if (type.isAssignableFrom(entry.getKey())) {
                return (T) entry.getValue();
            }
        }

        // Si no está registrado pero es instanciable, crearlo
        try {
            T newInstance = type.getDeclaredConstructor().newInstance();
            registerSingleton(type, newInstance);
            return newInstance;
        } catch (Exception e) {
            throw new RuntimeException("No se pudo crear o resolver el bean CDI de tipo: " + type.getName(), e);
        }
    }

    public void injectFields(Object target) {
        if (target == null) return;
        Class<?> clazz = target.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(CdiAnnotations.Inject.class)) {
                    field.setAccessible(true);
                    try {
                        Object dep = getBean(field.getType());
                        field.set(target, dep);
                    } catch (Exception e) {
                        throw new RuntimeException("Fallo inyectando campo " + field.getName() + " en " + clazz.getName(), e);
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    private void invokePostConstruct(Object instance) {
        for (Method m : instance.getClass().getDeclaredMethods()) {
            if (m.isAnnotationPresent(CdiAnnotations.PostConstruct.class)) {
                m.setAccessible(true);
                try {
                    m.invoke(instance);
                } catch (Exception e) {
                    throw new RuntimeException("Error en @PostConstruct de " + instance.getClass().getName(), e);
                }
            }
        }
    }

    public void shutdown() {
        for (Object instance : singletons.values()) {
            for (Method m : instance.getClass().getDeclaredMethods()) {
                if (m.isAnnotationPresent(CdiAnnotations.PreDestroy.class)) {
                    m.setAccessible(true);
                    try {
                        m.invoke(instance);
                    } catch (Exception ignored) {}
                }
            }
        }
        singletons.clear();
    }
}
