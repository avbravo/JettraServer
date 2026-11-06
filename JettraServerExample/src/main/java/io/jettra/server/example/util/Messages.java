package io.jettra.server.example.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Properties;

/**
 * Gestor centralizado de internacionalización (i18n) para JettraServerExample.
 * Carga las etiquetas desde messages.properties y provee resolución segura con fallbacks.
 */
public class Messages {

    private static final Properties PROPS = new Properties();

    static {
        loadProperties();
    }

    public static synchronized void loadProperties() {
        try (InputStream is = Messages.class.getClassLoader().getResourceAsStream("messages.properties")) {
            if (is != null) {
                try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                    PROPS.load(reader);
                }
            }
        } catch (Exception e) {
            System.err.println("[Messages] Advertencia cargando messages.properties: " + e.getMessage());
        }
    }

    public static String get(String key) {
        return get(key, key);
    }

    public static String get(String key, String defaultValue) {
        String val = PROPS.getProperty(key);
        return (val != null && !val.isBlank()) ? val.trim() : defaultValue;
    }

    public static String format(String key, Object... args) {
        String pattern = get(key, key);
        try {
            return MessageFormat.format(pattern, args);
        } catch (Exception e) {
            return pattern;
        }
    }
}

