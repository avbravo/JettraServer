package io.jettra.aura.components;

import io.jettra.aura.core.AuraContainer;
import io.jettra.aura.core.AuraTag;
import io.jettra.aura.model.IModel;
import io.jettra.aura.model.Model;
import io.jettra.aura.template.AuraMarkupDocument;
import io.jettra.aura.template.AuraTemplateParser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Componente repetidor de listas .
 * Clona el fragmento de marcado HTML interno para cada elemento de la colección
 * y delega en {@link #populateItem(AuraListItem)} o en un {@link Consumer} la vinculación
 * de componentes hijos a cada fila.
 *
 * @param <T> Tipo de objeto de cada elemento de la lista
 */
public class AuraListView<T> extends AuraContainer {

    private Consumer<AuraListItem<T>> populator;

    public AuraListView(String id) {
        super(id);
    }

    public AuraListView(String id, List<T> list) {
        super(id, Model.of(list != null ? list : Collections.emptyList()));
    }

    public AuraListView(String id, IModel<List<T>> model) {
        super(id, model);
    }

    public AuraListView(String id, List<T> list, Consumer<AuraListItem<T>> populator) {
        this(id, list);
        this.populator = populator;
    }

    public AuraListView(String id, IModel<List<T>> model, Consumer<AuraListItem<T>> populator) {
        this(id, model);
        this.populator = populator;
    }

    /**
     * Método invocado para poblar los componentes de cada fila de la lista.
     * Puede sobrescribirse o proveerse mediante un Consumer funcional.
     */
    protected void populateItem(AuraListItem<T> item) {
        if (populator != null) {
            populator.accept(item);
        }
    }

    /**
     * Reconstruye los items hijos ({@link AuraListItem}) a partir del modelo actual
     * y los registra en este contenedor para permitir la resolución de rutas de eventos
     * (por ejemplo, `productList:0:btnDelete`).
     */
    @SuppressWarnings("unchecked")
    public void populateItems() {
        getChildrenMap().clear();
        Object modelObj = getModelObject();
        List<T> list = (modelObj instanceof List<?>) ? (List<T>) modelObj : Collections.emptyList();
        int index = 0;
        for (T element : list) {
            AuraListItem<T> item = new AuraListItem<>(index++, Model.of(element));
            super.add(item);
            populateItem(item);
        }
    }

    @Override
    public io.jettra.aura.core.AuraComponent get(String id) {
        if (getChildrenMap().isEmpty()) {
            populateItems();
        }
        return super.get(id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void render(AuraTag tag, String markupBody, StringBuilder output) {
        onConfigure();
        if (!isVisible()) {
            return;
        }
        onBeforeRender();

        if (getModel() != null) {
            getModel().detach();
        }
        populateItems();

        if (getChildrenMap().isEmpty()) {
            return;
        }

        AuraMarkupDocument rowDoc = (markupBody != null && !markupBody.isBlank())
                ? AuraTemplateParser.parse(markupBody)
                : null;

        for (io.jettra.aura.core.AuraComponent child : getChildren()) {
            if (child instanceof AuraListItem<?> item) {
                AuraTag itemTag = new AuraTag(tag.getName());
                itemTag.getAttributes().putAll(tag.getAttributes());
                itemTag.removeAttribute("aura:id");
                itemTag.removeAttribute("j:id");
                itemTag.removeAttribute("data-aura-id");
                onComponentTag(itemTag);

                output.append(itemTag.renderOpen());
                if (!itemTag.isSelfClosing()) {
                    if (rowDoc != null) {
                        output.append(rowDoc.render(item));
                    }
                    output.append(itemTag.renderClose());
                }
            }
        }
    }
}
