package io.jettra.aura.components;

import io.jettra.aura.core.AuraComponent;
import io.jettra.aura.core.AuraTag;
import io.jettra.aura.model.IModel;
import io.jettra.aura.model.Model;

/**
 * Componente botón interactivo (&lt;button&gt; o &lt;input type="submit"&gt;).
 */
public class AuraButton extends AuraComponent {

    private Runnable onSubmitListener;

    public AuraButton(String id) {
        super(id);
    }

    public AuraButton(String id, String label) {
        super(id, Model.of(label));
    }

    public AuraButton(String id, Runnable onSubmitListener) {
        super(id);
        this.onSubmitListener = onSubmitListener;
    }

    public AuraButton(String id, IModel<?> model, Runnable onSubmitListener) {
        super(id, model);
        this.onSubmitListener = onSubmitListener;
    }

    public void onSubmit() {
        if (onSubmitListener != null) {
            onSubmitListener.run();
        }
    }

    @Override
    protected void onComponentTag(AuraTag tag) {
        super.onComponentTag(tag);
        if ("input".equalsIgnoreCase(tag.getName())) {
            tag.setAttribute("type", "submit");
            Object val = getModelObject();
            if (val != null) {
                tag.setAttribute("value", val.toString());
            }
        }
        tag.setAttribute("name", getPath());
    }

    @Override
    protected void onComponentTagBody(AuraTag tag, String markupBody, StringBuilder output) {
        Object val = getModelObject();
        if (val != null) {
            output.append(isEscapeModelStrings() ? escapeHtml(val.toString()) : val.toString());
        } else if (markupBody != null) {
            output.append(markupBody);
        }
    }
}

