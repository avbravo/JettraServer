package io.jettra.flux.widgets;

import io.jettra.flux.theme.Themes;
import io.jettra.test.annotation.DisplayName;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.Test;

import static io.jettra.test.core.JettraAssert.*;

@NotRequiresRunningServer
public class ValidationComponentsTest {

    @Test
    @DisplayName("ValidationState: Verify sealed hierarchy methods and pattern matching")
    void testValidationStateSealedHierarchy() {
        ValidationState valid = ValidationState.valid("Disponible");
        ValidationState invalid = ValidationState.invalid("Nombre de usuario duplicado");
        ValidationState warning = ValidationState.warning("Nombre de usuario similar");
        ValidationState pending = ValidationState.pending();
        ValidationState none = ValidationState.none();

        assertTrue(valid.isValid());
        assertEquals("Disponible", valid.message());

        assertTrue(invalid.isInvalid());
        assertEquals("Nombre de usuario duplicado", invalid.message());

        assertTrue(warning.isWarning());
        assertEquals("Nombre de usuario similar", warning.message());

        assertTrue(pending.isPending());
        assertEquals("Verificando...", pending.message());

        assertTrue(none.isNone());
        assertEquals("", none.message());

        // Pattern matching
        String statusLabel = switch (invalid) {
            case ValidationState.Invalid inv -> "ERROR: " + inv.message();
            case ValidationState.Valid v -> "OK";
            case ValidationState.Warning w -> "WARN";
            case ValidationState.Pending p -> "PENDING";
            case ValidationState.None n -> "NONE";
        };
        assertEquals("ERROR: Nombre de usuario duplicado", statusLabel);
    }

    @Test
    @DisplayName("ValidationMessage: Verify HTML rendering with state and accessibility attributes")
    void testValidationMessageRendering() {
        ValidationMessage errorMsg = ValidationMessage.forInput("username")
            .state(ValidationState.invalid("El nombre de usuario ya existe"));

        String html = errorMsg.render(Themes.Dark());
        assertTrue(html.contains("role=\"alert\""));
        assertTrue(html.contains("aria-live=\"polite\""));
        assertTrue(html.contains("data-for=\"username\""));
        assertTrue(html.contains("is-invalid"));
        assertTrue(html.contains("El nombre de usuario ya existe"));
        assertTrue(html.contains("fa-circle-exclamation"));

        ValidationMessage validMsg = ValidationMessage.of(ValidationState.valid("Usuario disponible"))
            .forField("username");
        String validHtml = validMsg.render(Themes.Dark());
        assertTrue(validHtml.contains("is-valid"));
        assertTrue(validHtml.contains("Usuario disponible"));
        assertTrue(validHtml.contains("fa-circle-check"));

        ValidationMessage hiddenMsg = ValidationMessage.of();
        String hiddenHtml = hiddenMsg.render(Themes.Dark());
        assertTrue(hiddenHtml.contains("display:none;"));
    }

    @Test
    @DisplayName("FeedbackAlert: Verify alert rendering and severity styles")
    void testFeedbackAlertRendering() {
        FeedbackAlert alert = FeedbackAlert.error("Error de Validación", "El usuario ya existe en la base de datos.");
        String html = alert.render(Themes.Dark());

        assertTrue(html.contains("Error de Validación"));
        assertTrue(html.contains("El usuario ya existe en la base de datos."));
        assertTrue(html.contains("INVALID"));
        assertTrue(html.contains("role=\"alert\""));
        assertTrue(html.contains("jettra-feedback-alert"));

        FeedbackAlert successAlert = FeedbackAlert.success("Operación Exitosa", "Usuario aprovisionado con éxito.");
        String successHtml = successAlert.render(Themes.Dark());
        assertTrue(successHtml.contains("Operación Exitosa"));
        assertTrue(successHtml.contains("Usuario aprovisionado con éxito."));
        assertTrue(successHtml.contains("VALID"));
    }

    @Test
    @DisplayName("TextField: Verify withValidationState decorates input with CSS classes and ARIA attributes")
    void testTextFieldValidationDecoration() {
        TextField input = TextField.of("username", "Ingresar usuario")
            .withValidationState(ValidationState.invalid("Usuario no válido"));

        String html = input.render(Themes.Dark());
        assertTrue(html.contains("is-invalid"));
        assertTrue(html.contains("aria-invalid=\"true\""));
        assertTrue(html.contains("name=\"username\""));

        TextField validInput = TextField.of("username")
            .withValidationState(ValidationState.valid("Válido"));
        String validHtml = validInput.render(Themes.Dark());
        assertTrue(validHtml.contains("is-valid"));
        assertFalse(validHtml.contains("aria-invalid=\"true\""));
    }
}
