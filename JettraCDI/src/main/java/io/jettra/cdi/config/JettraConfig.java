package io.jettra.cdi.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestor unificado de configuración para Jettra.
 * Soporta de forma nativa:
 * - jettra-config.properties
 * - jettra-rest.properties
 * - messages.properties (y variantes localizadas messages_{lang}.properties)
 * - Variables de entorno del sistema y propiedades de la JVM.
 */
public class JettraConfig {

    private static final Properties properties = new Properties();
    private static final Map<String, Properties> namedPropertiesCache = new ConcurrentHashMap<>();
    private static final JettraConfig INSTANCE = new JettraConfig();

    public static JettraConfig getInstance() {
        return INSTANCE;
    }

    public static <T> T getValue(String key, Class<T> targetType) {
        return getValue(key, targetType, null);
    }

    public <T> Optional<T> getOptional(String key, Class<T> targetType) {
        return Optional.ofNullable(getValue(key, targetType, null));
    }

    static {
        loadAll();
    }

    public static synchronized void loadAll() {
        // 1. Cargar jettra-config.properties
        loadResource("jettra-config.properties");

        // 2. Cargar jettra-rest.properties
        loadResource("jettra-rest.properties");

        // 3. Cargar messages.properties
        loadResource("messages.properties");

        // 4. Cargar idioma por defecto si existe (ej. messages_es.properties o messages_en.properties)
        String lang = getProperty("app.language", System.getProperty("user.language", "es"));
        loadResource("messages_" + lang + ".properties");
    }

    public static boolean loadResource(String resourceName) {
        if (resourceName == null || resourceName.isBlank()) return false;
        boolean loaded = false;

        // 1. Buscar en ClassLoader
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) cl = JettraConfig.class.getClassLoader();

        try (InputStream is = cl.getResourceAsStream(resourceName)) {
            if (is != null) {
                Properties p = new Properties();
                p.load(new java.io.InputStreamReader(is, StandardCharsets.UTF_8));
                properties.putAll(p);
                namedPropertiesCache.put(resourceName, p);
                loaded = true;
            }
        } catch (Exception ignored) {}

        // 2. Buscar en sistema de archivos (raíz o config/)
        if (!loaded) {
            String[] candidatePaths = new String[]{
                    resourceName,
                    "config/" + resourceName,
                    "src/main/resources/" + resourceName
            };
            for (String path : candidatePaths) {
                File f = new File(path);
                if (f.exists() && f.isFile()) {
                    try (InputStream fis = new FileInputStream(f)) {
                        Properties p = new Properties();
                        p.load(new java.io.InputStreamReader(fis, StandardCharsets.UTF_8));
                        properties.putAll(p);
                        namedPropertiesCache.put(resourceName, p);
                        loaded = true;
                        break;
                    } catch (Exception ignored) {}
                }
            }
        }
        return loaded;
    }

    public static Properties loadNamedBundle(String bundleName, String lang) {
        String baseName = bundleName != null ? bundleName.replace(".properties", "") : "messages";
        String targetName = (lang != null && !lang.isBlank()) ? baseName + "_" + lang + ".properties" : baseName + ".properties";

        if (namedPropertiesCache.containsKey(targetName)) {
            return namedPropertiesCache.get(targetName);
        }

        Properties p = new Properties();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) cl = JettraConfig.class.getClassLoader();

        try (InputStream is = cl.getResourceAsStream(targetName)) {
            if (is != null) {
                p.load(new java.io.InputStreamReader(is, StandardCharsets.UTF_8));
                namedPropertiesCache.put(targetName, p);
                return p;
            }
        } catch (Exception ignored) {}

        // Fallback a base sin sufijo
        try (InputStream isDef = cl.getResourceAsStream(baseName + ".properties")) {
            if (isDef != null) {
                p.load(new java.io.InputStreamReader(isDef, StandardCharsets.UTF_8));
                namedPropertiesCache.put(targetName, p);
                return p;
            }
        } catch (Exception ignored) {}

        return p;
    }

    public static String getProperty(String key) {
        if (key == null) return null;
        // 1. System Property
        String sys = System.getProperty(key);
        if (sys != null && !sys.isBlank()) return sys;

        // 2. Environment Variable
        String envKey = key.replace('.', '_').replace('-', '_').toUpperCase();
        String env = System.getenv(envKey);
        if (env != null && !env.isBlank()) return env;

        // 3. Properties cargadas
        return properties.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        String val = getProperty(key);
        return (val != null && !val.isBlank()) ? val : defaultValue;
    }

    public static void setProperty(String key, String value) {
        if (key != null) {
            if (value != null) {
                properties.setProperty(key, value);
            } else {
                properties.remove(key);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> getOptionalValue(String key, Class<T> type) {
        String val = getProperty(key);
        if (val == null || val.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable((T) convertValue(val, type));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static <T> T getValue(String key, Class<T> type, T defaultValue) {
        return getOptionalValue(key, type).orElse(defaultValue);
    }

    @SuppressWarnings("unchecked")
    public static Object convertValue(String val, Class<?> type) {
        if (val == null) return null;
        val = val.trim();
        if (type == String.class || type == Object.class) return val;
        if (type == Integer.class || type == int.class) return Integer.parseInt(val);
        if (type == Long.class || type == long.class) return Long.parseLong(val);
        if (type == Double.class || type == double.class) return Double.parseDouble(val);
        if (type == Float.class || type == float.class) return Float.parseFloat(val);
        if (type == Boolean.class || type == boolean.class) return Boolean.parseBoolean(val);
        if (type == Short.class || type == short.class) return Short.parseShort(val);
        if (type == Byte.class || type == byte.class) return Byte.parseByte(val);
        return val;
    }

    public static Properties getProperties() {
        return properties;
    }
}
