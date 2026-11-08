package io.jettra.flux.widgets;

import io.jettra.flux.theme.Themes;
import io.jettra.test.annotation.DisplayName;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.Test;

import static io.jettra.test.core.JettraAssert.*;

/**
 * Unit tests verifying Panel, PanelHeader, PanelBody, MetricCard, StatWidget, and Badge components.
 */
@NotRequiresRunningServer
public class PanelMetricComponentsTest {

    @Test
    @DisplayName("Badge: verify severities, presets, and rendering")
    void testBadgeVariants() {
        Badge activeBadge = Badge.active("ACTIVE");
        assertEquals("ACTIVE", activeBadge.getText());
        assertEquals("active", activeBadge.getSeverity());

        String activeHtml = activeBadge.render(Themes.FlatTheme());
        assertTrue(activeHtml.contains("ACTIVE"));
        assertTrue(activeHtml.contains("#34d399"));

        Badge neutralBadge = Badge.neutral("OFFLINE");
        assertEquals("neutral", neutralBadge.getSeverity());
        String neutralHtml = neutralBadge.render(Themes.FlatTheme());
        assertTrue(neutralHtml.contains("OFFLINE"));

        Badge infoBadge = Badge.info("9 ENGINES");
        assertEquals("info", infoBadge.getSeverity());
        String infoHtml = infoBadge.render(Themes.FlatTheme());
        assertTrue(infoHtml.contains("9 ENGINES"));
    }

    @Test
    @DisplayName("MetricCard: verify Java 25 record data, builder, and output hierarchy")
    void testMetricCardBuilderAndRecord() {
        MetricCard card = MetricCard.builder()
                .title("Active Databases")
                .value("4 Databases")
                .subtext("LSM / B-Tree Storage")
                .icon("fas fa-database", "#3b82f6")
                .badge(Badge.active("ACTIVE"))
                .build();

        assertNotNull(card);
        MetricCard.MetricData data = card.getData();
        assertEquals("Active Databases", data.title());
        assertEquals("4 Databases", data.value());
        assertEquals("LSM / B-Tree Storage", data.subtext());
        assertEquals("fas fa-database", data.icon());
        assertEquals("#3b82f6", data.color());
        assertNotNull(data.badge());

        String html = card.render(Themes.FlatTheme());
        assertTrue(html.contains("jettra-metric-card"), "Must include jettra-metric-card css class");
        assertTrue(html.contains("Active Databases"), "Must render title");
        assertTrue(html.contains("4 Databases"), "Must render primary value prominently");
        assertTrue(html.contains("LSM / B-Tree Storage"), "Must render auxiliary subtext");
        assertTrue(html.contains("ACTIVE"), "Must render status badge");
        assertTrue(html.contains("fas fa-database"), "Must render icon");
    }

    @Test
    @DisplayName("StatWidget: verify delegation to MetricCard")
    void testStatWidget() {
        StatWidget widget = StatWidget.of("Total Objects", "1,250 Entities");
        String html = widget.render(Themes.FlatTheme());
        assertTrue(html.contains("Total Objects"));
        assertTrue(html.contains("1,250 Entities"));
    }

    @Test
    @DisplayName("PanelHeader and PanelBody: verify composable structure and responsive grid")
    void testPanelHeaderAndBody() {
        PanelHeader header = PanelHeader.builder()
                .title("Engine Metrics")
                .subtitle("Distributed telemetry")
                .icon("fas fa-chart-line", "#38bdf8")
                .badge(Badge.active("LIVE"))
                .addAction(Button.of("Refresh"))
                .build();

        String headerHtml = header.render(Themes.FlatTheme());
        assertTrue(headerHtml.contains("Engine Metrics"));
        assertTrue(headerHtml.contains("Distributed telemetry"));
        assertTrue(headerHtml.contains("fas fa-chart-line"));
        assertTrue(headerHtml.contains("LIVE"));
        assertTrue(headerHtml.contains("Refresh"));

        PanelBody body = PanelBody.grid(4,
                MetricCard.of("Card 1", "100"),
                MetricCard.of("Card 2", "200")
        );
        assertTrue(body.isGrid());
        assertEquals(4, body.getColumns());

        String bodyHtml = body.render(Themes.FlatTheme());
        assertTrue(bodyHtml.contains("display: grid;"));
        assertTrue(bodyHtml.contains("Card 1"));
        assertTrue(bodyHtml.contains("Card 2"));
    }

    @Test
    @DisplayName("Panel & PanelBuilder: verify full assembly with 4-column metric cards")
    void testFullPanelComposition() {
        Panel panel = Panel.builder()
                .header(PanelHeader.builder()
                        .title("Operational Dashboard")
                        .subtitle("Engine Telemetry")
                        .icon("fas fa-server", "#38bdf8")
                        .badge(Badge.active("ONLINE"))
                        .build())
                .body(PanelBody.grid(4,
                        MetricCard.builder().title("Metric 1").value("10").subtext("Sub 1").badge(Badge.active("ACTIVE")).build(),
                        MetricCard.builder().title("Metric 2").value("20").subtext("Sub 2").badge(Badge.info("ACTIVE")).build(),
                        MetricCard.builder().title("Metric 3").value("30").subtext("Sub 3").badge(Badge.warning("ACTIVE")).build(),
                        MetricCard.builder().title("Metric 4").value("40").subtext("Sub 4").badge(Badge.success("ACTIVE")).build()
                ))
                .footer(Span.of("Cluster Health: 100% OK"))
                .build();

        assertNotNull(panel);
        String html = panel.render(Themes.FlatTheme());

        assertTrue(html.contains("Operational Dashboard"));
        assertTrue(html.contains("Engine Telemetry"));
        assertTrue(html.contains("ONLINE"));
        assertTrue(html.contains("Metric 1"));
        assertTrue(html.contains("Metric 2"));
        assertTrue(html.contains("Metric 3"));
        assertTrue(html.contains("Metric 4"));
        assertTrue(html.contains("Cluster Health: 100% OK"));
    }

    @Test
    @DisplayName("Panel backward compatibility: verify legacy Panel.of(header, child)")
    void testLegacyPanelCompatibility() {
        Panel legacy = Panel.of("Legacy Header", Paragraph.of("Legacy content")).toggleable(true);
        String html = legacy.render(Themes.FlatTheme());

        assertTrue(html.contains("espresso-panel"));
        assertTrue(html.contains("Legacy Header"));
        assertTrue(html.contains("Legacy content"));
        assertTrue(html.contains("fa-chevron-down"));
    }
}
