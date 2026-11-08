package io.jettra.flux.widgets;

import io.jettra.flux.core.Modifier;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

import java.util.ArrayList;
import java.util.List;

/**
 * JettraFluxSelect - Fluent, reactive select/dropdown component for JettraFlux.
 * Provides type-safe option modeling, form binding, onChange handlers, and styling.
 */
public class JettraFluxSelect extends Widget {

    public record Option(String value, String label, boolean selected) {}

    private String name = "";
    private String binding = null;
    private String placeholder = null;
    private String selectedValue = null;
    private String onChangeJs = null;
    private boolean disabled = false;
    private boolean required = false;
    private final List<Option> options = new ArrayList<>();

    private JettraFluxSelect(String name) {
        this.name = name;
        this.binding = name;
    }

    public static JettraFluxSelect of(String name) {
        return new JettraFluxSelect(name);
    }

    public static JettraFluxSelect of(String id, String name) {
        JettraFluxSelect s = new JettraFluxSelect(name);
        s.id(id);
        return s;
    }

    @Override
    public JettraFluxSelect id(String id) {
        super.id(id);
        return this;
    }

    @Override
    public JettraFluxSelect modifier(Modifier modifier) {
        super.modifier(modifier);
        return this;
    }

    public JettraFluxSelect name(String name) {
        this.name = name;
        return this;
    }

    public JettraFluxSelect binding(String binding) {
        this.binding = binding;
        if (this.name == null || this.name.isBlank()) {
            this.name = binding;
        }
        return this;
    }

    public JettraFluxSelect placeholder(String placeholder) {
        this.placeholder = placeholder;
        return this;
    }

    public JettraFluxSelect selected(String selectedValue) {
        this.selectedValue = selectedValue;
        return this;
    }

    public JettraFluxSelect onChange(String onChangeJs) {
        this.onChangeJs = onChangeJs;
        return this;
    }

    public JettraFluxSelect disabled(boolean disabled) {
        this.disabled = disabled;
        return this;
    }

    public JettraFluxSelect required(boolean required) {
        this.required = required;
        return this;
    }

    public JettraFluxSelect addOption(String value, String label) {
        return addOption(value, label, false);
    }

    public JettraFluxSelect addOption(String value, String label, boolean selected) {
        options.add(new Option(value, label, selected));
        if (selected) {
            this.selectedValue = value;
        }
        return this;
    }

    public List<Option> options() {
        return List.copyOf(options);
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();

        sb.append("<select ");
        if (id != null && !id.isBlank()) sb.append("id=\"").append(id).append("\" ");
        if (name != null && !name.isBlank()) sb.append("name=\"").append(name).append("\" ");
        if (binding != null && !binding.isBlank()) sb.append("data-binding=\"").append(binding).append("\" ");
        if (disabled) sb.append("disabled=\"disabled\" ");
        if (required) sb.append("required=\"required\" ");
        if (onChangeJs != null && !onChangeJs.isBlank()) {
            sb.append("onchange=\"").append(onChangeJs).append("\" ");
        }

        String baseClasses = "jettra-flux-select form-select";
        String extraClasses = modifier != null ? modifier.getClasses() : "";
        sb.append("class=\"").append((baseClasses + " " + extraClasses).trim()).append("\" ");

        String defaultStyles = "width:100%; padding:8px 12px; background:var(--j-bg-body, #0f172a); "
                + "border:1px solid var(--j-border, rgba(255,255,255,0.15)); border-radius:6px; "
                + "color:var(--j-text-primary, #f8fafc); font-size:12.5px; outline:none; cursor:pointer;";
        String extraStyles = modifier != null ? modifier.getStyles() : "";
        sb.append("style=\"").append((defaultStyles + " " + extraStyles).trim()).append("\">\n");

        if (placeholder != null && !placeholder.isBlank()) {
            boolean isPlaceholderSelected = (selectedValue == null || selectedValue.isBlank());
            sb.append("  <option value=\"\" disabled")
              .append(isPlaceholderSelected ? " selected" : "")
              .append(">").append(placeholder).append("</option>\n");
        }

        for (Option opt : options) {
            boolean isSelected = (selectedValue != null && selectedValue.equals(opt.value())) || (selectedValue == null && opt.selected());
            sb.append("  <option value=\"").append(opt.value()).append("\"")
              .append(isSelected ? " selected" : "")
              .append(">").append(opt.label()).append("</option>\n");
        }

        sb.append("</select>");
        return sb.toString();
    }
}
