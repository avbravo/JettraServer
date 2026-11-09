package io.jettra.aura.components;

import io.jettra.aura.core.AuraContainer;
import io.jettra.aura.core.AuraTag;
import io.jettra.aura.model.IModel;

/**
 * Componente modal interactivo con overlay de desenfoque y animación de apertura/cierre.
 */
public class AuraModal extends AuraContainer {

    private String title = "Diálogo";
    private boolean open = false;

    public AuraModal(String id) {
        super(id);
        setOutputMarkupId(true);
    }

    public AuraModal(String id, String title) {
        super(id);
        this.title = title;
        setOutputMarkupId(true);
    }

    public AuraModal(String id, IModel<?> model) {
        super(id, model);
        setOutputMarkupId(true);
    }

    public String getTitle() {
        return title;
    }

    public AuraModal setTitle(String title) {
        this.title = title;
        return this;
    }

    public boolean isOpen() {
        return open;
    }

    public void show() {
        this.open = true;
    }

    public void hide() {
        this.open = false;
    }

    @Override
    protected void onComponentTag(AuraTag tag) {
        super.onComponentTag(tag);
        tag.appendAttribute("class", "aura-modal-wrapper", " ");
        String display = open ? "display: flex;" : "display: none;";
        tag.appendAttribute("style", "position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); backdrop-filter: blur(4px); z-index: 9999; align-items: center; justify-content: center; padding: 1rem; " + display, ";");
    }

    @Override
    protected void onComponentTagBody(AuraTag tag, String markupBody, StringBuilder output) {
        String mid = getMarkupId();
        output.append("<div class=\"aura-modal-content\" style=\"background: white; border-radius: 12px; width: 100%; max-width: 500px; box-shadow: 0 20px 25px -5px rgba(0,0,0,0.2); overflow: hidden; border: 1px solid #e2e8f0;\">\n");
        output.append("  <div style=\"padding: 1rem 1.25rem; display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid #e2e8f0;\">\n");
        output.append("    <h3 style=\"margin: 0; font-size: 1.15rem; color: #0f172a;\">").append(escapeHtml(title)).append("</h3>\n");
        output.append("    <button type=\"button\" onclick=\"document.getElementById('").append(mid).append("').style.display='none'\" style=\"background: transparent; border: none; font-size: 1.25rem; cursor: pointer; color: #64748b;\">✕</button>\n");
        output.append("  </div>\n");
        output.append("  <div style=\"padding: 1.25rem;\">\n");
        if (markupBody != null) {
            output.append(markupBody);
        }
        output.append("  </div>\n");
        output.append("</div>\n");
    }
}

