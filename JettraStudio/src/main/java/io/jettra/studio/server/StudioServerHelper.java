package io.jettra.studio.server;

import io.jettra.studio.core.WebPage;
import io.jettra.server.JettraServer;
import java.util.function.Supplier;

/**
 * Convenience helper for mounting JettraStudio WebPages onto a JettraServer instance.
 */
public class StudioServerHelper {

    public static void mount(JettraServer server, String path, Class<? extends WebPage> pageClass) {
        if (server != null && path != null && pageClass != null) {
            server.addHandler(path, new StudioHandler(pageClass));
        }
    }

    public static void mount(JettraServer server, String path, Supplier<? extends WebPage> pageSupplier) {
        if (server != null && path != null && pageSupplier != null) {
            server.addHandler(path, new StudioHandler(pageSupplier));
        }
    }
}
