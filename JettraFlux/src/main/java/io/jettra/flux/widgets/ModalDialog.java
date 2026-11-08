package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

import java.util.ArrayList;
import java.util.List;

/**
 * ModalDialog component in JettraFlux.
 * Encapsulates modal overlay, backdrop blur, header, body and footer actions.
 */
public class ModalDialog extends Widget {

    private final String dialogId;
    private Widget headerWidget;
    private Widget bodyWidget;
    private Widget footerWidget;
    private boolean open = false;
    private String maxWidth = "580px";
    private String borderColor = "rgba(59, 130, 246, 0.4)";
    private final List<Widget> children = new ArrayList<>();

    private ModalDialog(String dialogId) {
        this.dialogId = dialogId;
        this.id = dialogId;
    }

    public static ModalDialog of(String dialogId) {
        return new ModalDialog(dialogId);
    }

    public ModalDialog header(Widget header) {
        this.headerWidget = header;
        return this;
    }

    public ModalDialog body(Widget body) {
        this.bodyWidget = body;
        return this;
    }

    public ModalDialog footer(Widget footer) {
        this.footerWidget = footer;
        return this;
    }

    public ModalDialog open(boolean open) {
        this.open = open;
        return this;
    }

    public ModalDialog maxWidth(String maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }

    public ModalDialog borderColor(String borderColor) {
        this.borderColor = borderColor;
        return this;
    }

    public ModalDialog child(Widget child) {
        if (child != null) this.children.add(child);
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        String display = open ? "flex" : "none";

        sb.append("<div id=\"").append(dialogId).append("\" ");
        sb.append("class=\"espresso-modal-overlay modal-dialog-container ").append(modifier.getClasses()).append("\" ");
        sb.append("style=\"display:").append(display).append("; position:fixed; top:0; left:0; width:100vw; height:100vh; ")
          .append("background:rgba(0,0,0,0.7); backdrop-filter:blur(6px); z-index:100000; align-items:center; justify-content:center; ")
          .append(modifier.getStyles()).append("\">\n");

        sb.append("  <div class=\"modal-dialog-card\" style=\"width:").append(maxWidth)
          .append("; max-width:94%; max-height:90vh; overflow-y:auto; background:var(--j-bg-surface, #0f172a); ")
          .append("border:1px solid ").append(borderColor)
          .append("; box-shadow:0 20px 50px rgba(0,0,0,0.5); padding:24px; border-radius:12px; position:relative; z-index:100001;\">\n");

        if (headerWidget != null) {
            sb.append("    <div class=\"modal-dialog-header\">");
            sb.append(headerWidget.render(theme));
            sb.append("    </div>\n");
        }

        if (bodyWidget != null) {
            sb.append("    <div class=\"modal-dialog-body\">");
            sb.append(bodyWidget.render(theme));
            sb.append("    </div>\n");
        }

        for (Widget child : children) {
            sb.append(child.render(theme));
        }

        if (footerWidget != null) {
            sb.append("    <div class=\"modal-dialog-footer\" style=\"margin-top:16px;\">");
            sb.append(footerWidget.render(theme));
            sb.append("    </div>\n");
        }

        sb.append("  </div>\n");
        sb.append("</div>\n");
        return sb.toString();
    }
}
