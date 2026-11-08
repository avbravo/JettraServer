package io.jettra.flux.widgets;

import io.jettra.flux.core.Modifier;
import io.jettra.flux.theme.Themes;
import io.jettra.test.annotation.DisplayName;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.Test;

import static io.jettra.test.core.JettraAssert.*;

/**
 * Unit test suite for JettraTreeNode, JettraCollapsible, and FluxTreeNode details integration.
 */
@NotRequiresRunningServer
public class JettraTreeCollapsibleTest {

    @Test
    @DisplayName("Test JettraCollapsible: declaration, state mutation, and accessible HTML rendering")
    public void testJettraCollapsibleDeclarationAndState() {
        Div content = Div.of(Span.of("Technical Payload Data").modifier(new Modifier().style("color:#10b981;")));
        JettraCollapsible collapsible = JettraCollapsible.of("Record Technical Details", content)
                .expanded(false)
                .badge("v1", "store-badge");

        assertFalse(collapsible.isExpanded());
        assertEquals("Record Technical Details", collapsible.getHeaderTitle());
        assertNotNull(collapsible.getDetails());
        assertTrue(collapsible.hasDetails());

        // Test toggle state
        collapsible.toggle();
        assertTrue(collapsible.isExpanded());

        // Test Render HTML
        String html = collapsible.render(Themes.FlatTheme());
        assertNotNull(html);
        assertTrue(html.contains("jettra-collapsible"));
        assertTrue(html.contains("Record Technical Details"));
        assertTrue(html.contains("Technical Payload Data"));
        assertTrue(html.contains("JettraCollapsible.toggle"));
        assertTrue(html.contains("role=\"region\""));
        assertTrue(html.contains("aria-expanded=\"true\""));
    }

    @Test
    @DisplayName("Test JettraTreeNode and FluxTreeNode: fluent withDetails and detailsExpanded builders")
    public void testJettraTreeNodeWithDetails() {
        Div detailWidget = Div.of(
                Span.of("Engine: DOCUMENT | Memory: 256 B"),
                Span.of("Address: doc:main_db:users:usr_99")
        );

        JettraTreeNode<String> treeNode = JettraTreeNode.<String>of("node_usr_99", "usr_99", "DOCUMENT_ITEM")
                .icon("fas fa-file-code")
                .badge("v2")
                .withDetails(detailWidget)
                .detailsExpanded(false)
                .expanded(true);

        assertTrue(treeNode.hasDetails());
        assertNotNull(treeNode.getDetails());
        assertFalse(treeNode.isDetailsExpanded());
        assertTrue(treeNode.isExpanded());

        // Toggle details
        treeNode.toggleDetails();
        assertTrue(treeNode.isDetailsExpanded());

        treeNode.toggleDetails();
        assertFalse(treeNode.isDetailsExpanded());
    }

    @Test
    @DisplayName("Test FluxTree HTML rendering with node details and toggle trigger button")
    public void testFluxTreeRenderingWithNodeDetails() {
        Div itemDetails = Div.of(
                Span.of("Raw Size: 180 Bytes | Schema Validated"),
                Span.of("Embedding: [0.12, -0.45, 0.88]")
        );

        FluxTreeNode<String> root = FluxTreeNode.of("root_db", "ecommerce_db");
        FluxTreeNode<String> branch = FluxTreeNode.of("branch_vector", "vectors");
        JettraTreeNode<String> item = JettraTreeNode.<String>of("item_emb_1", "emb_001")
                .icon("fas fa-project-diagram")
                .withDetails(itemDetails)
                .detailsExpanded(false);

        branch.child(item);
        root.child(branch);

        FluxTree<String> tree = FluxTree.of(root);
        tree.expandAll();

        String html = tree.render(Themes.FlatTheme());

        // Verify leaf node toggle for details
        assertTrue(html.contains("btn_toggle_details_item_emb_1"), "Leaf node with details must have details toggle button");
        assertTrue(html.contains("details_item_emb_1"), "Leaf node must have details panel container");
        assertTrue(html.contains("flux-tree-details-panel"), "Details container must have class flux-tree-details-panel");
        assertTrue(html.contains("FluxTree.toggleDetails"), "Tree must embed FluxTree.toggleDetails controller");
        assertTrue(html.contains("Raw Size: 180 Bytes"), "Details content must be rendered");
        assertTrue(html.contains("Embedding: [0.12, -0.45, 0.88]"), "Vector embedding details must be rendered");
    }

    @Test
    @DisplayName("Test FluxTree with branch node having both children and details")
    public void testFluxTreeBranchWithBothChildrenAndDetails() {
        Div branchDetails = Div.of(Span.of("Partition: 3 Shards | Replicas: 2"));
        FluxTreeNode<String> branch = FluxTreeNode.<String>of("unit_orders", "orders (1 item)")
                .withDetails(branchDetails)
                .child(FluxTreeNode.of("item_1", "order_1", "ORDER"));

        FluxTree<String> tree = FluxTree.of(branch);
        String html = tree.render(Themes.FlatTheme());

        assertTrue(html.contains("btn_toggle_unit_orders"), "Branch must have group toggle button");
        assertTrue(html.contains("btn_toggle_details_unit_orders"), "Branch with details must have details toggle button");
        assertTrue(html.contains("details_unit_orders"), "Branch must render details panel container");
        assertTrue(html.contains("Partition: 3 Shards"), "Branch details content must be rendered");
    }
}
