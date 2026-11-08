package io.jettra.studio.components;

import io.jettra.studio.core.MarkupContainer;
import io.jettra.studio.markup.MarkupTag;

/**
 * Modal dialog component for JettraStudio, styled according to the active theme.
 */
public class Modal extends MarkupContainer {

    private String title = "Modal Window";
    private boolean open = false;

    public Modal(String id) {
        super(id);
    }

    public Modal(String id, String title) {
        super(id);
        this.title = title;
    }

    public static Modal of(String id, String title) {
        return new Modal(id, title);
    }

    public Modal title(String title) {
        this.title = title;
        return this;
    }

    public boolean isOpen() {
        return open;
    }

    public Modal setOpen(boolean open) {
        this.open = open;
        return this;
    }

    public Modal open() {
        this.open = true;
        return this;
    }

    public Modal close() {
        this.open = false;
        return this;
    }

    @Override
    public void onComponentTag(MarkupTag tag) {
        super.onComponentTag(tag);
        tag.setAttribute("id", getId());
        addCssClass("games-modal");
        addStyle(open ? "display:block;" : "display:none;");
        addStyle("position:fixed; top:50%; left:50%; transform:translate(-50%, -50%); z-index:1000; min-width:380px; max-width:90vw;");
    }

    @Override
    protected void onComponentTagBody(MarkupTag tag, StringBuilder buffer) {
        // Modal Header
        buffer.append("<div class=\"games-modal-header\">\n");
        buffer.append("  <span class=\"games-modal-title\">").append(escapeHtml(title)).append("</span>\n");
        buffer.append("  <button type=\"button\" class=\"games-modal-close\" onclick=\"document.getElementById('")
              .append(getId()).append("').style.display='none'\">X</button>\n");
        buffer.append("</div>\n");

        // Modal Body
        buffer.append("<div class=\"games-modal-body\" style=\"padding:20px;\">\n");
        super.onComponentTagBody(tag, buffer);
        buffer.append("\n</div>\n");
    }
}
