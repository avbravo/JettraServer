package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.ArrayList;
import java.util.List;

public class JettraButton extends Widget {
    public enum Severity {
        PRIMARY, SECONDARY, SUCCESS, INFO, WARNING, HELP, DANGER
    }
    
    public enum Type {
        SOLID, OUTLINED, TEXT
    }

    private final String label;
    private final String icon;
    private Severity severity = Severity.PRIMARY;
    private Type type = Type.SOLID;
    private boolean rounded = false;
    private String actionMethod = null; // Used if wrapped in a form

    private JettraButton(String label, String icon) {
        this.label = label;
        this.icon = icon;
    }

    public static JettraButton of(String label) {
        return new JettraButton(label, null);
    }
    
    public static JettraButton of(String label, String icon) {
        return new JettraButton(label, icon);
    }

    public JettraButton severity(Severity severity) {
        this.severity = severity;
        return this;
    }

    public JettraButton type(Type type) {
        this.type = type;
        return this;
    }

    public JettraButton rounded() {
        this.rounded = true;
        return this;
    }
    
    public JettraButton actionMethod(String actionMethod) {
        this.actionMethod = actionMethod;
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        List<String> classes = new ArrayList<>();
        classes.add("espresso-btn");
        
        switch (severity) {
            case PRIMARY: classes.add("espresso-btn-primary"); break;
            case SECONDARY: classes.add("espresso-btn-secondary"); break;
            case SUCCESS: classes.add("espresso-btn-success"); break;
            case INFO: classes.add("espresso-btn-info"); break;
            case WARNING: classes.add("espresso-btn-warning"); break;
            case HELP: classes.add("espresso-btn-help"); break;
            case DANGER: classes.add("espresso-btn-danger"); break;
        }
        
        if (type == Type.OUTLINED) {
            classes.add("espresso-btn-outlined");
        } else if (type == Type.TEXT) {
            classes.add("espresso-btn-text");
        }
        
        if (rounded) {
            classes.add("espresso-btn-rounded");
        }

        classes.add("btn");
        String cssClass = String.join(" ", classes);
        
        StringBuilder sb = new StringBuilder();
        
        if (actionMethod != null) {
            // Render as submit button with action
            sb.append("<button type=\"submit\" ").append(renderCommonAttributes(theme, cssClass, theme.buttonStyle)).append(">\n");
        } else {
            // Render as standard button
            sb.append("<button type=\"button\" ").append(renderCommonAttributes(theme, cssClass, theme.buttonStyle)).append(">\n");
        }
        
        if (icon != null && !icon.isEmpty()) {
            if (icon.trim().startsWith("<svg")) {
                sb.append("<span style=\"display: inline-flex; align-items: center; margin-right: 0.5rem;\">").append(icon).append("</span>");
            } else {
                sb.append("<i class=\"").append(icon).append("\"></i> ");
            }
        }
        
        if (label != null && !label.isEmpty()) {
            sb.append("<span>").append(label).append("</span>");
        }
        
        sb.append("\n</button>");
        
        if (actionMethod != null) {
            // If action method is specified, we might want to wrap it in a form manually by the user,
            // or we just return the button and the user wraps it in a Form component.
            // Let's keep it pure and just output the button.
        }

        return sb.toString();
    }
}
