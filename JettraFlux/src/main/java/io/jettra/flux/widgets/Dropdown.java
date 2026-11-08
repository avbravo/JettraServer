package io.jettra.flux.widgets;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.Arrays;
import java.util.List;
public class Dropdown extends Widget {
    private final List<String> options;
    private String placeholder = "Select";
    private String selectedValue = null;

    private Dropdown(List<String> options) { this.options = options; }
    public static Dropdown of(String... options) { return new Dropdown(Arrays.asList(options)); }
    public static Dropdown of(List<String> options) { return new Dropdown(options); }
    public Dropdown placeholder(String placeholder) { this.placeholder = placeholder; return this; }
    public Dropdown selected(String selectedValue) { this.selectedValue = selectedValue; return this; }

    @Override public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        sb.append("<select ").append(renderCommonAttributes(theme, "espresso-dropdown form-select")).append(">");
        if (placeholder != null && !placeholder.isEmpty()) {
            sb.append("<option value=\"\" disabled").append(selectedValue == null || selectedValue.isEmpty() ? " selected" : "").append(">").append(placeholder).append("</option>");
        }
        if (options != null) {
            for (String opt : options) {
                boolean isSel = opt != null && opt.equals(selectedValue);
                sb.append("<option value=\"").append(opt).append("\"").append(isSel ? " selected" : "").append(">").append(opt).append("</option>");
            }
        }
        sb.append("</select>");
        return sb.toString();
    }
}
