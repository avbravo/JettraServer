package io.jettra.aura.template;

import io.jettra.aura.annotation.AuraTemplate;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Localizador y cargador de plantillas HTML para componentes y páginas JettraAura.
 * Aplica convención 1:1 Index.java busca Index.html en el mismo paquete,
 * en resources/templates, resources/webapp o en la ruta especificada por @AuraTemplate.
 */
public class AuraTemplateLocator {

    private static final Map<Class<?>, String> TEMPLATE_CACHE = new ConcurrentHashMap<>();

    private static final java.util.regex.Pattern EXTEND_PATTERN = java.util.regex.Pattern.compile(
        "<(?:aura|j):extend>(.*?)</(?:aura|j):extend>",
        java.util.regex.Pattern.DOTALL | java.util.regex.Pattern.CASE_INSENSITIVE
    );

    private static final java.util.regex.Pattern CHILD_PATTERN = java.util.regex.Pattern.compile(
        "<(?:aura|j):child\\s*(?:/>|>\\s*</(?:aura|j):child>)",
        java.util.regex.Pattern.DOTALL | java.util.regex.Pattern.CASE_INSENSITIVE
    );

    private static final java.util.regex.Pattern TITLE_PATTERN = java.util.regex.Pattern.compile(
        "<title>(.*?)</title>",
        java.util.regex.Pattern.DOTALL | java.util.regex.Pattern.CASE_INSENSITIVE
    );

    public static String loadTemplate(Class<?> componentClass) {
        if (componentClass == null) return null;

        return TEMPLATE_CACHE.computeIfAbsent(componentClass, clazz -> {
            String raw = doFindTemplate(clazz);
            if (raw != null) {
                return processTemplateInheritance(raw, clazz);
            }
            return createDefaultTemplate(clazz);
        });
    }

    private static String doFindTemplate(Class<?> clazz) {
        // 1. Verificar anotación @AuraTemplate explícita
        AuraTemplate annotation = clazz.getAnnotation(AuraTemplate.class);
        if (annotation != null && !annotation.value().isBlank()) {
            String path = annotation.value().trim();
            String content = readFromClasspathOrFile(path, clazz);
            if (content != null) return content;
        }

        // 2. Convención 1:1 - Mismo nombre junto a la clase Java en el classpath
        String simpleName = clazz.getSimpleName();
        String packagePath = clazz.getPackageName().replace('.', '/');

        String[] candidates = {
            packagePath + "/" + simpleName + ".html",
            packagePath + "/" + Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1) + ".html",
            "templates/" + simpleName + ".html",
            "templates/" + simpleName.toLowerCase() + ".html",
            "webapp/" + simpleName + ".html",
            "webapp/" + simpleName.toLowerCase() + ".html",
            "pages/" + simpleName + ".html",
            "pages/" + simpleName.toLowerCase() + ".html",
            simpleName + ".html",
            simpleName.toLowerCase() + ".html"
        };

        for (String candidate : candidates) {
            String content = readFromClasspathOrFile(candidate, clazz);
            if (content != null) {
                return content;
            }
        }

        return null;
    }

    private static String processTemplateInheritance(String template, Class<?> clazz) {
        if (template == null || clazz == null) return template;

        java.util.regex.Matcher extendMatcher = EXTEND_PATTERN.matcher(template);
        if (extendMatcher.find()) {
            String childBody = extendMatcher.group(1);
            Class<?> superClass = clazz.getSuperclass();

            if (superClass != null && superClass != Object.class && superClass != io.jettra.aura.core.AuraPage.class && superClass != io.jettra.aura.core.AuraWebPage.class) {
                String parentTemplate = loadTemplate(superClass);
                if (parentTemplate != null && !parentTemplate.isBlank()) {
                    java.util.regex.Matcher childMatcher = CHILD_PATTERN.matcher(parentTemplate);
                    if (childMatcher.find()) {
                        String combined = childMatcher.replaceFirst(java.util.regex.Matcher.quoteReplacement(childBody));
                        java.util.regex.Matcher titleMatcher = TITLE_PATTERN.matcher(template);
                        if (titleMatcher.find()) {
                            String childTitle = titleMatcher.group(1);
                            combined = combined.replaceFirst("<title>.*?</title>", "<title>" + java.util.regex.Matcher.quoteReplacement(childTitle) + "</title>");
                        }
                        return combined;
                    }
                }
            }
        }

        return template;
    }

    public static void clearCache() {
        TEMPLATE_CACHE.clear();
    }

    private static String readFromClasspathOrFile(String path, Class<?> contextClass) {
        // A. Classpath relativo al ClassLoader
        try (InputStream is = contextClass.getClassLoader().getResourceAsStream(path)) {
            if (is != null) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception ignored) {}

        // B. Classpath relativo a la clase (si no empieza con /)
        if (!path.startsWith("/")) {
            try (InputStream is = contextClass.getResourceAsStream(path)) {
                if (is != null) {
                    return new String(is.readAllBytes(), StandardCharsets.UTF_8);
                }
            } catch (Exception ignored) {}
        }

        // C. Archivo en sistema de archivos (ej. directorio de trabajo actual)
        try {
            Path p = Path.of(path);
            if (Files.exists(p)) {
                return Files.readString(p, StandardCharsets.UTF_8);
            }
            Path inSrc = Path.of("src/main/resources", path);
            if (Files.exists(inSrc)) {
                return Files.readString(inSrc, StandardCharsets.UTF_8);
            }
        } catch (Exception ignored) {}

        return null;
    }

    private static String createDefaultTemplate(Class<?> clazz) {
        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s - JettraAura</title>
                <style>
                    body { font-family: system-ui, sans-serif; padding: 2rem; background: #f8fafc; color: #0f172a; }
                    .card { background: white; padding: 2rem; border-radius: 12px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1); }
                </style>
            </head>
            <body>
                <div class="card">
                    <h1>Página %s (JettraAura)</h1>
                    <p>Plantilla HTML generada dinámicamente. Coloque un archivo <code>%s.html</code> junto a la clase <code>%s.java</code> para personalizar.</p>
                </div>
            </body>
            </html>
            """.formatted(clazz.getSimpleName(), clazz.getSimpleName(), clazz.getSimpleName(), clazz.getSimpleName());
    }
}

