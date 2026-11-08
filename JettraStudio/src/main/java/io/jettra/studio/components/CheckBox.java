package io.jettra.studio.components;

import io.jettra.studio.core.Component;
import io.jettra.studio.markup.MarkupTag;
import io.jettra.studio.model.IModel;
import io.jettra.studio.model.Model;

/**
 * CheckBox input component (<input type="checkbox">) bound to a Boolean IModel.
 */
public class CheckBox extends Component {

    public CheckBox(String id) {
        this(id, new Model<>(Boolean.FALSE));
    }

    public CheckBox(String id, IModel<Boolean> model) {
        super(id, model);
    }

    public static CheckBox of(String id, IModel<Boolean> model) {
        return new CheckBox(id, model);
    }

    @Override
    public void onComponentTag(MarkupTag tag) {
        super.onComponentTag(tag);
        tag.setAttribute("type", "checkbox");
        tag.setAttribute("name", getId());
        addCssClass("espresso-checkbox");

        Object val = getModelObject();
        boolean checked = (val instanceof Boolean b) ? b : Boolean.parseBoolean(String.valueOf(val));
        if (checked) {
            tag.setAttribute("checked", "checked");
        } else {
            tag.setAttribute("checked", null);
        }
    }
}
