package io.jettra.aura.components;

import io.jettra.aura.core.AuraContainer;
import io.jettra.aura.core.AuraTag;
import io.jettra.aura.model.IModel;

/**
 * Contenedor estilo tarjeta moderna con soporte para bordes, sombras y cabecera.
 */
public class AuraCard extends AuraContainer {

    private String headerTitle;

    public AuraCard(String id) {
        super(id);
    }

    public AuraCard(String id, String headerTitle) {
        super(id);
        this.headerTitle = headerTitle;
    }

    public AuraCard(String id, IModel<?> model) {
        super(id, model);
    }

    public String getHeaderTitle() {
        return headerTitle;
    }

    public AuraCard setHeaderTitle(String headerTitle) {
        this.headerTitle = headerTitle;
        return this;
    }

    @Override
    protected void onComponentTag(AuraTag tag) {
        super.onComponentTag(tag);
        tag.appendAttribute("class", "aura-card", " ");
        tag.appendAttribute("style", "background: var(--bg-card, #ffffff); border: 1px solid var(--border-subtle, #e2e8f0); border-radius: 12px; padding: 1.5rem; box-shadow: 0 1px 3px rgba(0,0,0,0.06);", ";");
    }

    @Override
    protected void onComponentTagBody(AuraTag tag, String markupBody, StringBuilder output) {
        if (headerTitle != null && !headerTitle.isBlank()) {
            output.append("<div style=\"font-size: 1.15rem; font-weight: 700; margin-bottom: 1rem; color: var(--text-main, #0f172a); border-bottom: 1px solid var(--border-subtle, #e2e8f0); padding-bottom: 0.5rem;\">")
                  .append(escapeHtml(headerTitle))
                  .append("</div>\n");
        }
        if (markupBody != null) {
            output.append(markupBody);
        }
    }
}

