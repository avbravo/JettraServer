package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.Arrays;
import java.util.List;

public class Link extends Widget {
    private final String href;
    private final List<Widget> children;
    private String target = "";

    private Link(String href, List<Widget> children) {
        this.href = href;
        this.children = children;
    }

    public static Link of(String href, Widget... children) {
        return new Link(href, Arrays.asList(children));
    }
    
    public static Link of(String href, String text) {
        return new Link(href, Arrays.asList(Text.of(text)));
    }
    
    public Link target(String target) {
        this.target = target;
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        sb.append("<a href=\"").append(href).append("\" ");
        if (!target.isEmpty()) {
            sb.append("target=\"").append(target).append("\" ");
        }
        sb.append(renderCommonAttributes(theme, "espresso-link")).append(">");
        for (Widget child : children) {
            sb.append(child.render(theme));
        }
        sb.append("</a>");
        return sb.toString();
    }
}
