package io.jettra.server.core;

import io.jettra.server.core.config.ServerConfig;
import io.jettra.server.core.storage.StorageMode;

/**
 * Punto de entrada por línea de comandos para iniciar JettraServer de manera independiente.
 */
public class JettraServerBootstrap {

    public static void main(String[] args) {
        ServerConfig config = ServerConfig.load();

        // Procesar argumentos de línea de comandos: --port=9090, --storage=OFF_HEAP, etc.
        for (String arg : args) {
            if (arg.startsWith("--port=")) {
                config.setPort(Integer.parseInt(arg.substring("--port=".length()).trim()));
            } else if (arg.startsWith("--storage=")) {
                config.setStorageMode(StorageMode.fromString(arg.substring("--storage=".length()).trim()));
            } else if (arg.startsWith("--host=")) {
                config.setHost(arg.substring("--host=".length()).trim());
            } else if (arg.startsWith("--context=")) {
                config.setContextPath(arg.substring("--context=".length()).trim());
            } else if (arg.startsWith("--data-dir=")) {
                config.setDataDir(arg.substring("--data-dir=".length()).trim());
            }
        }

        try {
            JettraServer server = JettraServer.create(config);
            server.start();

            // Hook para apagado limpio
            Runtime.getRuntime().addShutdownHook(new Thread(server::stop));

            // Mantener el hilo principal vivo si no es daemon
            Thread.currentThread().join();
        } catch (Exception e) {
            System.err.println("[JettraServer] Error fatal durante el arranque: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
