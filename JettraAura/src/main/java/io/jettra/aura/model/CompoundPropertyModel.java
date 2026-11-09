package io.jettra.aura.model;

/**
 * Modelo compuesto que propaga automáticamente el acceso a propiedades del objeto raíz
 * a todos los componentes hijos en función de su identificador (component.getId()).
 * Característica emblemática de Wicket ahora adaptada a JettraAura.
 *
 * @param <T> Tipo del objeto raíz del modelo compuesto
 */
public class CompoundPropertyModel<T> implements IModel<T> {

    private T target;

    public CompoundPropertyModel(T target) {
        this.target = target;
    }

    public static <T> CompoundPropertyModel<T> of(T target) {
        return new CompoundPropertyModel<>(target);
    }

    @Override
    public T getObject() {
        return target;
    }

    @Override
    public void setObject(T object) {
        this.target = object;
    }

    /**
     * Vincula una propiedad específica usando el ID del componente hijo.
     */
    public <P> IModel<P> bind(String propertyName) {
        return new IModel<>() {
            @Override
            @SuppressWarnings("unchecked")
            public P getObject() {
                if (target == null) return null;
                return (P) PropertyModel.readProperty(target, propertyName);
            }

            @Override
            public void setObject(P object) {
                if (target != null) {
                    PropertyModel.writeProperty(target, propertyName, object);
                }
            }
        };
    }
}

