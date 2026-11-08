package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.Arrays;
import java.util.List;

public class Modal extends Widget {
    private final List<Widget> children;
    private boolean isOpen = false;

    private Modal(List<Widget> children) {
        this.children = children;
    }

    public static Modal of(Widget... children) {
        return new Modal(Arrays.asList(children));
    }
    
    public Modal open(boolean open) {
        this.isOpen = open;
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        String display = isOpen ? "flex" : "none";
        String overlayStyle = "display: " + display + "; position: fixed; z-index: 1000; left: 0; top: 0; width: 100%; height: 100%; overflow: auto; background-color: rgba(0,0,0,0.4); justify-content: center; align-items: center;";
        
        sb.append("<div ").append(renderCommonAttributes(theme, "espresso-modal-overlay"))
          .append(" style=\"").append(overlayStyle).append("\">\n");
        
        sb.append("  <div class=\"espresso-modal-content\" style=\"background-color: var(--surface-color); padding: 30px; border: 1px solid rgba(128,128,128,0.2); border-radius: 12px; box-shadow: 0 10px 25px rgba(0,0,0,0.5); ").append(modifier.getStyles()).append("\">\n");
        for (Widget child : children) {
            sb.append(child.render(theme));
        }
        sb.append("  </div>\n");
        sb.append("</div>\n");
        return sb.toString();
    }
}
