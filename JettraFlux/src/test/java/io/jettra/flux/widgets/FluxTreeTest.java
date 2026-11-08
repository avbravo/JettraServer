package io.jettra.flux.widgets;

import io.jettra.flux.theme.Themes;
import io.jettra.test.annotation.DisplayName;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static io.jettra.test.core.JettraAssert.*;

/**
 * Unit test suite for FluxTree, FluxTreeNode, FluxTreeVisitor, and FluxTreeStateObserver.
 */
@NotRequiresRunningServer
public class FluxTreeTest {

    @Test
    @DisplayName("Test Composite Pattern: multi-level hierarchy construction, depth, and traversal")
    public void testTreeCompositeConstruction() {
        FluxTreeNode<String> rootDb = FluxTreeNode.of("db_ecommerce", "ecommerce_db", "DATABASE")
            .icon("fas fa-database")
            .badge("ACTIVE", "badge-active");

        FluxTreeNode<String> collOrders = FluxTreeNode.of("coll_orders", "orders (2 items)", "COLLECTION")
            .icon("fas fa-folder");

        FluxTreeNode<String> itemOrd1 = FluxTreeNode.of("item_ord_1", "ord_001", "ITEM")
            .icon("fas fa-file-code")
            .badge("v1");

        FluxTreeNode<String> itemOrd2 = FluxTreeNode.of("item_ord_2", "ord_002", "ITEM")
            .icon("fas fa-file-code")
            .badge("v2");

        collOrders.child(itemOrd1).child(itemOrd2);
        rootDb.child(collOrders);

        FluxTree<String> tree = FluxTree.of(rootDb);

        assertEquals(1, tree.getRootNodes().size());
        assertEquals(4, tree.getTotalNodeCount());

        assertEquals(0, rootDb.getDepth());
        assertEquals(1, collOrders.getDepth());
        assertEquals(2, itemOrd1.getDepth());
        assertEquals(2, itemOrd2.getDepth());

        assertFalse(rootDb.isLeaf());
        assertFalse(collOrders.isLeaf());
        assertTrue(itemOrd1.isLeaf());
        assertTrue(itemOrd2.isLeaf());

        assertNotNull(tree.findNode("item_ord_2"));
        assertEquals("ord_002", tree.findNode("item_ord_2").getLabel());
    }

    @Test
    @DisplayName("Test State Management: expandAll and collapseAll recursive propagation")
    public void testExpandAllAndCollapseAllOperations() {
        FluxTreeNode<String> root = FluxTreeNode.of("root", "Root");
        FluxTreeNode<String> branch1 = FluxTreeNode.of("b1", "Branch 1");
        FluxTreeNode<String> leaf1 = FluxTreeNode.of("l1", "Leaf 1");
        FluxTreeNode<String> leaf2 = FluxTreeNode.of("l2", "Leaf 2");

        branch1.child(leaf1).child(leaf2);
        root.child(branch1);

        FluxTree<String> tree = FluxTree.of(root);

        // Initially collapsed
        assertFalse(root.isExpanded());
        assertFalse(branch1.isExpanded());

        // Global expandAll
        tree.expandAll();
        assertTrue(root.isExpanded(), "Root must be expanded after expandAll");
        assertTrue(branch1.isExpanded(), "Branch must be expanded after expandAll");
        assertTrue(leaf1.isExpanded(), "Leaf must be expanded after expandAll");

        // Global collapseAll
        tree.collapseAll();
        assertFalse(root.isExpanded(), "Root must be collapsed after collapseAll");
        assertFalse(branch1.isExpanded(), "Branch must be collapsed after collapseAll");
        assertFalse(leaf1.isExpanded(), "Leaf must be collapsed after collapseAll");
    }

    @Test
    @DisplayName("Test Visitor Pattern: traversal and mass node transformations")
    public void testVisitorPattern() {
        FluxTreeNode<Integer> root = FluxTreeNode.of("r", "Root", 10);
        FluxTreeNode<Integer> c1 = FluxTreeNode.of("c1", "Child 1", 20);
        FluxTreeNode<Integer> c2 = FluxTreeNode.of("c2", "Child 2", 30);

        root.child(c1).child(c2);
        FluxTree<Integer> tree = FluxTree.of(root);

        AtomicInteger sum = new AtomicInteger(0);
        tree.accept(node -> {
            if (node.getData() != null) {
                sum.addAndGet(node.getData());
            }
        });

        assertEquals(60, sum.get(), "Visitor must visit all nodes and accumulate sum");

        // Use visitor to expand all nodes
        tree.accept(FluxTreeVisitor.expandAll());
        assertTrue(root.isExpanded());
        assertTrue(c1.isExpanded());
        assertTrue(c2.isExpanded());
    }

