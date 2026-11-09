package io.jettra.aura.model;

/**
 * Modelo desacoplable y de carga perezosa (Lazy Loadable Detachable Model).
 * Carga los datos bajo demanda en la primera llamada a {@link #getObject()} y
 * descarta la referencia en memoria transitoria al ejecutarse {@link #detach()}
 * al final del ciclo de vida de la petición HTTP, reduciendo drásticamente la
 * presión sobre el recolector de basura (GC).
 *
 * @param <T> Tipo de objeto cargado
 */
public abstract class LoadableDetachableModel<T> implements IModel<T> {

    private transient boolean attached = false;
    private transient T transientModelObject;

    public LoadableDetachableModel() {}

    public LoadableDetachableModel(T object) {
        this.transientModelObject = object;
        this.attached = (object != null);
    }

    /**
     * Método abstracto que obtiene el objeto (por ejemplo, desde una base de datos o servicio JPA).
     */
    protected abstract T load();

    @Override
    public T getObject() {
        if (!attached) {
            transientModelObject = load();
            attached = true;
        }
        return transientModelObject;
    }

    @Override
    public void setObject(T object) {
        this.transientModelObject = object;
        this.attached = true;
    }

    @Override
    public void detach() {
        if (attached) {
            onDetach();
            attached = false;
            transientModelObject = null;
        }
    }

    protected void onDetach() {}

    public boolean isAttached() {
        return attached;
    }
}

