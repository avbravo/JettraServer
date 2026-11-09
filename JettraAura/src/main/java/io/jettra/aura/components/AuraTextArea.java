package io.jettra.aura.components;

import io.jettra.aura.core.AuraTag;
import io.jettra.aura.model.IModel;

/**
 * Campo de texto multilínea (&lt;textarea&gt;).
 */
public class AuraTextArea<T> extends AuraFormComponent<T> {

    public AuraTextArea(String id) {
        super(id);
    }

    public AuraTextArea(String id, IModel<T> model) {
        super(id, model);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void convertInput(String input) {
        setModelObject((T) input);
    }

    @Override
    protected void onComponentTagBody(AuraTag tag, String markupBody, StringBuilder output) {
        Object val = getModelObject();
        if (val != null) {
            output.append(isEscapeModelStrings() ? escapeHtml(val.toString()) : val.toString());
        }
    }
}

