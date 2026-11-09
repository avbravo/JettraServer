package io.jettra.aura.components;

import io.jettra.aura.core.AuraTag;
import io.jettra.aura.model.IModel;
import java.util.Collections;
import java.util.List;

/**
 * Menú desplegable de selección (&lt;select&gt;).
 *
 * @param <T> Tipo de las opciones
 */
public class AuraDropDownChoice<T> extends AuraFormComponent<T> {

    private List<T> choices = Collections.emptyList();

    public AuraDropDownChoice(String id) {
        super(id);
    }

    public AuraDropDownChoice(String id, List<T> choices) {
        super(id);
        this.choices = choices != null ? choices : Collections.emptyList();
    }

    public AuraDropDownChoice(String id, IModel<T> model, List<T> choices) {
        super(id, model);
        this.choices = choices != null ? choices : Collections.emptyList();
    }

    public List<T> getChoices() {
        return choices;
    }

    public AuraDropDownChoice<T> setChoices(List<T> choices) {
        this.choices = choices != null ? choices : Collections.emptyList();
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void convertInput(String input) {
        if (input == null || choices == null) return;
        for (T choice : choices) {
            if (choice != null && choice.toString().equals(input)) {
                setModelObject(choice);
                return;
            }
        }
        setModelObject((T) input);
    }

    @Override
    protected void onComponentTagBody(AuraTag tag, String markupBody, StringBuilder output) {
        Object selected = getModelObject();
        for (T choice : choices) {
            if (choice == null) continue;
            String str = choice.toString();
            boolean isSelected = (selected != null && selected.toString().equals(str));
            output.append("<option value=\"").append(escapeHtml(str)).append("\"");
            if (isSelected) {
                output.append(" selected=\"selected\"");
            }
            output.append(">").append(escapeHtml(str)).append("</option>\n");
        }
    }
}

