package io.jettra.flux.theme;

import io.jettra.flux.widgets.*;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.Test;

import static io.jettra.test.core.JettraAssert.*;

@NotRequiresRunningServer
public class SLThemeTest {

    @Test
    public void testSLThemeData() {
        ThemeData theme = SLTheme.create();
        assertNotNull(theme);
        assertEquals("#84cf29", theme.primaryColor);
        assertEquals("#0e131d", theme.backgroundColor);
        assertEquals("#0e131d", theme.onPrimaryColor);
        assertEquals("#f1f5f9", theme.onSurfaceColor);
        assertNotNull(theme.customCss);
        assertNotNull(theme.customJs);
        assertTrue(theme.generateGlobalCss().contains("--primary-color: #84cf29;"));
        assertTrue(theme.generateGlobalCss().contains("--sl-green: #84cf29;"));
    }

    @Test
    public void testThemesFactoryMethods() {
        ThemeData theme1 = Themes.SLTheme();
        ThemeData theme2 = Themes.SL();
        assertNotNull(theme1);
        assertNotNull(theme2);
        assertEquals("#84cf29", theme1.primaryColor);
        assertEquals("#84cf29", theme2.primaryColor);
    }

    @Test
    public void testThemeRegistryLookup() {
        ThemeData t1 = ThemeRegistry.getTheme("SLTheme");
        ThemeData t2 = ThemeRegistry.getTheme("SL");
        ThemeData t3 = ThemeRegistry.getTheme("sl");
        ThemeData t4 = ThemeRegistry.getTheme("sltheme");

        assertNotNull(t1, "SLTheme should be resolved by ThemeRegistry");
        assertNotNull(t2, "SL should be resolved by ThemeRegistry");
        assertNotNull(t3, "sl (case-insensitive) should be resolved by ThemeRegistry");
        assertNotNull(t4, "sltheme (case-insensitive) should be resolved by ThemeRegistry");
        assertEquals("#84cf29", t1.primaryColor);
    }

    @Test
    public void testThemeChangedRenderingWithSL() {
        ThemeChanged widget = ThemeChanged.of().current("SL");
        String html = widget.render(Themes.SLTheme());
        assertNotNull(html);
        assertTrue(html.contains("SLTheme") || html.contains("SL"));
        assertTrue(html.contains("changeJettraTheme"));
    }

    @Test
    public void testWidgetsRenderingWithSLTheme() {
        ThemeData sl = Themes.SLTheme();

        // Button
        Button btn = Button.of("Teleport");
        String btnHtml = btn.render(sl);
        assertTrue(btnHtml.contains("<button"));
        assertTrue(btnHtml.contains("espresso-button"));

        // Card
        Card card = Card.of(Text.of("Second Life Floater"));
        String cardHtml = card.render(sl);
        assertTrue(cardHtml.contains("espresso-card"));

        // TextField
        TextField tf = TextField.of("sim", "Enter SIM name");
        String tfHtml = tf.render(sl);
        assertTrue(tfHtml.contains("espresso-textfield") || tfHtml.contains("<input"));

        // Datatable
        Datatable table = Datatable.of(java.util.List.of("Region"), java.util.List.of(java.util.List.of("Astaroth")));
        String tableHtml = table.render(sl);
        assertTrue(tableHtml.contains("espresso-datatable") || tableHtml.contains("<table"));

        // Scaffold
        Scaffold scaffold = Scaffold.of().body(card);
        String scaffoldHtml = scaffold.render(sl);
        assertTrue(scaffoldHtml.contains("jettra-scaffold-layout"));
        assertTrue(scaffoldHtml.contains("--primary-color: #84cf29;"));
    }
}
