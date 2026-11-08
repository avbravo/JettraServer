package io.jettra.flux.widgets;

import io.jettra.flux.theme.Themes;
import io.jettra.test.annotation.DisplayName;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.Test;

import static io.jettra.test.core.JettraAssert.*;

@NotRequiresRunningServer
public class TopToolbarTest {

    @Test
    @DisplayName("TopToolbar renders action buttons when actionButtonsVisible is true")
    void testTopToolbarRendersActions() {
        TopToolbar toolbar = TopToolbar.of()
            .addLeft(Span.of("Left Title"))
            .actionButtons(
                Button.of("+ DB"),
                Button.of("Backup")
            );

        String html = toolbar.render(Themes.Dark());
        assertTrue(html.contains("Left Title"));
        assertTrue(html.contains("+ DB"));
        assertTrue(html.contains("Backup"));
    }

    @Test
    @DisplayName("TopToolbar suppresses action buttons when withActionButtonsVisible(false)")
    void testTopToolbarSuppressesActions() {
        TopToolbar toolbar = TopToolbar.of()
            .addLeft(Span.of("Left Title"))
            .actionButtons(
                Button.of("+ DB"),
                Button.of("Backup")
            )
            .withActionButtonsVisible(false);

        String html = toolbar.render(Themes.Dark());
        assertTrue(html.contains("Left Title"));
        assertFalse(html.contains("+ DB"));
        assertFalse(html.contains("Backup"));
    }

    @Test
    @DisplayName("TopToolbar suppresses entire toolbar when withTopToolbarVisible(false)")
    void testTopToolbarSuppressesEntireBar() {
        TopToolbar toolbar = TopToolbar.of()
            .addLeft(Span.of("Left Title"))
            .actionButtons(Button.of("+ DB"))
            .withTopToolbarVisible(false);

        String html = toolbar.render(Themes.Dark());
        assertEquals("", html.trim());
    }

    @Test
    @DisplayName("DashboardHeader suppresses action buttons when withActionButtonsVisible(false)")
    void testDashboardHeaderSuppressesActions() {
        DashboardHeader header = DashboardHeader.of("Information")
            .actionButtons(Button.of("+ DB"), Button.of("Restore"))
            .withActionButtonsVisible(false);

        String html = header.render(Themes.Dark());
        assertTrue(html.contains("Information"));
        assertFalse(html.contains("+ DB"));
        assertFalse(html.contains("Restore"));
    }

    @Test
    @DisplayName("DashboardLayout respects withTopToolbarVisible(false)")
    void testDashboardLayoutSuppressesHeader() {
        TopToolbar toolbar = TopToolbar.of().addLeft(Span.of("Visible Top Bar"));
        DashboardLayout layout = DashboardLayout.of(toolbar, Div.of(Text.of("Main Content")))
            .withTopToolbarVisible(false);

        String html = layout.render(Themes.Dark());
        assertFalse(html.contains("Visible Top Bar"));
        assertTrue(html.contains("Main Content"));
    }

    @Test
    @DisplayName("DashboardLayout propagates withActionButtonsVisible(false) to TopToolbar and DashboardHeader")
    void testDashboardLayoutSuppressesActionButtons() {
        TopToolbar toolbar = TopToolbar.of()
            .addLeft(Span.of("Dashboard Title"))
            .actionButtons(Button.of("+ DB"), Button.of("Backup"));

        DashboardLayout layout = DashboardLayout.of(toolbar, Div.of(Text.of("Body Content")))
            .withActionButtonsVisible(false);

        String html = layout.render(Themes.Dark());
        assertTrue(html.contains("Dashboard Title"));
        assertFalse(html.contains("+ DB"));
        assertFalse(html.contains("Backup"));
        assertTrue(html.contains("Body Content"));

        DashboardHeader header = DashboardHeader.of("Components Title")
            .actionButtons(Button.of("+ DB"), Button.of("Restore"));

        DashboardLayout layoutWithHeader = DashboardLayout.of(header, Div.of(Text.of("Components Content")))
            .withActionButtonsVisible(false);

        String htmlHeader = layoutWithHeader.render(Themes.Dark());
        assertTrue(htmlHeader.contains("Components Title"));
        assertFalse(htmlHeader.contains("+ DB"));
        assertFalse(htmlHeader.contains("Restore"));
        assertTrue(htmlHeader.contains("Components Content"));
    }

    @Test
    @DisplayName("PanelHeader suppresses action widgets when withActionsVisible(false)")
    void testPanelHeaderSuppressesActions() {
        PanelHeader header = PanelHeader.of("Databases Title", "Overview of instances")
            .actions(Button.of("+ DB"), Button.of("Export"))
            .withActionsVisible(false);

        String html = header.render(Themes.Dark());
        assertTrue(html.contains("Databases Title"));
        assertTrue(html.contains("Overview of instances"));
        assertFalse(html.contains("+ DB"));
        assertFalse(html.contains("Export"));
    }
}
