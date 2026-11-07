package io.jettra.server.jakarta.cdi;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;
import jakarta.inject.Inject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contenedor de Inyección de Dependencias y Ciclo de Vida CDI 4.1 para Jakarta EE 11 y Java 25.
 * Admite tanto resolución ágil nativa en memoria (zero-overhead) como integración completa
 * con el contenedor oficial Jakarta SE (Weld SE / SeContainer).
 */
public class CdiContainer {

    private final Map<Class<?>, Object> singletons = new ConcurrentHashMap<>();
    private SeContainer seContainer;

    public CdiContainer() {
    }

    /**
     * Inicia programáticamente el contenedor oficial CDI 4.1 SeContainer (Weld SE).
     */
    public synchronized void startSeContainer() {
        if (seContainer == null || !seContainer.isRunning()) {
            try {
                SeContainerInitializer initializer = SeContainerInitializer.newInstance();
                this.seContainer = initializer.initialize();
            } catch (Throwable ignored) {
                // Si no se encuentra beans.xml o se ejecuta en modo standalone mínimo, opera con el motor de inyección interno
            }
        }
    }

    public <T> void registerSingleton(Class<T> type, T instance) {
        singletons.put(type, instance);
        injectFields(instance);
        invokePostConstruct(instance);
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> type) {
        // 1. Si existe SeContainer oficial y puede resolverlo
        if (seContainer != null && seContainer.isRunning()) {
            try {
                var instance = seContainer.select(type);
                if (instance.isResolvable()) {
                    return instance.get();
                }
            } catch (Throwable ignored) {}
        }

        // 2. Buscar en registro singleton local
        Object existing = singletons.get(type);
        if (existing != null) {
            return (T) existing;
        }

        // 3. Buscar si existe una subclase o implementación registrada
        for (Map.Entry<Class<?>, Object> entry : singletons.entrySet()) {
            if (type.isAssignableFrom(entry.getKey())) {
                return (T) entry.getValue();
            }
        }

        // 4. Si no está registrado pero es instanciable, crearlo e inyectar dependencias
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
                if (isInjectAnnotated(field)) {
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

    private boolean isInjectAnnotated(Field field) {
        return field.isAnnotationPresent(Inject.class)
                || field.isAnnotationPresent(CdiAnnotations.Inject.class);
    }

    private void invokePostConstruct(Object instance) {
        for (Method m : instance.getClass().getDeclaredMethods()) {
            if (m.isAnnotationPresent(PostConstruct.class)
                    || m.isAnnotationPresent(CdiAnnotations.PostConstruct.class)) {
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
                if (m.isAnnotationPresent(PreDestroy.class)
                        || m.isAnnotationPresent(CdiAnnotations.PreDestroy.class)) {
                    m.setAccessible(true);
                    try {
                        m.invoke(instance);
                    } catch (Exception ignored) {}
                }
            }
        }
        singletons.clear();

        if (seContainer != null && seContainer.isRunning()) {
            try {
                seContainer.close();
            } catch (Throwable ignored) {}
            seContainer = null;
        }
    }

    public SeContainer getSeContainer() {
        return seContainer;
    }
}
