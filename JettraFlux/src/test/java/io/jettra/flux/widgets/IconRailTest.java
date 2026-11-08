package io.jettra.flux.widgets;

import io.jettra.flux.theme.Themes;
import io.jettra.test.annotation.DisplayName;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.Test;

import static io.jettra.test.core.JettraAssert.*;

@NotRequiresRunningServer
public class IconRailTest {

    @Test
    @DisplayName("IconRail renders top and bottom items with structural classes")
    void testIconRailBasicRender() {
        IconRail rail = IconRail.builder()
            .addTopItem(IconRailItem.of("databases", "/databases", Icon.of("fas fa-database"), "Databases"))
            .addTopItem(IconRailItem.of("engines", "/engines", Icon.of("fas fa-table"), "DATABASE"))
            .addBottomItem(IconRailItem.of("settings", "/engines?tab=settings", Icon.of("fas fa-cog"), "Settings"))
            .build();

        String html = rail.render(Themes.Dark());
        assertTrue(html.contains("jettra-icon-rail"));
        assertTrue(html.contains("rail-top-section"));
        assertTrue(html.contains("rail-bottom-section"));
        assertTrue(html.contains("href=\"/databases\""));
        assertTrue(html.contains("title=\"Databases\""));
        assertTrue(html.contains("title=\"DATABASE\""));
        assertTrue(html.contains("title=\"Settings\""));
    }

    @Test
    @DisplayName("IconRail manages selection state using selectItem (State Pattern)")
    void testIconRailSelectionState() {
        IconRail rail = IconRail.builder()
            .addTopItem(IconRailItem.of("databases", "/databases", Icon.of("fas fa-database"), "Databases"))
            .addTopItem(IconRailItem.of("engines", "/engines", Icon.of("fas fa-table"), "DATABASE"))
            .addTopItem(IconRailItem.of("components", "/components", Icon.of("fas fa-server"), "SERVER"))
            .addTopItem(IconRailItem.of("users", "/users", Icon.of("fas fa-users"), "SECURITY"))
            .addBottomItem(IconRailItem.of("settings", "/engines?tab=settings", Icon.of("fas fa-cog"), "Settings"))
            .build();

        // Initially none active
        for (IconRailItem item : rail.getAllItems()) {
            assertFalse(item.isActive());
        }

        // Select 'databases'
        rail.selectItem("databases");
        assertTrue(rail.getActiveItem().isPresent());
        assertEquals("databases", rail.getActiveItem().get().getKey());
        assertTrue(rail.getAllItems().get(0).isActive());
        assertFalse(rail.getAllItems().get(1).isActive());

        String htmlDb = rail.render(Themes.Dark());
        assertTrue(htmlDb.contains("href=\"/databases\" title=\"Databases\" aria-label=\"Databases\" aria-current=\"page\""));
        assertTrue(htmlDb.contains("rail-item active"));

        // Select 'components' dynamically
        rail.selectItem("components");
        assertEquals("components", rail.getActiveItem().get().getKey());
        assertFalse(rail.getAllItems().get(0).isActive()); // databases now inactive
        assertFalse(rail.getAllItems().get(1).isActive()); // engines inactive
        assertTrue(rail.getAllItems().get(2).isActive());  // components active

        String htmlComp = rail.render(Themes.Dark());
        assertFalse(htmlComp.contains("href=\"/databases\" title=\"Databases\" aria-label=\"Databases\" aria-current=\"page\""));
        assertTrue(htmlComp.contains("href=\"/components\" title=\"SERVER\" aria-label=\"SERVER\" aria-current=\"page\""));

        // Select 'settings' in bottom items
        rail.selectItem("settings");
        assertEquals("settings", rail.getActiveItem().get().getKey());
        assertFalse(rail.getAllItems().get(2).isActive());
        assertTrue(rail.getAllItems().get(4).isActive());

        String htmlSettings = rail.render(Themes.Dark());
        assertTrue(htmlSettings.contains("href=\"/engines?tab=settings\" title=\"Settings\" aria-label=\"Settings\" aria-current=\"page\""));
    }

    @Test
    @DisplayName("IconRail matches and activates item by route path (selectByRoute)")
    void testIconRailRouteMatching() {
        IconRail rail = IconRail.of()
            .addTopItem(IconRailItem.of("databases", "/databases", Icon.of("fas fa-database"), "Databases"))
            .addTopItem(IconRailItem.of("engines", "/engines", Icon.of("fas fa-table"), "DATABASE"))
            .addTopItem(IconRailItem.of("users", "/users", Icon.of("fas fa-users"), "SECURITY"))
            .addBottomItem(IconRailItem.of("settings", "/engines?tab=settings", Icon.of("fas fa-cog"), "Settings"));

        rail.selectByRoute("/databases");
        assertEquals("databases", rail.getActiveItem().map(IconRailItem::getKey).orElse(null));

        rail.selectByRoute("/users");
        assertEquals("users", rail.getActiveItem().map(IconRailItem::getKey).orElse(null));

        rail.selectByRoute("/engines?tab=settings");
        assertEquals("settings", rail.getActiveItem().map(IconRailItem::getKey).orElse(null));

        rail.selectByRoute("/engines?tab=schema&engine=DOCUMENT&target_db=system_db");
        assertEquals("engines", rail.getActiveItem().map(IconRailItem::getKey).orElse(null));
    }
}
