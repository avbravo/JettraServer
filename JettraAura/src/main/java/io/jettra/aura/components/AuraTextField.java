package io.jettra.aura.components;

import io.jettra.aura.core.AuraTag;
import io.jettra.aura.model.IModel;

/**
 * Campo de texto estándar (&lt;input type="text"&gt;).
 *
 * @param <T> Tipo de valor
 */
public class AuraTextField<T> extends AuraFormComponent<T> {

    private Class<T> type;

    public AuraTextField(String id) {
        super(id);
    }

    public AuraTextField(String id, Class<T> type) {
        super(id);
        this.type = type;
    }

    public AuraTextField(String id, IModel<T> model) {
        super(id, model);
    }

    public AuraTextField(String id, IModel<T> model, Class<T> type) {
        super(id, model);
        this.type = type;
    }

    public AuraTextField<T> setType(Class<T> type) {
        this.type = type;
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void convertInput(String input) {
        if (input == null) {
            setModelObject(null);
            return;
        }

        if (type == null || type == String.class) {
            setModelObject(input);
        } else if (type == Integer.class || type == int.class) {
            try { setModelObject((T) Integer.valueOf(input.trim())); } catch (Exception ignored) {}
        } else if (type == Long.class || type == long.class) {
            try { setModelObject((T) Long.valueOf(input.trim())); } catch (Exception ignored) {}
        } else if (type == Double.class || type == double.class) {
            try { setModelObject((T) Double.valueOf(input.trim())); } catch (Exception ignored) {}
        } else {
            setModelObject(input);
        }
    }

    @Override
    protected void onComponentTag(AuraTag tag) {
        super.onComponentTag(tag);
        if (!tag.getAttributes().containsKey("type")) {
            tag.setAttribute("type", "text");
        }
        Object val = getModelObject();
        if (val != null) {
            tag.setAttribute("value", val.toString());
        }
    }
}

