package io.jettra.flux.widgets;

/**
 * JettraMultiSelect - Alias and framework standard for MultiSelect in JettraFlux.
 */
public class JettraMultiSelect extends MultiSelect {

    protected JettraMultiSelect(String name) {
        super(name);
    }

    public static JettraMultiSelect of(String name) {
        return new JettraMultiSelect(name);
    }

    public static JettraMultiSelect of(String id, String name) {
        JettraMultiSelect ms = new JettraMultiSelect(name);
        ms.id(id);
        return ms;
    }
}
