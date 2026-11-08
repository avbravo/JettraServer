package io.jettra.flux.widgets;

import io.jettra.flux.core.FluxEscapers;
import io.jettra.flux.core.Modifier;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Gatherers;

/**
 * Native First-Class Tree Widget for JettraFlux.
 * Implements:
 * - Composite Pattern (arbitrary nesting of FluxTreeNode<T>)
 * - State and Observer Pattern (reactive expansion, collapse, and listeners)
 * - Visitor Pattern traversal (mass updates, metrics, search)
 * - Accessible WAI-ARIA Treeview semantics (role="tree", role="treeitem", role="group", aria-expanded)
 * - Global deterministic expandAll() and collapseAll() operations in memory and interface.
 *
 * @param <T> data payload type
 */
public class FluxTree<T> extends Widget {

    private final String treeId;
    private final List<FluxTreeNode<T>> rootNodes = new ArrayList<>();
    private final List<FluxTreeStateObserver<T>> observers = new ArrayList<>();
    private boolean defaultExpanded = false;
    private String ariaLabel = "Hierarchical Storage Tree";
    private boolean showLines = true;
    private String onNodeSelectJs;

    public FluxTree() {
        this("fluxtree_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8));
    }

    public FluxTree(String treeId) {
        this.treeId = treeId != null ? treeId : ("fluxtree_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8));
        this.id(this.treeId);
    }

    public static <T> FluxTree<T> of() {
        return new FluxTree<>();
    }

    public static <T> FluxTree<T> of(String treeId) {
        return new FluxTree<>(treeId);
    }

    @SafeVarargs
    public static <T> FluxTree<T> of(FluxTreeNode<T>... roots) {
        FluxTree<T> tree = new FluxTree<>();
        if (roots != null) {
            for (FluxTreeNode<T> root : roots) {
                tree.root(root);
            }
        }
        return tree;
    }

    public String getTreeId() {
        return treeId;
    }

    public List<FluxTreeNode<T>> getRootNodes() {
        return Collections.unmodifiableList(rootNodes);
    }

    public FluxTree<T> root(FluxTreeNode<T> rootNode) {
        if (rootNode != null) {
            this.rootNodes.add(rootNode);
        }
        return this;
    }

    public FluxTree<T> roots(List<FluxTreeNode<T>> roots) {
        if (roots != null) {
            this.rootNodes.addAll(roots);
        }
        return this;
    }

    public boolean isDefaultExpanded() {
        return defaultExpanded;
    }

    public FluxTree<T> defaultExpanded(boolean defaultExpanded) {
        this.defaultExpanded = defaultExpanded;
        if (defaultExpanded) {
            expandAll();
        } else {
            collapseAll();
        }
        return this;
    }

    public String getAriaLabel() {
        return ariaLabel;
    }

    public FluxTree<T> ariaLabel(String ariaLabel) {
        this.ariaLabel = ariaLabel;
        return this;
    }

    public boolean isShowLines() {
        return showLines;
    }

    public FluxTree<T> showLines(boolean showLines) {
        this.showLines = showLines;
        return this;
    }

    public FluxTree<T> onNodeSelect(String jsHandler) {
        this.onNodeSelectJs = jsHandler;
        return this;
    }

    // --- State / Observer Pattern ---

    public FluxTree<T> addObserver(FluxTreeStateObserver<T> observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
        return this;
    }

    public FluxTree<T> removeObserver(FluxTreeStateObserver<T> observer) {
        if (observer != null) {
            observers.remove(observer);
        }
        return this;
    }

    /**
     * Recursively expands all nodes in the tree in memory and notifies observers.
     *
     * @return this tree for fluent chaining
     */
    public FluxTree<T> expandAll() {
        for (FluxTreeNode<T> root : rootNodes) {
            root.expandAll();
        }
        for (FluxTreeStateObserver<T> observer : observers) {
            try {
                observer.onTreeExpandedAll();
            } catch (Exception ignored) {}
        }
        return this;
    }

    /**
     * Recursively collapses all nodes in the tree in memory and notifies observers.
     *
     * @return this tree for fluent chaining
     */
    public FluxTree<T> collapseAll() {
        for (FluxTreeNode<T> root : rootNodes) {
            root.collapseAll();
        }
        for (FluxTreeStateObserver<T> observer : observers) {
            try {
                observer.onTreeCollapsedAll();
            } catch (Exception ignored) {}
        }
        return this;
    }

    /**
     * Recursively sets expansion state for all nodes in the tree.
     *
     * @param expanded true to expand all, false to collapse all
     * @return this tree for fluent chaining
     */
    public FluxTree<T> setAllExpanded(boolean expanded) {
        return expanded ? expandAll() : collapseAll();
    }

    /**
     * Collapses all nested/secondary levels in the tree while preserving root nodes expanded.
     *
     * @return this tree for fluent chaining
     */
    public FluxTree<T> collapseToRoot() {
        for (FluxTreeNode<T> root : rootNodes) {
            root.collapseChildren(true);
        }
        for (FluxTreeStateObserver<T> observer : observers) {
            try {
                observer.onTreeCollapsedAll();
            } catch (Exception ignored) {}
        }
        return this;
    }

    /**
     * Collapses tree children, optionally preserving root node expansion.
     *
     * @param preserveRoots whether to keep root nodes expanded
     * @return this tree for fluent chaining
     */
    public FluxTree<T> collapseChildren(boolean preserveRoots) {
        return preserveRoots ? collapseToRoot() : collapseAll();
    }

    /**
     * Recursively collapses all child subtrees in memory while preserving the root level nodes.
     *
     * @return this tree for fluent chaining
     */
    public FluxTree<T> collapseAllSubtrees() {
        for (FluxTreeNode<T> root : rootNodes) {
            root.collapseSubtrees();
        }
        for (FluxTreeStateObserver<T> observer : observers) {
            try {
                observer.onTreeCollapsedAll();
            } catch (Exception ignored) {}
        }
        return this;
    }

    /**
     * Recursively collapses all node details panels in the tree while preserving
     * visible node rows (condensing record payload information).
     *
     * @return this tree for fluent chaining
     */
    public FluxTree<T> collapseAllDetails() {
        for (FluxTreeNode<T> root : rootNodes) {
            root.collapseAllDetails();
        }
        return this;
    }

    /**
     * Propagates recursive collapse across the entire hierarchical tree and its subtrees.
     *
     * @param preserveRoots whether to keep root database nodes expanded
     * @return this tree for fluent chaining
     */
    public FluxTree<T> collapseHierarchical(boolean preserveRoots) {
        if (preserveRoots) {
            collapseAllSubtrees();
            collapseAllDetails();
        } else {
            collapseAll();
        }
        return this;
    }

    /**
     * Applies an immutable expansion state record to matching nodes in the tree.
     *
     * @param state node expansion state
     * @return this tree for fluent chaining
     */
    public FluxTree<T> applyExpansionState(NodeExpansionState state) {
        if (state != null) {
            FluxTreeNode<T> node = findNode(state.nodeId());
            if (node != null) {
                node.applyExpansionState(state);
            }
        }
        return this;
    }

    /**
     * Applies a collection of immutable expansion states to the tree.
     *
     * @param states collection of node expansion states
     * @return this tree for fluent chaining
     */
    public FluxTree<T> applyExpansionStates(Collection<NodeExpansionState> states) {
        if (states == null || states.isEmpty()) return this;
        Map<String, NodeExpansionState> map = new HashMap<>();
        for (NodeExpansionState s : states) {
            if (s != null) map.put(s.nodeId(), s);
        }
        accept(node -> {
            NodeExpansionState s = map.get(node.getId());
            if (s != null) {
                node.applyExpansionState(s);
            }
        });
        return this;
    }

    /**
     * Collects immutable expansion state records for all nodes in the tree.
     *
     * @return list of expansion states
     */
    public List<NodeExpansionState> collectExpansionStates() {
        List<NodeExpansionState> list = new ArrayList<>();
        for (FluxTreeNode<T> root : rootNodes) {
            list.addAll(root.collectExpansionStates());
        }
        return Collections.unmodifiableList(list);
    }

    /**
     * Immutable record representing aggregated tree expansion metrics.
     */
    public record TreeExpansionSummary(int totalNodes, int expandedNodes, int collapsedNodes) {}

    /**
     * Calculates tree expansion summary using Java 25 Stream Gatherers fold operation
     * and Pattern Matching over sealed NodeExpansionState records.
     *
     * @return calculated expansion summary
     */
    public TreeExpansionSummary summarizeExpansion() {
        return flatten().stream()
            .map(FluxTreeNode::getExpansionState)
            .gather(Gatherers.fold(
                () -> new TreeExpansionSummary(0, 0, 0),
                (acc, state) -> switch (state) {
                    case NodeExpansionState.ExpandedNodeState e ->
                        new TreeExpansionSummary(acc.totalNodes() + 1, acc.expandedNodes() + 1, acc.collapsedNodes());
                    case NodeExpansionState.CollapsedNodeState c ->
                        new TreeExpansionSummary(acc.totalNodes() + 1, acc.expandedNodes(), acc.collapsedNodes() + 1);
                }
            ))
            .findFirst()
            .orElse(new TreeExpansionSummary(0, 0, 0));
    }

    // --- Button Factory Methods (Pure JettraFlux API) ---

    public Button createExpandAllButton() {
        return expandAllButton(this.treeId);
    }

    public Button createExpandAllButton(String label, String icon) {
        return expandAllButton(this.treeId, label, icon);
    }

    public Button createCollapseAllButton() {
        return collapseAllButton(this.treeId);
    }

    public Button createCollapseAllButton(String label, String icon) {
        return collapseAllButton(this.treeId, label, icon);
    }

    public Button createCollapseToRootButton() {
        return collapseToRootButton(this.treeId);
    }

    public Button createCollapseToRootButton(String label, String icon) {
        return collapseToRootButton(this.treeId, label, icon);
    }

    public static Button expandAllButton(String treeId) {
        return expandAllButton(treeId, "Expand All", "fas fa-expand-alt");
    }

    public static Button expandAllButton(String treeId, String label, String icon) {
        Button btn = Button.of(Icon.of(icon != null ? icon : "fas fa-expand-alt"), Text.of(" " + label));
        btn.modifier(new Modifier()
            .attribute("type", "button")
            .attribute("title", label)
            .attribute("onclick", "if (window.expandAllTreeNodes) { expandAllTreeNodes(); } else if (window.FluxTree) { window.FluxTree.expandAll('" + FluxEscapers.escapeJs(treeId) + "'); }")
            .cssClass("btn-action btn-secondary")
            .style("padding:3px 6px; font-size:9px; margin-right:3px; background:var(--j-primary-light,rgba(56,189,248,0.15)); border-color:var(--j-primary,#38bdf8); color:var(--j-primary,#38bdf8); font-weight:600; cursor:pointer;"));
        return btn;
    }

    public static Button collapseAllButton(String treeId) {
        return collapseAllButton(treeId, "Collapse All", "fas fa-compress-alt");
    }

    public static Button collapseAllButton(String treeId, String label, String icon) {
        Button btn = Button.of(Icon.of(icon != null ? icon : "fas fa-compress-alt"), Text.of(" " + label));
        btn.modifier(new Modifier()
            .attribute("type", "button")
            .attribute("title", label)
            .attribute("onclick", "if (window.collapseAllTreeNodes) { collapseAllTreeNodes(); } else if (window.FluxTree) { window.FluxTree.collapseAll('" + FluxEscapers.escapeJs(treeId) + "'); }")
            .cssClass("btn-action btn-secondary")
            .style("padding:3px 6px; font-size:9px; margin-right:4px; background:var(--j-bg-subsurface,#1e293b); border-color:var(--j-border,#334155); color:var(--j-text-muted,#94a3b8); font-weight:600; cursor:pointer;"));
        return btn;
    }

    public static Button collapseToRootButton(String treeId) {
        return collapseToRootButton(treeId, "Collapse All", "fas fa-compress-alt");
    }

    public static Button collapseToRootButton(String treeId, String label, String icon) {
        Button btn = Button.of(Icon.of(icon != null ? icon : "fas fa-compress-alt"), Text.of(" " + label));
        btn.modifier(new Modifier()
            .attribute("type", "button")
            .attribute("title", label)
            .attribute("onclick", "if (window.collapseAllTreeNodes) { collapseAllTreeNodes(); } else if (window.FluxTree) { window.FluxTree.collapseToRoot('" + FluxEscapers.escapeJs(treeId) + "'); }")
            .cssClass("btn-action btn-secondary")
            .style("padding:3px 6px; font-size:9px; margin-right:4px; background:var(--j-bg-subsurface,#1e293b); border-color:var(--j-border,#334155); color:var(--j-text-muted,#94a3b8); font-weight:600; cursor:pointer;"));
        return btn;
    }

    /**
     * Traverses the entire tree hierarchy using the Visitor Pattern.
     *
     * @param visitor functional visitor to apply
     */
    public void accept(FluxTreeVisitor<T> visitor) {
        if (visitor != null) {
            for (FluxTreeNode<T> root : rootNodes) {
                root.accept(visitor);
            }
        }
    }

    /**
     * Finds a node by ID anywhere in the tree.
     *
     * @param targetId node ID
     * @return node if found, otherwise null
     */
    public FluxTreeNode<T> findNode(String targetId) {
        for (FluxTreeNode<T> root : rootNodes) {
            FluxTreeNode<T> found = root.findNode(targetId);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    /**
     * Returns a flat list of all nodes in the tree in pre-order traversal.
     */
    public List<FluxTreeNode<T>> flatten() {
        List<FluxTreeNode<T>> flat = new ArrayList<>();
        accept(flat::add);
        return flat;
    }

    public int getTotalNodeCount() {
        return flatten().size();
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();

        sb.append("<div id=\"").append(treeId).append("\" role=\"tree\" aria-label=\"")
          .append(escapeHtml(ariaLabel)).append("\" class=\"jettra-flux-tree\" style=\"display:flex; flex-direction:column; gap:4px; width:100%; user-select:none;\">\n");

        for (FluxTreeNode<T> root : rootNodes) {
            renderNode(sb, root, theme);
        }

        sb.append("</div>\n");

        // Embedded deterministic reactive tree controller
        sb.append("<script>\n")
          .append("  window.FluxTree = window.FluxTree || {};\n")
          .append("  window.FluxTree.toggle = function(nodeId, treeId) {\n")
          .append("    var group = document.getElementById('group_' + nodeId);\n")
          .append("    var node = document.getElementById('node_' + nodeId);\n")
          .append("    var btn = document.getElementById('btn_toggle_' + nodeId);\n")
          .append("    var icon = document.getElementById('icon_' + nodeId);\n")
          .append("    if (!group) {\n")
          .append("      if (document.getElementById('details_' + nodeId)) {\n")
          .append("        window.FluxTree.toggleDetails(nodeId, treeId);\n")
          .append("      }\n")
          .append("      return;\n")
          .append("    }\n")
          .append("    var isExpanded = group.style.display !== 'none';\n")
          .append("    if (isExpanded) {\n")
          .append("      group.style.display = 'none';\n")
          .append("      if (node) node.setAttribute('aria-expanded', 'false');\n")
          .append("      if (btn) btn.setAttribute('aria-expanded', 'false');\n")
          .append("      if (icon) { icon.className = 'fas fa-chevron-right flux-tree-toggle-icon'; }\n")
          .append("    } else {\n")
          .append("      group.style.display = 'block';\n")
          .append("      if (node) node.setAttribute('aria-expanded', 'true');\n")
          .append("      if (btn) btn.setAttribute('aria-expanded', 'true');\n")
          .append("      if (icon) { icon.className = 'fas fa-chevron-down flux-tree-toggle-icon'; }\n")
          .append("    }\n")
          .append("  };\n")
          .append("  window.FluxTree.toggleDetails = function(nodeId, treeId) {\n")
          .append("    var details = document.getElementById('details_' + nodeId);\n")
          .append("    var btn = document.getElementById('btn_toggle_details_' + nodeId);\n")
          .append("    var icon = document.getElementById('icon_details_' + nodeId);\n")
          .append("    if (!details) return;\n")
          .append("    var isExpanded = details.style.display !== 'none';\n")
          .append("    if (isExpanded) {\n")
          .append("      details.style.display = 'none';\n")
          .append("      details.setAttribute('aria-expanded', 'false');\n")
          .append("      if (btn) btn.setAttribute('aria-expanded', 'false');\n")
          .append("      if (icon) { icon.className = 'fas fa-chevron-right flux-tree-toggle-icon'; }\n")
          .append("    } else {\n")
          .append("      details.style.display = 'block';\n")
          .append("      details.setAttribute('aria-expanded', 'true');\n")
          .append("      if (btn) btn.setAttribute('aria-expanded', 'true');\n")
          .append("      if (icon) { icon.className = 'fas fa-chevron-down flux-tree-toggle-icon'; }\n")
          .append("    }\n")
          .append("  };\n")
          .append("  window.FluxTree.expandAll = function(treeId) {\n")
          .append("    var root = treeId ? document.getElementById(treeId) : document;\n")
          .append("    if (!root) root = document;\n")
          .append("    var parent = root.closest ? root.closest('.db-subtree-container, .tree-collapsible-content') : null;\n")
          .append("    if (parent) {\n")
          .append("      parent.style.display = 'block';\n")
          .append("      parent.setAttribute('aria-expanded', 'true');\n")
          .append("      parent.setAttribute('data-state', 'expanded');\n")
          .append("    }\n")
          .append("    var groups = root.querySelectorAll('.flux-tree-group');\n")
          .append("    for (var i = 0; i < groups.length; i++) { groups[i].style.display = 'block'; }\n")
          .append("    var nodes = root.querySelectorAll('.flux-tree-node');\n")
          .append("    for (var j = 0; j < nodes.length; j++) { nodes[j].setAttribute('aria-expanded', 'true'); }\n")
          .append("    var btns = root.querySelectorAll('.flux-tree-toggle-btn');\n")
          .append("    for (var k = 0; k < btns.length; k++) { btns[k].setAttribute('aria-expanded', 'true'); }\n")
          .append("    var icons = root.querySelectorAll('.flux-tree-toggle-icon');\n")
          .append("    for (var l = 0; l < icons.length; l++) { icons[l].className = 'fas fa-chevron-down flux-tree-toggle-icon'; }\n")
          .append("    var detailsPanels = root.querySelectorAll('.flux-tree-details-panel');\n")
          .append("    for (var d = 0; d < detailsPanels.length; d++) { detailsPanels[d].style.display = 'block'; detailsPanels[d].setAttribute('aria-expanded', 'true'); }\n")
          .append("  };\n")
          .append("  window.FluxTree.collapseAll = function(treeId, preserveRoot) {\n")
          .append("    var root = treeId ? document.getElementById(treeId) : document;\n")
          .append("    if (!root) root = document;\n")
          .append("    var groups = root.querySelectorAll('.flux-tree-group');\n")
          .append("    for (var i = 0; i < groups.length; i++) {\n")
          .append("      if (preserveRoot && groups[i].parentElement && groups[i].parentElement.parentElement === root) {\n")
          .append("        groups[i].style.display = 'block';\n")
          .append("        continue;\n")
          .append("      }\n")
          .append("      groups[i].style.display = 'none';\n")
          .append("    }\n")
          .append("    var subtrees = root.querySelectorAll('.tree-collapsible-content, .db-subtree-container, [id^=\"eng_subtree_\"], [id^=\"unit_subtree_\"], [id^=\"item_detail_\"]');\n")
          .append("    for (var s = 0; s < subtrees.length; s++) {\n")
          .append("      if (preserveRoot && subtrees[s].classList.contains('db-subtree-container')) {\n")
          .append("        subtrees[s].style.display = 'block';\n")
          .append("        subtrees[s].setAttribute('aria-expanded', 'true');\n")
          .append("        continue;\n")
          .append("      }\n")
          .append("      subtrees[s].style.display = 'none';\n")
          .append("      subtrees[s].setAttribute('aria-expanded', 'false');\n")
          .append("      if (subtrees[s].hasAttribute('data-state')) subtrees[s].setAttribute('data-state', 'collapsed');\n")
          .append("    }\n")
          .append("    var nodes = root.querySelectorAll('.flux-tree-node, [role=\"treeitem\"]');\n")
          .append("    for (var j = 0; j < nodes.length; j++) {\n")
          .append("      if (preserveRoot && nodes[j].parentElement === root) {\n")
          .append("        nodes[j].setAttribute('aria-expanded', 'true');\n")
          .append("        continue;\n")
          .append("      }\n")
          .append("      nodes[j].setAttribute('aria-expanded', 'false');\n")
          .append("      if (nodes[j].hasAttribute('data-state')) nodes[j].setAttribute('data-state', 'collapsed');\n")
          .append("    }\n")
          .append("    var btns = root.querySelectorAll('.flux-tree-toggle-btn, .flux-tree-details-toggle, [id^=\"btn_toggle_\"]');\n")
          .append("    for (var k = 0; k < btns.length; k++) {\n")
          .append("      if (preserveRoot && btns[k].closest('.flux-tree-node') && btns[k].closest('.flux-tree-node').parentElement === root) {\n")
          .append("        btns[k].setAttribute('aria-expanded', 'true');\n")
          .append("        continue;\n")
          .append("      }\n")
          .append("      btns[k].setAttribute('aria-expanded', 'false');\n")
          .append("      if (btns[k].hasAttribute('data-state')) btns[k].setAttribute('data-state', 'collapsed');\n")
          .append("    }\n")
          .append("    var icons = root.querySelectorAll('.flux-tree-toggle-icon, .tree-toggle-icon');\n")
          .append("    for (var l = 0; l < icons.length; l++) {\n")
          .append("      if (preserveRoot && icons[l].closest('.flux-tree-node') && icons[l].closest('.flux-tree-node').parentElement === root) {\n")
          .append("        icons[l].className = 'fas fa-chevron-down flux-tree-toggle-icon';\n")
          .append("        continue;\n")
          .append("      }\n")
          .append("      if (icons[l].className.indexOf('fa-caret-') >= 0) {\n")
          .append("        icons[l].className = 'fas fa-caret-right tree-toggle-icon';\n")
          .append("      } else {\n")
          .append("        icons[l].className = 'fas fa-chevron-right flux-tree-toggle-icon';\n")
          .append("      }\n")
          .append("    }\n")
          .append("    var detailsPanels = root.querySelectorAll('.flux-tree-details-panel');\n")
          .append("    for (var d = 0; d < detailsPanels.length; d++) {\n")
          .append("      detailsPanels[d].style.display = 'none';\n")
          .append("      detailsPanels[d].setAttribute('aria-expanded', 'false');\n")
          .append("    }\n")
          .append("  };\n")
          .append("  window.FluxTree.collapseToRoot = function(treeId) {\n")
          .append("    window.FluxTree.collapseAll(treeId, true);\n")
          .append("  };\n")
          .append("  window.FluxTree.collapseSubtrees = function(treeId) {\n")
          .append("    window.FluxTree.collapseAll(treeId, true);\n")
          .append("  };\n")
          .append("  window.FluxTree.collapseAllDetails = function(treeId) {\n")
          .append("    var root = treeId ? document.getElementById(treeId) : document;\n")
          .append("    if (!root) root = document;\n")
          .append("    var detailsPanels = root.querySelectorAll('.flux-tree-details-panel, [id^=\"item_detail_\"]');\n")
          .append("    for (var d = 0; d < detailsPanels.length; d++) {\n")
          .append("      detailsPanels[d].style.display = 'none';\n")
          .append("      detailsPanels[d].setAttribute('aria-expanded', 'false');\n")
          .append("    }\n")
          .append("    var detailBtns = root.querySelectorAll('.flux-tree-details-toggle');\n")
          .append("    for (var b = 0; b < detailBtns.length; b++) {\n")
          .append("      detailBtns[b].setAttribute('aria-expanded', 'false');\n")
          .append("    }\n")
          .append("    var detailIcons = root.querySelectorAll('[id^=\"icon_details_\"], [id^=\"icon_item_detail_\"]');\n")
          .append("    for (var i = 0; i < detailIcons.length; i++) {\n")
          .append("      detailIcons[i].className = 'fas fa-chevron-right flux-tree-toggle-icon';\n")
          .append("    }\n")
          .append("  };\n")
          .append("  window.FluxTree.collapseHierarchical = function(treeId, preserveRoot) {\n")
          .append("    window.FluxTree.collapseAll(treeId, preserveRoot);\n")
          .append("  };\n")
          .append("</script>\n");

        return sb.toString();
    }

    private void renderNode(StringBuilder sb, FluxTreeNode<T> node, ThemeData theme) {
        String nodeId = node.getId();
        boolean hasChildren = node.hasChildren();
        boolean hasDetails = node.hasDetails();
        boolean isExpanded = node.isExpanded();
        boolean isDetailsExpanded = node.isDetailsExpanded();
        String groupDisplay = isExpanded ? "block" : "none";
        String chevronIcon = isExpanded ? "fas fa-chevron-down" : "fas fa-chevron-right";
        String detailsChevron = isDetailsExpanded ? "fas fa-chevron-down" : "fas fa-chevron-right";

        sb.append("<div id=\"node_").append(nodeId).append("\" role=\"treeitem\" aria-expanded=\"")
          .append(isExpanded).append("\" class=\"flux-tree-node\" style=\"display:flex; flex-direction:column;\">\n");

        // Node Row Header
        sb.append("  <div class=\"flux-tree-node-row\" style=\"display:flex; align-items:center; justify-content:space-between; padding:3px 6px; border-radius:5px; transition:background 0.15s ease; gap:6px;\">\n");

        // Left section: Toggle Chevron + Icon + Label
        sb.append("    <div style=\"display:inline-flex; align-items:center; gap:5px; min-width:0; flex:1; cursor:pointer;\" ");
        if (hasChildren) {
            sb.append("onclick=\"FluxTree.toggle('").append(nodeId).append("', '").append(treeId).append("')\" ");
        } else if (hasDetails) {
            sb.append("onclick=\"FluxTree.toggleDetails('").append(nodeId).append("', '").append(treeId).append("')\" ");
        } else if (onNodeSelectJs != null) {
            sb.append("onclick=\"").append(onNodeSelectJs.replace("{id}", nodeId)).append("\" ");
        }
        sb.append(">\n");

        if (hasChildren) {
            sb.append("      <button id=\"btn_toggle_").append(nodeId).append("\" type=\"button\" class=\"flux-tree-toggle-btn\" ")
              .append("aria-expanded=\"").append(isExpanded).append("\" aria-controls=\"group_").append(nodeId).append("\" ")
              .append("onclick=\"event.stopPropagation(); FluxTree.toggle('").append(nodeId).append("', '").append(treeId).append("')\" ")
              .append("style=\"background:none; border:none; padding:2px 4px; cursor:pointer; display:inline-flex; align-items:center; justify-content:center; color:var(--j-primary,#38bdf8); font-size:10px;\">")
              .append("<i id=\"icon_").append(nodeId).append("\" class=\"").append(chevronIcon).append(" flux-tree-toggle-icon\"></i>")
              .append("</button>\n");
        } else if (hasDetails) {
            sb.append("      <button id=\"btn_toggle_details_").append(nodeId).append("\" type=\"button\" class=\"flux-tree-toggle-btn flux-tree-details-toggle\" ")
              .append("aria-expanded=\"").append(isDetailsExpanded).append("\" aria-controls=\"details_").append(nodeId).append("\" ")
              .append("onclick=\"event.stopPropagation(); FluxTree.toggleDetails('").append(nodeId).append("', '").append(treeId).append("')\" ")
              .append("style=\"background:none; border:none; padding:2px 4px; cursor:pointer; display:inline-flex; align-items:center; justify-content:center; color:var(--j-primary,#38bdf8); font-size:10px;\">")
              .append("<i id=\"icon_details_").append(nodeId).append("\" class=\"").append(detailsChevron).append(" flux-tree-toggle-icon\"></i>")
              .append("</button>\n");
        } else {
            // Leaf indentation spacer
            sb.append("      <span style=\"display:inline-block; width:16px;\"></span>\n");
        }

        // Icon
        if (node.getIcon() != null && !node.getIcon().isBlank()) {
            sb.append("      <i class=\"").append(node.getIcon()).append("\" style=\"color:").append(node.getIconColor())
              .append("; font-size:11px; flex-shrink:0;\"></i>\n");
        }

        // Label
        sb.append("      <span class=\"flux-tree-label\" style=\"font-size:11.5px; font-weight:600; color:var(--j-text-primary,#f8fafc); overflow:hidden; text-overflow:ellipsis; white-space:nowrap;\">")
          .append(escapeHtml(node.getLabel()))
          .append("</span>\n");

        // Badge if present
        if (node.getBadge() != null && !node.getBadge().isBlank()) {
            sb.append("      <span class=\"").append(node.getBadgeClass())
              .append("\" style=\"font-size:8.5px; padding:1px 5px; border-radius:3px; font-weight:700; flex-shrink:0;\">")
              .append(escapeHtml(node.getBadge()))
              .append("</span>\n");
        }

        // If node has BOTH children AND details, also show a small Info badge button to toggle details
        if (hasChildren && hasDetails) {
            sb.append("      <button id=\"btn_toggle_details_").append(nodeId).append("\" type=\"button\" class=\"flux-tree-details-toggle\" ")
              .append("aria-expanded=\"").append(isDetailsExpanded).append("\" aria-controls=\"details_").append(nodeId).append("\" ")
              .append("onclick=\"event.stopPropagation(); FluxTree.toggleDetails('").append(nodeId).append("', '").append(treeId).append("')\" ")
              .append("style=\"background:rgba(56,189,248,0.1); border:1px solid rgba(56,189,248,0.25); color:var(--j-primary,#38bdf8); font-size:8.5px; border-radius:3px; padding:1px 5px; cursor:pointer; display:inline-flex; align-items:center; gap:2px;\">")
              .append("<i class=\"fas fa-info-circle\"></i> Details</button>\n");
        }

        sb.append("    </div>\n");

        // Right section: Actions
        if (!node.getActions().isEmpty()) {
            sb.append("    <div class=\"flux-tree-actions\" style=\"display:inline-flex; align-items:center; gap:4px; flex-shrink:0;\" onclick=\"event.stopPropagation();\">\n");
            for (Widget action : node.getActions()) {
                sb.append(action.render(theme));
            }
            sb.append("    </div>\n");
        }

        sb.append("  </div>\n");

        // Collapsible Node Details Panel
        if (hasDetails) {
            String detailsDisplay = isDetailsExpanded ? "block" : "none";
            sb.append("  <div id=\"details_").append(nodeId).append("\" role=\"region\" class=\"flux-tree-details-panel\" ")
              .append("aria-expanded=\"").append(isDetailsExpanded).append("\" ")
              .append("style=\"display:").append(detailsDisplay).append("; margin-left:22px; margin-top:3px; margin-bottom:4px;\">\n");
            sb.append(node.getDetails().render(theme)).append("\n");
            sb.append("  </div>\n");
        }

        // Subtree Child Group Container
        if (hasChildren) {
            String borderStyle = showLines ? "border-left: 2px dashed rgba(56,189,248,0.25);" : "";
            sb.append("  <div id=\"group_").append(nodeId).append("\" role=\"group\" class=\"flux-tree-group\" ")
              .append("style=\"display:").append(groupDisplay).append("; margin-left:12px; ").append(borderStyle).append(" padding-left:8px; margin-top:2px;\">\n");

            for (FluxTreeNode<T> child : node.getChildren()) {
                renderNode(sb, child, theme);
            }

            sb.append("  </div>\n");
        }

        sb.append("</div>\n");
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
