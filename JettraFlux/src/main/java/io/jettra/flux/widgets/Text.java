package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

public class Text extends Widget {
    private final String text;

    private Text(String text) {
        this.text = text;
    }

    public static Text of(String text) {
        return new Text(text);
    }

    @Override
    public String render(ThemeData theme) {
        return "<span " + renderCommonAttributes(theme, "espresso-text", theme.textStyle) + ">" + text + "</span>";
    }
}
