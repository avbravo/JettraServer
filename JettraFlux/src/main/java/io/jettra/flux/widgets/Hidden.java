package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

/**
 * Component representing an HTML &lt;input type="hidden"&gt; field in JettraFlux.
 * Allows managing form actions, database identifiers, and hidden parameters cleanly
 * using pure Java code without manual HTML strings.
 */
public class Hidden extends Widget {
    private final String name;
    private String value = "";

    protected Hidden(String name, String value) {
        this.name = name;
        this.value = value != null ? value : "";
    }

    /**
     * Creates a new Hidden input widget with specified name and value.
     *
     * @param name the form field name
     * @param value the form field value
     * @return a new Hidden widget instance
     */
    public static Hidden of(String name, Object value) {
        return new Hidden(name, value != null ? value.toString() : "");
    }

    /**
     * Creates a new Hidden input widget with specified name and empty value.
     *
     * @param name the form field name
     * @return a new Hidden widget instance
     */
    public static Hidden of(String name) {
        return new Hidden(name, "");
    }

    /**
     * Sets the value for this hidden field.
     *
     * @param value the value to set
     * @return this widget instance for fluent chaining
     */
    public Hidden value(Object value) {
        this.value = value != null ? value.toString() : "";
        return this;
    }

    @Override
    public Hidden binding(String property) {
        super.binding(property);
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        String valAttr = (value != null) ? " value=\"" + value.replace("\"", "&quot;") + "\"" : " value=\"\"";
        String inputName = (name != null) ? name : "";
        if (modifier.getAttributes().containsKey("name")) {
            inputName = modifier.getAttributes().remove("name");
        }
        return "<input type=\"hidden\" name=\"" + inputName + "\"" + valAttr + " " + renderCommonAttributes(theme, "espresso-hidden") + "/>";
    }
}
