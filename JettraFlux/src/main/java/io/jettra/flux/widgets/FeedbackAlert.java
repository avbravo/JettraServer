package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

/**
 * Modern FeedbackAlert widget in JettraFlux for card/page-level user feedback.
 * Supports title, description, dismissibility, and direct binding to ValidationState.
 */
public class FeedbackAlert extends Widget {

    private String title = "";
    private String message = "";
    private ValidationState state = ValidationState.none();
    private boolean dismissible = false;

    public FeedbackAlert() {
        super();
    }

    public static FeedbackAlert of() {
        return new FeedbackAlert();
    }

    public static FeedbackAlert of(ValidationState state) {
        FeedbackAlert fa = new FeedbackAlert();
        fa.state = state != null ? state : ValidationState.none();
        fa.message = fa.state.message();
        return fa;
    }

    public static FeedbackAlert error(String title, String message) {
        FeedbackAlert fa = new FeedbackAlert();
        fa.title = title != null ? title : "";
        fa.message = message != null ? message : "";
        fa.state = ValidationState.invalid(message);
        return fa;
    }

    public static FeedbackAlert success(String title, String message) {
        FeedbackAlert fa = new FeedbackAlert();
        fa.title = title != null ? title : "";
        fa.message = message != null ? message : "";
        fa.state = ValidationState.valid(message);
        return fa;
    }

    public static FeedbackAlert warning(String title, String message) {
        FeedbackAlert fa = new FeedbackAlert();
        fa.title = title != null ? title : "";
        fa.message = message != null ? message : "";
        fa.state = ValidationState.warning(message);
        return fa;
    }

    public FeedbackAlert title(String title) {
        this.title = title != null ? title : "";
        return this;
    }

    public FeedbackAlert message(String message) {
        this.message = message != null ? message : "";
        return this;
    }

    public FeedbackAlert state(ValidationState state) {
        this.state = state != null ? state : ValidationState.none();
        if (this.message.isEmpty()) {
            this.message = this.state.message();
        }
        return this;
    }

    public FeedbackAlert dismissible(boolean dismissible) {
        this.dismissible = dismissible;
        return this;
    }

    public ValidationState getState() {
        return state;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String render(ThemeData theme) {
        if (state.isNone() && (message == null || message.isBlank())) {
            return "";
        }

        String bgColor;
        String borderColor;
        String textColor;
        String iconClass;
        String badgeClass;

        switch (state) {
            case ValidationState.Invalid inv -> {
                bgColor = "rgba(239, 68, 68, 0.12)";
                borderColor = "rgba(239, 68, 68, 0.4)";
                textColor = "#fca5a5";
                iconClass = "fas fa-shield-halved";
                badgeClass = "badge-raft";
            }
            case ValidationState.Valid v -> {
                bgColor = "rgba(34, 197, 94, 0.12)";
                borderColor = "rgba(34, 197, 94, 0.4)";
                textColor = "#86efac";
                iconClass = "fas fa-check-circle";
                badgeClass = "badge-active";
            }
            case ValidationState.Warning w -> {
                bgColor = "rgba(245, 158, 11, 0.12)";
                borderColor = "rgba(245, 158, 11, 0.4)";
                textColor = "#fde047";
                iconClass = "fas fa-triangle-exclamation";
                badgeClass = "badge-engine";
            }
            case ValidationState.Pending p -> {
                bgColor = "rgba(56, 189, 248, 0.12)";
                borderColor = "rgba(56, 189, 248, 0.4)";
                textColor = "#7dd3fc";
                iconClass = "fas fa-spinner fa-spin";
                badgeClass = "badge-raft";
            }
            case ValidationState.None n -> {
                bgColor = "rgba(30, 41, 59, 0.9)";
                borderColor = "rgba(59, 130, 246, 0.4)";
                textColor = "#f8fafc";
                iconClass = "fas fa-info-circle";
                badgeClass = "badge-active";
            }
        }

        StringBuilder sb = new StringBuilder();
        String elementId = (id != null && !id.isBlank()) ? "id=\"" + id + "\" " : "";

        sb.append("<div ").append(elementId)
          .append("class=\"jettra-feedback-alert\" role=\"alert\" ")
          .append("style=\"background: ").append(bgColor).append("; border: 1px solid ").append(borderColor).append("; padding: 14px 20px; border-radius: 10px; margin-bottom: 20px; display: flex; align-items: center; justify-content: space-between; ")
          .append(modifier != null ? modifier.getStyles() : "").append("\">\n");

        sb.append("  <div style=\"display:flex; align-items:center; gap:12px;\">\n");
        sb.append("    <i class=\"").append(iconClass).append("\" style=\"color:").append(textColor).append("; font-size:18px;\"></i>\n");
        sb.append("    <div>\n");
        if (title != null && !title.isBlank()) {
            sb.append("      <div style=\"font-size:14px; font-weight:700; color:#f8fafc;\">").append(title).append("</div>\n");
        }
        sb.append("      <div style=\"font-size:13px; color:").append(textColor).append("; font-weight:500;\">").append(message).append("</div>\n");
        sb.append("    </div>\n");
        sb.append("  </div>\n");

        if (!title.isBlank()) {
            sb.append("  <span class=\"store-badge ").append(badgeClass).append("\">").append(state.getClass().getSimpleName().toUpperCase()).append("</span>\n");
        }

        sb.append("</div>\n");

        return sb.toString();
    }
}
