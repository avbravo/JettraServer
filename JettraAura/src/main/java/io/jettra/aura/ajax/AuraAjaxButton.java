package io.jettra.aura.ajax;

import io.jettra.aura.components.AuraButton;
import io.jettra.aura.core.AuraTag;
import io.jettra.aura.model.IModel;
import java.util.function.Consumer;

/**
 * Botón reactivo que procesa la acción mediante una petición asíncrona Ajax
 * sin recargar la página completa.
 */
public class AuraAjaxButton extends AuraButton {

    private Consumer<AuraAjaxRequestTarget> onAjaxSubmitConsumer;

    public AuraAjaxButton(String id) {
        super(id);
        setOutputMarkupId(true);
    }

    public AuraAjaxButton(String id, String label) {
        super(id, label);
        setOutputMarkupId(true);
    }

    public AuraAjaxButton(String id, Consumer<AuraAjaxRequestTarget> onAjaxSubmitConsumer) {
        super(id);
        this.onAjaxSubmitConsumer = onAjaxSubmitConsumer;
        setOutputMarkupId(true);
    }

    public AuraAjaxButton(String id, IModel<?> model, Consumer<AuraAjaxRequestTarget> onAjaxSubmitConsumer) {
        super(id, model, null);
        this.onAjaxSubmitConsumer = onAjaxSubmitConsumer;
        setOutputMarkupId(true);
    }

    public void onAjaxSubmit(AuraAjaxRequestTarget target) {
        if (onAjaxSubmitConsumer != null) {
            onAjaxSubmitConsumer.accept(target);
        } else {
            super.onSubmit();
        }
    }

    @Override
    public void onSubmit() {
        if (onAjaxSubmitConsumer != null) {
            onAjaxSubmitConsumer.accept(new AuraAjaxRequestTarget(getPage()));
        } else {
            super.onSubmit();
        }
    }

    @Override
    protected void onComponentTag(AuraTag tag) {
        super.onComponentTag(tag);
        tag.setAttribute("type", "button");
        tag.setAttribute("onclick", "auraAjax('" + getPath() + "', 'click', null, this); return false;");
    }
}

