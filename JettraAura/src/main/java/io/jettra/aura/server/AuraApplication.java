package io.jettra.aura.server;

import io.jettra.aura.annotation.AuraPath;
import io.jettra.aura.core.AuraPage;
import io.jettra.server.jakarta.cdi.CdiContainer;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Clase de configuración y orquestación central de una aplicación web JettraAura.
 * Gestiona el mapeo de rutas a clases de página, inyección de dependencias CDI (Jakarta EE)
 * y políticas de ciclo de vida.
 */
public class AuraApplication implements Serializable {

    private static volatile AuraApplication instance;

    private final Map<String, Class<? extends AuraPage>> pageRoutes = new ConcurrentHashMap<>();
    private Class<? extends AuraPage> homePage;
    private Class<? extends AuraPage> errorPage;
    private Class<? extends AuraPage> loginPage;
    private Function<Class<?>, Object> customInjector;
    private CdiContainer cdiContainer;

    public AuraApplication() {
        instance = this;
    }

    public static AuraApplication get() {
        if (instance == null) {
            synchronized (AuraApplication.class) {
                if (instance == null) {
                    instance = new AuraApplication();
                }
            }
        }
        return instance;
    }

    public static void set(AuraApplication app) {
        instance = app;
    }

    public AuraApplication mountPage(String path, Class<? extends AuraPage> pageClass) {
        if (path != null && pageClass != null) {
            pageRoutes.put(normalizePath(path), pageClass);
        }
        return this;
    }

    public AuraApplication registerPage(Class<? extends AuraPage> pageClass) {
        if (pageClass == null) return this;
        AuraPath pathAnnotation = pageClass.getAnnotation(AuraPath.class);
        String path;
        if (pathAnnotation != null && !pathAnnotation.value().isBlank()) {
            path = pathAnnotation.value();
        } else {
            String name = pageClass.getSimpleName().toLowerCase();
            if ("index".equals(name) || "home".equals(name)) {
                path = "/";
            } else {
                path = "/" + name;
            }
        }
        return mountPage(path, pageClass);
    }

    public Class<? extends AuraPage> resolvePage(String uri) {
        if (uri == null) return homePage;
        String normalized = normalizePath(uri);
        Class<? extends AuraPage> exact = pageRoutes.get(normalized);
        if (exact != null) return exact;

        // Comprobar página de inicio
        if ("/".equals(normalized) && homePage != null) {
            return homePage;
        }

        // Búsqueda por coincidencia de prefijo
        for (Map.Entry<String, Class<? extends AuraPage>> entry : pageRoutes.entrySet()) {
            String route = entry.getKey();
            if (normalized.equals(route) || normalized.equals(route + ".html")) {
                return entry.getValue();
            }
        }

        return null;
    }

    public Map<String, Class<? extends AuraPage>> getPageRoutes() {
        return Collections.unmodifiableMap(pageRoutes);
    }

    public Class<? extends AuraPage> getHomePage() {
        return homePage;
    }

    public AuraApplication setHomePage(Class<? extends AuraPage> homePage) {
        this.homePage = homePage;
        if (homePage != null) {
            mountPage("/", homePage);
        }
        return this;
    }

    public Class<? extends AuraPage> getErrorPage() {
        return errorPage;
    }

    public AuraApplication setErrorPage(Class<? extends AuraPage> errorPage) {
        this.errorPage = errorPage;
        return this;
    }

    public Class<? extends AuraPage> getLoginPage() {
        return loginPage;
    }

    public AuraApplication setLoginPage(Class<? extends AuraPage> loginPage) {
        this.loginPage = loginPage;
        if (loginPage != null) {
            mountPage("/login", loginPage);
        }
        return this;
    }

    public CdiContainer getCdiContainer() {
        return cdiContainer;
    }

    public String getPathForPage(Class<? extends AuraPage> pageClass) {
        if (pageClass == null) return null;
        for (Map.Entry<String, Class<? extends AuraPage>> entry : pageRoutes.entrySet()) {
            if (pageClass.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public AuraApplication setCdiContainer(CdiContainer cdiContainer) {
        this.cdiContainer = cdiContainer;
        return this;
    }

    public AuraApplication setCustomInjector(Function<Class<?>, Object> customInjector) {
        this.customInjector = customInjector;
        return this;
    }

    /**
     * Crea e inyecta una nueva instancia de la página solicitada.
     */
    @SuppressWarnings("unchecked")
    public <T extends AuraPage> T newPageInstance(Class<T> pageClass) {
        try {
            T page;
            if (cdiContainer != null) {
                try {
                    page = (T) cdiContainer.getBean(pageClass);
                } catch (Exception ignored) {
                    page = null;
                }
                if (page == null) {
                    page = pageClass.getDeclaredConstructor().newInstance();
                    cdiContainer.injectFields(page);
                }
            } else if (customInjector != null) {
                page = (T) customInjector.apply(pageClass);
                if (page == null) page = pageClass.getDeclaredConstructor().newInstance();
            } else {
                page = pageClass.getDeclaredConstructor().newInstance();
            }
            return page;
        } catch (Exception e) {
            throw new RuntimeException("Error al instanciar la página Aura " + pageClass.getName(), e);
        }
    }

    private static String normalizePath(String path) {
        if (path == null || path.isBlank() || "/".equals(path)) return "/";
        String p = path.trim();
        if (!p.startsWith("/")) p = "/" + p;
        if (p.endsWith("/") && p.length() > 1) p = p.substring(0, p.length() - 1);
        return p;
    }
}
