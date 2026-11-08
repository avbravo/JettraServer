package io.jettra.studio.components;

import io.jettra.studio.core.Component;
import io.jettra.studio.markup.MarkupTag;
import io.jettra.studio.model.IModel;
import io.jettra.studio.model.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Dropdown Select component (<select>) rendering a list of choices.
 *
 * @param <T> Choice object type
 */
public class Select<T> extends Component {

    private List<T> choices = new ArrayList<>();
    private Function<T, String> labelRenderer = Objects::toString;
    private Function<T, String> valueRenderer = Objects::toString;
    private String nullValidPrompt;

    public Select(String id) {
        this(id, new Model<>(null), new ArrayList<>());
    }

    public Select(String id, IModel<T> model, List<T> choices) {
        super(id, model);
        if (choices != null) {
            this.choices = choices;
        }
    }

    public static <T> Select<T> of(String id, IModel<T> model, List<T> choices) {
        return new Select<>(id, model, choices);
    }

    public Select<T> choices(List<T> choices) {
        if (choices != null) {
            this.choices = choices;
        }
        return this;
    }

    public Select<T> labelRenderer(Function<T, String> renderer) {
        if (renderer != null) {
            this.labelRenderer = renderer;
        }
        return this;
    }

    public Select<T> valueRenderer(Function<T, String> renderer) {
        if (renderer != null) {
            this.valueRenderer = renderer;
        }
        return this;
    }

    public Select<T> setNullValid(String prompt) {
        this.nullValidPrompt = prompt;
        return this;
    }

    @Override
    public void onComponentTag(MarkupTag tag) {
        super.onComponentTag(tag);
        tag.setAttribute("name", getId());
        addCssClass("espresso-select");
    }

    @Override
    protected void onComponentTagBody(MarkupTag tag, StringBuilder buffer) {
        Object currentVal = getModelObject();

        if (nullValidPrompt != null) {
            buffer.append("<option value=\"\">").append(escapeHtml(nullValidPrompt)).append("</option>\n");
        }

        for (T choice : choices) {
            if (choice == null) continue;
            String valStr = valueRenderer.apply(choice);
            String labelStr = labelRenderer.apply(choice);

            boolean isSelected = false;
            if (currentVal != null) {
                if (currentVal.equals(choice) || currentVal.toString().equals(valStr)) {
                    isSelected = true;
                }
            }

            buffer.append("<option value=\"").append(escapeHtml(valStr)).append("\"")
                  .append(isSelected ? " selected=\"selected\"" : "")
                  .append(">")
                  .append(escapeHtml(labelStr))
                  .append("</option>\n");
        }
    }
}
