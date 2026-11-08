package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

public class TextField extends Widget {
    private final String placeholder;
    private final String name;

    private String value = "";
    private ValidationState validationState = ValidationState.none();

    private TextField(String name, String placeholder) {
        this.name = name;
        this.placeholder = placeholder;
    }

    public static TextField of() {
        return new TextField("", "");
    }

    public static TextField of(String name) {
        return new TextField(name, "");
    }

    public static TextField of(String name, String placeholder) {
        return new TextField(name, placeholder);
    }

    public TextField value(Object value) {
        this.value = value != null ? value.toString() : "";
        return this;
    }

    public TextField withValidationState(ValidationState state) {
        this.validationState = state != null ? state : ValidationState.none();
        return this;
    }

    public ValidationState getValidationState() {
        return validationState;
    }

    @Override
    public TextField binding(String property) {
        super.binding(property);
        return this;
    }

    @Override
    public TextField binding(Object property) {
        super.binding(property);
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        String valAttr = (value != null && !value.isEmpty()) ? " value=\"" + value.replace("\"", "&quot;") + "\"" : "";
        String inputName = modifier.getAttributes().containsKey("name") ? modifier.getAttributes().remove("name") : name;
        String stateClass = validationState.isInvalid() ? " is-invalid" : (validationState.isValid() ? " is-valid" : "");
        String ariaInvalid = validationState.isInvalid() ? " aria-invalid=\"true\"" : "";
        return "<input type=\"text\" name=\"" + inputName + "\"" + valAttr + " placeholder=\"" + placeholder + "\"" + ariaInvalid + " " + renderCommonAttributes(theme, "espresso-textfield form-control" + stateClass) + " />";
    }
}
