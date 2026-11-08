package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.Themes;
import io.jettra.test.annotation.DisplayName;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.Test;

import static io.jettra.test.core.JettraAssert.*;

@NotRequiresRunningServer
public class ViewSwitcherTest {

    @Test
    @DisplayName("ViewSwitcher: Verify fluent construction, view registration, and active view selection")
    void testViewSwitcherConstruction() {
        Widget listView = Div.of(Text.of("List Content"));
        Widget treeView = Div.of(Text.of("Tree Content"));

        ViewSwitcher vs = ViewSwitcher.of("dbWorkspaceSwitcher")
            .addView("list", "List View", "fas fa-th-list", listView, true)
            .addView("tree", "Tree View", "fas fa-project-diagram", treeView, false);

        assertEquals("dbWorkspaceSwitcher", vs.getSwitcherId());
        assertEquals("list", vs.getActiveViewId());
        assertEquals(2, vs.getViews().size());

        ViewSwitcher.ViewItem item1 = vs.getViews().get(0);
        assertEquals("list", item1.id());
        assertEquals("List View", item1.label());
        assertEquals("fas fa-th-list", item1.icon());

        ViewSwitcher.ViewItem item2 = vs.getViews().get(1);
        assertEquals("tree", item2.id());
        assertEquals("Tree View", item2.label());
        assertEquals("fas fa-project-diagram", item2.icon());
    }

    @Test
    @DisplayName("ViewSwitcher: Verify Builder pattern assembly and activeView configuration")
    void testViewSwitcherBuilder() {
        Widget v1 = Span.of("V1");
        Widget v2 = Span.of("V2");

        ViewSwitcher vs = ViewSwitcher.builder()
            .id("customSwitcher")
            .ariaLabel("Database Layout Switcher")
            .addView("v1", "Perspective 1", "fas fa-list", v1)
            .addView("v2", "Perspective 2", "fas fa-tree", v2)
            .activeView("v2")
            .build();

        assertEquals("customSwitcher", vs.getSwitcherId());
        assertEquals("v2", vs.getActiveViewId());
        assertEquals(2, vs.getViews().size());
    }

    @Test
    @DisplayName("ViewSwitcher: Verify HTML rendering, ARIA tablist/tab/tabpanel semantics, and display states")
    void testViewSwitcherRendering() {
        Widget listContent = Div.of(Span.of("Database Cards"));
        Widget treeContent = Div.of(Span.of("Hierarchical Tree Nodes"));

        ViewSwitcher vs = ViewSwitcher.of("mainDbSwitcher")
            .addView("list", "List View", "fas fa-th-list", "10", listContent, true)
            .addView("tree", "Tree View", "fas fa-project-diagram", null, treeContent, false);

        String html = vs.render(Themes.Dark());

        // Container
        assertTrue(html.contains("id=\"mainDbSwitcher\""), "Container must render switcher id");
        assertTrue(html.contains("jettra-view-switcher"), "Container must declare view switcher CSS class");

        // Header controls with ARIA
        assertTrue(html.contains("role=\"tablist\""), "Header must declare role tablist");
        assertTrue(html.contains("role=\"tab\""), "Buttons must declare role tab");
        assertTrue(html.contains("id=\"mainDbSwitcher_tab_list\""), "List tab button must have id");
        assertTrue(html.contains("id=\"mainDbSwitcher_tab_tree\""), "Tree tab button must have id");
        assertTrue(html.contains("aria-selected=\"true\""), "Active tab must have aria-selected=true");
        assertTrue(html.contains("aria-selected=\"false\""), "Inactive tab must have aria-selected=false");
        assertTrue(html.contains("fas fa-th-list"), "Must render list icon");
        assertTrue(html.contains("fas fa-project-diagram"), "Must render tree icon");
        assertTrue(html.contains("List View"), "Must render list label");
        assertTrue(html.contains("Tree View"), "Must render tree label");
        assertTrue(html.contains("10"), "Must render badge on list tab");

        // Panels
        assertTrue(html.contains("role=\"tabpanel\""), "Panels must declare role tabpanel");
        assertTrue(html.contains("id=\"mainDbSwitcher_panel_list\""), "List panel must have id");
        assertTrue(html.contains("id=\"mainDbSwitcher_panel_tree\""), "Tree panel must have id");
        assertTrue(html.contains("Database Cards"), "List panel must render child content");
        assertTrue(html.contains("Hierarchical Tree Nodes"), "Tree panel must render child content");
        assertTrue(html.contains("style=\"display:block; width:100%;\""), "Active panel must have display:block");
        assertTrue(html.contains("style=\"display:none; width:100%;\""), "Inactive panel must have display:none");

        // Client switching script
        assertTrue(html.contains("window.ViewSwitcher"), "Must embed idempotent ViewSwitcher controller");
        assertTrue(html.contains("switchView"), "Controller must define switchView function");
    }
}
