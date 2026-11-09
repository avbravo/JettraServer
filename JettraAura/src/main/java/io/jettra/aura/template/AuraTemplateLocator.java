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
 * Aplica convención 1:1 estilo Apache Wicket: Index.java busca Index.html en el mismo paquete,
 * en resources/templates, resources/webapp o en la ruta especificada por @AuraTemplate.
 */
public class AuraTemplateLocator {

    private static final Map<Class<?>, String> TEMPLATE_CACHE = new ConcurrentHashMap<>();

    public static String loadTemplate(Class<?> componentClass) {
        if (componentClass == null) return null;

        return TEMPLATE_CACHE.computeIfAbsent(componentClass, clazz -> {
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

            // Fallback: plantilla mínima por defecto para desarrollo
            return createDefaultTemplate(clazz);
        });
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

