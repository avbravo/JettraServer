package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

public class Checkbox extends Widget {
    private final String label;
    private final String name;

    private Checkbox(String name, String label) {
        this.name = name;
        this.label = label;
    }

    public static Checkbox of(String name, String label) {
        return new Checkbox(name, label);
    }

    public static Checkbox of(String label) {
        return new Checkbox(label, label);
    }

    @Override
    public String render(ThemeData theme) {
        String labelStyle = "display: flex; align-items: center; gap: 8px; color: " + theme.onSurfaceColor + "; font-size: 16px;";
        String checkboxStyle = "accent-color: " + theme.primaryColor + "; width: 18px; height: 18px;";
        
        return "<label " + renderCommonAttributes(theme, "espresso-checkbox-label") + " style=\"" + labelStyle + " " + modifier.getStyles() + "\">" +
               "<input type=\"checkbox\" name=\"" + name + "\" style=\"" + checkboxStyle + "\" />" +
               "<span>" + label + "</span>" +
               "</label>";
    }
}
