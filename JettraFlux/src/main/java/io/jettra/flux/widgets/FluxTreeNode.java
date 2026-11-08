package io.jettra.flux.widgets;

import io.jettra.flux.core.JettraComponent;
import io.jettra.flux.core.Widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Composite Tree Node component for JettraFlux.
 * Encapsulates:
 * - Generic payload data (T)
 * - Arbitrary hierarchical nesting (Composite Pattern)
 * - State management (expanded, selected)
 * - Recursive operations (expandAll, collapseAll)
 * - Visitor Pattern traversal (accept)
 * - Visual metadata (icons, semantic badges, action buttons)
 *
 * @param <T> data payload type
 */
public class FluxTreeNode<T> {

    private final String id;
    private T data;
    private String label;
    private String icon = "fas fa-folder";
    private String iconColor = "var(--j-primary,#38bdf8)";
    private String badge;
    private String badgeClass = "store-badge";
    private boolean expanded = false;
    private boolean selected = false;
    private String type = "DEFAULT";
    private FluxTreeNode<T> parent;
    private final List<FluxTreeNode<T>> children = new ArrayList<>();
    private final List<Widget> actions = new ArrayList<>();
    private JettraComponent details;
    private boolean detailsExpanded = false;

    public FluxTreeNode(String id, String label, T data) {
        this.id = Objects.requireNonNull(id, "Node ID cannot be null");
        this.label = label != null ? label : id;
        this.data = data;
    }

    public static <T> FluxTreeNode<T> of(String id, String label) {
        return new FluxTreeNode<>(id, label, null);
    }

    public static <T> FluxTreeNode<T> of(String id, String label, T data) {
        return new FluxTreeNode<>(id, label, data);
    }

    public String getId() {
        return id;
    }

    public T getData() {
        return data;
    }

