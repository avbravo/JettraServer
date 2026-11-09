package io.jettra.aura.components;

import io.jettra.aura.annotation.AuraPath;
import io.jettra.aura.core.AuraPage;

/**
 * Enlace navegable que apunta directamente a una clase de página {@link AuraPage} registrada.
 * Similar a BookmarkablePageLink en Apache Wicket.
 */
public class AuraBookmarkablePageLink<C extends AuraPage> extends AuraLink {

    private final Class<C> pageClass;

    public AuraBookmarkablePageLink(String id, Class<C> pageClass) {
        super(id);
        this.pageClass = pageClass;
        setHref(resolvePageUrl(pageClass));
    }

    public Class<C> getPageClass() {
        return pageClass;
    }

    private static String resolvePageUrl(Class<? extends AuraPage> pageClass) {
        if (pageClass == null) return "#";
        AuraPath path = pageClass.getAnnotation(AuraPath.class);
        if (path != null && !path.value().isBlank()) {
            return path.value();
        }
        String name = pageClass.getSimpleName().toLowerCase();
        if ("index".equals(name) || "home".equals(name)) return "/";
        return "/" + name;
    }
}

