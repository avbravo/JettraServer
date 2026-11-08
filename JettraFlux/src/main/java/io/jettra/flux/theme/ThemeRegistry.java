package io.jettra.flux.theme;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ThemeRegistry {
    private static final Map<String, ThemeData> themes = new LinkedHashMap<>();
    private static final Map<String, ThemeData> aliases = new LinkedHashMap<>();

    static {
        try {
            Class.forName("io.jettra.flux.theme.Themes");
        } catch (Exception ignored) {}
        loadDynamicThemes();
    }

    public static void registerTheme(String name, ThemeData theme) {
        themes.put(name, theme);
    }

    public static void registerAlias(String alias, ThemeData theme) {
        aliases.put(alias, theme);
    }

    public static ThemeData getTheme(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        ThemeData direct = themes.get(name);
        if (direct != null) {
            return direct;
        }
        ThemeData aliasDirect = aliases.get(name);
        if (aliasDirect != null) {
            return aliasDirect;
        }
        
        String query = name.trim();
        for (Map.Entry<String, ThemeData> entry : themes.entrySet()) {
            String key = entry.getKey();
            if (key.equalsIgnoreCase(query)) {
                return entry.getValue();
            }
            if (key.equalsIgnoreCase(query + "Theme") || query.equalsIgnoreCase(key + "Theme")) {
                return entry.getValue();
            }
            if (key.endsWith("Theme") && key.substring(0, key.length() - 5).equalsIgnoreCase(query)) {
                return entry.getValue();
            }
            if (query.endsWith("Theme") && query.substring(0, query.length() - 5).equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        for (Map.Entry<String, ThemeData> entry : aliases.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(query)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public static ThemeData getTheme(String name, ColorMode mode) {
        if (mode == null) {
            return getTheme(name);
        }
        JettraTheme jt = JettraTheme.fromName(name);
        if (jt != null) {
            return jt.create(mode);
        }
        return getTheme(name);
    }

    public static ThemeData getTheme(JettraTheme theme) {
        if (theme == null) return null;
        return theme.create();
    }

    public static ThemeData getTheme(JettraTheme theme, ColorMode mode) {
        if (theme == null) return null;
        return mode != null ? theme.create(mode) : theme.create();
    }

    public static String[] getAvailableThemeNames() {
        return themes.keySet().toArray(new String[0]);
    }

    private static void loadDynamicThemes() {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = classLoader.getResources("META-INF/theme.json");
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                try (InputStream is = url.openStream()) {
                    String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    parseAndRegisterTheme(json);
                } catch (Exception e) {
                    System.err.println("[ThemeRegistry] Error loading theme from " + url + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("[ThemeRegistry] Error scanning for themes: " + e.getMessage());
        }
    }

    private static void parseAndRegisterTheme(String json) {
        Map<String, String> map = new HashMap<>();
        // Match "key": "value" ignoring whitespace. Also works with newlines.
        // We use DOTALL so that values can contain newlines (like CSS) if formatted with \n in JSON strings,
        // but since it's JSON, newlines are usually \n escaped. We'll handle standard string content.
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        while (m.find()) {
            // Replace literal \n with actual newline for CSS/JS
            String value = m.group(2).replace("\\n", "\n");
            map.put(m.group(1), value);
        }
        
        String name = map.get("name");
        if (name != null && !name.isEmpty()) {
            ThemeData data = new ThemeData(
                map.getOrDefault("primary", "#ffffff"),
                map.getOrDefault("secondary", "#ffffff"),
                map.getOrDefault("background", "#ffffff"),
                map.getOrDefault("surface", "#ffffff"),
                map.getOrDefault("onPrimary", "#000000"),
                map.getOrDefault("onSurface", "#000000"),
                map.getOrDefault("buttonStyle", ""),
                map.getOrDefault("cardStyle", ""),
                map.getOrDefault("containerStyle", ""),
                map.getOrDefault("textStyle", ""),
                map.getOrDefault("customCss", ""),
                map.getOrDefault("customJs", "")
            );
            registerTheme(name, data);
            System.out.println("[ThemeRegistry] Loaded dynamic plugin theme: " + name);
        }
    }
}
