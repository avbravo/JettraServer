package io.jettra.aura.core;

import io.jettra.aura.model.IModel;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Componente contenedor capaz de albergar una jerarquía de componentes hijos.
 */
public class AuraContainer extends io.jettra.aura.core.AuraComponent {

    private final Map<String, io.jettra.aura.core.AuraComponent> children = new LinkedHashMap<>();

    public AuraContainer(String id) {
        super(id);
    }

    public AuraContainer(String id, IModel<?> model) {
        super(id, model);
    }

    public AuraContainer add(io.jettra.aura.core.AuraComponent... components) {
        if (components != null) {
            for (io.jettra.aura.core.AuraComponent child : components) {
                if (child != null) {
                    child.setParent(this);
                    children.put(child.getId(), child);
                    child.onInitialize();
                }
            }
        }
        return this;
    }

    public io.jettra.aura.core.AuraComponent get(String id) {
        if (id == null || id.isBlank()) return null;
        if (id.contains(":")) {
            String[] parts = id.split(":", 2);
            io.jettra.aura.core.AuraComponent first = children.get(parts[0]);
            if (first == null) {
                first = findDirectOrNested(parts[0]);
            }
            if (first instanceof AuraContainer c) {
                return c.get(parts[1]);
            }
            return null;
        }
        io.jettra.aura.core.AuraComponent direct = children.get(id);
        if (direct != null) {
            return direct;
        }
        return findDirectOrNested(id);
    }

    private io.jettra.aura.core.AuraComponent findDirectOrNested(String id) {
        for (io.jettra.aura.core.AuraComponent child : children.values()) {
            if (child instanceof AuraContainer c && !(child instanceof io.jettra.aura.components.AuraListView)) {
                io.jettra.aura.core.AuraComponent found = c.get(id);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    public io.jettra.aura.core.AuraComponent findComponent(String pathOrId) {
        return get(pathOrId);
    }

    public boolean contains(String id) {
        return children.containsKey(id);
    }

    public io.jettra.aura.core.AuraComponent remove(String id) {
        io.jettra.aura.core.AuraComponent removed = children.remove(id);
        if (removed != null) {
            removed.setParent(null);
        }
        return removed;
    }

    public Collection<io.jettra.aura.core.AuraComponent> getChildren() {
        return Collections.unmodifiableCollection(children.values());
    }

    public Map<String, io.jettra.aura.core.AuraComponent> getChildrenMap() {
        return children;
    }

    /**
     * Inspecciona campos de la clase anotados con @AuraComponent o de tipo AuraComponent
     * para agregarlos automáticamente al contenedor si no fueron añadidos manualmente.
     */
    public void autoBindAnnotatedComponents() {
        Class<?> clazz = getClass();
        while (clazz != null && clazz != AuraContainer.class && clazz != io.jettra.aura.core.AuraComponent.class) {
            for (Field f : clazz.getDeclaredFields()) {
                if (io.jettra.aura.core.AuraComponent.class.isAssignableFrom(f.getType())) {
                    try {
                        f.setAccessible(true);
                        Object val = f.get(this);
                        if (val instanceof io.jettra.aura.core.AuraComponent comp) {
                            if (!children.containsKey(comp.getId())) {
                                add(comp);
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    @Override
    public void detach() {
        super.detach();
        for (io.jettra.aura.core.AuraComponent child : children.values()) {
            child.detach();
        }
    }
}
