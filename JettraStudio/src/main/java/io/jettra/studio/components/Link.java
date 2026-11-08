package io.jettra.studio.components;

import io.jettra.studio.core.Component;
import io.jettra.studio.markup.MarkupTag;
import io.jettra.studio.model.IModel;
import io.jettra.studio.model.Model;

/**
 * Anchor Link component for JettraStudio, with click callbacks or direct destination URLs.
 */
public class Link extends Component {

    private String href;
    private Runnable clickAction;

    public Link(String id) {
        super(id);
    }

    public Link(String id, String href) {
        super(id);
        this.href = href;
    }

    public Link(String id, Runnable clickAction) {
        super(id);
        this.clickAction = clickAction;
    }

    public Link(String id, IModel<String> labelModel, Runnable clickAction) {
        super(id, labelModel);
        this.clickAction = clickAction;
    }

    public static Link of(String id, String href) {
        return new Link(id, href);
    }

    public static Link of(String id, Runnable clickAction) {
        return new Link(id, clickAction);
    }

    public Link href(String href) {
        this.href = href;
        return this;
    }

    public Link onClick(Runnable clickAction) {
        this.clickAction = clickAction;
        return this;
    }

    public void onClick() {
        if (clickAction != null) {
            clickAction.run();
        }
    }

    @Override
    public void onComponentTag(MarkupTag tag) {
        super.onComponentTag(tag);
        if (href != null && !href.isBlank()) {
            tag.setAttribute("href", href);
        } else {
            // Self-referencing link or action hook
            tag.setAttribute("href", "?_action=" + getId());
        }
    }

    @Override
    protected void onComponentTagBody(MarkupTag tag, StringBuilder buffer) {
        Object val = getModelObject();
        if (val != null && !val.toString().isEmpty()) {
            buffer.append(escapeHtml(val.toString()));
        } else {
            buffer.append(tag.getBodyContent());
        }
    }
}
