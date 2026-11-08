package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

public class SidebarLogo extends Widget {
    private final String iconClass;
    private final String text;

    private SidebarLogo(String iconClass, String text) {
        this.iconClass = iconClass;
        this.text = text;
    }

    public static SidebarLogo of(String iconClass, String text) {
        return new SidebarLogo(iconClass, text);
    }

    @Override
    public String render(ThemeData theme) {
        return "<div class='sidebar-logo'><i class='" + iconClass + "'></i> " + text + "</div>";
    }
}
