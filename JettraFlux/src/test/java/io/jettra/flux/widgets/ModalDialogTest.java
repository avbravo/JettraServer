package io.jettra.flux.widgets;

import io.jettra.flux.theme.Themes;
import io.jettra.test.annotation.DisplayName;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.Test;

import static io.jettra.test.core.JettraAssert.*;

@NotRequiresRunningServer
public class ModalDialogTest {

    @Test
    @DisplayName("Should render ModalDialog structure with header, body and footer")
    void testModalDialogRender() {
        ModalDialog modal = ModalDialog.of("editDocumentModal")
            .maxWidth("580px")
            .open(false)
            .header(Text.of("Edit Document"))
            .body(Text.of("Document Content Form"))
            .footer(LoadingButton.of("Save Changes (New Version)"));

        String html = modal.render(Themes.FlatTheme());
        assertTrue(html.contains("id=\"editDocumentModal\""));
        assertTrue(html.contains("display:none"));
        assertTrue(html.contains("Edit Document"));
        assertTrue(html.contains("Document Content Form"));
        assertTrue(html.contains("Save Changes (New Version)"));
    }

    @Test
    @DisplayName("Should render open ModalDialog when open is true")
    void testModalDialogOpen() {
        ModalDialog modal = ModalDialog.of("testModal")
            .open(true);

        String html = modal.render(Themes.FlatTheme());
        assertTrue(html.contains("display:flex"));
    }
}
