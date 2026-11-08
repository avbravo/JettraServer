package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

public class ActionIcon extends Widget {
    private final String iconClass;
    private final String jsAction;

    private ActionIcon(String iconClass, String jsAction) {
        this.iconClass = iconClass;
        this.jsAction = jsAction;
    }

    public static ActionIcon of(String iconClass, String jsAction) {
        return new ActionIcon(iconClass, jsAction);
    }

    @Override
    public String render(ThemeData theme) {
        return "<i " + renderCommonAttributes(theme, iconClass) + " onclick=\"" + jsAction + "\"></i>";
    }
}
