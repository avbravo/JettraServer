package io.jettra.flux.theme;

import io.jettra.flux.widgets.*;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.Test;

import static io.jettra.test.core.JettraAssert.*;

@NotRequiresRunningServer
public class GamesThemeTest {

    @Test
    public void testGamesThemeData() {
        ThemeData theme = GamesTheme.create();
        assertNotNull(theme);
        assertEquals("#ffd700", theme.primaryColor);
        assertEquals("#040610", theme.backgroundColor);
        assertEquals("#040610", theme.onPrimaryColor);
        assertEquals("#f8fafc", theme.onSurfaceColor);
        assertNotNull(theme.customCss);
        assertNotNull(theme.customJs);
        assertTrue(theme.generateGlobalCss().contains("--primary-color: #ffd700;"));
        assertTrue(theme.generateGlobalCss().contains("games-perspective-grid-canvas"));
        assertTrue(theme.generateGlobalCss().contains("games-btn-gold"));
        assertTrue(theme.generateGlobalCss().contains("games-modal"));
        assertTrue(theme.customJs.contains("initGamesPerspectiveGrid"));
    }

    @Test
    public void testThemesFactoryMethods() {
        ThemeData theme1 = Themes.GamesTheme();
        ThemeData theme2 = Themes.Games();
        assertNotNull(theme1);
        assertNotNull(theme2);
        assertEquals("#ffd700", theme1.primaryColor);
        assertEquals("#ffd700", theme2.primaryColor);

        // White mode
        ThemeData themeWhite = Themes.Games(ColorMode.WHITE);
        assertNotNull(themeWhite);
        assertEquals(ColorMode.WHITE, themeWhite.getColorMode());
        assertEquals("#d97706", themeWhite.primaryColor);
        assertEquals("#f8fafc", themeWhite.backgroundColor);
    }

    @Test
    public void testThemeRegistryLookup() {
        ThemeData t1 = ThemeRegistry.getTheme("GamesTheme");
        ThemeData t2 = ThemeRegistry.getTheme("Games");
        ThemeData t3 = ThemeRegistry.getTheme("games");
        ThemeData t4 = ThemeRegistry.getTheme("gamestheme");

        assertNotNull(t1, "GamesTheme should be resolved by ThemeRegistry");
        assertNotNull(t2, "Games should be resolved by ThemeRegistry");
        assertNotNull(t3, "games (case-insensitive) should be resolved by ThemeRegistry");
        assertNotNull(t4, "gamestheme (case-insensitive) should be resolved by ThemeRegistry");
        assertEquals("#ffd700", t1.primaryColor);
        assertEquals("#ffd700", t2.primaryColor);
    }

    @Test
    public void testThemeChangedRenderingWithGames() {
        ThemeChanged widget = ThemeChanged.of().current("Games");
        String html = widget.render(Themes.GamesTheme());
        assertNotNull(html);
        assertTrue(html.contains("GamesTheme") || html.contains("Games"));
        assertTrue(html.contains("changeJettraTheme"));
    }

    @Test
    public void testWidgetsRenderingWithGamesTheme() {
        ThemeData games = Themes.GamesTheme();

        // Button
        Button btn = Button.of("EXPLORADOR ENGINES");
        String btnHtml = btn.render(games);
        assertTrue(btnHtml.contains("<button"));
        assertTrue(btnHtml.contains("espresso-button"));

        // Card
        Card card = Card.of(Text.of("FICHA TÉCNICA DE BASE DE DATOS"));
        String cardHtml = card.render(games);
        assertTrue(cardHtml.contains("espresso-card"));

        // TextField
        TextField tf = TextField.of("search_box", "Ej: FROM facturas WHERE total > 500");
        String tfHtml = tf.render(games);
        assertTrue(tfHtml.contains("espresso-textfield") || tfHtml.contains("<input"));

        // Datatable
        Datatable table = Datatable.of(
            java.util.List.of("ID", "Detalle", "Acción"),
            java.util.List.of(java.util.List.of("FAC-2026-00001", "Factura Fiscal #1", "VER"))
        );
        String tableHtml = table.render(games);
        assertTrue(tableHtml.contains("espresso-datatable") || tableHtml.contains("<table"));

        // Scaffold
        Scaffold scaffold = Scaffold.of().body(card);
        String scaffoldHtml = scaffold.render(games);
        assertTrue(scaffoldHtml.contains("jettra-scaffold-layout"));
        assertTrue(scaffoldHtml.contains("--primary-color: #ffd700;"));
    }

    @Test
    public void testGamesTokensSemanticIntegrity() {
        ThemeTokens darkTok = Games.getTokens(ColorMode.DARK);
        assertEquals("#040610", darkTok.surfaceBackground());
        assertEquals("#0e1220", darkTok.cardBackground());
        assertEquals("#f8fafc", darkTok.textPrimary());
        assertEquals("#ffd700", darkTok.accentPrimary());
        assertEquals("#00d4ff", darkTok.accentSecondary());

        ThemeTokens whiteTok = Games.getTokens(ColorMode.WHITE);
        assertEquals("#f8fafc", whiteTok.surfaceBackground());
        assertEquals("#ffffff", whiteTok.cardBackground());
        assertEquals("#040610", whiteTok.textPrimary());
        assertEquals("#d97706", whiteTok.accentPrimary());
        assertEquals("#0284c7", whiteTok.accentSecondary());
    }
}
