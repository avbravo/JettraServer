package io.jettra.flux.widgets;

import io.jettra.flux.theme.Themes;
import io.jettra.test.annotation.DisplayName;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.Test;

import java.time.Year;
import java.util.List;

import static io.jettra.test.core.JettraAssert.*;

/**
 * Unit test suite for AppFooter and associated metadata records.
 */
@NotRequiresRunningServer
class AppFooterTest {

    @Test
    @DisplayName("FooterMetadata resolves dynamic copyright year and encapsulates links")
    void testFooterMetadata() {
        FooterLink docLink = FooterLink.of("Docs", "/docs", "fas fa-book");
        FooterMetadata meta = FooterMetadata.of(
            "JettraCloud",
            "v2.5.0",
            "Jettra Innovations",
            "Healthy",
            List.of(docLink)
        );

        assertEquals("JettraCloud", meta.appName());
        assertEquals("v2.5.0", meta.version());
        assertEquals(Year.now().getValue(), meta.copyrightYear());
        assertEquals("Jettra Innovations", meta.copyrightHolder());
        assertEquals("Healthy", meta.connectionStatus());
        assertEquals(1, meta.links().size());
        assertEquals("Docs", meta.links().get(0).text());
        assertEquals("/docs", meta.links().get(0).href());
        assertEquals("fas fa-book", meta.links().get(0).icon());
    }

    @Test
    @DisplayName("AppFooter renders semantic <footer> with branding and default slots")
    void testAppFooterRendering() {
        AppFooter footer = AppFooter.of()
            .appName("JettraDB Studio")
            .version("1.0.0-SNAPSHOT")
            .copyright("JettraStack")
            .status("Cluster Active", "#10b981")
            .link("Documentation", "/info", "fas fa-info-circle")
            .link("API Swagger", "/swagger", "fas fa-code");

        String html = footer.render(Themes.Matrix());

        assertNotNull(html);
        assertTrue(html.startsWith("<footer"), "Must start with semantic <footer> element");
        assertTrue(html.contains("</footer>"), "Must properly close <footer> element");
        assertTrue(html.contains("jettra-app-footer"), "Must contain standard CSS class");
        assertTrue(html.contains("JettraDB Studio"), "Must render app name");
        assertTrue(html.contains("JettraStack"), "Must render copyright holder");
        assertTrue(html.contains(String.valueOf(Year.now().getValue())), "Must render dynamic year");
        assertTrue(html.contains("Documentation"), "Must render center link text");
        assertTrue(html.contains("/info"), "Must render link href");
        assertTrue(html.contains("Cluster Active"), "Must render connection status");
        assertTrue(html.contains("1.0.0-SNAPSHOT"), "Must render build version");
    }

    @Test
    @DisplayName("AppFooter correctly renders custom slot overrides")
    void testAppFooterCustomSlots() {
        AppFooter footer = AppFooter.of()
            .leftSlot(Span.of("Custom Left"))
            .centerSlot(Span.of("Custom Center"))
            .rightSlot(Span.of("Custom Right"));

        String html = footer.render(Themes.Matrix());

        assertTrue(html.contains("Custom Left"), "Must render custom left slot");
        assertTrue(html.contains("Custom Center"), "Must render custom center slot");
        assertTrue(html.contains("Custom Right"), "Must render custom right slot");
        assertFalse(html.contains("All rights reserved"), "Default left slot should not render when overridden");
    }
}
