package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.Arrays;
import java.util.List;

public class Button extends Widget {
    private final List<Widget> children;

    private Button(List<Widget> children) {
        this.children = children;
    }

    public static Button of(Widget... children) {
        return new Button(Arrays.asList(children));
    }

    public static Button of(String label) {
        return new Button(Arrays.asList(Text.of(label)));
    }

    @Override
    public String render(ThemeData theme) {
        String type = "button";
        if (modifier.getAttributes().containsKey("type")) {
            type = modifier.getAttributes().remove("type");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<button type=\"").append(type).append("\" ").append(renderCommonAttributes(theme, "espresso-button btn btn-primary", theme.buttonStyle)).append(">\n");
        for (Widget child : children) {
            sb.append(child.render(theme));
        }
        sb.append("</button>\n");
        return sb.toString();
    }
}
