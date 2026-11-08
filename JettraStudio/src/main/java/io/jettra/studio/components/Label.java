package io.jettra.studio.components;

import io.jettra.studio.core.WebComponent;
import io.jettra.studio.markup.MarkupTag;
import io.jettra.studio.model.IModel;
import io.jettra.studio.model.Model;

/**
 * Standard text Label component for JettraStudio.
 * Replaces the body of an HTML element marked with jettrat:id with the model's text string.
 *
 * Example:
 * Java: add(new Label("helloMessage", "Hello JettraStudio!"));
 * HTML: <div jettrat:id="helloMessage">[Placeholder]</div>
 */
public class Label extends WebComponent {

    public Label(String id) {
        super(id, new Model<>(""));
    }

    public Label(String id, String text) {
        super(id, new Model<>(text));
    }

    public Label(String id, IModel<?> model) {
        super(id, model);
    }

    public static Label of(String id, String text) {
        return new Label(id, text);
    }

    public static Label of(String id, IModel<?> model) {
        return new Label(id, model);
    }

    @Override
    protected void onComponentTagBody(MarkupTag tag, StringBuilder buffer) {
        Object val = getModelObject();
        if (val != null) {
            String str = val.toString();
            buffer.append(isEscapeModelStrings() ? escapeHtml(str) : str);
        }
    }
}
