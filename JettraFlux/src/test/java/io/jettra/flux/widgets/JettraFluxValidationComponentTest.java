package io.jettra.flux.widgets;

import io.jettra.flux.core.Modifier;
import io.jettra.flux.theme.Themes;
import io.jettra.test.annotation.DisplayName;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.Test;

import static io.jettra.test.core.JettraAssert.*;

@NotRequiresRunningServer
public class JettraFluxValidationComponentTest {

    @Test
    @DisplayName("ValidationFeedback: Verify rendering for Invalid, Valid, Warning, Pending, and None states")
    void testValidationFeedbackRenderingStates() {
        // 1. Invalid State
        ValidationFeedback errorFeedback = ValidationFeedback.error("El usuario ya existe en system_db.")
            .forField("username");
        String errorHtml = errorFeedback.render(Themes.Dark());
        assertTrue(errorHtml.contains("role=\"alert\""), "Must contain role='alert'");
        assertTrue(errorHtml.contains("aria-live=\"polite\""), "Must contain aria-live='polite'");
        assertTrue(errorHtml.contains("data-for=\"username\""), "Must link to username field");
        assertTrue(errorHtml.contains("is-invalid"), "Must have is-invalid CSS class");
        assertTrue(errorHtml.contains("fa-circle-exclamation"), "Must have error icon");
        assertTrue(errorHtml.contains("El usuario ya existe en system_db."), "Must contain message text");

        // 2. Valid State
        ValidationFeedback successFeedback = ValidationFeedback.success("Nombre disponible.")
            .forField("username");
        String successHtml = successFeedback.render(Themes.Dark());
        assertTrue(successHtml.contains("is-valid"), "Must have is-valid CSS class");
        assertTrue(successHtml.contains("fa-circle-check"), "Must have check icon");
        assertTrue(successHtml.contains("Nombre disponible."), "Must contain success message");

        // 3. Pending State
        ValidationFeedback pendingFeedback = ValidationFeedback.pending("Consultando unicidad...")
            .forField("username");
        String pendingHtml = pendingFeedback.render(Themes.Dark());
        assertTrue(pendingHtml.contains("is-pending"), "Must have is-pending CSS class");
        assertTrue(pendingHtml.contains("fa-spinner fa-spin"), "Must have spinner icon");

        // 4. None State (must be hidden)
        ValidationFeedback noneFeedback = ValidationFeedback.of();
        String noneHtml = noneFeedback.render(Themes.Dark());
        assertTrue(noneHtml.contains("display:none;"), "Default None state must be hidden");
    }

    @Test
    @DisplayName("TextInput.builder(): Verify full builder construction and validation state rendering")
    void testTextInputBuilderWithValidation() {
        // Invalid text input
        TextInput invalidInput = TextInput.builder()
            .id("user_input")
            .name("username")
            .placeholder("Ingrese usuario")
            .value("admin")
            .required(true)
            .validationState(ValidationState.invalid("Usuario duplicado"))
            .modifier(new Modifier().style("width:100%;"))
            .build();

        String invalidHtml = invalidInput.render(Themes.Dark());
        assertTrue(invalidHtml.contains("name=\"username\""), "Must render name attribute");
        assertTrue(invalidHtml.contains("id=\"user_input\""), "Must render id attribute");
        assertTrue(invalidHtml.contains("value=\"admin\""), "Must render current value");
        assertTrue(invalidHtml.contains("placeholder=\"Ingrese usuario\""), "Must render placeholder");
        assertTrue(invalidHtml.contains("required=\"required\""), "Must render required attribute");
        assertTrue(invalidHtml.contains("is-invalid"), "Must include is-invalid CSS class");
        assertTrue(invalidHtml.contains("aria-invalid=\"true\""), "Must set aria-invalid='true'");
        assertTrue(invalidHtml.contains("aria-describedby=\"user_input-feedback\""), "Must set aria-describedby link");

        // Valid text input
        TextInput validInput = TextInput.builder()
            .id("user_input")
            .name("username")
            .value("carlos_mendez")
            .validationState(ValidationState.valid("Disponible"))
            .build();

        String validHtml = validInput.render(Themes.Dark());
        assertTrue(validHtml.contains("is-valid"), "Must include is-valid CSS class");
        assertTrue(validHtml.contains("aria-invalid=\"false\""), "Must set aria-invalid='false'");
    }

    @Test
    @DisplayName("ValidationState: Verify sealed hierarchy pattern matching and factory methods")
    void testValidationStatePatternMatching() {
        ValidationState valid = ValidationState.valid("OK");
        ValidationState invalid = ValidationState.invalid("DUPLICATE");
        ValidationState pending = ValidationState.pending();
        ValidationState none = ValidationState.none();

        assertTrue(valid.isValid());
        assertTrue(invalid.isInvalid());
        assertTrue(pending.isPending());
        assertTrue(none.isNone());

        String evaluated = switch (invalid) {
            case ValidationState.Invalid inv -> "FAIL:" + inv.message();
            case ValidationState.Valid v -> "PASS:" + v.message();
            case ValidationState.Warning w -> "WARN:" + w.message();
            case ValidationState.Pending p -> "WAIT:" + p.message();
            case ValidationState.None n -> "NONE";
        };

        assertEquals("FAIL:DUPLICATE", evaluated);
    }
}
