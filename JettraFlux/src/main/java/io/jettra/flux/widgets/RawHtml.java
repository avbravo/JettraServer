package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

public class RawHtml extends Widget {
    private final String content;

    private RawHtml(String content) {
        this.content = content;
    }

    public static RawHtml of(String content) {
        return new RawHtml(content);
    }

    @Override
    public String render(ThemeData theme) {
        return content;
    }
}
