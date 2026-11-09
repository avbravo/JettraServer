package io.jettra.aura.core;

import java.io.Serializable;

/**
 * Listener funcional para el manejo de eventos de componentes en JettraAura.
 */
@FunctionalInterface
public interface AuraEventListener extends Serializable {

    void onEvent(AuraEvent event);
}