    public FluxTreeNode<T> data(T data) {
        this.data = data;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public FluxTreeNode<T> label(String label) {
        this.label = label;
        return this;
    }

    public String getIcon() {
        return icon;
    }

    public FluxTreeNode<T> icon(String icon) {
        this.icon = icon;
        return this;
    }

    public String getIconColor() {
        return iconColor;
    }

    public FluxTreeNode<T> iconColor(String iconColor) {
        this.iconColor = iconColor;
        return this;
    }

    public String getBadge() {
        return badge;
    }

    public FluxTreeNode<T> badge(String badge) {
        this.badge = badge;
        return this;
    }

    public FluxTreeNode<T> badge(String badge, String badgeClass) {
        this.badge = badge;
        this.badgeClass = badgeClass;
        return this;
    }

    public String getBadgeClass() {
        return badgeClass;
    }

    public FluxTreeNode<T> badgeClass(String badgeClass) {
        this.badgeClass = badgeClass;
        return this;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public FluxTreeNode<T> expanded(boolean expanded) {
        this.expanded = expanded;
        return this;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public boolean isSelected() {
        return selected;
    }

    public FluxTreeNode<T> selected(boolean selected) {
        this.selected = selected;
        return this;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getType() {
        return type;
    }

    public FluxTreeNode<T> type(String type) {
        this.type = type;
        return this;
    }

    public FluxTreeNode<T> getParent() {
        return parent;
    }

    public void setParent(FluxTreeNode<T> parent) {
        this.parent = parent;
    }

    public List<FluxTreeNode<T>> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public int getChildCount() {
        return children.size();
    }

    public FluxTreeNode<T> child(FluxTreeNode<T> child) {
        if (child != null) {
            child.setParent(this);
            this.children.add(child);
        }
        return this;
    }

    public FluxTreeNode<T> children(List<FluxTreeNode<T>> newChildren) {
        if (newChildren != null) {
            for (FluxTreeNode<T> ch : newChildren) {
                child(ch);
            }
        }
        return this;
    }

    @SafeVarargs
    public final FluxTreeNode<T> children(FluxTreeNode<T>... newChildren) {
        if (newChildren != null) {
            for (FluxTreeNode<T> ch : newChildren) {
                child(ch);
            }
        }
        return this;
    }

    public List<Widget> getActions() {
        return Collections.unmodifiableList(actions);
    }

    public FluxTreeNode<T> action(Widget actionWidget) {
        if (actionWidget != null) {
            this.actions.add(actionWidget);
        }
        return this;
    }

    public FluxTreeNode<T> actions(List<Widget> actionWidgets) {
        if (actionWidgets != null) {
            this.actions.addAll(actionWidgets);
        }
        return this;
    }

    public JettraComponent getDetails() {
        return details;
    }

    public boolean hasDetails() {
        return details != null;
    }

    public FluxTreeNode<T> withDetails(JettraComponent details) {
        this.details = details;
        return this;
    }

    public FluxTreeNode<T> details(JettraComponent details) {
        return withDetails(details);
    }

    public boolean isDetailsExpanded() {
        return detailsExpanded;
    }

    public FluxTreeNode<T> detailsExpanded(boolean detailsExpanded) {
        this.detailsExpanded = detailsExpanded;
        return this;
    }

    public void setDetailsExpanded(boolean detailsExpanded) {
        this.detailsExpanded = detailsExpanded;
    }

    public void toggleDetails() {
        this.detailsExpanded = !this.detailsExpanded;
    }

    // --- Recursive State Operations ---

    public void expand() {
        this.expanded = true;
    }

    public void collapse() {
        this.expanded = false;
    }

    public void toggle() {
        this.expanded = !this.expanded;
    }

    /**
     * Recursively expands this node and all of its descendants.
     *
     * @return this node for fluent chaining
     */
    public FluxTreeNode<T> expandAll() {
        this.expanded = true;
        for (FluxTreeNode<T> child : children) {
            child.expandAll();
        }
        return this;
    }

    /**
     * Recursively collapses this node and all of its descendants,
     * including attached record details panels.
     *
     * @return this node for fluent chaining
     */
    public FluxTreeNode<T> collapseAll() {
        this.expanded = false;
        this.detailsExpanded = false;
        for (FluxTreeNode<T> child : children) {
            child.collapseAll();
        }
        return this;
    }

    /**
     * Recursively collapses all detail panels across this node and its descendants,
     * condensing record information while maintaining the parent hierarchy structure.
     *
     * @return this node for fluent chaining
     */
    public FluxTreeNode<T> collapseAllDetails() {
        this.detailsExpanded = false;
        for (FluxTreeNode<T> child : children) {
            child.collapseAllDetails();
        }
        return this;
    }

    /**
     * Recursively collapses all child subtrees while leaving this node's expansion intact.
     *
     * @return this node for fluent chaining
     */
    public FluxTreeNode<T> collapseSubtrees() {
        for (FluxTreeNode<T> child : children) {
            child.collapseAll();
        }
        return this;
    }

    /**
     * Recursively sets expansion state for this node and all of its descendants.
     *
     * @param expanded true to expand all, false to collapse all
     * @return this node for fluent chaining
     */
    public FluxTreeNode<T> setAllExpanded(boolean expanded) {
        return expanded ? expandAll() : collapseAll();
    }

    /**
     * Recursively collapses all child descendants while preserving this node's expansion state.
     *
     * @param preserveThis whether to keep this node expanded
     * @return this node for fluent chaining
     */
    public FluxTreeNode<T> collapseChildren(boolean preserveThis) {
        if (preserveThis) {
            this.expanded = true;
        } else {
            this.expanded = false;
            this.detailsExpanded = false;
        }
        for (FluxTreeNode<T> child : children) {
            child.collapseAll();
        }
        return this;
    }

    /**
     * Captures the current expansion state of this node as an immutable Record.
     *
     * @return NodeExpansionState record
     */
    public NodeExpansionState getExpansionState() {
        return expanded ? new NodeExpansionState.ExpandedNodeState(id) : new NodeExpansionState.CollapsedNodeState(id);
    }

    /**
     * Applies an immutable expansion state record to this node using Java 25 Pattern Matching.
     *
     * @param state expansion state record
     */
    public void applyExpansionState(NodeExpansionState state) {
        if (state == null) return;
        switch (state) {
            case NodeExpansionState.ExpandedNodeState(String targetId) -> {
                if (Objects.equals(this.id, targetId)) {
                    this.expanded = true;
                }
            }
            case NodeExpansionState.CollapsedNodeState(String targetId) -> {
                if (Objects.equals(this.id, targetId)) {
                    this.expanded = false;
                }
            }
        }
    }

    /**
     * Collects immutable expansion state records for this node and all descendants.
     *
     * @return list of expansion state records
     */
    public List<NodeExpansionState> collectExpansionStates() {
        List<NodeExpansionState> states = new ArrayList<>();
        collectExpansionStates(states);
        return Collections.unmodifiableList(states);
    }

    private void collectExpansionStates(List<NodeExpansionState> acc) {
        acc.add(getExpansionState());
        for (FluxTreeNode<T> child : children) {
            child.collectExpansionStates(acc);
        }
    }

    /**
     * Traverses this node and its descendants using the Visitor Pattern.
     *
     * @param visitor functional visitor to apply
     */
    public void accept(FluxTreeVisitor<T> visitor) {
        if (visitor != null) {
            visitor.visit(this);
            for (FluxTreeNode<T> child : children) {
                child.accept(visitor);
            }
        }
    }

    /**
     * Searches for a node with the given ID within this subtree.
     *
     * @param targetId ID to search for
     * @return found node or null
     */
    public FluxTreeNode<T> findNode(String targetId) {
        if (Objects.equals(this.id, targetId)) {
            return this;
        }
        for (FluxTreeNode<T> child : children) {
            FluxTreeNode<T> found = child.findNode(targetId);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    /**
     * Returns a flattened pre-order list of this node and all its descendants.
     *
     * @return flat list of nodes
     */
    public List<FluxTreeNode<T>> flatten() {
        List<FluxTreeNode<T>> flat = new ArrayList<>();
        accept(flat::add);
        return flat;
    }

    /**
     * Returns the depth level of this node from the root (root = 0).
     */
    public int getDepth() {
        int depth = 0;
        FluxTreeNode<T> curr = this.parent;
        while (curr != null) {
            depth++;
            curr = curr.getParent();
        }
        return depth;
    }

    @Override
    public String toString() {
        return "FluxTreeNode{" +
            "id='" + id + '\'' +
            ", label='" + label + '\'' +
            ", type='" + type + '\'' +
            ", expanded=" + expanded +
            ", childrenCount=" + children.size() +
            '}';
    }
}
