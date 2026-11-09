package io.jettra.aura.components;

import io.jettra.aura.core.AuraTag;
import io.jettra.aura.model.IModel;

/**
 * Casilla de verificación (&lt;input type="checkbox"&gt;).
 */
public class AuraCheckBox extends AuraFormComponent<Boolean> {

    public AuraCheckBox(String id) {
        super(id);
    }

    public AuraCheckBox(String id, IModel<Boolean> model) {
        super(id, model);
    }

    @Override
    protected void convertInput(String input) {
        setModelObject("on".equalsIgnoreCase(input) || "true".equalsIgnoreCase(input) || "1".equals(input));
    }

    @Override
    protected void onComponentTag(AuraTag tag) {
        super.onComponentTag(tag);
        tag.setAttribute("type", "checkbox");
        Boolean checked = (Boolean) getModelObject();
        if (Boolean.TRUE.equals(checked)) {
            tag.setAttribute("checked", "checked");
        } else {
            tag.removeAttribute("checked");
        }
    }
}

