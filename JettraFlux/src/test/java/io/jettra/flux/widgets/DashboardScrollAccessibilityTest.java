package io.jettra.flux.widgets;

import io.jettra.flux.theme.ColorMode;
import io.jettra.flux.theme.Themes;
import io.jettra.test.annotation.DisplayName;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.Test;

import static io.jettra.test.core.JettraAssert.*;

/**
 * Unit Test Suite for Dashboard Vertical Scrolling, Keyboard Accessibility,
 * Sticky Header anchoring, and Theme Token Scrollbar styling in JettraFlux.
 */
@NotRequiresRunningServer
public class DashboardScrollAccessibilityTest {

    @Test
    @DisplayName("DashboardContentBody renders with overflow-y: auto, tabindex=0, and aria accessibility role")
    void testDashboardContentBodyScrollingAndAccessibility() {
        Paragraph p1 = Paragraph.of("Metric Content 1");
        Paragraph p2 = Paragraph.of("Metric Content 2");

        DashboardContentBody body = DashboardContentBody.of(p1, p2)
                .ariaLabel("Main Cluster Telemetry Body");

        String html = body.render(Themes.Matrix(ColorMode.DARK));

        assertNotNull(html);
        assertTrue(html.contains("jettra-dashboard-content-body"), "Must include content body class");
        assertTrue(html.contains("tabindex=\"0\""), "Must have tabindex=0 for keyboard focus and directional navigation");
        assertTrue(html.contains("role=\"region\""), "Must declare accessible role");
        assertTrue(html.contains("aria-label=\"Main Cluster Telemetry Body\""), "Must output specified aria-label");
        assertTrue(html.contains("overflow-y:auto") || html.contains("overflow-y: auto"), "Must allow vertical scroll");
        assertTrue(html.contains("overflow-x:hidden") || html.contains("overflow-x: hidden"), "Must prevent horizontal scroll");
        assertTrue(html.contains("flex-grow:1") || html.contains("flex-grow: 1"), "Must grow to take available flex space");
        assertTrue(html.contains("scrollbar-width: thin;"), "Must specify thin scrollbar for modern browsers");
        assertTrue(html.contains("scrollbar-color: var(--jf-accent"), "Must link scrollbar color to semantic theme accent token");
        assertTrue(html.contains("Metric Content 1"));
        assertTrue(html.contains("Metric Content 2"));
    }

    @Test
    @DisplayName("DashboardHeader renders with sticky top-0, flex-shrink: 0, and theme background")
    void testDashboardHeaderStickyAndFlexShrink() {
        DashboardHeader header = DashboardHeader.of("Telemetry Overview")
                .currentTheme("Matrix")
                .colorMode(ColorMode.DARK)
                .sticky(true);

        String html = header.render(Themes.Matrix(ColorMode.DARK));

        assertNotNull(html);
        assertTrue(html.contains("position:sticky") || html.contains("position: sticky"), "Header must stick to the top during vertical scrolling");
        assertTrue(html.contains("top:0") || html.contains("top: 0"), "Header must anchor at top: 0");
        assertTrue(html.contains("flex-shrink:0") || html.contains("flex-shrink: 0"), "Header must not shrink when body content expands");
        assertTrue(html.contains("background:var(--jf-surface") || html.contains("background: var(--jf-surface"), "Header must use opaque theme surface background");
        assertTrue(html.contains("Telemetry Overview"), "Header must display title");
        assertTrue(html.contains("jettra-theme-dropdown") || html.contains("jettra-dashboard-theme-control-bar"),
                "Header must embed theme control bar with mode toggle");
    }

    @Test
    @DisplayName("DashboardRoot enforces min-height: 100vh and display: flex; flex-direction: column")
    void testDashboardRootContainerConstraints() {
        DashboardRoot root = DashboardRoot.of(
                DashboardHeader.of("Root Header"),
                DashboardContentBody.of(Paragraph.of("Root Body"))
        );

        String html = root.render(Themes.FlatTheme());

        assertNotNull(html);
        assertTrue(html.contains("jettra-dashboard-root"), "Must contain dashboard root class");
        assertTrue(html.contains("min-height:100vh") || html.contains("min-height: 100vh"), "Must ensure minimum 100vh height");
        assertTrue(html.contains("flex-direction:column") || html.contains("flex-direction: column"), "Must flow in vertical column");
        assertTrue(html.contains("Root Header"));
        assertTrue(html.contains("Root Body"));
    }

    @Test
    @DisplayName("DashboardLayout assembles DashboardRoot, DashboardHeader, and DashboardContentBody seamlessly")
    void testDashboardLayoutCompositeStructure() {
        DashboardHeader header = DashboardHeader.of("Jettra Flux Console");
        DashboardContentBody body = DashboardContentBody.of(
                StatCard.of("Active Nodes", "3", "Cluster Online", true),
                StatCard.of("Storage Used", "1.2 GB", "Engine Capacity", false)
        );

        DashboardLayout layout = DashboardLayout.of(header, body);
        String html = layout.render(Themes.Matrix(ColorMode.DARK));

        assertNotNull(html);
        assertTrue(html.contains("jettra-dashboard-root"), "Layout must wrap in DashboardRoot");
        assertTrue(html.contains("position:sticky") || html.contains("position: sticky"), "Layout must preserve sticky header");
        assertTrue(html.contains("tabindex=\"0\""), "Layout body must remain keyboard accessible");
        assertTrue(html.contains("overflow-y:auto") || html.contains("overflow-y: auto"), "Layout body must scroll");
        assertTrue(html.contains("Active Nodes"));
        assertTrue(html.contains("Storage Used"));
    }

    @Test
    @DisplayName("FlexColumn renders display: flex; flex-direction: column; width: 100%")
    void testFlexColumnRendering() {
        FlexColumn col = FlexColumn.of(
                Paragraph.of("Line 1"),
                Paragraph.of("Line 2")
        );

        String html = col.render(Themes.FlatTheme());

        assertNotNull(html);
        assertTrue(html.contains("jettra-flex-column"), "Must have jettra-flex-column class");
        assertTrue(html.contains("display:flex; flex-direction:column;"), "Must configure flex column style");
        assertTrue(html.contains("Line 1"));
        assertTrue(html.contains("Line 2"));
    }
}
