package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

public class TextArea extends Widget {
    private String name = "";
    private String value = "";
    private int rows = 4;
    private int cols = 50;
    private String placeholder = "";

    private TextArea() {
    }

    public static TextArea create() {
        return new TextArea();
    }

    public static TextArea of() {
        return new TextArea();
    }

    public static TextArea of(String name) {
        TextArea ta = new TextArea();
        ta.name = name;
        return ta;
    }

    public TextArea name(String name) {
        this.name = name;
        return this;
    }

    public TextArea value(String value) {
        this.value = value;
        return this;
    }

    public TextArea rows(int rows) {
        this.rows = rows;
        return this;
    }

    public TextArea cols(int cols) {
        this.cols = cols;
        return this;
    }
    
    public TextArea placeholder(String placeholder) {
        this.placeholder = placeholder;
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        sb.append("<textarea ").append(renderCommonAttributes(theme, "espresso-textarea form-control"));
        
        if (!name.isEmpty()) {
            sb.append("name=\"").append(name).append("\" ");
        }
        if (!placeholder.isEmpty()) {
            sb.append("placeholder=\"").append(placeholder).append("\" ");
        }
        sb.append("rows=\"").append(rows).append("\" cols=\"").append(cols).append("\">");
        sb.append(value);
        sb.append("</textarea>");
        return sb.toString();
    }
}
