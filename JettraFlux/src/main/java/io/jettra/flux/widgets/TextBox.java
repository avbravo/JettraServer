package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.Arrays;
import java.util.List;

public class TextBox extends Widget {
    private final List<Widget> children;

    private TextBox(List<Widget> children) {
        this.children = children;
    }

    public static TextBox of(Widget... children) {
        return new TextBox(Arrays.asList(children));
    }

    public static TextBox of(String text) {
        return new TextBox(Arrays.asList(Text.of(text)));
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div ").append(renderCommonAttributes(theme, "espresso-textbox")).append(">\n");
        for (Widget child : children) {
            sb.append(child.render(theme));
        }
        sb.append("</div>\n");
        return sb.toString();
    }
}
