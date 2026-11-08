package io.jettra.studio.components;

import io.jettra.studio.core.WebComponent;
import io.jettra.studio.markup.MarkupTag;
import io.jettra.studio.model.IModel;
import io.jettra.studio.model.Model;

/**
 * Image component (<img>) in JettraStudio.
 */
public class Image extends WebComponent {

    private String altText = "";
    private Integer width;
    private Integer height;

    public Image(String id) {
        super(id);
    }

    public Image(String id, String src) {
        super(id, new Model<>(src));
    }

    public Image(String id, IModel<String> srcModel) {
        super(id, srcModel);
    }

    public static Image of(String id, String src) {
        return new Image(id, src);
    }

    public Image alt(String altText) {
        this.altText = altText;
        return this;
    }

    public Image width(int width) {
        this.width = width;
        return this;
    }

    public Image height(int height) {
        this.height = height;
        return this;
    }

    @Override
    public void onComponentTag(MarkupTag tag) {
        super.onComponentTag(tag);
        Object src = getModelObject();
        if (src != null) {
            tag.setAttribute("src", src.toString());
        }
        if (altText != null) {
            tag.setAttribute("alt", altText);
        }
        if (width != null) {
            tag.setAttribute("width", String.valueOf(width));
        }
        if (height != null) {
            tag.setAttribute("height", String.valueOf(height));
        }
        tag.setSelfClosing(true);
    }
}
