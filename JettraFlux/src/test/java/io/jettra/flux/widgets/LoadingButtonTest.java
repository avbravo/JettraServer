package io.jettra.flux.widgets;

import io.jettra.flux.theme.Themes;
import io.jettra.test.annotation.DisplayName;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.Test;

import static io.jettra.test.core.JettraAssert.*;

@NotRequiresRunningServer
public class LoadingButtonTest {

    @Test
    @DisplayName("Should render IDLE state with default label and icon")
    void testIdleState() {
        LoadingButton btn = LoadingButton.of("Save Changes (New Version)")
            .idleIcon("fas fa-save")
            .savingLabel("Saving...")
            .savingIcon("fas fa-circle-notch fa-spin");

        assertEquals(LoadingButton.ButtonState.IDLE, btn.getState());
        assertEquals("Save Changes (New Version)", btn.getCurrentLabel());
        assertEquals("fas fa-save", btn.getCurrentIcon());

        String html = btn.render(Themes.FlatTheme());
        assertTrue(html.contains("Save Changes (New Version)"));
        assertTrue(html.contains("fas fa-save"));
        assertTrue(html.contains("data-state=\"idle\""));
        assertFalse(html.contains("disabled=\"disabled\""));
    }

    @Test
    @DisplayName("Should render SAVING state with disabled attribute and spinner icon")
    void testSavingState() {
        LoadingButton btn = LoadingButton.of("Save Changes (New Version)")
            .state(LoadingButton.ButtonState.SAVING)
            .savingLabel("Saving...")
            .savingIcon("fas fa-circle-notch fa-spin");

        assertEquals(LoadingButton.ButtonState.SAVING, btn.getState());
        assertEquals("Saving...", btn.getCurrentLabel());
        assertEquals("fas fa-circle-notch fa-spin", btn.getCurrentIcon());

        String html = btn.render(Themes.FlatTheme());
        assertTrue(html.contains("Saving..."));
        assertTrue(html.contains("fas fa-circle-notch fa-spin"));
        assertTrue(html.contains("data-state=\"saving\""));
        assertTrue(html.contains("disabled=\"disabled\""));
    }

    @Test
    @DisplayName("Should render SUCCESS and ERROR states with appropriate labels")
    void testSuccessAndErrorStates() {
        LoadingButton btnSuccess = LoadingButton.of("Save")
            .successLabel("Version Saved!")
            .state(LoadingButton.ButtonState.SUCCESS);

        String successHtml = btnSuccess.render(Themes.FlatTheme());
        assertTrue(successHtml.contains("Version Saved!"));
        assertTrue(successHtml.contains("data-state=\"success\""));

        LoadingButton btnError = LoadingButton.of("Save")
            .errorLabel("Save Failed")
            .state(LoadingButton.ButtonState.ERROR);

        String errorHtml = btnError.render(Themes.FlatTheme());
        assertTrue(errorHtml.contains("Save Failed"));
        assertTrue(errorHtml.contains("data-state=\"error\""));
    }
}
