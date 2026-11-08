package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

/**
 * Strongly typed visual feedback widget in JettraFlux for displaying
 * form field validation status, errors, warnings, and pending progress.
 * Adheres to accessibility standards (role="alert", aria-live="polite").
 */
public class ValidationFeedback extends Widget {

    private ValidationState state = ValidationState.none();
    private String forField = "";
    private boolean showIcon = true;

    public ValidationFeedback() {
        super();
    }

    public static ValidationFeedback of() {
        return new ValidationFeedback();
    }

    public static ValidationFeedback of(ValidationState state) {
        ValidationFeedback vf = new ValidationFeedback();
        vf.state = state != null ? state : ValidationState.none();
        return vf;
    }

    public static ValidationFeedback forInput(String fieldName) {
        ValidationFeedback vf = new ValidationFeedback();
        vf.forField = fieldName != null ? fieldName : "";
        vf.id = (fieldName != null ? fieldName : "") + "-feedback";
        return vf;
    }

    public static ValidationFeedback error(String message) {
        return of(ValidationState.invalid(message));
    }

    public static ValidationFeedback success(String message) {
        return of(ValidationState.valid(message));
    }

    public static ValidationFeedback warning(String message) {
        return of(ValidationState.warning(message));
    }

    public static ValidationFeedback pending(String message) {
        return of(ValidationState.pending(message));
    }

    public ValidationFeedback state(ValidationState state) {
        this.state = state != null ? state : ValidationState.none();
        return this;
    }

    public ValidationFeedback forField(String fieldName) {
        this.forField = fieldName != null ? fieldName : "";
        if (this.id == null || this.id.isBlank()) {
            this.id = fieldName + "-feedback";
        }
        return this;
    }

    public ValidationFeedback showIcon(boolean show) {
        this.showIcon = show;
        return this;
    }

    public ValidationState getState() {
        return state;
    }

    public String getForField() {
        return forField;
    }

    @Override
    public String render(ThemeData theme) {
        String msg = state != null ? state.message() : "";
        boolean isHidden = state == null || state.isNone() || msg == null || msg.isBlank();
        String displayStyle = isHidden ? "display:none;" : "display:flex;";

        String color;
        String iconClass;
        String cssStatus;

        ValidationState currentState = state != null ? state : ValidationState.none();
        switch (currentState) {
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
        String forAttr = (forField != null && !forField.isBlank()) ? "data-for=\"" + forField + "\" " : "";

        sb.append("<div ").append(elementId).append(forAttr)
          .append("class=\"jettra-validation-feedback jettra-validation-message ").append(cssStatus).append("\" ")
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
