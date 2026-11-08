package io.jettra.flux.widgets;

import io.jettra.flux.theme.Themes;
import io.jettra.test.annotation.DisplayName;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.Test;

import static io.jettra.test.core.JettraAssert.*;

@NotRequiresRunningServer
public class SelectFilterTest {

    @Test
    @DisplayName("SelectFilter: Verify builder options, records, and selected value initialization")
    void testSelectFilterConstruction() {
        SelectFilter selectFilter = SelectFilter.of("userSelect", "target_user")
            .placeholder("Search existing users...")
            .emptyMessage("No matching users")
            .required(true)
            .addOption("alice", "Alice Johnson", "alice@enterprise.io", "DB_ADMIN")
            .addOption("bob", "Bob Smith", "bob@enterprise.io", "READ_WRITE", true);

        assertEquals("userSelect", selectFilter.getId());
        assertEquals(2, selectFilter.options().size());
        assertEquals("bob", selectFilter.selectedValue());

        SelectFilter.FilterOption opt1 = selectFilter.options().get(0);
        assertEquals("alice", opt1.value());
        assertEquals("Alice Johnson", opt1.label());
        assertEquals("alice@enterprise.io", opt1.description());
        assertEquals("DB_ADMIN", opt1.badge());
        assertFalse(opt1.selected());

        SelectFilter.FilterOption opt2 = selectFilter.options().get(1);
        assertEquals("bob", opt2.value());
        assertEquals("Bob Smith", opt2.label());
        assertTrue(opt2.selected());
    }

    @Test
    @DisplayName("SelectFilter: Verify HTML rendering contains search input, hidden form field, and interactive option list")
    void testSelectFilterRendering() {
        SelectFilter selectFilter = SelectFilter.of("userFilter", "assigned_user")
            .placeholder("Filter by username...")
            .required(true)
            .addOption("dev_user", "dev_user", "dev@jettra.io", "READ_ONLY")
            .onSelect("console.log('Selected: ' + '{value}')");

        String html = selectFilter.render(Themes.Dark());

        assertTrue(html.contains("id=\"userFilter\""));
        assertTrue(html.contains("id=\"userFilter_value\""));
        assertTrue(html.contains("name=\"assigned_user\""));
        assertTrue(html.contains("required=\"required\""));
        assertTrue(html.contains("id=\"userFilter_search\""));
        assertTrue(html.contains("placeholder=\"Filter by username...\""));
        assertTrue(html.contains("id=\"userFilter_options\""));
        assertTrue(html.contains("data-value=\"dev_user\""));
        assertTrue(html.contains("dev@jettra.io"));
        assertTrue(html.contains("READ_ONLY"));
        assertTrue(html.contains("console.log('Selected: '"));
    }
}
