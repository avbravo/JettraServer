package io.jettra.flux.core;

import io.jettra.flux.theme.ThemeData;

/**
 * Encapsulated composite view component in JettraFlux.
 * Allows assembling complex layouts, dashboards, and views through a fluent lifecycle.
 */
public abstract class FluxView extends Widget {

    public FluxView() {
        super();
    }

    /**
     * Builds and returns the underlying Widget hierarchy of this view.
     */
    public abstract Widget build();

    @Override
    public String render(ThemeData theme) {
        Widget root = build();
        if (root == null) {
            return "";
        }
        if (this.modifier != null) {
            root.modifier(this.modifier);
        }
        return root.render(theme);
    }
}
