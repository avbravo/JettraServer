package io.jettra.flux.widgets;

import io.jettra.flux.core.Modifier;
import io.jettra.flux.theme.ColorMode;
import io.jettra.flux.theme.Themes;
import io.jettra.test.annotation.DisplayName;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.Test;

import static io.jettra.test.core.JettraAssert.*;

/**
 * Unit Test Suite for FluidContainer, FlexLayout, Box.fillMaxSize, and Modifier fluid utilities in JettraFlux.
 */
@NotRequiresRunningServer
public class FluidContainerTest {

    @Test
    @DisplayName("FluidContainer renders full-width layout with width: 100%, min-width: 100%, and responsive padding")
    void testFluidContainerFullWidthRendering() {
        Paragraph p1 = Paragraph.of("Telemetry Content");
        FluidContainer container = FluidContainer.of(p1);

        String html = container.render(Themes.Matrix(ColorMode.DARK));

        assertNotNull(html);
        assertTrue(html.contains("jettra-fluid-container"), "Must include jettra-fluid-container CSS class");
        assertTrue(html.contains("width:100%") || html.contains("width: 100%"), "Must span 100% width");
        assertTrue(html.contains("min-width:100%") || html.contains("min-width: 100%"), "Must enforce 100% min-width");
        assertTrue(html.contains("flex:1") || html.contains("flex: 1"), "Must occupy flex: 1 space");
        assertTrue(html.contains("padding:16px 20px") || html.contains("padding: 16px 20px"), "Default padding must match Engines canvas");
        assertTrue(html.contains("Telemetry Content"));
    }

    @Test
    @DisplayName("FluidContainer supports custom responsive padding")
    void testFluidContainerCustomPadding() {
        FluidContainer container = FluidContainer.of(Paragraph.of("Padded Content"))
                .padding(24, 32);

        String html = container.render(Themes.FlatTheme());

        assertNotNull(html);
        assertTrue(html.contains("padding:24px 32px") || html.contains("padding: 24px 32px"));
        assertTrue(html.contains("Padded Content"));
    }

    @Test
    @DisplayName("FlexLayout.fullWidth sets full-width and flex: 1 container properties")
    void testFlexLayoutFullWidth() {
        FlexLayout layout = FlexLayout.fullWidth(
                StatCard.of("Nodes", "5", "Online", true),
                StatCard.of("Capacity", "80%", "Optimal", false)
        );

        String html = layout.render(Themes.FlatTheme());

        assertNotNull(html);
        assertTrue(html.contains("jettra-flex-layout"), "Must include jettra-flex-layout class");
        assertTrue(html.contains("width:100%") || html.contains("width: 100%"));
        assertTrue(html.contains("min-width:100%") || html.contains("min-width: 100%"));
        assertTrue(html.contains("Nodes"));
        assertTrue(html.contains("Capacity"));
    }

    @Test
    @DisplayName("Box.fillMaxSize sets width: 100% and height: 100% for full viewport coverage")
    void testBoxFillMaxSize() {
        Box box = Box.fillMaxSize(Paragraph.of("Fullscreen Box Content"));

        String html = box.render(Themes.Matrix(ColorMode.DARK));

        assertNotNull(html);
        assertTrue(html.contains("jettra-box"), "Must include jettra-box class");
        assertTrue(html.contains("width:100%") || html.contains("width: 100%"));
        assertTrue(html.contains("min-width:100%") || html.contains("min-width: 100%"));
        assertTrue(html.contains("height:100%") || html.contains("height: 100%"));
        assertTrue(html.contains("Fullscreen Box Content"));
    }

    @Test
    @DisplayName("Modifier fluid helper methods format styles correctly")
    void testModifierFluidHelpers() {
        Modifier mod = new Modifier()
                .fullWidth()
                .flex1()
                .flexGrow(2)
                .boxSizingBorderBox()
                .padding(12, 18);

        String styles = mod.getStyles();

        assertNotNull(styles);
        assertTrue(styles.contains("width:100%;"));
        assertTrue(styles.contains("min-width:100%;"));
        assertTrue(styles.contains("box-sizing:border-box;"));
        assertTrue(styles.contains("flex:1;"));
        assertTrue(styles.contains("flex-grow:2;"));
        assertTrue(styles.contains("padding:12px 18px;"));
    }
}
