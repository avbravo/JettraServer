package io.jettra.aura.components;

import io.jettra.aura.ajax.AuraAjaxRequestTarget;
import io.jettra.aura.core.AuraContainer;
import io.jettra.aura.core.AuraTag;
import io.jettra.aura.model.IModel;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Componente modal interactivo con overlay de desenfoque y animación de apertura/cierre.
 */
public class AuraModal extends AuraContainer {

    private String title = "Diálogo";
    private Supplier<String> titleSupplier;
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

    public AuraModal(String id, Supplier<String> titleSupplier) {
        super(id);
        this.titleSupplier = titleSupplier;
        setOutputMarkupId(true);
    }

    public AuraModal(String id, IModel<?> model) {
        super(id, model);
        setOutputMarkupId(true);
    }

    public String getTitle() {
        if (titleSupplier != null) {
            String dynamicTitle = titleSupplier.get();
            if (dynamicTitle != null) return dynamicTitle;
        }
        return title;
    }

    public AuraModal setTitle(String title) {
        this.title = title;
        return this;
    }

    public AuraModal setTitleSupplier(Supplier<String> titleSupplier) {
        this.titleSupplier = titleSupplier;
        return this;
    }

    public boolean isOpen() {
        return open;
    }

    public void show() {
        this.open = true;
    }

    public void show(AuraAjaxRequestTarget target) {
        this.open = true;
        if (target != null) {
            target.add(this);
        }
    }

    public void hide() {
        this.open = false;
    }

    public void hide(AuraAjaxRequestTarget target) {
        this.open = false;
        if (target != null) {
            target.add(this);
        }
    }

    @Override
    protected void onComponentTag(AuraTag tag) {
        super.onComponentTag(tag);
        tag.appendAttribute("class", "aura-modal-wrapper", " ");
        String display = open ? "display: flex;" : "display: none;";
        tag.appendAttribute("style", "position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(2, 6, 23, 0.72); backdrop-filter: blur(6px); z-index: 9999; align-items: center; justify-content: center; padding: 1rem; " + display, ";");
    }

    @Override
    public void renderContainer(AuraTag tag, Consumer<StringBuilder> bodyRenderer, StringBuilder output) {
        onConfigure();
        if (!isVisible()) {
            return;
        }
        onBeforeRender();
        autoBindAnnotatedComponents();

        AuraTag mutableTag = new AuraTag(tag.getName());
        mutableTag.getAttributes().putAll(tag.getAttributes());
        mutableTag.removeAttribute("aura:id");
        mutableTag.removeAttribute("j:id");
        mutableTag.removeAttribute("data-aura-id");

        onComponentTag(mutableTag);

        String mid = getMarkupId();
        String modalTitle = getTitle();

        output.append(mutableTag.renderOpen());
        output.append("<div class=\"aura-modal-content\" style=\"background: var(--bg-card, #1e293b); color: var(--text-main, #f8fafc); border-radius: 14px; width: 100%; max-width: 540px; box-shadow: 0 25px 50px -12px rgba(0,0,0,0.5); overflow: hidden; border: 1px solid var(--border, #334155);\">\n");
        output.append("  <div class=\"aura-modal-header\" style=\"padding: 1rem 1.35rem; display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid var(--border, #334155); background: rgba(15, 23, 42, 0.45);\">\n");
        output.append("    <h3 style=\"margin: 0; font-size: 1.1rem; font-weight: 700; color: var(--text-main, #f8fafc);\">").append(escapeHtml(modalTitle)).append("</h3>\n");
        output.append("    <button type=\"button\" onclick=\"auraCloseModal('").append(mid).append("', '").append(getPath()).append("')\" style=\"background: transparent; border: none; font-size: 1.15rem; cursor: pointer; color: var(--text-muted, #94a3b8); padding: 0.2rem 0.4rem; border-radius: 6px;\">✕</button>\n");
        output.append("  </div>\n");
        output.append("  <div class=\"aura-modal-body\" style=\"padding: 1.35rem;\">\n");
        if (bodyRenderer != null) {
            bodyRenderer.accept(output);
        }
        output.append("  </div>\n");
        output.append("</div>\n");
        output.append(mutableTag.renderClose());
    }

    @Override
    protected void onComponentTagBody(AuraTag tag, String markupBody, StringBuilder output) {
        String mid = getMarkupId();
        String modalTitle = getTitle();
        output.append("<div class=\"aura-modal-content\" style=\"background: var(--bg-card, #1e293b); color: var(--text-main, #f8fafc); border-radius: 14px; width: 100%; max-width: 540px; box-shadow: 0 25px 50px -12px rgba(0,0,0,0.5); overflow: hidden; border: 1px solid var(--border, #334155);\">\n");
        output.append("  <div class=\"aura-modal-header\" style=\"padding: 1rem 1.35rem; display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid var(--border, #334155); background: rgba(15, 23, 42, 0.45);\">\n");
        output.append("    <h3 style=\"margin: 0; font-size: 1.1rem; font-weight: 700; color: var(--text-main, #f8fafc);\">").append(escapeHtml(modalTitle)).append("</h3>\n");
        output.append("    <button type=\"button\" onclick=\"auraCloseModal('").append(mid).append("', '").append(getPath()).append("')\" style=\"background: transparent; border: none; font-size: 1.15rem; cursor: pointer; color: var(--text-muted, #94a3b8);\">✕</button>\n");
        output.append("  </div>\n");
        output.append("  <div class=\"aura-modal-body\" style=\"padding: 1.35rem;\">\n");
        if (markupBody != null) {
            output.append(markupBody);
        }
        output.append("  </div>\n");
        output.append("</div>\n");
    }
}

