package io.jettra.flux.widgets;

import io.jettra.flux.core.JettraComponent;
import io.jettra.flux.core.Widget;

import java.util.List;

/**
 * Modern typed Tree Node abstraction for JettraFlux.
 * Extends FluxTreeNode to provide enhanced declarative builder capabilities,
 * including first-class collapsible details (.withDetails) and expansion control (.expanded).
 *
 * @param <T> data payload type
 */
public class JettraTreeNode<T> extends FluxTreeNode<T> {

    public JettraTreeNode(String id, String label, T data) {
        super(id, label, data);
    }

    public static <T> JettraTreeNode<T> of(String id, String label) {
        return new JettraTreeNode<>(id, label, null);
    }

    public static <T> JettraTreeNode<T> of(String id, String label, T data) {
        return new JettraTreeNode<>(id, label, data);
    }

    @Override
    public JettraTreeNode<T> withDetails(JettraComponent details) {
        super.withDetails(details);
        return this;
    }

    @Override
    public JettraTreeNode<T> details(JettraComponent details) {
        super.details(details);
        return this;
    }

    @Override
    public JettraTreeNode<T> expanded(boolean expanded) {
        super.expanded(expanded);
        return this;
    }

    @Override
    public JettraTreeNode<T> detailsExpanded(boolean detailsExpanded) {
        super.detailsExpanded(detailsExpanded);
        return this;
    }

    @Override
    public JettraTreeNode<T> icon(String icon) {
        super.icon(icon);
        return this;
    }

    @Override
    public JettraTreeNode<T> iconColor(String iconColor) {
        super.iconColor(iconColor);
        return this;
    }

    @Override
    public JettraTreeNode<T> badge(String badge) {
        super.badge(badge);
        return this;
    }

    @Override
    public JettraTreeNode<T> badge(String badge, String badgeClass) {
        super.badge(badge, badgeClass);
        return this;
    }

    @Override
    public JettraTreeNode<T> badgeClass(String badgeClass) {
        super.badgeClass(badgeClass);
        return this;
    }

    @Override
    public JettraTreeNode<T> selected(boolean selected) {
        super.selected(selected);
        return this;
    }

    @Override
    public JettraTreeNode<T> type(String type) {
        super.type(type);
        return this;
    }

    @Override
    public JettraTreeNode<T> child(FluxTreeNode<T> child) {
        super.child(child);
        return this;
    }

    @Override
    public JettraTreeNode<T> children(List<FluxTreeNode<T>> newChildren) {
        super.children(newChildren);
        return this;
    }

    @Override
    public JettraTreeNode<T> action(Widget actionWidget) {
        super.action(actionWidget);
        return this;
    }

    @Override
    public JettraTreeNode<T> actions(List<Widget> actionWidgets) {
        super.actions(actionWidgets);
        return this;
    }
}
