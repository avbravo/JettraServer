package io.jettra.server.example.flux;

import io.jettra.flux.core.FluxView;
import io.jettra.flux.core.Widget;
import io.jettra.flux.widgets.Button;
import io.jettra.flux.widgets.Div;
import io.jettra.flux.widgets.Text;

/**
 * Vista reactiva construida en Java puro con JettraFlux.
 * Muestra el estado del subsistema de persistencia de triple modo.
 */
public class StorageMonitorFluxView extends FluxView {

    private final String mode;
    private final String description;

    public StorageMonitorFluxView() {
        this("JVM_RAM", "Memoria RAM Heap estándar");
    }

    public StorageMonitorFluxView(String mode, String description) {
        this.mode = mode;
        this.description = description;
    }

    @Override
    public Widget build() {
        return Div.of(
            Div.of(
                Text.of("⚡ Monitor de Almacenamiento Reactivo - JettraFlux")
            ),
            Div.of(
                Text.of("Modo Persistencia Activo: [" + mode + "]"),
                Text.of(" - " + description)
            ),
            Div.of(
                Text.of("Tecnología: Panama FFM, Arena nativa y Zero-GC Paging")
            ),
            Div.of(
                Button.of("Forzar Sincronización MemTable"),
                Button.of("Ver Métricas en Bruto")
            ),
            Div.of(
                Button.of("⬅ Volver al Dashboard Jakarta EE")
            )
        );
    }
}

