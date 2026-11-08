package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

import java.util.ArrayList;
import java.util.List;

/**
 * Standard composite DashboardLayout in JettraFlux.
 *
 * Implements the recommended accessible layout hierarchy:
 *   DashboardRoot (min-height: 100vh; display: flex; flex-direction: column;)
 *     ├── DashboardHeader (flex-shrink: 0; sticky top-0;)
 *     └── DashboardContentBody (flex-grow: 1; overflow-y: auto; tabindex="0";)
 */
public class DashboardLayout extends Widget {

    private Widget header;
    private Widget body;
    private final List<Widget> items = new ArrayList<>();

    public DashboardLayout() {
        super();
        this.id = "jettra-dashboard-layout-" + System.identityHashCode(this);
    }

    public static DashboardLayout of(Widget header, Widget body) {
        DashboardLayout layout = new DashboardLayout();
        layout.header = header;
        layout.body = body;
        return layout;
    }

    public static DashboardLayout of(Widget... widgets) {
        DashboardLayout layout = new DashboardLayout();
        if (widgets != null) {
            for (Widget w : widgets) {
                if (w != null) layout.items.add(w);
            }
        }
        return layout;
    }

    public DashboardLayout header(Widget header) {
        this.header = header;
        return this;
    }

    public DashboardLayout body(Widget body) {
        this.body = body;
        return this;
    }

    private boolean topToolbarVisible = true;
    private boolean actionButtonsVisible = true;

    public DashboardLayout withTopToolbarVisible(boolean visible) {
        this.topToolbarVisible = visible;
        return this;
    }

    public boolean isTopToolbarVisible() {
        return topToolbarVisible;
    }

    public DashboardLayout withActionButtonsVisible(boolean visible) {
        this.actionButtonsVisible = visible;
        return this;
    }

    public boolean isActionButtonsVisible() {
        return actionButtonsVisible;
    }

    public Widget getHeader() {
        return header;
    }

    public Widget getBody() {
        return body;
    }

    @Override
    public String render(ThemeData theme) {
        DashboardRoot root = DashboardRoot.of();
        if (this.modifier != null) {
            root.modifier(this.modifier);
        }

        if (header != null && topToolbarVisible) {
            if (header instanceof TopToolbar tt) {
                tt.withTopToolbarVisible(topToolbarVisible);
                tt.withActionButtonsVisible(actionButtonsVisible);
            } else if (header instanceof DashboardHeader dh) {
                dh.withTopToolbarVisible(topToolbarVisible);
                dh.withActionButtonsVisible(actionButtonsVisible);
            }
            root.add(header);
        }

        if (body != null) {
            if (body instanceof DashboardContentBody dcb) {
                root.add(dcb);
            } else {
                root.add(DashboardContentBody.of(body));
            }
        }

        for (Widget item : items) {
            if (item != null) {
                root.add(item);
            }
        }

        return root.render(theme);
    }
}
