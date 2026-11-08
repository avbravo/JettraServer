package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

import java.util.ArrayList;
import java.util.List;

/**
 * LoadingButton component implementing the State Pattern for asynchronous operations.
 * States: IDLE, SAVING, SUCCESS, ERROR.
 * Pure Java typed component in JettraFlux.
 */
public class LoadingButton extends Widget {

    public enum ButtonState {
        IDLE,
        SAVING,
        SUCCESS,
        ERROR
    }

    private ButtonState state = ButtonState.IDLE;
    private String idleLabel = "Save";
    private String savingLabel = "Saving...";
    private String successLabel = "Saved!";
    private String errorLabel = "Error";
    private String idleIcon = "fas fa-save";
    private String savingIcon = "fas fa-circle-notch fa-spin";
    private String successIcon = "fas fa-check";
    private String errorIcon = "fas fa-exclamation-triangle";
    private String onClickAction = "";
    private String buttonVariant = "btn-primary";
    private String customBgColor = "";
    private int timeoutMs = 10000;
    private final List<Widget> extraChildren = new ArrayList<>();

    private LoadingButton(String idleLabel) {
        this.idleLabel = idleLabel;
    }

    public static LoadingButton of(String idleLabel) {
        return new LoadingButton(idleLabel);
    }

    public static LoadingButton of(String idleLabel, String idleIcon) {
        LoadingButton btn = new LoadingButton(idleLabel);
        btn.idleIcon = idleIcon;
        return btn;
    }

    public LoadingButton state(ButtonState state) {
        this.state = state != null ? state : ButtonState.IDLE;
        return this;
    }

    public ButtonState getState() {
        return state;
    }

    public LoadingButton savingLabel(String savingLabel) {
        this.savingLabel = savingLabel;
        return this;
    }

    public LoadingButton successLabel(String successLabel) {
        this.successLabel = successLabel;
        return this;
    }

    public LoadingButton errorLabel(String errorLabel) {
        this.errorLabel = errorLabel;
        return this;
    }

    public LoadingButton idleIcon(String idleIcon) {
        this.idleIcon = idleIcon;
        return this;
    }

    public LoadingButton savingIcon(String savingIcon) {
        this.savingIcon = savingIcon;
        return this;
    }

    public LoadingButton onClickAction(String jsHandler) {
        this.onClickAction = jsHandler;
        return this;
    }

    public LoadingButton variant(String variantClass) {
        this.buttonVariant = variantClass;
        return this;
    }

    public LoadingButton backgroundColor(String color) {
        this.customBgColor = color;
        return this;
    }

    public LoadingButton timeout(int timeoutMillis) {
        this.timeoutMs = timeoutMillis;
        return this;
    }

    public LoadingButton child(Widget child) {
        if (child != null) this.extraChildren.add(child);
        return this;
    }

    public String getCurrentLabel() {
        return switch (state) {
            case IDLE -> idleLabel;
            case SAVING -> savingLabel;
            case SUCCESS -> successLabel;
            case ERROR -> errorLabel;
        };
    }

    public String getCurrentIcon() {
        return switch (state) {
            case IDLE -> idleIcon;
            case SAVING -> savingIcon;
            case SUCCESS -> successIcon;
            case ERROR -> errorIcon;
        };
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        boolean disabled = state == ButtonState.SAVING;

        String styleAttr = modifier.getStyles();
        if (customBgColor != null && !customBgColor.isBlank()) {
            styleAttr = (styleAttr != null && !styleAttr.isBlank() ? styleAttr + " " : "") + "background: " + customBgColor + ";";
        }

        sb.append("<button type=\"button\" ");
        sb.append("id=\"").append(id).append("\" ");
        sb.append("class=\"espresso-button btn ").append(buttonVariant).append(" ")
          .append(modifier.getClasses()).append("\" ");
        
        sb.append("data-state=\"").append(state.name().toLowerCase()).append("\" ");
        sb.append("data-idle-label=\"").append(escapeHtml(idleLabel)).append("\" ");
        sb.append("data-saving-label=\"").append(escapeHtml(savingLabel)).append("\" ");
        sb.append("data-idle-icon=\"").append(escapeHtml(idleIcon)).append("\" ");
        sb.append("data-saving-icon=\"").append(escapeHtml(savingIcon)).append("\" ");
        sb.append("data-timeout=\"").append(timeoutMs).append("\" ");

        if (disabled) {
            sb.append("disabled=\"disabled\" ");
        }

        if (onClickAction != null && !onClickAction.isBlank() && !disabled) {
            sb.append("onclick=\"").append(onClickAction).append("\" ");
        }

        // Add any modifier attributes
        modifier.getAttributes().forEach((k, v) -> {
            if (!k.equals("type") && !k.equals("id") && !k.equals("class") && !k.equals("onclick")) {
                sb.append(k).append("=\"").append(v).append("\" ");
            }
        });

        if (styleAttr != null && !styleAttr.isBlank()) {
            sb.append("style=\"").append(styleAttr).append("\" ");
        }
        sb.append(">\n");

        // Icon
        String iconClass = getCurrentIcon();
        if (iconClass != null && !iconClass.isBlank()) {
            sb.append("  <i class=\"").append(iconClass).append(" btn-icon\" style=\"margin-right:6px;\"></i>\n");
        }

        // Label
        sb.append("  <span class=\"btn-text\">").append(escapeHtml(getCurrentLabel())).append("</span>\n");

        // Extra children
        for (Widget child : extraChildren) {
            sb.append(child.render(theme));
        }

        sb.append("</button>\n");
        return sb.toString();
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
