package io.jettra.flux.widgets;

import io.jettra.flux.theme.Themes;
import io.jettra.test.annotation.DisplayName;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.Test;

import static io.jettra.test.core.JettraAssert.*;

@NotRequiresRunningServer
public class JettraRenameDatabaseModalTest {

    @Test
    @DisplayName("Should render JettraRenameDatabaseModal with RENAME DATABASE button, target fields and client helper")
    void testRenameDatabaseModalRender() {
        JettraRenameDatabaseModal modal = JettraRenameDatabaseModal.of("renameDbModal")
            .title("Rename Database")
            .subtitle("Full migration flow: clone all engines, migrate users, and delete origin.")
            .formAction("/databases")
            .actionName("rename_db")
            .oldDbParamName("old_db")
            .newDbParamName("new_db")
            .confirmButtonText("RENAME DATABASE")
            .cancelButtonText("Cancel");

        String html = modal.render(Themes.FlatTheme());

        assertNotNull(html, "Rendered HTML must not be null");
        assertTrue(html.contains("id=\"renameDbModal\""), "Must contain root modal overlay ID");
        assertTrue(html.contains("role=\"dialog\""), "Must declare accessible dialog role");
        assertTrue(html.contains("aria-modal=\"true\""), "Must declare aria-modal attribute");
        assertTrue(html.contains("Rename Database"), "Must render modal title");
        assertTrue(html.contains("Full migration flow"), "Must render explanatory subtitle");
        assertTrue(html.contains("name=\"action\" value=\"rename_db\""), "Must include hidden action parameter");
        assertTrue(html.contains("id=\"renameDbModal_oldDbInput\""), "Must include hidden old_db input");
        assertTrue(html.contains("id=\"renameDbModal_oldDbDisplay\""), "Must include disabled old_db display field");
        assertTrue(html.contains("id=\"renameDbModal_newDbInput\""), "Must include new_db input field");
        assertTrue(html.contains("name=\"new_db\""), "Must submit new_db parameter");
        assertTrue(html.contains("RENAME DATABASE"), "Must render RENAME DATABASE button text in uppercase");
        assertTrue(html.contains("window.JettraRenameDatabaseModal"), "Must embed JettraRenameDatabaseModal JS controller");
        assertTrue(html.contains("JettraRenameDatabaseModal.open"), "Must embed open client method");
        assertTrue(html.contains("JettraRenameDatabaseModal.close"), "Must embed close client method");
    }

    @Test
    @DisplayName("Should support fluent configuration and custom button text")
    void testFluentCustomization() {
        JettraRenameDatabaseModal modal = JettraRenameDatabaseModal.builder("customRenameDialog")
            .title("Clonar y Renombrar Base de Datos")
            .headerBadge("Jettra Cloner")
            .currentDbLabel("Base de Datos Origen:")
            .newDbLabel("Nuevo Nombre de Base de Datos:")
            .confirmButtonText("RENAME DATABASE")
            .confirmButtonIcon("fas fa-exchange-alt");

        assertEquals("customRenameDialog", modal.getDialogId());
        assertEquals("Clonar y Renombrar Base de Datos", modal.getTitle());
        assertEquals("RENAME DATABASE", modal.getConfirmButtonText());

        String html = modal.render(Themes.FlatTheme());
        assertTrue(html.contains("Clonar y Renombrar Base de Datos"));
        assertTrue(html.contains("Jettra Cloner"));
        assertTrue(html.contains("Base de Datos Origen:"));
        assertTrue(html.contains("Nuevo Nombre de Base de Datos:"));
        assertTrue(html.contains("fas fa-exchange-alt"));
    }
}
