package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

public class SidebarCategory extends Widget {
    private final String text;

    private SidebarCategory(String text) {
        this.text = text;
    }

    public static SidebarCategory of(String text) {
        return new SidebarCategory(text);
    }

    @Override
    public String render(ThemeData theme) {
        return "<div class='sidebar-category'>" + text + "</div>";
    }
}
