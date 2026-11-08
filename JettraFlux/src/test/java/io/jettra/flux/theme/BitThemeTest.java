package io.jettra.flux.theme;

import io.jettra.flux.widgets.*;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.Test;

import static io.jettra.test.core.JettraAssert.*;

@NotRequiresRunningServer
public class BitThemeTest {

    @Test
    public void testBitThemeData() {
        ThemeData theme = BitTheme.create();
        assertNotNull(theme);
        assertEquals("#00f2fe", theme.primaryColor);
        assertEquals("#38bdf8", theme.secondaryColor);
        assertEquals("#080d1a", theme.backgroundColor);
        assertEquals("#111827", theme.surfaceColor);
        assertEquals("#080d1a", theme.onPrimaryColor);
        assertEquals("#f3f4f6", theme.onSurfaceColor);
        assertNotNull(theme.customCss);
        assertNotNull(theme.customJs);
        assertTrue(theme.generateGlobalCss().contains("--primary-color: #00f2fe;"));
        assertTrue(theme.generateGlobalCss().contains(".espresso-dashboard"));
        assertTrue(theme.generateGlobalCss().contains(".espresso-button"));
        assertTrue(theme.customJs.contains("[BitTheme] Theme initialized successfully."));
    }

    @Test
    public void testThemesFactoryMethods() {
        ThemeData theme1 = Themes.BitTheme();
        ThemeData theme2 = Themes.Bit();
        assertNotNull(theme1);
        assertNotNull(theme2);
        assertEquals("#00f2fe", theme1.primaryColor);
        assertEquals("#00f2fe", theme2.primaryColor);

        // White mode
        ThemeData themeWhite = Themes.Bit(ColorMode.WHITE);
        assertNotNull(themeWhite);
        assertEquals(ColorMode.WHITE, themeWhite.getColorMode());
        assertEquals("#0284c7", themeWhite.primaryColor);
        assertEquals("#f0f9ff", themeWhite.backgroundColor);
    }

    @Test
    public void testThemeRegistryLookup() {
        ThemeData t1 = ThemeRegistry.getTheme("BitTheme");
        ThemeData t2 = ThemeRegistry.getTheme("Bit");
        ThemeData t3 = ThemeRegistry.getTheme("bit");
        ThemeData t4 = ThemeRegistry.getTheme("bittheme");

        assertNotNull(t1, "BitTheme should be resolved by ThemeRegistry");
        assertNotNull(t2, "Bit should be resolved by ThemeRegistry");
        assertNotNull(t3, "bit (case-insensitive) should be resolved by ThemeRegistry");
        assertNotNull(t4, "bittheme (case-insensitive) should be resolved by ThemeRegistry");
        assertEquals("#00f2fe", t1.primaryColor);
        assertEquals("#00f2fe", t2.primaryColor);
    }

    @Test
    public void testThemeChangedRenderingWithBit() {
        ThemeChanged widget = ThemeChanged.of().current("Bit");
        String html = widget.render(Themes.BitTheme());
        assertNotNull(html);
        assertTrue(html.contains("BitTheme") || html.contains("Bit"));
        assertTrue(html.contains("changeJettraTheme"));
    }

    @Test
    public void testWidgetsRenderingWithBitTheme() {
        ThemeData bit = Themes.BitTheme();

        // Button
        Button btn = Button.of("Iniciar Sistema");
        String btnHtml = btn.render(bit);
        assertTrue(btnHtml.contains("<button"));
        assertTrue(btnHtml.contains("espresso-button"));

        // Card
        Card card = Card.of(Text.of("Panel de Control Cyber"));
        String cardHtml = card.render(bit);
        assertTrue(cardHtml.contains("espresso-card"));

        // TextField
        TextField tf = TextField.of("search_user", "Buscar Registro");
        String tfHtml = tf.render(bit);
        assertTrue(tfHtml.contains("espresso-textfield") || tfHtml.contains("<input"));

        // Datatable
        Datatable table = Datatable.of(
            java.util.List.of("ID", "Servicio", "Estado"),
            java.util.List.of(java.util.List.of("1", "Auth Service", "ACTIVO"))
        );
        String tableHtml = table.render(bit);
        assertTrue(tableHtml.contains("espresso-datatable") || tableHtml.contains("<table"));

        // Scaffold
        Scaffold scaffold = Scaffold.of().body(card);
        String scaffoldHtml = scaffold.render(bit);
        assertTrue(scaffoldHtml.contains("jettra-scaffold-layout"));
        assertTrue(scaffoldHtml.contains("--primary-color: #00f2fe;"));
    }

    @Test
    public void testBitTokensSemanticIntegrity() {
        ThemeTokens darkTok = Bit.getTokens(ColorMode.DARK);
        assertEquals("#080d1a", darkTok.surfaceBackground());
        assertEquals("#111827", darkTok.cardBackground());
        assertEquals("#f3f4f6", darkTok.textPrimary());
        assertEquals("#00f2fe", darkTok.accentPrimary());
        assertEquals("#38bdf8", darkTok.accentSecondary());

        ThemeTokens whiteTok = Bit.getTokens(ColorMode.WHITE);
        assertEquals("#f0f9ff", whiteTok.surfaceBackground());
        assertEquals("#ffffff", whiteTok.cardBackground());
        assertEquals("#080d1a", whiteTok.textPrimary());
        assertEquals("#0284c7", whiteTok.accentPrimary());
        assertEquals("#0369a1", whiteTok.accentSecondary());
    }

    @Test
    public void testLegacyPackageCompatibility() {
        ThemeData legacyDark = io.jettra.themes.bit.BitTheme.create();
        assertNotNull(legacyDark);
        assertEquals("#00f2fe", legacyDark.primaryColor);

        ThemeData legacyWhite = io.jettra.themes.bit.BitTheme.create(ColorMode.WHITE);
        assertNotNull(legacyWhite);
        assertEquals(ColorMode.WHITE, legacyWhite.getColorMode());

        assertNotNull(io.jettra.themes.bit.BitTheme.Template.CustomCSS);
        assertNotNull(io.jettra.themes.bit.BitTheme.Template.CustomJS);
    }
}
