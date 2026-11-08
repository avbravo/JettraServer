package io.jettra.flux.theme;

import io.jettra.flux.widgets.*;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.Test;

import static io.jettra.test.core.JettraAssert.*;

@NotRequiresRunningServer
public class CoreThemeTest {

    @Test
    public void testCoreThemeDataProperties() {
        ThemeData theme = CoreTheme.create();
        assertNotNull(theme, "CoreTheme.create() should not be null");
        assertEquals("#f59e0b", theme.primaryColor);
        assertEquals("#38bdf8", theme.secondaryColor);
        assertEquals("#0b0e14", theme.backgroundColor);
        assertEquals("#161f2e", theme.surfaceColor);
        assertEquals("#0b0e14", theme.onPrimaryColor);
        assertEquals("#f1f5f9", theme.onSurfaceColor);

        assertNotNull(theme.buttonStyle);
        assertTrue(theme.buttonStyle.contains("#f59e0b"));
        assertNotNull(theme.cardStyle);
        assertTrue(theme.cardStyle.contains("#f59e0b"));
        assertNotNull(theme.containerStyle);
        assertNotNull(theme.textStyle);

        assertNotNull(theme.customCss);
        assertTrue(theme.customCss.contains("CoreTheme"));
        assertTrue(theme.customCss.contains("Orbitron"));
        assertTrue(theme.customCss.contains("Rajdhani"));

        assertNotNull(theme.customJs);
    }

    @Test
    public void testCoreThemeFactoryMethods() {
        ThemeData t1 = Themes.CoreTheme();
        ThemeData t2 = Themes.Core();

        assertNotNull(t1, "Themes.CoreTheme() should not be null");
        assertNotNull(t2, "Themes.Core() should not be null");
        assertEquals("#f59e0b", t1.primaryColor);
        assertEquals("#f59e0b", t2.primaryColor);
    }

    @Test
    public void testThemeRegistryResolution() {
        ThemeData t1 = ThemeRegistry.getTheme("CoreTheme");
        ThemeData t2 = ThemeRegistry.getTheme("Core");
        ThemeData t3 = ThemeRegistry.getTheme("coretheme");
        ThemeData t4 = ThemeRegistry.getTheme("core");

        assertNotNull(t1, "CoreTheme should be resolved by ThemeRegistry");
        assertNotNull(t2, "Core should be resolved by ThemeRegistry");
        assertNotNull(t3, "coretheme (lowercase) should be resolved by ThemeRegistry");
        assertNotNull(t4, "core (lowercase) should be resolved by ThemeRegistry");

        assertEquals("#f59e0b", t1.primaryColor);
        assertEquals("#f59e0b", t2.primaryColor);
        assertEquals("#f59e0b", t3.primaryColor);
        assertEquals("#f59e0b", t4.primaryColor);
    }

    @Test
    public void testThemeChangedIntegration() {
        ThemeChanged widget = ThemeChanged.of().current("Core");
        String html = widget.render(Themes.CoreTheme());

        assertNotNull(html);
        assertTrue(html.contains("CoreTheme") || html.contains("Core"));
        assertTrue(html.contains("⚛️"), "Should contain the Core theme icon ⚛️");
    }

    @Test
    public void testWidgetsRenderingWithCoreTheme() {
        ThemeData core = Themes.CoreTheme();

        Button btn = Button.of("RESET WORLD");
        String btnHtml = btn.render(core);
        assertNotNull(btnHtml);
        assertTrue(btnHtml.contains("<button"));
        assertTrue(btnHtml.contains("espresso-button"));

        Card card = Card.of(Text.of("CONTROLES DE CÁMARA"));
        String cardHtml = card.render(core);
        assertNotNull(cardHtml);
        assertTrue(cardHtml.contains("CONTROLES DE CÁMARA"));

        String globalCss = core.generateGlobalCss();
        assertNotNull(globalCss);
        assertTrue(globalCss.contains("--primary-color: #f59e0b"));
        assertTrue(globalCss.contains("--secondary-color: #38bdf8"));
        assertTrue(globalCss.contains("--background-color: #0b0e14"));
    }
}
