package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.Arrays;
import java.util.List;

public class Form extends Widget {
    private final List<Widget> children;
    private String action = "";
    private String method = "POST";

    private Form(List<Widget> children) {
        this.children = children;
    }

    public static Form of(Widget... children) {
        return new Form(Arrays.asList(children));
    }

    public Form action(String action) {
        this.action = action;
        return this;
    }

    public Form method(String method) {
        this.method = method;
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        sb.append("<form ").append(renderCommonAttributes(theme, "espresso-form"));
        if (!action.isEmpty()) {
            sb.append("action=\"").append(action).append("\" ");
        }
        sb.append("method=\"").append(method).append("\">\n");
        for (Widget child : children) {
            sb.append(child.render(theme));
        }
        sb.append("</form>\n");
        return sb.toString();
    }
}
