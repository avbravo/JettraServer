package io.jettra.studio.components;

import io.jettra.studio.markup.MarkupTag;
import io.jettra.studio.model.IModel;
import io.jettra.studio.model.Model;

/**
 * Label that converts newline characters (\n) into HTML line breaks (&lt;br/&gt;).
 */
public class MultiLineLabel extends Label {

    public MultiLineLabel(String id) {
        super(id);
    }

    public MultiLineLabel(String id, String text) {
        super(id, text);
    }

    public MultiLineLabel(String id, IModel<?> model) {
        super(id, model);
    }

    @Override
    protected void onComponentTagBody(MarkupTag tag, StringBuilder buffer) {
        Object val = getModelObject();
        if (val != null) {
            String str = isEscapeModelStrings() ? escapeHtml(val.toString()) : val.toString();
            buffer.append(str.replace("\r\n", "<br/>").replace("\n", "<br/>"));
        }
    }
}
