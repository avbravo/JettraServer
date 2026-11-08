package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;

/**
 * Root container for JettraFlux dashboards.
 * Enforces the recommended structural layout:
 *   DashboardRoot (min-height: 100vh; display: flex; flex-direction: column;)
 *     ├── DashboardHeader (flex-shrink: 0; sticky top-0;)
 *     └── DashboardContentBody (flex-grow: 1; overflow-y: auto; tabindex="0";)
 */
public class DashboardRoot extends DashboardRootContainer {

    public DashboardRoot() {
        super();
    }

    public static DashboardRoot of(Widget... widgets) {
        DashboardRoot root = new DashboardRoot();
        if (widgets != null) {
            for (Widget w : widgets) {
                if (w != null) root.add(w);
            }
        }
        return root;
    }
}
