package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.Arrays;
import java.util.List;

public class Span extends Widget {
    private final List<Widget> children;

    private Span(List<Widget> children) {
        this.children = children;
    }

    public static Span of(Widget... children) {
        return new Span(Arrays.asList(children));
    }
    
    public static Span of(String text) {
        return new Span(Arrays.asList(Text.of(text)));
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        sb.append("<span ").append(renderCommonAttributes(theme, "espresso-span")).append(">");
        for (Widget child : children) {
            sb.append(child.render(theme));
        }
        sb.append("</span>");
        return sb.toString();
    }
}
