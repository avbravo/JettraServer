package io.jettra.flux.core;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Enrutador de eventos (Actions) para simplificar la validación en el servidor.
 */
public class ActionBinder {
    private final Map<String, String> params;
    private boolean executed = false;

    public ActionBinder(Map<String, String> params) {
        this.params = params;
    }

    /**
     * Define una acción que se ejecutará si params contiene la clave dada.
     */
    public ActionBinder on(String key, Consumer<Map<String, String>> action) {
        if (!executed && params.containsKey(key)) {
            action.accept(params);
            executed = true;
        }
        return this;
    }

    /**
     * Define una acción que se ejecutará si params contiene la clave dada (sin recibir los parámetros).
     */
    public ActionBinder on(String key, Runnable action) {
        if (!executed && params.containsKey(key)) {
            action.run();
            executed = true;
        }
        return this;
    }

    /**
     * Retorna true si alguna acción fue ejecutada.
     */
    public boolean execute() {
        return executed;
    }
}
