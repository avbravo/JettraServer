package io.jettra.flux.widgets;

/**
 * Functional Visitor interface for traversing, transforming, and mutating
 * FluxTreeNode hierarchies. Implements the Visitor Pattern without violating
 * the Open/Closed Principle.
 *
 * @param <T> data payload type
 */
@FunctionalInterface
public interface FluxTreeVisitor<T> {

    /**
     * Visits the given node during tree traversal.
     *
     * @param node tree node being visited
     */
    void visit(FluxTreeNode<T> node);

    /**
     * Creates a visitor that expands all visited nodes.
     *
     * @param <T> node payload type
     * @return expand all visitor
     */
    static <T> FluxTreeVisitor<T> expandAll() {
        return node -> node.setExpanded(true);
    }

    /**
     * Creates a visitor that collapses all visited nodes.
     *
     * @param <T> node payload type
     * @return collapse all visitor
     */
    static <T> FluxTreeVisitor<T> collapseAll() {
        return node -> node.setExpanded(false);
    }
}
