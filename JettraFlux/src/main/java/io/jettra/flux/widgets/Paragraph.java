package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.Arrays;
import java.util.List;

public class Paragraph extends Widget {
    private final List<Widget> children;

    private Paragraph(List<Widget> children) {
        this.children = children;
    }

    public static Paragraph of(Widget... children) {
        return new Paragraph(Arrays.asList(children));
    }
    
    public static Paragraph of(String text) {
        return new Paragraph(Arrays.asList(Text.of(text)));
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        sb.append("<p ").append(renderCommonAttributes(theme, "espresso-paragraph")).append(">\n");
        for (Widget child : children) {
            sb.append(child.render(theme));
        }
        sb.append("</p>\n");
        return sb.toString();
    }
}
