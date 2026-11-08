package io.jettra.flux.theme;

import io.jettra.flux.widgets.*;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.Test;

import static io.jettra.test.core.JettraAssert.*;

@NotRequiresRunningServer
public class HeroesThemeTest {

    @Test
    public void testHeroesThemeData() {
        ThemeData theme = HeroesTheme.create();
        assertNotNull(theme);
        assertEquals("#4f46e5", theme.primaryColor);
        assertEquals("#111827", theme.backgroundColor);
        assertEquals("#ffffff", theme.onPrimaryColor);
        assertEquals("#f9fafb", theme.onSurfaceColor);
        assertNotNull(theme.customCss);
        assertNotNull(theme.customJs);
        assertTrue(theme.generateGlobalCss().contains("--primary-color: #4f46e5;"));
        assertTrue(theme.generateGlobalCss().contains("--hero-primary: #4f46e5;"));
    }

    @Test
    public void testThemesFactoryMethods() {
        ThemeData theme1 = Themes.HeroesTheme();
        ThemeData theme2 = Themes.Heroes();
        assertNotNull(theme1);
        assertNotNull(theme2);
        assertEquals("#4f46e5", theme1.primaryColor);
        assertEquals("#4f46e5", theme2.primaryColor);
    }

    @Test
    public void testThemeRegistryLookup() {
        ThemeData t1 = ThemeRegistry.getTheme("HeroesTheme");
        ThemeData t2 = ThemeRegistry.getTheme("Heroes");
        ThemeData t3 = ThemeRegistry.getTheme("heroes");
        ThemeData t4 = ThemeRegistry.getTheme("heroestheme");

        assertNotNull(t1, "HeroesTheme should be resolved by ThemeRegistry");
        assertNotNull(t2, "Heroes should be resolved by ThemeRegistry");
        assertNotNull(t3, "heroes (case-insensitive) should be resolved by ThemeRegistry");
        assertNotNull(t4, "heroestheme (case-insensitive) should be resolved by ThemeRegistry");
        assertEquals("#4f46e5", t1.primaryColor);
    }

    @Test
    public void testThemeChangedRenderingWithHeroes() {
        ThemeChanged widget = ThemeChanged.of().current("Heroes");
        String html = widget.render(Themes.HeroesTheme());
        assertNotNull(html);
        assertTrue(html.contains("HeroesTheme") || html.contains("Heroes"));
        assertTrue(html.contains("changeJettraTheme"));
    }

    @Test
    public void testWidgetsRenderingWithHeroesTheme() {
        ThemeData heroes = Themes.HeroesTheme();

        // Button
        Button btn = Button.of("Get Started");
        String btnHtml = btn.render(heroes);
        assertTrue(btnHtml.contains("<button"));
        assertTrue(btnHtml.contains("espresso-button"));

        // Card
        Card card = Card.of(Text.of("Hero Card"));
        String cardHtml = card.render(heroes);
        assertTrue(cardHtml.contains("espresso-card"));

        // TextField
        TextField tf = TextField.of("email", "you@example.com");
        String tfHtml = tf.render(heroes);
        assertTrue(tfHtml.contains("espresso-textfield") || tfHtml.contains("<input"));

        // Datatable
        Datatable table = Datatable.of(java.util.List.of("Column"), java.util.List.of(java.util.List.of("Row1")));
        String tableHtml = table.render(heroes);
        assertTrue(tableHtml.contains("espresso-datatable") || tableHtml.contains("<table"));

        // Scaffold
        Scaffold scaffold = Scaffold.of().body(card);
        String scaffoldHtml = scaffold.render(heroes);
        assertTrue(scaffoldHtml.contains("jettra-scaffold-layout"));
        assertTrue(scaffoldHtml.contains("--primary-color: #4f46e5;"));
    }
}
