package io.jettra.aura.core;

import io.jettra.aura.model.IModel;
import java.util.Map;

/**
 * Especialización de {@link AuraPage} optimizada para aplicaciones web modernas.
 * Incluye estilos CSS base inspirados en JettraFlux y soporte nativo para temas Claro/Oscuro.
 */
public class AuraWebPage extends AuraPage {

    public AuraWebPage() {
        super();
        initWebPage();
    }

    public AuraWebPage(IModel<?> model) {
        super(model);
        initWebPage();
    }

    public AuraWebPage(Map<String, String> parameters) {
        super(parameters);
        initWebPage();
    }

    private void initWebPage() {
        // Estilos CSS por defecto integrados con el ecosistema visual de Jettra
    }
}

