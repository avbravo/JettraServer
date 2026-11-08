package io.jettra.flux.widgets;

import java.util.Objects;

/**
 * Immutable sealed state contract for hierarchical tree node expansion in JettraFlux.
 * Built with Java 25 sealed interfaces, records, and pattern matching.
 */
public sealed interface NodeExpansionState permits NodeExpansionState.ExpandedNodeState, NodeExpansionState.CollapsedNodeState {

    /**
     * Node identifier.
     *
     * @return node ID
     */
    String nodeId();

    /**
     * Whether this state represents an expanded node.
     *
     * @return true if expanded, false if collapsed
     */
    boolean isExpanded();

    /**
     * Inverted state for this node.
     *
     * @return toggle state
     */
    default NodeExpansionState toggle() {
        return isExpanded() ? new CollapsedNodeState(nodeId()) : new ExpandedNodeState(nodeId());
    }

    /**
     * Immutable record representing an expanded node state.
     *
     * @param nodeId node identifier
     */
    record ExpandedNodeState(String nodeId) implements NodeExpansionState {
        public ExpandedNodeState {
            Objects.requireNonNull(nodeId, "nodeId cannot be null");
        }

        @Override
        public boolean isExpanded() {
            return true;
        }
    }

    /**
     * Immutable record representing a collapsed node state.
     *
     * @param nodeId node identifier
     */
    record CollapsedNodeState(String nodeId) implements NodeExpansionState {
        public CollapsedNodeState {
            Objects.requireNonNull(nodeId, "nodeId cannot be null");
        }

        @Override
        public boolean isExpanded() {
            return false;
        }
    }

    /**
     * Factory method for creating an ExpandedNodeState.
     *
     * @param nodeId node ID
     * @return expanded state record
     */
    static NodeExpansionState expanded(String nodeId) {
        return new ExpandedNodeState(nodeId);
    }

    /**
     * Factory method for creating a CollapsedNodeState.
     *
     * @param nodeId node ID
     * @return collapsed state record
     */
    static NodeExpansionState collapsed(String nodeId) {
        return new CollapsedNodeState(nodeId);
    }

    /**
     * Pattern matching transformation to determine state description.
     *
     * @param state node expansion state
     * @return text descriptor
     */
    static String describe(NodeExpansionState state) {
        return switch (state) {
            case ExpandedNodeState(String id) -> "EXPANDED:" + id;
            case CollapsedNodeState(String id) -> "COLLAPSED:" + id;
        };
    }
}
