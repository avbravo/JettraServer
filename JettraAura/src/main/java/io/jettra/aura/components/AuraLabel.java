package io.jettra.aura.components;

import io.jettra.aura.core.AuraComponent;
import io.jettra.aura.core.AuraTag;
import io.jettra.aura.model.IModel;
import io.jettra.aura.model.Model;
import java.util.function.Supplier;

/**
 * Componente de etiqueta de texto .
 * Reemplaza el contenido interno de la etiqueta HTML asociada por el valor de su modelo.
 */
public class AuraLabel extends AuraComponent {

    public AuraLabel(String id) {
        super(id);
    }

    public AuraLabel(String id, String text) {
        super(id, Model.of(text));
    }

    public AuraLabel(String id, IModel<?> model) {
        super(id, model);
    }

    public AuraLabel(String id, Supplier<?> supplier) {
        super(id, new IModel<>() {
            @Override
            public Object getObject() {
                return supplier != null ? supplier.get() : null;
            }

            @Override
            public void setObject(Object object) {}
        });
    }

    @Override
    protected void onComponentTagBody(AuraTag tag, String markupBody, StringBuilder output) {
        Object val = getModelObject();
        String str = val != null ? String.valueOf(val) : "";
        if (isEscapeModelStrings()) {
            output.append(escapeHtml(str));
        } else {
            output.append(str);
        }
    }
}

