package io.jettra.aura.components;

import io.jettra.aura.core.AuraContainer;
import io.jettra.aura.model.IModel;
import io.jettra.aura.model.Model;

/**
 * Contenedor individual de cada fila o elemento dentro de un {@link AuraListView}.
 *
 * @param <T> Tipo de objeto del elemento
 */
public class AuraListItem<T> extends AuraContainer {

    private final int index;

    public AuraListItem(int index, IModel<T> model) {
        super(String.valueOf(index), model);
        this.index = index;
    }

    public AuraListItem(int index, T item) {
        this(index, Model.of(item));
    }

    public int getIndex() {
        return index;
    }

    @SuppressWarnings("unchecked")
    public T getModelData() {
        return (T) getModelObject();
    }
}

