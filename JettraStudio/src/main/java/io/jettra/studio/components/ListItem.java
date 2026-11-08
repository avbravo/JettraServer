package io.jettra.studio.components;

import io.jettra.studio.core.MarkupContainer;
import io.jettra.studio.model.IModel;
import io.jettra.studio.model.Model;

/**
 * Container for an individual repeated item inside a ListView.
 *
 * @param <T> Item object type
 */
public class ListItem<T> extends MarkupContainer {

    private final int index;

    public ListItem(int index, IModel<T> model) {
        super(String.valueOf(index), model);
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    @SuppressWarnings("unchecked")
    public T getModelObject() {
        return (T) super.getModelObject();
    }
}
