package io.jettra.aura.core;

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * Comportamiento modificador de atributos para etiquetas HTML (similar a AttributeModifier en Wicket).
 */
public class AuraTagModifier implements Serializable {

    private final String attributeName;
    private final Supplier<String> valueSupplier;

    public AuraTagModifier(String attributeName, String value) {
        this(attributeName, () -> value);
    }

    public AuraTagModifier(String attributeName, Supplier<String> valueSupplier) {
        this.attributeName = attributeName;
        this.valueSupplier = valueSupplier;
    }

    public static AuraTagModifier of(String attributeName, String value) {
        return new AuraTagModifier(attributeName, value);
    }

    public static AuraTagModifier of(String attributeName, Supplier<String> valueSupplier) {
        return new AuraTagModifier(attributeName, valueSupplier);
    }

    public static AuraTagModifier append(String attributeName, String value) {
        return new AuraTagModifier(attributeName, value) {
            @Override
            public void modify(AuraTag tag) {
                tag.appendAttribute(attributeName, value, " ");
            }
        };
    }

    public void modify(AuraTag tag) {
        if (tag != null && attributeName != null) {
            String val = valueSupplier != null ? valueSupplier.get() : null;
            tag.setAttribute(attributeName, val);
        }
    }

    public String getAttributeName() {
        return attributeName;
    }
}

