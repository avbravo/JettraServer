package io.jettra.studio.components;

import io.jettra.studio.core.Component;
import io.jettra.studio.markup.MarkupTag;
import io.jettra.studio.model.IModel;
import io.jettra.studio.model.Model;

/**
 * Multiline text input component (<textarea>) bound to an IModel.
 *
 * @param <T> Model value type
 */
public class TextArea<T> extends Component {

    private int rows = 4;
    private int cols = 40;
    private String placeholder;

    public TextArea(String id) {
        this(id, new Model<>(null));
    }

    public TextArea(String id, IModel<T> model) {
        super(id, model);
    }

    public static <T> TextArea<T> of(String id, IModel<T> model) {
        return new TextArea<>(id, model);
    }

    public TextArea<T> rows(int rows) {
        this.rows = rows;
        return this;
    }

    public TextArea<T> cols(int cols) {
        this.cols = cols;
        return this;
    }

    public TextArea<T> placeholder(String placeholder) {
        this.placeholder = placeholder;
        return this;
    }

    @Override
    public void onComponentTag(MarkupTag tag) {
        super.onComponentTag(tag);
        tag.setAttribute("name", getId());
        tag.setAttribute("rows", String.valueOf(rows));
        tag.setAttribute("cols", String.valueOf(cols));
        addCssClass("espresso-textarea");

        if (placeholder != null && !placeholder.isBlank()) {
            tag.setAttribute("placeholder", placeholder);
        }
    }

    @Override
    protected void onComponentTagBody(MarkupTag tag, StringBuilder buffer) {
        Object val = getModelObject();
        if (val != null) {
            buffer.append(escapeHtml(val.toString()));
        }
    }
}
