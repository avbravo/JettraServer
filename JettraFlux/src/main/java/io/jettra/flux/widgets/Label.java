package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.Arrays;
import java.util.List;

public class Label extends Widget {
    private final List<Widget> children;
    private String forId = "";

    private Label(List<Widget> children) {
        this.children = children;
    }

    public static Label of(Widget... children) {
        return new Label(Arrays.asList(children));
    }
    
    public static Label of(String text) {
        return new Label(Arrays.asList(Text.of(text)));
    }

    public Label forId(String forId) {
        this.forId = forId;
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        sb.append("<label ").append(renderCommonAttributes(theme, "espresso-label form-label"));
        if (!forId.isEmpty()) {
            sb.append("for=\"").append(forId).append("\" ");
        }
        sb.append(">\n");
        for (Widget child : children) {
            sb.append(child.render(theme));
        }
        sb.append("</label>\n");
        return sb.toString();
    }
}
