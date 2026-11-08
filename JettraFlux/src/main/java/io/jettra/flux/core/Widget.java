package io.jettra.flux.core;

import io.jettra.flux.theme.ThemeData;
import java.util.UUID;

/**
 * The base class for all UI elements in JettraEspressoUI.
 * Inspired by Flutter's Widget and Jetpack Compose's @Composable.
 */
public abstract class Widget implements JettraComponent {
    protected String id;
    protected Modifier modifier = new Modifier();
    protected java.util.function.Consumer<Object> onClick;

    public Widget() {
        this.id = "esp-" + UUID.randomUUID().toString().substring(0, 8);
    }

    public Widget modifier(Modifier modifier) {
        if (this.modifier != null && modifier != null) {
            java.util.Map<String, String> existingAttrs = this.modifier.getAttributes();
            if (!existingAttrs.isEmpty()) {
                for (java.util.Map.Entry<String, String> entry : existingAttrs.entrySet()) {
                    if (!modifier.getAttributes().containsKey(entry.getKey())) {
                        modifier.attribute(entry.getKey(), entry.getValue());
                    }
                }
            }
        }
        this.modifier = modifier;
        return this;
    }

    public Widget attribute(String key, String value) {
        this.modifier.attribute(key, value);
        return this;
    }

    public Widget id(String id) {
        this.id = id;
        return this;
    }

    public Widget binding(String property) {
        if (property != null && !property.isEmpty()) {
            this.modifier.attribute("data-bind", property);
            this.modifier.attribute("name", property);
            if (this.id == null || this.id.startsWith("esp-")) {
                this.id = property;
            }
        }
        return this;
    }

    public Widget binding(Class<?> modelClass, String property) {
        binding(property);
        if (modelClass != null && property != null && !property.isEmpty()) {
            java.util.Map<String, String> htmlAttrs = io.jettra.rules.core.JettraRulesWebEngine.getHtmlAttributes(modelClass, property);
            for (java.util.Map.Entry<String, String> entry : htmlAttrs.entrySet()) {
                if (!this.modifier.getAttributes().containsKey(entry.getKey())) {
                    this.modifier.attribute(entry.getKey(), entry.getValue());
                }
            }
        }
        return this;
    }

    public Widget binding(Object property) {
        if (property != null) {
            return binding(property.toString());
        }
        return this;
    }

    public Widget value(Object value) {
        if (value != null) {
            this.modifier.attribute("value", value.toString());
        }
        return this;
    }

    public Widget onClick(java.util.function.Consumer<Object> onClick) {
        this.onClick = onClick;
        return this;
    }

    public String getId() {
        return id;
    }

    /**
     * Renders this widget to an HTML string using the provided theme.
     */
    public abstract String render(ThemeData theme);

    /**
     * Helper to render common attributes like id, classes, styles, and onClick handlers.
     */
    protected String renderCommonAttributes(ThemeData theme, String defaultClasses) {
        return renderCommonAttributes(theme, defaultClasses, "");
    }

    protected String renderCommonAttributes(ThemeData theme, String defaultClasses, String defaultStyles) {
        StringBuilder sb = new StringBuilder();
        sb.append("id=\"").append(id).append("\" ");
        
        String finalClasses = defaultClasses + " " + modifier.getClasses();
        if (!finalClasses.trim().isEmpty()) {
            sb.append("class=\"").append(finalClasses.trim()).append("\" ");
        }

        String combinedStyles = ((defaultStyles != null ? defaultStyles : "") + " " + modifier.getStyles()).trim();
        if (!combinedStyles.isEmpty()) {
            sb.append("style=\"").append(combinedStyles).append("\" ");
        }

        if (onClick != null) {
            sb.append("onclick=\"espressoFire('").append(id).append("')\" ");
        }
        
        for (java.util.Map.Entry<String, String> entry : modifier.getAttributes().entrySet()) {
            sb.append(entry.getKey()).append("=\"").append(entry.getValue()).append("\" ");
        }

        return sb.toString();
    }
}
