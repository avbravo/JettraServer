package io.jettra.flux.widgets;

import io.jettra.flux.theme.Themes;
import io.jettra.test.annotation.DisplayName;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.Test;

import java.util.Set;

import static io.jettra.test.core.JettraAssert.*;

@NotRequiresRunningServer
public class ToggleSelectionItemTest {

    @Test
    @DisplayName("ToggleSelectionItem: Verify fluent builder, attributes, and assigned databases")
    void testToggleSelectionItemConstruction() {
        ToggleSelectionItem item = ToggleSelectionItem.of("dev_user", "Developer User")
            .subtitle("dev@jettra.io")
            .roleBadge("DB_ADMIN")
            .statusBadge("Authorized")
            .selected(true)
            .disabled(false)
            .assignedDatabases(Set.of("system_db", "analytics_db"))
            .addAssignedDatabase("metrics_db");

        assertEquals("dev_user", item.value());
        assertEquals("Developer User", item.title());
        assertEquals("dev@jettra.io", item.subtitle());
        assertEquals("DB_ADMIN", item.roleBadge());
        assertEquals("Authorized", item.statusBadge());
        assertTrue(item.isSelected());
        assertFalse(item.isDisabled());
        assertEquals(3, item.assignedDatabases().size());
        assertTrue(item.assignedDatabases().contains("system_db"));
        assertTrue(item.assignedDatabases().contains("analytics_db"));
        assertTrue(item.assignedDatabases().contains("metrics_db"));
    }

    @Test
    @DisplayName("ToggleSelectionItem: Verify HTML rendering contains checkbox, role, status badges, and data attributes")
    void testToggleSelectionItemRendering() {
        ToggleSelectionItem item = ToggleSelectionItem.of("admin_user", "Administrator")
            .id("item_admin")
            .subtitle("admin@jettra.io")
            .roleBadge("CLUSTER_ADMIN")
            .statusBadge("Authorized")
            .selected(true)
            .disabled(true)
            .assignedDatabases(Set.of("system_db", "*"));

        String html = item.render(Themes.Dark());

        assertTrue(html.contains("id=\"item_admin\""));
        assertTrue(html.contains("class=\"jettra-toggle-selection-item selected disabled\""));
        assertTrue(html.contains("data-value=\"admin_user\""));
        assertTrue(html.contains("data-title=\"Administrator\""));
        assertTrue(html.contains("data-assigned-dbs=\"system_db,*\"") || html.contains("data-assigned-dbs=\"*,system_db\""));
        assertTrue(html.contains("checked=\"checked\""));
        assertTrue(html.contains("disabled=\"disabled\""));
        assertTrue(html.contains("Administrator"));
        assertTrue(html.contains("admin@jettra.io"));
        assertTrue(html.contains("CLUSTER_ADMIN"));
        assertTrue(html.contains("Authorized"));
    }
}
