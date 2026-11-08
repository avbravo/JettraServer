package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

/**
 * Contextual validation feedback widget in JettraFlux.
 * Renders inline form field feedback (error, warning, success, or pending indicator)
 * adhering to accessible HTML standards (role="alert" / aria-live="polite").
 */
public class ValidationMessage extends Widget {

    private ValidationState state = ValidationState.none();
    private String forInput = "";
    private boolean showIcon = true;

    public ValidationMessage() {
        super();
    }

    public static ValidationMessage of() {
        return new ValidationMessage();
    }

    public static ValidationMessage of(ValidationState state) {
        ValidationMessage vm = new ValidationMessage();
        vm.state = state != null ? state : ValidationState.none();
        return vm;
    }

    public static ValidationMessage error(String message) {
        return of(ValidationState.invalid(message));
    }

    public static ValidationMessage success(String message) {
        return of(ValidationState.valid(message));
    }

    public static ValidationMessage warning(String message) {
        return of(ValidationState.warning(message));
    }

    public static ValidationMessage forInput(String inputName) {
        ValidationMessage vm = new ValidationMessage();
        vm.forInput = inputName != null ? inputName : "";
        vm.id = inputName + "-feedback";
        return vm;
    }

    public ValidationMessage state(ValidationState state) {
        this.state = state != null ? state : ValidationState.none();
        return this;
    }

    public ValidationMessage forField(String inputName) {
        this.forInput = inputName != null ? inputName : "";
        if (this.id == null || this.id.isBlank()) {
            this.id = inputName + "-feedback";
        }
        return this;
    }

    public ValidationMessage showIcon(boolean show) {
        this.showIcon = show;
        return this;
    }

    public ValidationState getState() {
        return state;
    }

    public String getForInput() {
        return forInput;
    }

    @Override
    public String render(ThemeData theme) {
        String msg = state.message();
        String displayStyle = (state.isNone() || msg == null || msg.isBlank()) ? "display:none;" : "display:flex;";

        String color;
        String iconClass;
        String cssStatus;

        switch (state) {
            case ValidationState.Invalid inv -> {
                color = "#ef4444";
                iconClass = "fas fa-circle-exclamation";
                cssStatus = "is-invalid error-feedback";
            }
            case ValidationState.Valid v -> {
                color = "#22c55e";
                iconClass = "fas fa-circle-check";
                cssStatus = "is-valid success-feedback";
            }
            case ValidationState.Warning w -> {
                color = "#f59e0b";
                iconClass = "fas fa-triangle-exclamation";
                cssStatus = "is-warning warning-feedback";
            }
            case ValidationState.Pending p -> {
                color = "#38bdf8";
                iconClass = "fas fa-spinner fa-spin";
                cssStatus = "is-pending pending-feedback";
            }
            case ValidationState.None n -> {
                color = "#94a3b8";
                iconClass = "";
                cssStatus = "";
            }
        }

        StringBuilder sb = new StringBuilder();
        String elementId = (id != null && !id.isBlank()) ? "id=\"" + id + "\" " : "";
        String forAttr = (forInput != null && !forInput.isBlank()) ? "data-for=\"" + forInput + "\" " : "";

        sb.append("<div ").append(elementId).append(forAttr)
          .append("class=\"jettra-validation-message ").append(cssStatus).append("\" ")
          .append("role=\"alert\" aria-live=\"polite\" ")
          .append("style=\"").append(displayStyle).append(" align-items:center; gap:6px; font-size:12px; margin-top:4px; color:").append(color).append("; ")
          .append(modifier != null ? modifier.getStyles() : "").append("\">\n");

        if (showIcon && !iconClass.isEmpty()) {
            sb.append("  <i class=\"").append(iconClass).append("\"></i>\n");
        }
        sb.append("  <span class=\"jettra-validation-text\">").append(msg != null ? msg : "").append("</span>\n");
        sb.append("</div>\n");

        return sb.toString();
    }
}
