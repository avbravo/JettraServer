package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.Arrays;
import java.util.List;

public class Alert extends Widget {
    private final List<Widget> children;

    private String severity = "info";

    private Alert(List<Widget> children) {
        this.children = children;
    }

    public static Alert of(Widget... children) {
        return new Alert(Arrays.asList(children));
    }

    public Alert severity(String severity) {
        this.severity = severity;
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        String cssClass = "espresso-alert espresso-alert-" + severity;
        sb.append("<div ").append(renderCommonAttributes(theme, cssClass)).append(">\n");
        for (Widget child : children) {
            sb.append(child.render(theme));
        }
        sb.append("</div>\n");
        return sb.toString();
    }
}
