package io.jettra.flux.widgets;

/**
 * Observer interface for reacting to tree state modifications (expansion,
 * collapse, selection, and global operations).
 * Implements the Observer Pattern for reactive tree state synchronization.
 *
 * @param <T> data payload type
 */
public interface FluxTreeStateObserver<T> {

    /**
     * Fired when a single node is expanded or collapsed.
     *
     * @param node the node that toggled
     * @param expanded new expansion state
     */
    default void onNodeToggled(FluxTreeNode<T> node, boolean expanded) {}

    /**
     * Fired when a node is selected.
     *
     * @param node the selected node
     */
    default void onNodeSelected(FluxTreeNode<T> node) {}

    /**
     * Fired when all nodes in the tree are expanded.
     */
    default void onTreeExpandedAll() {}

    /**
     * Fired when all nodes in the tree are collapsed.
     */
    default void onTreeCollapsedAll() {}
}
