package io.jettra.studio.i18n;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe Localization Manager for JettraStudio.
 * Resolves internationalized strings from .properties files by class and locale hierarchy:
 * 1. Specific component bundle: e.g. HomePage_es.properties
 * 2. Class default bundle: e.g. HomePage.properties
 * 3. Application bundle: e.g. messages_es.properties / messages.properties
 */
public class LocalizationManager {

    private static final LocalizationManager INSTANCE = new LocalizationManager();

    private LanguageStudio currentLanguage = LanguageStudio.ES;
    private final Map<String, Properties> bundleCache = new ConcurrentHashMap<>();

    private LocalizationManager() {}

    public static LocalizationManager getInstance() {
        return INSTANCE;
    }

    public LanguageStudio getCurrentLanguage() {
        return currentLanguage;
    }

    public void setCurrentLanguage(LanguageStudio currentLanguage) {
        if (currentLanguage != null) {
            this.currentLanguage = currentLanguage;
        }
    }

    public String getString(String key) {
        return getString(key, key);
    }

    public String getString(String key, String defaultValue) {
        return getString(null, key, currentLanguage.getLocale(), defaultValue);
    }

    public String getString(Class<?> scopeClass, String key, String defaultValue) {
        return getString(scopeClass, key, currentLanguage.getLocale(), defaultValue);
    }

    public String getString(Class<?> scopeClass, String key, Locale locale, String defaultValue) {
        if (key == null) return defaultValue;

        String langCode = (locale != null) ? locale.getLanguage() : currentLanguage.getCode();

        // 1. Try class-scoped bundle (e.g. com/example/HomePage_es.properties)
        if (scopeClass != null) {
            String classBase = scopeClass.getName().replace('.', '/');
            String localizedVal = getBundleValue(classBase + "_" + langCode + ".properties", key);
            if (localizedVal != null) return localizedVal;

            String baseVal = getBundleValue(classBase + ".properties", key);
            if (baseVal != null) return baseVal;
        }

        // 2. Try global application bundles (e.g. messages_es.properties, messages.properties)
        String globalLocalized = getBundleValue("messages_" + langCode + ".properties", key);
        if (globalLocalized != null) return globalLocalized;

        String globalDefault = getBundleValue("messages.properties", key);
        if (globalDefault != null) return globalDefault;

        return defaultValue != null ? defaultValue : key;
    }

    private String getBundleValue(String resourcePath, String key) {
        Properties props = bundleCache.computeIfAbsent(resourcePath, path -> {
            Properties p = new Properties();
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            try (InputStream is = cl.getResourceAsStream(path)) {
                if (is != null) {
                    try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                        p.load(reader);
                    }
                }
            } catch (Exception ignored) {}
            return p;
        });

        return props.getProperty(key);
    }

    /**
     * Clears cached properties bundles (useful during development / hot-reload).
     */
    public void clearCache() {
        bundleCache.clear();
    }
}
