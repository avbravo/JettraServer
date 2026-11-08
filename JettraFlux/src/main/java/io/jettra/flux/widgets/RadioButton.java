package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

public class RadioButton extends Widget {
    private String name = "";
    private String value = "";
    private boolean checked = false;
    private Widget label;
    private String onChange = null;

    private RadioButton() {
    }

    public static RadioButton create() {
        return new RadioButton();
    }

    public static RadioButton of(String id, String label) {
        return new RadioButton().name(id).label(label);
    }

    @Override
    public RadioButton id(String id) {
        super.id(id);
        return this;
    }

    public RadioButton name(String name) {
        this.name = name;
        return this;
    }

    public RadioButton value(String value) {
        this.value = value;
        return this;
    }

    public RadioButton checked(boolean checked) {
        this.checked = checked;
        return this;
    }

    public RadioButton onChange(String onChange) {
        this.onChange = onChange;
        return this;
    }

    public RadioButton label(Widget label) {
        this.label = label;
        return this;
    }
    
    public RadioButton label(String text) {
        this.label = Text.of(text);
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"espresso-radio-container\">\n");
        sb.append("  <input type=\"radio\" ").append(renderCommonAttributes(theme, "espresso-radio"));
        
        if (!name.isEmpty()) {
            sb.append("name=\"").append(name).append("\" ");
        }
        if (!value.isEmpty()) {
            sb.append("value=\"").append(value).append("\" ");
        }
        if (checked) {
            sb.append("checked=\"checked\" ");
        }
        if (onChange != null && !onChange.isBlank()) {
            sb.append("onchange=\"").append(onChange).append("\" ");
        }
        sb.append("/>\n");
        
        if (label != null) {
            sb.append("  <label for=\"").append(this.id).append("\">")
              .append(label.render(theme))
              .append("</label>\n");
        }
        sb.append("</div>\n");
        return sb.toString();
    }
}