    @Test
    @DisplayName("Test Observer Pattern: reactive tree state listeners")
    public void testObserverPattern() {
        FluxTree<String> tree = FluxTree.of(FluxTreeNode.of("n1", "Node 1"));
        AtomicBoolean expandedNotified = new AtomicBoolean(false);
        AtomicBoolean collapsedNotified = new AtomicBoolean(false);

        tree.addObserver(new FluxTreeStateObserver<>() {
            @Override
            public void onTreeExpandedAll() {
                expandedNotified.set(true);
            }

            @Override
            public void onTreeCollapsedAll() {
                collapsedNotified.set(true);
            }
        });

        tree.expandAll();
        assertTrue(expandedNotified.get(), "Observer must be notified on expandAll");

        tree.collapseAll();
        assertTrue(collapsedNotified.get(), "Observer must be notified on collapseAll");
    }

    @Test
    @DisplayName("Test Accessible WAI-ARIA DOM rendering and client controller functions")
    public void testRenderWaiAriaAndAccessibleAttributes() {
        FluxTreeNode<String> root = FluxTreeNode.of("db_main", "Main DB", "db")
            .icon("fas fa-database")
            .badge("ACTIVE", "store-badge");

        FluxTreeNode<String> child = FluxTreeNode.of("coll_users", "users", "coll")
            .icon("fas fa-users");

        root.child(child);
        root.expand(); // expanded root

        FluxTree<String> tree = FluxTree.of("test_tree_01");
        tree.root(root);

        String html = tree.render(Themes.FlatTheme());

        assertNotNull(html);
        assertTrue(html.contains("role=\"tree\""), "Tree must declare role=tree");
        assertTrue(html.contains("role=\"treeitem\""), "Node must declare role=treeitem");
        assertTrue(html.contains("role=\"group\""), "Subtree group must declare role=group");
        assertTrue(html.contains("aria-expanded=\"true\""), "Expanded node must declare aria-expanded=true");
        assertTrue(html.contains("Main DB"), "Must render label Main DB");
        assertTrue(html.contains("fas fa-database"), "Must render database icon");
        assertTrue(html.contains("fas fa-chevron-down"), "Expanded node must display chevron-down");
        assertTrue(html.contains("window.FluxTree.toggle"), "Must include FluxTree.toggle client controller");
        assertTrue(html.contains("window.FluxTree.expandAll"), "Must include FluxTree.expandAll client controller");
        assertTrue(html.contains("window.FluxTree.collapseAll"), "Must include FluxTree.collapseAll client controller");
        assertTrue(html.contains("window.FluxTree.collapseToRoot"), "Must include FluxTree.collapseToRoot client controller");
    }

    @Test
    @DisplayName("Test collapseToRoot: preserve root level while collapsing all child descendants")
    public void testCollapseToRootPreservingRootNodes() {
        FluxTreeNode<String> root = FluxTreeNode.of("r1", "Root");
        FluxTreeNode<String> child1 = FluxTreeNode.of("c1", "Child 1");
        FluxTreeNode<String> grandChild = FluxTreeNode.of("gc1", "GrandChild 1");

        child1.child(grandChild);
        root.child(child1);

        FluxTree<String> tree = FluxTree.of(root);
        tree.expandAll();

        assertTrue(root.isExpanded());
        assertTrue(child1.isExpanded());
        assertTrue(grandChild.isExpanded());

        // Collapse to root
        tree.collapseToRoot();

        assertTrue(root.isExpanded(), "Root node must remain expanded after collapseToRoot");
        assertFalse(child1.isExpanded(), "Child 1 must be collapsed");
        assertFalse(grandChild.isExpanded(), "Grandchild must be collapsed");
    }

    @Test
    @DisplayName("Java 25: Test NodeExpansionState sealed records and Pattern Matching dispatch")
    public void testNodeExpansionStatePatternMatchingAndRecords() {
        NodeExpansionState expandedState = NodeExpansionState.expanded("node_orders");
        NodeExpansionState collapsedState = NodeExpansionState.collapsed("node_items");

        assertTrue(expandedState.isExpanded());
        assertFalse(collapsedState.isExpanded());

        assertEquals("EXPANDED:node_orders", NodeExpansionState.describe(expandedState));
        assertEquals("COLLAPSED:node_items", NodeExpansionState.describe(collapsedState));

        FluxTreeNode<String> node = FluxTreeNode.of("node_orders", "Orders");
        assertFalse(node.isExpanded());

        node.applyExpansionState(expandedState);
        assertTrue(node.isExpanded(), "Pattern matching must apply expanded state");

        node.applyExpansionState(collapsedState); // mismatching id, shouldn't change
        assertTrue(node.isExpanded());

        node.applyExpansionState(NodeExpansionState.collapsed("node_orders"));
        assertFalse(node.isExpanded(), "Pattern matching must apply collapsed state");
    }

