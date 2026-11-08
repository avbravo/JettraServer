package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

public class TopAvatar extends Widget {
    private final String text;

    private TopAvatar(String text) {
        this.text = text;
    }

    public static TopAvatar of(String text) {
        return new TopAvatar(text);
    }

    @Override
    public String render(ThemeData theme) {
        return "<div class='top-avatar'>" + text + "</div>";
    }
}
