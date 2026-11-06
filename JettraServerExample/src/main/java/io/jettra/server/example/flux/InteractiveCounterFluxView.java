package io.jettra.server.example.flux;

import io.jettra.flux.core.FluxView;
import io.jettra.flux.core.Widget;
import io.jettra.flux.widgets.Button;
import io.jettra.flux.widgets.Div;
import io.jettra.flux.widgets.Text;

/**
 * Vista de demostración interactiva con sincronización en tiempo real vía JettraFlux.
 */
public class InteractiveCounterFluxView extends FluxView {

    private final int count;

    public InteractiveCounterFluxView() {
        this(42);
    }

    public InteractiveCounterFluxView(int count) {
        this.count = count;
    }

    @Override
    public Widget build() {
        return Div.of(
            Div.of(
                Text.of("🎛️ Demostración Interactiva JettraFlux")
            ),
            Div.of(
                Text.of("Valor del Contador Reactivo: " + count)
            ),
            Div.of(
                Button.of("Incrementar (+)"),
                Button.of("Decrementar (-)"),
                Button.of("Reiniciar a Cero")
            ),
            Div.of(
                Button.of("⬅ Volver al Dashboard")
            )
        );
    }
}

