package io.jettra.flux.widgets;

import io.jettra.flux.theme.Themes;
import io.jettra.test.annotation.DisplayName;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.Test;

import static io.jettra.test.core.JettraAssert.*;

@NotRequiresRunningServer
public class JettraConfirmDialogTest {

    @Test
    @DisplayName("Should render JettraConfirmDialog with warning, target display, and buttons")
    void testConfirmDialogRender() {
        JettraConfirmDialog dialog = JettraConfirmDialog.of("deleteModal")
            .title("Confirm Database Deletion")
            .warningMessage("This will permanently purge database.")
            .targetItemLabel("Database:")
            .targetItemValue("orders_db")
            .confirmText("Confirm Delete")
            .cancelText("Dismiss")
            .formAction("/databases")
            .actionName("drop_db")
            .targetParamName("target_db");

        String html = dialog.render(Themes.FlatTheme());

        assertNotNull(html);
        assertTrue(html.contains("id=\"deleteModal\""));
        assertTrue(html.contains("role=\"alertdialog\""));
        assertTrue(html.contains("aria-modal=\"true\""));
        assertTrue(html.contains("Confirm Database Deletion"));
        assertTrue(html.contains("This will permanently purge database."));
        assertTrue(html.contains("orders_db"));
        assertTrue(html.contains("Confirm Delete"));
        assertTrue(html.contains("window.JettraConfirmDialog"));
        assertTrue(html.contains("JettraConfirmDialog.close"));
    }

    @Test
    @DisplayName("Should render JettraCardPanel with header, badge, and unified content")
    void testJettraCardPanelRender() {
        JettraCardPanel panel = JettraCardPanel.of("Unified Storage Workspace")
            .subtitle("Multi-Model Engines and Active Databases")
            .icon("fas fa-layer-group")
            .badge("3 Databases", "badge-active")
            .add(Text.of("Section 1 Content"))
            .add(Text.of("Section 2 Content"));

        String html = panel.render(Themes.FlatTheme());

        assertNotNull(html);
        assertTrue(html.contains("Unified Storage Workspace"));
        assertTrue(html.contains("Multi-Model Engines and Active Databases"));
        assertTrue(html.contains("3 Databases"));
        assertTrue(html.contains("Section 1 Content"));
        assertTrue(html.contains("Section 2 Content"));
    }

    @Test
    @DisplayName("Should render EmptyStateComponent with icon, title, description, and action")
    void testEmptyStateComponentRender() {
        EmptyStateComponent empty = EmptyStateComponent.of("No Active Databases", "No databases found matching your role.")
            .icon("fas fa-shield-alt")
            .action("Request Access", "alert('Requested')");

        String html = empty.render(Themes.FlatTheme());

        assertNotNull(html);
        assertTrue(html.contains("jettra-empty-state"));
        assertTrue(html.contains("No Active Databases"));
        assertTrue(html.contains("No databases found matching your role."));
        assertTrue(html.contains("Request Access"));
    }
}
