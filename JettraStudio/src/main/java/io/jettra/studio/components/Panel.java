package io.jettra.studio.components;

import io.jettra.studio.core.MarkupContainer;
import io.jettra.studio.markup.HtmlResourceLoader;
import io.jettra.studio.markup.MarkupTag;
import io.jettra.studio.model.IModel;

/**
 * Reusable panel component paired with its own HTML file, reusable panel component for JettraStudio.
 * Example: UserCardPanel.java pairs with UserCardPanel.html.
 */
public class Panel extends MarkupContainer {

    public Panel(String id) {
        super(id);
    }

    public Panel(String id, IModel<?> model) {
        super(id, model);
    }

    @Override
    protected void onComponentTagBody(MarkupTag tag, StringBuilder buffer) {
        // Load panel's own HTML markup
        String panelHtml = HtmlResourceLoader.getInstance().loadMarkup(this.getClass());
        if (panelHtml != null && !panelHtml.isBlank()) {
            renderMarkupContent(panelHtml, buffer);
        } else {
            // Fallback to placeholder template
            super.onComponentTagBody(tag, buffer);
        }
    }
}
