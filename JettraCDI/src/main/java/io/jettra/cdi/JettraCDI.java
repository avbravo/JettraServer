    package io.jettra.cdi;

import io.jettra.cdi.config.ConfigInjector;
import io.jettra.cdi.config.JettraConfig;
import io.jettra.cdi.context.JettraContext;
import io.jettra.core.inject.annotation.Inject;
import io.jettra.core.inject.annotation.InjectProperties;
import io.jettra.scoped.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contenedor CDI nativo ultra-ligero de Jettra.
 * Gestiona ciclo de vida, inyección de dependencias (@Inject),
 * inyección de propiedades (@InjectProperties, @JettraConfigProperty, @ConfigProperty),
 * ámbitos (@ApplicationScoped, @Singleton, @RequestScoped, @SessionScoped, @ViewScoped)
 * y métodos de inicialización (@PostConstruct).
 */
public class JettraCDI {

    private static final JettraCDI INSTANCE = new JettraCDI();

    private final Map<Class<?>, Object> singletons = new ConcurrentHashMap<>();
    private final Set<Class<?>> managedBeanClasses = ConcurrentHashMap.newKeySet();
    private final Map<String, Object> namedBeans = new ConcurrentHashMap<>();

    public JettraCDI() {
    }

    public static JettraCDI current() {
        return INSTANCE;
    }

    public static JettraCDI getInstance() {
        return INSTANCE;
    }

