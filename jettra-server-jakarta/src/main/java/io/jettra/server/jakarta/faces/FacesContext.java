package io.jettra.server.jakarta.faces;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contexto y ciclo de vida de Jakarta Faces 4.0 adaptado para el modelo reactivo de Jettra.
 */
public class FacesContext {

    private static final ThreadLocal<FacesContext> CURRENT_INSTANCE = new ThreadLocal<>();

    private final String viewId;
    private final Map<String, Object> viewMap = new ConcurrentHashMap<>();

    public FacesContext(String viewId) {
        this.viewId = viewId;
    }

    public static FacesContext getCurrentInstance() {
        return CURRENT_INSTANCE.get();
    }

    public static void setCurrentInstance(FacesContext context) {
        CURRENT_INSTANCE.set(context);
    }

    public String getViewId() {
        return viewId;
    }

    public Map<String, Object> getViewMap() {
        return viewMap;
    }

    public void release() {
        viewMap.clear();
        CURRENT_INSTANCE.remove();
    }
}
