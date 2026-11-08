package io.jettra.flux.widgets;

import io.jettra.flux.theme.Themes;
import io.jettra.test.annotation.DisplayName;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.Test;

import static io.jettra.test.core.JettraAssert.*;

/**
 * JettraTest suite verifying the SampleDatabaseCard widget:
 * 1. Java 25 record immutability and builder pattern.
 * 2. Available state rendering (radio selector, available badge, quick install action).
 * 3. Installed state rendering (installed badge with record count, explore and uninstall actions).
 */
@NotRequiresRunningServer
public class SampleDatabaseCardTest {

    @Test
    @DisplayName("SampleDatabaseCard: verify builder and available state rendering")
    void testSampleDatabaseCardAvailableState() {
        SampleDatabaseCard card = SampleDatabaseCard.builder()
                .databaseName("ExampleHrEnterpriseDb")
                .engineType("RECORDS")
                .displayName("Java 25 Enterprise HR & Payroll")
                .description("Immutable Record instances for Employees and Departments")
                .estimatedRecords(1000)
                .installed(false)
                .icon("fas fa-id-card-alt")
                .build();

        assertNotNull(card);
        assertEquals("ExampleHrEnterpriseDb", card.getData().databaseName());
        assertEquals("RECORDS", card.getData().engineType());
        assertFalse(card.getData().installed());

        String html = card.render(Themes.FlatTheme());
        assertTrue(html.contains("id=\"sample-card-ExampleHrEnterpriseDb\""), "Must contain card wrapper id");
        assertTrue(html.contains("id=\"radio_sample_ExampleHrEnterpriseDb\""), "Must contain radio button id");
        assertTrue(html.contains("ExampleHrEnterpriseDb"), "Must display database name");
        assertTrue(html.contains("RECORDS"), "Must display engine tag");
        assertTrue(html.contains("Available (~1000 records)"), "Must display available status badge");
        assertTrue(html.contains("installSampleDb('ExampleHrEnterpriseDb')"), "Must render quick install button action");
        assertFalse(html.contains("openConfirmUninstallSampleDbModal"), "Available card must not render uninstall button");
    }

    @Test
    @DisplayName("SampleDatabaseCard: verify installed state rendering with explore and uninstall controls")
    void testSampleDatabaseCardInstalledState() {
        SampleDatabaseCard card = SampleDatabaseCard.builder()
                .databaseName("ExampleMeteorologyIotDb")
                .engineType("TIMESERIES")
                .displayName("IoT Meteorological Weather Stations")
                .description("High-frequency telemetry data")
                .estimatedRecords(2500)
                .recordCount(2500)
                .installed(true)
                .icon("fas fa-cloud-sun-rain")
                .build();

        assertNotNull(card);
        assertTrue(card.getData().installed());
        assertEquals(2500, card.getData().recordCount());

        String html = card.render(Themes.FlatTheme());
        assertTrue(html.contains("Installed (2500 records)"), "Must display installed status badge with count");
        assertTrue(html.contains("/engines?target_db=ExampleMeteorologyIotDb"), "Must render explore button with engine route");
        assertTrue(html.contains("openConfirmUninstallSampleDbModal('ExampleMeteorologyIotDb')"), "Must render uninstall action modal handler");
        assertFalse(html.contains("installSampleDb('ExampleMeteorologyIotDb')"), "Installed card must not render install button");
    }
}
