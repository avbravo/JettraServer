package io.jettra.studio.components;

import io.jettra.studio.core.Component;
import io.jettra.studio.markup.MarkupTag;
import io.jettra.studio.model.IModel;
import io.jettra.studio.model.Model;

/**
 * Text input component (<input type="text">) bound to an IModel.
 *
 * @param <T> Model value type
 */
public class TextField<T> extends Component {

    private String placeholder;
    private boolean required = false;

    public TextField(String id) {
        this(id, new Model<>(null));
    }

    public TextField(String id, IModel<T> model) {
        super(id, model);
    }

    public static <T> TextField<T> of(String id, IModel<T> model) {
        return new TextField<>(id, model);
    }

    public TextField<T> placeholder(String placeholder) {
        this.placeholder = placeholder;
        return this;
    }

    public TextField<T> required(boolean required) {
        this.required = required;
        return this;
    }

    @Override
    public void onComponentTag(MarkupTag tag) {
        super.onComponentTag(tag);
        tag.setAttribute("name", getId());
        if (tag.getAttribute("type") == null) {
            tag.setAttribute("type", "text");
        }
        addCssClass("espresso-textfield");

        Object val = getModelObject();
        if (val != null) {
            tag.setAttribute("value", val.toString());
        }
        if (placeholder != null && !placeholder.isBlank()) {
            tag.setAttribute("placeholder", placeholder);
        }
        if (required) {
            tag.setAttribute("required", "required");
        }
    }
}
