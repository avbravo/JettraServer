package io.jettra.studio.components;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import io.jettra.studio.core.WebComponent;
import io.jettra.studio.markup.MarkupTag;
import io.jettra.studio.theme.StudioThemeManager;

/**
 * Adapter component that embeds and renders any JettraFlux Widget
 * (e.g. StatCard, VisitorGraphCard, TransactionHistoryCard, Avatar, etc.)
 * directly into JettraStudio HTML templates.
 */
public class FluxWidget extends WebComponent {

    private final Widget widget;

    public FluxWidget(String id, Widget widget) {
        super(id);
        this.widget = widget;
    }

    public static FluxWidget of(String id, Widget widget) {
        return new FluxWidget(id, widget);
    }

    public Widget getWidget() {
        return widget;
    }

    @Override
    protected void onComponentTagBody(MarkupTag tag, StringBuilder buffer) {
        if (widget != null) {
            String themeName = (getPage() != null) ? getPage().getCurrentThemeName() : "games";
            io.jettra.flux.theme.ColorMode mode = (getPage() != null) ? getPage().getCurrentColorMode() : io.jettra.flux.theme.ColorMode.DARK;
            ThemeData theme = StudioThemeManager.getInstance().resolveTheme(themeName, mode);
            buffer.append(widget.render(theme));
        } else {
            super.onComponentTagBody(tag, buffer);
        }
    }
}
