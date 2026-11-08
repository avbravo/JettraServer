package io.jettra.flux.widgets;

/**
 * Convenience alias for {@link Hidden} representing an HTML &lt;input type="hidden"&gt; element in JettraFlux.
 */
public class InputHidden extends Hidden {

    private InputHidden(String name, String value) {
        super(name, value);
    }

    /**
     * Creates a new InputHidden input widget with specified name and value.
     *
     * @param name the form field name
     * @param value the form field value
     * @return a new InputHidden widget instance
     */
    public static InputHidden of(String name, Object value) {
        return new InputHidden(name, value != null ? value.toString() : "");
    }

    /**
     * Creates a new InputHidden input widget with specified name and empty value.
     *
     * @param name the form field name
     * @return a new InputHidden widget instance
     */
    public static InputHidden of(String name) {
        return new InputHidden(name, "");
    }
}
