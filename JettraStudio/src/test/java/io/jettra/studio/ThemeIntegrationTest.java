package io.jettra.studio;

import io.jettra.flux.theme.ColorMode;
import io.jettra.flux.theme.ThemeData;
import io.jettra.studio.components.Label;
import io.jettra.studio.core.WebPage;
import io.jettra.studio.theme.StudioThemeManager;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.Test;

import static io.jettra.test.core.JettraAssert.*;

@NotRequiresRunningServer
public class ThemeIntegrationTest {

    public static class ThemedPage extends WebPage {
        public ThemedPage() {
            add(new Label("title", "Pantalla de Demostración"));
        }
    }

    @Test
    public void testGamesThemeInjectionIntoHead() {
        ThemedPage page = new ThemedPage();
        page.setCurrentThemeName("Games");
        page.setCurrentColorMode(ColorMode.DARK);
        page.setCustomMarkup("<!DOCTYPE html><html><head><title>Games Demo</title></head><body><h1 jettrat:id=\"title\">Texto</h1></body></html>");

        String html = page.renderPage();
        assertNotNull(html);

        // Verify head contains theme CSS & JS
        assertTrue(html.contains("<style>"));
        assertTrue(html.contains("--primary-color: #ffd700;"));
        assertTrue(html.contains("games-perspective-grid-canvas"));
        assertTrue(html.contains("Pantalla de Demostración"));
    }

    @Test
    public void testPoliceThemeResolution() {
        StudioThemeManager mgr = StudioThemeManager.getInstance();
        ThemeData police = mgr.resolveTheme("Police", ColorMode.DARK);
        assertNotNull(police);
        assertEquals("#ffd700", police.primaryColor);
        assertEquals("#090d16", police.backgroundColor);
    }

    @Test
    public void testWhiteModeResolution() {
        StudioThemeManager mgr = StudioThemeManager.getInstance();
        ThemeData whiteGames = mgr.resolveTheme("Games", ColorMode.WHITE);
        assertNotNull(whiteGames);
        assertEquals(ColorMode.WHITE, whiteGames.getColorMode());
        assertEquals("#d97706", whiteGames.primaryColor);
    }
}
