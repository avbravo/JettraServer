package io.jettra.aura.components;

import io.jettra.aura.core.AuraContainer;
import io.jettra.aura.core.AuraTag;
import io.jettra.aura.model.IModel;
import io.jettra.aura.template.AuraMarkupDocument;
import io.jettra.aura.template.AuraTemplateLocator;
import io.jettra.aura.template.AuraTemplateParser;

/**
 * Componente de panel modular reutilizable (similar a Panel en Apache Wicket).
 * Cada subclase de {@link AuraPanel} puede poseer su propia plantilla HTML homónima
 * (ej. UserCard.java ↔ UserCard.html) y encapsular su propia jerarquía de componentes hijos.
 */
public class AuraPanel extends AuraContainer {

    public AuraPanel(String id) {
        super(id);
    }

    public AuraPanel(String id, IModel<?> model) {
        super(id, model);
    }

    @Override
    protected void onComponentTagBody(AuraTag tag, String markupBody, StringBuilder output) {
        autoBindAnnotatedComponents();

        // 1. Cargar la plantilla HTML propia del panel
        String panelTemplate = AuraTemplateLocator.loadTemplate(getClass());
        if (panelTemplate != null && !panelTemplate.isBlank()) {
            AuraMarkupDocument panelDoc = AuraTemplateParser.parse(panelTemplate);
            output.append(panelDoc.render(this));
        } else if (markupBody != null) {
            output.append(markupBody);
        }
    }
}

