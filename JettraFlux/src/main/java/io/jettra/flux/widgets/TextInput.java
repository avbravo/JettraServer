package io.jettra.flux.widgets;

import io.jettra.flux.core.Modifier;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

import java.util.HashMap;
import java.util.Map;

/**
 * Idiomatic JettraFlux text input component supporting modern builder patterns,
 * direct {@link ValidationState} decoration, and accessible HTML rendering.
 */
public class TextInput extends Widget {

    private final String name;
    private final String placeholder;
    private String value = "";
    private ValidationState validationState = ValidationState.none();
    private boolean required = false;
    private boolean readOnly = false;
    private boolean disabled = false;
    private String inputType = "text";
    private final Map<String, String> customAttributes = new HashMap<>();

    private TextInput(Builder builder) {
        this.id = builder.id;
        this.name = builder.name != null ? builder.name : "";
        this.placeholder = builder.placeholder != null ? builder.placeholder : "";
        this.value = builder.value != null ? builder.value : "";
        this.validationState = builder.validationState != null ? builder.validationState : ValidationState.none();
        this.required = builder.required;
        this.readOnly = builder.readOnly;
        this.disabled = builder.disabled;
        this.inputType = builder.inputType != null ? builder.inputType : "text";
        if (builder.modifier != null) {
            this.modifier = builder.modifier;
        }
        this.customAttributes.putAll(builder.customAttributes);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static TextInput of() {
        return builder().build();
    }

    public static TextInput of(String name) {
        return builder().name(name).build();
    }

    public static TextInput of(String name, String placeholder) {
        return builder().name(name).placeholder(placeholder).build();
    }

    public TextInput value(Object value) {
        this.value = value != null ? value.toString() : "";
        return this;
    }

    public TextInput validationState(ValidationState state) {
        this.validationState = state != null ? state : ValidationState.none();
        return this;
    }

    public ValidationState getValidationState() {
        return validationState;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public boolean isRequired() {
        return required;
    }

    @Override
    public TextInput attribute(String key, String val) {
        super.attribute(key, val);
        this.customAttributes.put(key, val);
        return this;
    }

    @Override
    public TextInput modifier(Modifier modifier) {
        super.modifier(modifier);
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        sb.append("<input type=\"").append(inputType).append("\" ");

        String inputName = modifier.getAttributes().containsKey("name") ? modifier.getAttributes().remove("name") : name;
        if (inputName != null && !inputName.isBlank()) {
            sb.append("name=\"").append(escapeAttr(inputName)).append("\" ");
        }

        if (id != null && !id.isBlank()) {
            sb.append("id=\"").append(escapeAttr(id)).append("\" ");
        }

        if (value != null && !value.isEmpty()) {
            sb.append("value=\"").append(escapeAttr(value)).append("\" ");
        }

        if (placeholder != null && !placeholder.isEmpty()) {
            sb.append("placeholder=\"").append(escapeAttr(placeholder)).append("\" ");
        }

        if (required) {
            sb.append("required=\"required\" ");
        }
        if (readOnly) {
            sb.append("readonly=\"readonly\" ");
        }
        if (disabled) {
            sb.append("disabled=\"disabled\" ");
        }

        // Accessibility & validation state decoration
        String stateClass = "";
        if (validationState.isInvalid()) {
            stateClass = " is-invalid";
            sb.append("aria-invalid=\"true\" ");
        } else if (validationState.isValid()) {
            stateClass = " is-valid";
            sb.append("aria-invalid=\"false\" ");
        } else if (validationState.isPending()) {
            stateClass = " is-pending";
        }

        if (id != null && !id.isBlank()) {
            sb.append("aria-describedby=\"").append(escapeAttr(id)).append("-feedback\" ");
        }

        for (Map.Entry<String, String> entry : customAttributes.entrySet()) {
            if (!"name".equals(entry.getKey()) && !"id".equals(entry.getKey()) && !"type".equals(entry.getKey()) && !"value".equals(entry.getKey())) {
                sb.append(entry.getKey()).append("=\"").append(escapeAttr(entry.getValue())).append("\" ");
            }
        }

        String cssClasses = "espresso-textfield form-control jettra-text-input" + stateClass;
        sb.append(renderCommonAttributes(theme, cssClasses));
        sb.append(" />");

        return sb.toString();
    }

    private String escapeAttr(String val) {
        if (val == null) return "";
        return val.replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public static class Builder {
        private String id;
        private String name = "";
        private String placeholder = "";
        private String value = "";
        private ValidationState validationState = ValidationState.none();
        private boolean required = false;
        private boolean readOnly = false;
        private boolean disabled = false;
        private String inputType = "text";
        private Modifier modifier;
        private final Map<String, String> customAttributes = new HashMap<>();

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder placeholder(String placeholder) {
            this.placeholder = placeholder;
            return this;
        }

        public Builder value(Object value) {
            this.value = value != null ? value.toString() : "";
            return this;
        }

        public Builder validationState(ValidationState state) {
            this.validationState = state != null ? state : ValidationState.none();
            return this;
        }

        public Builder required(boolean required) {
            this.required = required;
            return this;
        }

        public Builder readOnly(boolean readOnly) {
            this.readOnly = readOnly;
            return this;
        }

        public Builder disabled(boolean disabled) {
            this.disabled = disabled;
            return this;
        }

        public Builder inputType(String inputType) {
            this.inputType = inputType != null ? inputType : "text";
            return this;
        }

        public Builder modifier(Modifier modifier) {
            this.modifier = modifier;
            return this;
        }

        public Builder attribute(String key, String val) {
            if (key != null && val != null) {
                this.customAttributes.put(key, val);
            }
            return this;
        }

        public TextInput build() {
            return new TextInput(this);
        }
    }
}
