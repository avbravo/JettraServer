package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.Arrays;
import java.util.List;

public class Dialog extends Widget {
    private final List<Widget> children;
    private boolean open = false;

    private Dialog(List<Widget> children) {
        this.children = children;
    }

    public static Dialog of(Widget... children) {
        return new Dialog(Arrays.asList(children));
    }
    
    public Dialog open(boolean open) {
        this.open = open;
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        sb.append("<style>\n");
        sb.append("dialog[open].espresso-dialog { position: fixed; top: 50%; left: 50%; transform: translate(-50%, -50%); margin: 0; z-index: 1000; background: var(--surface-color, #fff); border-radius: 8px; box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04); border: 1px solid rgba(128,128,128,0.2); }\n");
        sb.append("dialog[open].espresso-dialog::backdrop { background: rgba(0, 0, 0, 0.5); }\n");
        // Polyfill for backdrop if showModal is not used (which requires JS)
        if (open) {
            sb.append("body { overflow: hidden; }\n");
            sb.append(".espresso-dialog-backdrop { position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; background: rgba(0, 0, 0, 0.5); z-index: 999; }\n");
        }
        sb.append("</style>\n");
        
        if (open) {
            sb.append("<div class=\"espresso-dialog-backdrop\"></div>\n");
        }

        sb.append("<dialog ").append(renderCommonAttributes(theme, "espresso-dialog"));
        if (open) {
            sb.append(" open");
        }
        sb.append(" style=\"").append(modifier.getStyles()).append("\">\n");
        
        for (Widget child : children) {
            sb.append(child.render(theme));
        }
        sb.append("</dialog>\n");
        return sb.toString();
    }
}
