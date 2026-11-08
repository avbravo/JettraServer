package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

/**
 * Component representing an HTML &lt;input type="password"&gt; field in JettraFlux.
 */
public class PasswordField extends Widget {
    private final String placeholder;
    private final String name;
    private String value = "";

    private PasswordField(String name, String placeholder) {
        this.name = name;
        this.placeholder = placeholder != null ? placeholder : "";
    }

    /**
     * Creates a new PasswordField with name and placeholder.
     *
     * @param name the input name
     * @param placeholder the input placeholder
     * @return a new PasswordField widget instance
     */
    public static PasswordField of(String name, String placeholder) {
        return new PasswordField(name, placeholder);
    }

    /**
     * Creates a new PasswordField with name and empty placeholder.
     *
     * @param name the input name
     * @return a new PasswordField widget instance
     */
    public static PasswordField of(String name) {
        return new PasswordField(name, "");
    }

    /**
     * Sets the default value of the password input.
     *
     * @param value the value to set
     * @return this widget instance
     */
    public PasswordField value(Object value) {
        this.value = value != null ? value.toString() : "";
        return this;
    }

    @Override
    public PasswordField binding(String property) {
        super.binding(property);
        return this;
    }

    @Override
    public PasswordField binding(Object property) {
        super.binding(property);
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        String valAttr = (value != null && !value.isEmpty()) ? " value=\"" + value.replace("\"", "&quot;") + "\"" : "";
        String inputName = modifier.getAttributes().containsKey("name") ? modifier.getAttributes().remove("name") : name;
        return "<input type=\"password\" name=\"" + inputName + "\"" + valAttr + " placeholder=\"" + placeholder + "\" " + renderCommonAttributes(theme, "espresso-passwordfield form-control") + " />";
    }
}
