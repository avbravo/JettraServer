package io.jettra.studio.markup;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads HTML markup files associated with WebPage and Panel classes.
 * Follows the standard convention: HomePage.java is paired with HomePage.html.
 */
public class HtmlResourceLoader {

    private static final HtmlResourceLoader INSTANCE = new HtmlResourceLoader();
    private final Map<String, String> markupCache = new ConcurrentHashMap<>();
    private boolean devMode = true;

    private HtmlResourceLoader() {}

    public static HtmlResourceLoader getInstance() {
        return INSTANCE;
    }

    public boolean isDevMode() {
        return devMode;
    }

    public void setDevMode(boolean devMode) {
        this.devMode = devMode;
    }

    /**
     * Loads the HTML markup string for the given component class.
     */
    public String loadMarkup(Class<?> componentClass) {
        if (componentClass == null) return "";

        String className = componentClass.getSimpleName();
        String packageName = componentClass.getPackageName();
        String packagePath = packageName.replace('.', '/');

        String cacheKey = componentClass.getName();
        if (!devMode) {
            String cached = markupCache.get(cacheKey);
            if (cached != null) return cached;
        }

        // 1. Try classpath in same package: e.g. com/example/HomePage.html
        String classpathSamePackage = packagePath + "/" + className + ".html";
        String content = loadFromClasspath(classpathSamePackage);
        if (content != null) {
            markupCache.put(cacheKey, content);
            return content;
        }

        // 2. Try classpath root or pages folder: e.g. pages/HomePage.html or HomePage.html
        String[] candidatePaths = {
            "pages/" + className + ".html",
            "templates/" + className + ".html",
            className + ".html"
        };
        for (String path : candidatePaths) {
            content = loadFromClasspath(path);
            if (content != null) {
                markupCache.put(cacheKey, content);
                return content;
            }
        }

        // 3. In devMode, try local filesystem relative to workspace
        if (devMode) {
            String[] localFilePaths = {
                "src/main/resources/" + classpathSamePackage,
                "src/main/java/" + classpathSamePackage,
                "src/main/resources/pages/" + className + ".html",
                "src/main/resources/templates/" + className + ".html",
                "src/main/resources/" + className + ".html"
            };
            for (String lPath : localFilePaths) {
                File f = new File(lPath);
                if (f.exists() && f.isFile()) {
                    try {
                        String fileContent = Files.readString(f.toPath(), StandardCharsets.UTF_8);
                        markupCache.put(cacheKey, fileContent);
                        return fileContent;
                    } catch (Exception ignored) {}
                }
            }
        }

        return null;
    }

    private String loadFromClasspath(String path) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try (InputStream is = cl.getResourceAsStream(path)) {
            if (is != null) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception ignored) {}
        return null;
    }

    /**
     * Stores markup manually in cache (useful for dynamic tests or programmatic HTML).
     */
    public void registerMarkup(Class<?> clazz, String html) {
        if (clazz != null && html != null) {
            markupCache.put(clazz.getName(), html);
        }
    }

    public void clearCache() {
        markupCache.clear();
    }
}
