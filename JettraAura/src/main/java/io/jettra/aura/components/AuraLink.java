package io.jettra.aura.components;

import io.jettra.aura.core.AuraComponent;
import io.jettra.aura.core.AuraTag;
import io.jettra.aura.model.IModel;

/**
 * Componente de hipervínculo (&lt;a&gt;) con soporte para URL directa o listener de eventos.
 */
public class AuraLink extends AuraComponent {

    private String href;
    private Runnable onClickListener;

    public AuraLink(String id) {
        super(id);
    }

    public AuraLink(String id, String href) {
        super(id);
        this.href = href;
    }

    public AuraLink(String id, Runnable onClickListener) {
        super(id);
        this.onClickListener = onClickListener;
    }

    public AuraLink(String id, IModel<?> model, Runnable onClickListener) {
        super(id, model);
        this.onClickListener = onClickListener;
    }

    public String getHref() {
        if (href != null) return href;
        if (onClickListener != null) {
            return "?aura:target=" + getPath() + "&aura:event=click";
        }
        return "#";
    }

    public AuraLink setHref(String href) {
        this.href = href;
        return this;
    }

    public void onClick() {
        if (onClickListener != null) {
            onClickListener.run();
        }
    }

    @Override
    protected void onComponentTag(AuraTag tag) {
        super.onComponentTag(tag);
        tag.setAttribute("href", getHref());
    }

    @Override
    protected void onComponentTagBody(AuraTag tag, String markupBody, StringBuilder output) {
        Object val = getModelObject();
        if (val != null) {
            output.append(isEscapeModelStrings() ? escapeHtml(val.toString()) : val.toString());
        } else if (markupBody != null) {
            output.append(markupBody);
        }
    }
}

