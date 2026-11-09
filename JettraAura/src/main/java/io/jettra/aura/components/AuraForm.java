package io.jettra.aura.components;

import io.jettra.aura.core.AuraContainer;
import io.jettra.aura.core.AuraRequest;
import io.jettra.aura.core.AuraTag;
import io.jettra.aura.model.IModel;
import java.util.function.Consumer;

/**
 * Contenedor de formulario (&lt;form&gt;) que gestiona el ciclo de vida de envío de datos (POST),
 * deserialización de parámetros en modelos y ejecución de acciones.
 *
 * @param <T> Tipo de objeto del modelo principal del formulario
 */
public class AuraForm<T> extends AuraContainer {

    private Consumer<T> onSubmitConsumer;
    private Runnable onSubmitRunnable;
    private Runnable onErrorRunnable;
    private String action;

    public AuraForm(String id) {
        super(id);
    }

    public AuraForm(String id, IModel<T> model) {
        super(id, model);
    }

    public AuraForm(String id, Runnable onSubmit) {
        super(id);
        this.onSubmitRunnable = onSubmit;
    }

    public AuraForm(String id, IModel<T> model, Consumer<T> onSubmit) {
        super(id, model);
        this.onSubmitConsumer = onSubmit;
    }

    public AuraForm(String id, IModel<T> model, Runnable onSubmit) {
        super(id, model);
        this.onSubmitRunnable = onSubmit;
    }

    public AuraForm<T> setAction(String action) {
        this.action = action;
        return this;
    }

    public AuraForm<T> setOnSubmit(Runnable onSubmit) {
        this.onSubmitRunnable = onSubmit;
        return this;
    }

    public AuraForm<T> setOnSubmit(Consumer<T> onSubmit) {
        this.onSubmitConsumer = onSubmit;
        return this;
    }

    public AuraForm<T> setOnError(Runnable onError) {
        this.onErrorRunnable = onError;
        return this;
    }

    @Override
    protected void onComponentTag(AuraTag tag) {
        super.onComponentTag(tag);
        tag.setAttribute("method", "post");
        if (action != null && !action.isBlank()) {
            tag.setAttribute("action", action);
        }
    }

    /**
     * Procesa los datos enviados en la petición POST y ejecuta el submit.
     */
    public void processSubmit(AuraRequest req) {
        if (req == null) return;

        // 1. Procesar todos los componentes de formulario anidados
        processFormComponents(this, req);

        // 2. Ejecutar onSubmit
        try {
            onSubmit();
        } catch (Exception e) {
            onError();
        }
    }

    private void processFormComponents(AuraContainer container, AuraRequest req) {
        for (io.jettra.aura.core.AuraComponent child : container.getChildren()) {
            if (child instanceof AuraFormComponent<?> fc) {
                fc.processInput(req);
            } else if (child instanceof AuraContainer subContainer) {
                processFormComponents(subContainer, req);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void onSubmit() {
        if (onSubmitConsumer != null) {
            T modelObj = (T) getModelObject();
            onSubmitConsumer.accept(modelObj);
        } else if (onSubmitRunnable != null) {
            onSubmitRunnable.run();
        }
    }

    public void onError() {
        if (onErrorRunnable != null) {
            onErrorRunnable.run();
        }
    }
}
