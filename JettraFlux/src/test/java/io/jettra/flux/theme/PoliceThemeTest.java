package io.jettra.flux.theme;

import io.jettra.flux.widgets.*;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.Test;

import static io.jettra.test.core.JettraAssert.*;

@NotRequiresRunningServer
public class PoliceThemeTest {

    @Test
    public void testPoliceThemeData() {
        ThemeData theme = PoliceTheme.create();
        assertNotNull(theme);
        assertEquals("#ffd700", theme.primaryColor);
        assertEquals("#090d16", theme.backgroundColor);
        assertEquals("#090d16", theme.onPrimaryColor);
        assertEquals("#f8fafc", theme.onSurfaceColor);
        assertNotNull(theme.customCss);
        assertNotNull(theme.customJs);
        assertTrue(theme.generateGlobalCss().contains("--primary-color: #ffd700;"));
        assertTrue(theme.generateGlobalCss().contains("police-radar-canvas"));
        assertTrue(theme.generateGlobalCss().contains("police-badge"));
        assertTrue(theme.customJs.contains("initPoliceRadar"));
    }

    @Test
    public void testThemesFactoryMethods() {
        ThemeData theme1 = Themes.PoliceTheme();
        ThemeData theme2 = Themes.Police();
        assertNotNull(theme1);
        assertNotNull(theme2);
        assertEquals("#ffd700", theme1.primaryColor);
        assertEquals("#ffd700", theme2.primaryColor);

        // White mode
        ThemeData themeWhite = Themes.Police(ColorMode.WHITE);
        assertNotNull(themeWhite);
        assertEquals(ColorMode.WHITE, themeWhite.getColorMode());
        assertEquals("#b45309", themeWhite.primaryColor);
        assertEquals("#f1f5f9", themeWhite.backgroundColor);
    }

    @Test
    public void testThemeRegistryLookup() {
        ThemeData t1 = ThemeRegistry.getTheme("PoliceTheme");
        ThemeData t2 = ThemeRegistry.getTheme("Police");
        ThemeData t3 = ThemeRegistry.getTheme("police");
        ThemeData t4 = ThemeRegistry.getTheme("policetheme");

        assertNotNull(t1, "PoliceTheme should be resolved by ThemeRegistry");
        assertNotNull(t2, "Police should be resolved by ThemeRegistry");
        assertNotNull(t3, "police (case-insensitive) should be resolved by ThemeRegistry");
        assertNotNull(t4, "policetheme (case-insensitive) should be resolved by ThemeRegistry");
        assertEquals("#ffd700", t1.primaryColor);
        assertEquals("#ffd700", t2.primaryColor);
    }

    @Test
    public void testThemeChangedRenderingWithPolice() {
        ThemeChanged widget = ThemeChanged.of().current("Police");
        String html = widget.render(Themes.PoliceTheme());
        assertNotNull(html);
        assertTrue(html.contains("PoliceTheme") || html.contains("Police"));
        assertTrue(html.contains("changeJettraTheme"));
    }

    @Test
    public void testWidgetsRenderingWithPoliceTheme() {
        ThemeData police = Themes.PoliceTheme();

        // Button
        Button btn = Button.of("Desplegar Patrulla");
        String btnHtml = btn.render(police);
        assertTrue(btnHtml.contains("<button"));
        assertTrue(btnHtml.contains("espresso-button"));

        // Card
        Card card = Card.of(Text.of("Centro de Monitoreo Táctico"));
        String cardHtml = card.render(police);
        assertTrue(cardHtml.contains("espresso-card"));

        // TextField
        TextField tf = TextField.of("search_node", "Buscar Servidor o Patrulla");
        String tfHtml = tf.render(police);
        assertTrue(tfHtml.contains("espresso-textfield") || tfHtml.contains("<input"));

        // Datatable
        Datatable table = Datatable.of(
            java.util.List.of("Servidor", "Estado", "Latencia", "Multinodo"),
            java.util.List.of(java.util.List.of("srv-1", "ONLINE", "1.2 ms", "ON"))
        );
        String tableHtml = table.render(police);
        assertTrue(tableHtml.contains("espresso-datatable") || tableHtml.contains("<table"));

        // Scaffold
        Scaffold scaffold = Scaffold.of().body(card);
        String scaffoldHtml = scaffold.render(police);
        assertTrue(scaffoldHtml.contains("jettra-scaffold-layout"));
        assertTrue(scaffoldHtml.contains("--primary-color: #ffd700;"));
    }

    @Test
    public void testPoliceTokensSemanticIntegrity() {
        ThemeTokens darkTok = Police.getTokens(ColorMode.DARK);
        assertEquals("#090d16", darkTok.surfaceBackground());
        assertEquals("#0f1526", darkTok.cardBackground());
        assertEquals("#f8fafc", darkTok.textPrimary());
        assertEquals("#ffd700", darkTok.accentPrimary());
        assertEquals("#00d4ff", darkTok.accentSecondary());

        ThemeTokens whiteTok = Police.getTokens(ColorMode.WHITE);
        assertEquals("#f1f5f9", whiteTok.surfaceBackground());
        assertEquals("#ffffff", whiteTok.cardBackground());
        assertEquals("#090d16", whiteTok.textPrimary());
        assertEquals("#b45309", whiteTok.accentPrimary());
        assertEquals("#0284c7", whiteTok.accentSecondary());
    }
}