    public void registerBean(Class<?> clazz) {
        if (clazz != null && !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())) {
            managedBeanClasses.add(clazz);
        }
    }

    public <T> void registerSingleton(Class<T> clazz, T instance) {
        if (clazz != null && instance != null) {
            singletons.put(clazz, instance);
            managedBeanClasses.add(clazz);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> clazz) {
        if (clazz == null) return null;

        // 1. Si ya existe como singleton
        if (singletons.containsKey(clazz)) {
            return (T) singletons.get(clazz);
        }

        // 2. Si coincide con una interfaz ya implementada en singletons
        for (Map.Entry<Class<?>, Object> entry : singletons.entrySet()) {
            if (clazz.isAssignableFrom(entry.getKey())) {
                return (T) entry.getValue();
            }
        }

        // 3. Si es una interfaz, buscar una clase administrada o convención Impl
        if (clazz.isInterface()) {
            for (Class<?> implClass : managedBeanClasses) {
                if (clazz.isAssignableFrom(implClass)) {
                    return (T) getBean(implClass);
                }
            }
            // Intentar convención NameImpl
            try {
                Class<?> implClass = Class.forName(clazz.getName() + "Impl");
                managedBeanClasses.add(implClass);
                return (T) getBean(implClass);
            } catch (ClassNotFoundException ignored) {}
        }

        // 4. Ámbito RequestScoped
        if (clazz.isAnnotationPresent(RequestScoped.class)) {
            JettraContext ctx = JettraContext.getCurrent();
            Object obj = ctx.get(JettraContext.Scope.REQUEST, clazz.getName());
            if (obj == null) {
                obj = createInstance(clazz);
                ctx.set(JettraContext.Scope.REQUEST, clazz.getName(), obj);
            }
            return (T) obj;
        }

        // 5. Ámbito SessionScoped
        if (clazz.isAnnotationPresent(SessionScoped.class)) {
            JettraContext ctx = JettraContext.getCurrent();
            Object obj = ctx.get(JettraContext.Scope.SESSION, clazz.getName());
            if (obj == null) {
                obj = createInstance(clazz);
                ctx.set(JettraContext.Scope.SESSION, clazz.getName(), obj);
            }
            return (T) obj;
        }

        // 6. Ámbito ViewScoped
        if (clazz.isAnnotationPresent(ViewScoped.class)) {
            JettraContext ctx = JettraContext.getCurrent();
            Object obj = ctx.get(JettraContext.Scope.VIEW, clazz.getName());
            if (obj == null) {
                obj = createInstance(clazz);
                ctx.set(JettraContext.Scope.VIEW, clazz.getName(), obj);
            }
            return (T) obj;
        }

        // 7. Ámbito ApplicationScoped o Singleton (por defecto si no es abstracto)
        boolean isApplicationScoped = clazz.isAnnotationPresent(ApplicationScoped.class)
                || clazz.isAnnotationPresent(Singleton.class)
                || !clazz.isInterface();

        if (isApplicationScoped) {
            synchronized (singletons) {
                if (singletons.containsKey(clazz)) {
                    return (T) singletons.get(clazz);
                }
                T instance = createInstance(clazz);
                if (instance != null) {
                    singletons.put(clazz, instance);
                }
                return instance;
            }
        }

        return createInstance(clazz);
    }

    public Object getBeanByName(String name) {
        if (name == null || name.isBlank()) return null;
        Object found = namedBeans.get(name);
        if (found != null) return found;

        for (Class<?> clazz : managedBeanClasses) {
            String beanName = extractBeanName(clazz);
            if (name.equalsIgnoreCase(beanName)) {
                return getBean(clazz);
            }
        }
        for (Map.Entry<Class<?>, Object> entry : singletons.entrySet()) {
            String beanName = extractBeanName(entry.getKey());
            if (name.equalsIgnoreCase(beanName)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private String extractBeanName(Class<?> clazz) {
        for (Annotation ann : clazz.getAnnotations()) {
            if (ann.annotationType().getSimpleName().equals("Named")) {
                try {
                    Method vm = ann.annotationType().getMethod("value");
                    String v = (String) vm.invoke(ann);
                    if (v != null && !v.isBlank()) return v;
                } catch (Exception ignored) {}
            }
        }
        String simple = clazz.getSimpleName();
        return Character.toLowerCase(simple.charAt(0)) + simple.substring(1);
    }

    public <T> T createInstance(Class<T> clazz) {
        if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
            return null;
        }
        try {
            var constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            T instance = constructor.newInstance();
            inject(instance);
            invokePostConstruct(instance);
            return instance;
        } catch (Exception e) {
            System.err.println("[JettraCDI] Error instanciando bean: " + clazz.getName() + " - " + e.getMessage());
            return null;
        }
    }

    public void inject(Object target) {
        if (target == null) return;
        Class<?> current = target.getClass();

        // 1. Inyección de configuración (@JettraConfigProperty, @ConfigProperty)
        ConfigInjector.inject(target);

        // 2. Inyección de campos (@Inject, @InjectProperties)
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                field.setAccessible(true);

                // Inyección de @Inject
                if (hasInjectAnnotation(field)) {
                    try {
                        Object existing = field.get(target);
                        if (existing == null) {
                            Object dep = getBean(field.getType());
                            if (dep != null) {
                                field.set(target, dep);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("[JettraCDI] Error inyectando @Inject en " + field.getName() + ": " + e.getMessage());
                    }
                }
                // Inyección de @InjectProperties (messages, etc.)
                else if (field.isAnnotationPresent(InjectProperties.class)) {
                    InjectProperties ip = field.getAnnotation(InjectProperties.class);
                    String bundleName = ip.name();
                    String lang = JettraConfig.getProperty("app.language", "es");
                    Properties p = JettraConfig.loadNamedBundle(bundleName, lang);
                    try {
                        field.set(target, p);
                    } catch (Exception e) {
                        System.err.println("[JettraCDI] Error inyectando @InjectProperties en " + field.getName() + ": " + e.getMessage());
                    }
                }
            }
            current = current.getSuperclass();
        }
    }

    private boolean hasInjectAnnotation(Field field) {
        if (field.isAnnotationPresent(Inject.class)) return true;
        for (Annotation ann : field.getAnnotations()) {
            if (ann.annotationType().getSimpleName().equals("Inject")) {
                return true;
            }
        }
        return false;
    }

    public void invokePostConstruct(Object instance) {
        if (instance == null) return;
        Class<?> current = instance.getClass();
        while (current != null && current != Object.class) {
            for (Method method : current.getDeclaredMethods()) {
                boolean hasPost = false;
                for (Annotation ann : method.getAnnotations()) {
                    if (ann.annotationType().getSimpleName().equals("PostConstruct")) {
                        hasPost = true;
                        break;
                    }
                }
                if (hasPost && method.getParameterCount() == 0) {
                    try {
                        method.setAccessible(true);
                        method.invoke(instance);
                    } catch (Exception e) {
                        System.err.println("[JettraCDI] Error ejecutando @PostConstruct en " + current.getName() + ": " + e.getMessage());
                    }
                }
            }
            current = current.getSuperclass();
        }
    }

    public void invokePreDestroy(Object instance) {
        if (instance == null) return;
        Class<?> current = instance.getClass();
        while (current != null && current != Object.class) {
            for (Method method : current.getDeclaredMethods()) {
                boolean hasPre = false;
                for (Annotation ann : method.getAnnotations()) {
                    if (ann.annotationType().getSimpleName().equals("PreDestroy")) {
                        hasPre = true;
                        break;
                    }
                }
                if (hasPre && method.getParameterCount() == 0) {
                    try {
                        method.setAccessible(true);
                        method.invoke(instance);
                    } catch (Exception e) {
                        System.err.println("[JettraCDI] Error ejecutando @PreDestroy en " + current.getName() + ": " + e.getMessage());
                    }
                }
            }
            current = current.getSuperclass();
        }
    }

    public void clearRequestScope() {
        JettraContext.getCurrent().destroyRequest();
    }

    public void clearAll() {
        for (Object inst : singletons.values()) {
            invokePreDestroy(inst);
        }
        singletons.clear();
        managedBeanClasses.clear();
        namedBeans.clear();
        JettraContext.clearAll();
    }
    
}
