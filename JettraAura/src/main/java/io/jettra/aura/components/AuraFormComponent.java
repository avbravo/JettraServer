package io.jettra.aura.components;

import io.jettra.aura.core.AuraComponent;
import io.jettra.aura.core.AuraRequest;
import io.jettra.aura.core.AuraTag;
import io.jettra.aura.model.IModel;

/**
 * Clase base para componentes que capturan entrada de usuario en formularios.
 *
 * @param <T> Tipo del valor del campo
 */
public abstract class AuraFormComponent<T> extends AuraComponent {

    private boolean required = false;
    private String rawInput;

    public AuraFormComponent(String id) {
        super(id);
    }

    public AuraFormComponent(String id, IModel<T> model) {
        super(id, model);
    }

    public boolean isRequired() {
        return required;
    }

    public AuraFormComponent<T> setRequired(boolean required) {
        this.required = required;
        return this;
    }

    public String getInputName() {
        return getId();
    }

    public String getRawInput() {
        return rawInput;
    }

    public void processInput(AuraRequest req) {
        if (req != null) {
            String val = req.getParameter(getInputName());
            if (val == null) {
                val = req.getParameter(getPath());
            }
            this.rawInput = val;
            convertInput(val);
        }
    }

    protected abstract void convertInput(String input);

    @Override
    protected void onComponentTag(AuraTag tag) {
        super.onComponentTag(tag);
        tag.setAttribute("name", getInputName());
        if (required) {
            tag.setAttribute("required", "required");
        }
    }
}

