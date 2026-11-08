package io.jettra.flux.widgets;

import io.jettra.flux.theme.Themes;
import io.jettra.test.annotation.DisplayName;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.Test;

import java.util.Set;

import static io.jettra.test.core.JettraAssert.*;

@NotRequiresRunningServer
public class UserSelectionTableTest {

    @Test
    @DisplayName("UserSelectionTable: Verify composite construction, items, and initial selected values")
    void testUserSelectionTableConstruction() {
        ToggleSelectionItem u1 = ToggleSelectionItem.of("alice", "Alice Morgan")
            .subtitle("alice@jettra.io")
            .roleBadge("DB_ADMIN")
            .selected(true)
            .assignedDatabases(Set.of("prod_db"));

        ToggleSelectionItem u2 = ToggleSelectionItem.of("bob", "Bob Smith")
            .subtitle("bob@jettra.io")
            .roleBadge("READ_WRITE")
            .selected(false);

        UserSelectionTable table = UserSelectionTable.of("userTable", "assigned_users")
            .searchPlaceholder("Search users...")
            .emptyMessage("No users found")
            .maxHeight("300px")
            .quickActions(true)
            .addItem(u1)
            .addItem(u2);

        assertEquals("userTable", table.getId());
        assertEquals("assigned_users", table.name());
        assertEquals(2, table.items().size());
        assertEquals("alice", table.items().get(0).value());
        assertEquals("bob", table.items().get(1).value());
    }

    @Test
    @DisplayName("UserSelectionTable: Verify HTML rendering contains hidden value input, toolbar, counter, and items")
    void testUserSelectionTableRendering() {
        ToggleSelectionItem u1 = ToggleSelectionItem.of("carlos", "Carlos Valdes")
            .subtitle("carlos@jettra.io")
            .roleBadge("MANAGER")
            .selected(true);

        ToggleSelectionItem u2 = ToggleSelectionItem.of("diana", "Diana Prince")
            .subtitle("diana@jettra.io")
            .roleBadge("READ_ONLY")
            .selected(false);

        UserSelectionTable table = UserSelectionTable.of("myTable", "assigned_users_field")
            .searchPlaceholder("Find system identities...")
            .addItem(u1)
            .addItem(u2);

        String html = table.render(Themes.Dark());

        assertTrue(html.contains("id=\"myTable\""));
        assertTrue(html.contains("class=\"jettra-user-selection-table\""));
        assertTrue(html.contains("id=\"myTable_value\""));
        assertTrue(html.contains("name=\"assigned_users_field\""));
        assertTrue(html.contains("value=\"carlos\""));
        assertTrue(html.contains("id=\"myTable_search\""));
        assertTrue(html.contains("placeholder=\"Find system identities...\""));
        assertTrue(html.contains("id=\"myTable_counter\""));
        assertTrue(html.contains("1 / 2 Authorized"));
        assertTrue(html.contains("Select All"));
        assertTrue(html.contains("Clear All"));
        assertTrue(html.contains("id=\"myTable_items\""));
        assertTrue(html.contains("Carlos Valdes"));
        assertTrue(html.contains("Diana Prince"));
        assertTrue(html.contains("window.UserSelectionTable"));
        assertTrue(html.contains("syncForDatabase"));
    }
}
