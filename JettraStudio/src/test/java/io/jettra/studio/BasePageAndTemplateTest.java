package io.jettra.studio;

import io.jettra.studio.components.Label;
import io.jettra.studio.core.BasePage;
import io.jettra.studio.i18n.LanguageStudio;
import io.jettra.studio.i18n.LocalizationManager;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.Test;

import static io.jettra.test.core.JettraAssert.*;

@NotRequiresRunningServer
public class BasePageAndTemplateTest {

    public static class ChildDashboardPage extends BasePage {
        public ChildDashboardPage() {
            add(new Label("dashboardTitle", "Panel de Control Operativo"));
        }
    }

    @Test
    public void testBasePageTemplateInheritance() {
        ChildDashboardPage page = new ChildDashboardPage();
        page.setCustomMarkup("<div class=\"child-content\">\n"
            + "  <h2 jettrat:id=\"dashboardTitle\">Titulo por defecto</h2>\n"
            + "  <p><jettrat:message key=\"app.welcome\"/></p>\n"
            + "</div>");

        String html = page.renderPage();
        assertNotNull(html);

        // Header from BasePage template
        assertTrue(html.contains("JettraStudio"));
        assertTrue(html.contains("jettra-base-header"));

        // Child content replaced <jettrat:child/>
        assertTrue(html.contains("child-content"));
        assertTrue(html.contains("Panel de Control Operativo"));

        // i18n interpolation
        assertTrue(html.contains("¡Bienvenido a JettraStudio!"));

        // Theme selector from template
        assertTrue(html.contains("jettra-theme-dropdown-container") || html.contains("<select"));

        // Footer from BasePage template
        assertTrue(html.contains("jettra-base-footer"));
    }

    @Test
    public void testLanguageSwitchingInTemplate() {
        LocalizationManager.getInstance().setCurrentLanguage(LanguageStudio.EN);
        try {
            ChildDashboardPage page = new ChildDashboardPage();
            page.setCustomMarkup("<span><jettrat:message key=\"app.welcome\"/></span>");
            String html = page.renderPage();
            assertTrue(html.contains("Welcome to JettraStudio!"));
        } finally {
            // Restore to Spanish
            LocalizationManager.getInstance().setCurrentLanguage(LanguageStudio.ES);
        }
    }
}
