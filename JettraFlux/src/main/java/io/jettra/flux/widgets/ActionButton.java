package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

public class ActionButton extends Widget {
    private final String iconClass;
    private final String text;

    private ActionButton(String iconClass, String text) {
        this.iconClass = iconClass;
        this.text = text;
    }

    public static ActionButton of(String iconClass, String text) {
        return new ActionButton(iconClass, text);
    }

    @Override
    public String render(ThemeData theme) {
        return "<button " + renderCommonAttributes(theme, "top-btn-today") + "><i class='" + iconClass + "'></i> " + text + "</button>";
    }
}
