package io.jettra.server.example.flux;

import io.jettra.flux.core.FluxView;
import io.jettra.flux.core.Widget;
import io.jettra.flux.widgets.Button;
import io.jettra.flux.widgets.Div;
import io.jettra.flux.widgets.Text;

/**
 * Vista reactiva de telemetría y rendimiento del servidor escrita con JettraFlux en Java puro.
 */
public class TelemetryFluxView extends FluxView {

    @Override
    public Widget build() {
        Runtime rt = Runtime.getRuntime();
        long freeMb = rt.freeMemory() / (1024 * 1024);
        long totalMb = rt.totalMemory() / (1024 * 1024);
        int cores = rt.availableProcessors();

        return Div.of(
            Div.of(
                Text.of("📊 Telemetría en Tiempo Real de JettraServer")
            ),
            Div.of(
                Text.of("Concurrencia: Project Loom Virtual Threads Activos"),
                Text.of("Procesadores Disponibles: " + cores),
                Text.of("Memoria Heap: " + (totalMb - freeMb) + " MB / " + totalMb + " MB")
            ),
            Div.of(
                Text.of("Protocolo de Red: NIO Asíncrono con Cold Start < 2ms")
            ),
            Div.of(
                Button.of("Ejecutar Garbage Collection ZGC"),
                Button.of("Descargar Volcado de Hilos Loom")
            ),
            Div.of(
                Button.of("⬅ Volver al Dashboard")
            )
        );
    }
}

