package io.jettra.aura.ajax;

import io.jettra.aura.components.AuraLink;
import io.jettra.aura.core.AuraTag;
import io.jettra.aura.model.IModel;
import java.util.function.Consumer;

/**
 * Hipervínculo (&lt;a&gt;) reactivo que dispara una petición Ajax.
 */
public class AuraAjaxLink extends AuraLink {

    private Consumer<AuraAjaxRequestTarget> onAjaxClickConsumer;

    public AuraAjaxLink(String id) {
        super(id);
        setOutputMarkupId(true);
    }

    public AuraAjaxLink(String id, Consumer<AuraAjaxRequestTarget> onAjaxClickConsumer) {
        super(id);
        this.onAjaxClickConsumer = onAjaxClickConsumer;
        setOutputMarkupId(true);
    }

    public AuraAjaxLink(String id, IModel<?> model, Consumer<AuraAjaxRequestTarget> onAjaxClickConsumer) {
        super(id, model, null);
        this.onAjaxClickConsumer = onAjaxClickConsumer;
        setOutputMarkupId(true);
    }

    public void onAjaxClick(AuraAjaxRequestTarget target) {
        if (onAjaxClickConsumer != null) {
            onAjaxClickConsumer.accept(target);
        } else {
            onClick();
        }
    }

    @Override
    protected void onComponentTag(AuraTag tag) {
        super.onComponentTag(tag);
        tag.setAttribute("href", "javascript:void(0)");
        tag.setAttribute("onclick", "auraAjax('" + getPath() + "', 'click'); return false;");
    }
}

