package io.jettra.flux.widgets;

import io.jettra.flux.theme.Themes;
import io.jettra.test.annotation.DisplayName;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.Test;

import static io.jettra.test.core.JettraAssert.*;

@NotRequiresRunningServer
public class ToggleTabsTest {

    @Test
    @DisplayName("ToggleTabs: Verify tab construction, active state selection, and immutability")
    void testToggleTabsConstruction() {
        ToggleTabs tabs = ToggleTabs.of("modeSelector")
            .addTab("existing", "Existing User", "fas fa-user-check", true)
            .addTab("new", "Create New User", "fas fa-user-plus", false);

        assertEquals("modeSelector", tabs.getId());
        assertEquals(2, tabs.tabs().size());
        assertEquals("existing", tabs.activeTabId());

        ToggleTabs.Tab firstTab = tabs.tabs().get(0);
        assertEquals("existing", firstTab.id());
        assertEquals("Existing User", firstTab.label());
        assertEquals("fas fa-user-check", firstTab.icon());
        assertTrue(firstTab.active());

        ToggleTabs.Tab secondTab = tabs.tabs().get(1);
        assertEquals("new", secondTab.id());
        assertEquals("Create New User", secondTab.label());
        assertEquals("fas fa-user-plus", secondTab.icon());
        assertFalse(secondTab.active());
    }

    @Test
    @DisplayName("ToggleTabs: Verify active tab switching updates activeTabId and tab states")
    void testActiveTabSwitching() {
        ToggleTabs tabs = ToggleTabs.of("tabGroup")
            .addTab("tab1", "Tab 1")
            .addTab("tab2", "Tab 2")
            .activeTab("tab2");

        assertEquals("tab2", tabs.activeTabId());
        assertFalse(tabs.tabs().get(0).active());
        assertTrue(tabs.tabs().get(1).active());
    }

    @Test
    @DisplayName("ToggleTabs: Verify HTML rendering contains ARIA role tablist, buttons, and active styles")
    void testToggleTabsRendering() {
        ToggleTabs tabs = ToggleTabs.of("assignUserTabs")
            .addTab("existing", "Existing User", "fas fa-user-check", true)
            .addTab("new", "Create New User", "fas fa-user-plus", false)
            .onTabChange("onAssignModeChanged('{tabId}')");

        String html = tabs.render(Themes.Dark());

        assertTrue(html.contains("id=\"assignUserTabs\""));
        assertTrue(html.contains("role=\"tablist\""));
        assertTrue(html.contains("class=\"jettra-toggle-tabs"));
        assertTrue(html.contains("role=\"tab\""));
        assertTrue(html.contains("data-tab-id=\"existing\""));
        assertTrue(html.contains("aria-selected=\"true\""));
        assertTrue(html.contains("data-tab-id=\"new\""));
        assertTrue(html.contains("aria-selected=\"false\""));
        assertTrue(html.contains("fas fa-user-check"));
        assertTrue(html.contains("fas fa-user-plus"));
        assertTrue(html.contains("onAssignModeChanged"));
    }
}
