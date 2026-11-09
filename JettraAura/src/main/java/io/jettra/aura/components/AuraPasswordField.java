package io.jettra.aura.components;

import io.jettra.aura.core.AuraTag;
import io.jettra.aura.model.IModel;

/**
 * Campo de contraseña seguro (&lt;input type="password"&gt;).
 */
public class AuraPasswordField extends AuraFormComponent<String> {

    public AuraPasswordField(String id) {
        super(id);
    }

    public AuraPasswordField(String id, IModel<String> model) {
        super(id, model);
    }

    @Override
    protected void convertInput(String input) {
        setModelObject(input);
    }

    @Override
    protected void onComponentTag(AuraTag tag) {
        super.onComponentTag(tag);
        tag.setAttribute("type", "password");
    }
}