    @Test
    @DisplayName("Java 25: Test Stream Gatherers fold aggregation for TreeExpansionSummary")
    public void testStreamGathererExpansionSummary() {
        FluxTreeNode<String> root = FluxTreeNode.of("r", "Root");
        FluxTreeNode<String> c1 = FluxTreeNode.of("c1", "Child 1");
        FluxTreeNode<String> c2 = FluxTreeNode.of("c2", "Child 2");

        root.child(c1).child(c2);
        FluxTree<String> tree = FluxTree.of(root);

        root.expand();
        c1.expand();
        c2.collapse();

        FluxTree.TreeExpansionSummary summary = tree.summarizeExpansion();

        assertEquals(3, summary.totalNodes(), "Must summarize 3 total nodes");
        assertEquals(2, summary.expandedNodes(), "Must summarize 2 expanded nodes");
        assertEquals(1, summary.collapsedNodes(), "Must summarize 1 collapsed node");
    }

    @Test
    @DisplayName("Test Button Factory: Pure JettraFlux API generation for Expand All and Collapse All")
    public void testButtonFactoryGeneration() {
        FluxTree<String> tree = FluxTree.of("test_tree");
        io.jettra.flux.core.Widget expandBtn = tree.createExpandAllButton();
        io.jettra.flux.core.Widget collapseBtn = tree.createCollapseToRootButton();

        String expandHtml = expandBtn.render(Themes.FlatTheme());
        String collapseHtml = collapseBtn.render(Themes.FlatTheme());

        assertNotNull(expandHtml);
        assertNotNull(collapseHtml);

        assertTrue(expandHtml.contains("FluxTree.expandAll('test_tree')"), "Expand button must call FluxTree.expandAll");
        assertTrue(collapseHtml.contains("FluxTree.collapseToRoot('test_tree')"), "Collapse button must call FluxTree.collapseToRoot");
    }

    @Test
    @DisplayName("Test Hierarchical and Subtree Collapse: collapseAllSubtrees, collapseAllDetails, and collapseHierarchical")
    public void testHierarchicalCollapseAndDetailsCondensing() {
        FluxTreeNode<String> root = FluxTreeNode.of("db_root", "database");
        FluxTreeNode<String> engine = FluxTreeNode.of("eng_doc", "DOCUMENT");
        FluxTreeNode<String> unit = FluxTreeNode.of("unit_users", "users");
        FluxTreeNode<String> item = FluxTreeNode.of("item_u1", "u1");

        item.details(Div.of(Text.of("Payload JSON content")));
        item.detailsExpanded(true);

        unit.child(item);
        engine.child(unit);
        root.child(engine);

        FluxTree<String> tree = FluxTree.of(root);

        // Expand all in memory
        tree.expandAll();
        item.detailsExpanded(true);

        assertTrue(root.isExpanded());
        assertTrue(engine.isExpanded());
        assertTrue(unit.isExpanded());
        assertTrue(item.isExpanded());
        assertTrue(item.isDetailsExpanded());

        // Test collapseAllDetails: condenses record info while leaving nodes visible
        tree.collapseAllDetails();
        assertTrue(root.isExpanded());
        assertTrue(engine.isExpanded());
        assertTrue(unit.isExpanded());
        assertTrue(item.isExpanded());
        assertFalse(item.isDetailsExpanded(), "Details panel must be condensed/collapsed");

        // Test collapseHierarchical preserving roots: leaves root open, collapses child subtrees and details
        item.detailsExpanded(true);
        tree.collapseHierarchical(true);
        assertTrue(root.isExpanded(), "Root must remain expanded when preserving roots");
        assertFalse(engine.isExpanded(), "Child engine subtree must be collapsed");
        assertFalse(unit.isExpanded(), "Child unit subtree must be collapsed");
        assertFalse(item.isDetailsExpanded(), "Item details must be collapsed");

        // Test global collapseHierarchical without preserving roots
        tree.expandAll();
        tree.collapseHierarchical(false);
        assertFalse(root.isExpanded(), "Root must be collapsed when preserveRoots is false");
        assertFalse(engine.isExpanded());
        assertFalse(unit.isExpanded());
        assertFalse(item.isDetailsExpanded());
    }
}

