package io.jettra.flux.core;

import java.util.ArrayList;
import java.util.List;

/**
 * JSBuilder provides a Fluent API to generate JavaScript code dynamically.
 */
public class JSBuilder {
    private final List<String> lines = new ArrayList<>();

    public static JSBuilder create() {
        return new JSBuilder();
    }

    public JSBuilder addFunction(String functionName, String params, String body) {
        lines.add("function " + functionName + "(" + params + ") {\n" + body + "\n}");
        return this;
    }

    public JSBuilder addFunction(String functionName, String body) {
        return addFunction(functionName, "", body);
    }
    
    public JSBuilder addStatement(String statement) {
        lines.add(statement + (statement.endsWith(";") ? "" : ";"));
        return this;
    }

    public JSBuilder addRaw(String rawJs) {
        lines.add(rawJs);
        return this;
    }

    public static String escapeJs(String text) {
        return FluxEscapers.escapeJs(text);
    }

    public static String quote(String text) {
        return FluxEscapers.quoteJs(text);
    }

    public static String escapeHtml(String text) {
        return FluxEscapers.escapeHtml(text);
    }

    public static String escapeAttr(String text) {
        return FluxEscapers.escapeAttr(text);
    }

    public String build() {
        StringBuilder sb = new StringBuilder();
        sb.append("<script>\n");
        for (String line : lines) {
            sb.append(line).append("\n");
        }
        sb.append("</script>\n");
        return sb.toString();
    }
}
