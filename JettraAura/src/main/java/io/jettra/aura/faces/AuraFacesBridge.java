package io.jettra.aura.faces;

import io.jettra.aura.core.AuraComponent;
import io.jettra.aura.core.AuraPage;
import io.jettra.aura.core.AuraRequest;
import io.jettra.aura.core.AuraResponse;
import io.jettra.aura.core.AuraSession;
import io.jettra.aura.server.AuraApplication;
import io.jettra.aura.server.AuraHandler;
import io.jettra.flux.core.Widget;
import io.jettra.server.jakarta.faces.FacesContext;
import io.jettra.server.jakarta.servlet.HttpServletRequest;
import io.jettra.server.jakarta.servlet.HttpServletResponse;
import io.jettra.server.jakarta.servlet.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Puente de integración nativa entre JettraAura (variante optimizada de JettraFlux para plantillas)
 * y páginas HTML (.html) / Jakarta Faces (.xhtml).
 * Permite renderizar páginas o componentes Aura dentro de vistas Faces y procesar eventos AJAX/POST.
 */
public final class AuraFacesBridge {

    private AuraFacesBridge() {}

    /**
     * Renderiza el fragmento de contenido de una {@link AuraPage} junto con el runtime reactivo
     * para incrustarlo dentro de una plantilla Jakarta Faces (.xhtml).
     */
    public static String renderSection(AuraPage page) {
        if (page == null) return "";
        return page.renderSection();
    }

    /**
     * Convierte cualquier {@link AuraComponent} o {@link AuraPage} en un {@link Widget} de JettraFlux.
     */
    public static Widget toFluxWidget(AuraComponent component) {
        if (component == null) return null;
        return component.toFluxWidget();
    }

    /**
     * Resuelve o instancia una {@link AuraPage} asociada a la sesión HTTP de Jakarta Servlet / Faces,
     * garantizando la preservación del estado de los componentes y diálogos entre peticiones.
     */
    @SuppressWarnings("unchecked")
    public static <T extends AuraPage> T resolvePage(HttpServletRequest req, Class<T> pageClass) {
        HttpSession httpSession = req != null ? req.getSession(true) : null;
        AuraSession auraSession = null;
        if (httpSession != null) {
            Object existing = httpSession.getAttribute("aura_session");
            if (existing instanceof AuraSession s) {
                auraSession = s;
            } else {
                auraSession = AuraSession.resolve(httpSession.getId());
                httpSession.setAttribute("aura_session", auraSession);
            }
        } else {
            auraSession = AuraSession.resolve(null);
        }
        AuraSession.set(auraSession);

        String key = AuraHandler.normalizePageKey(req != null ? req.getRequestURI() : pageClass.getName(), pageClass);
        AuraPage page = auraSession.getPage(key);
        if (!pageClass.isInstance(page)) {
            page = AuraApplication.get().newPageInstance(pageClass);
            auraSession.putPage(key, page);
        }
        page.setSession(auraSession);
        return (T) page;
    }

    /**
     * Procesa una petición de Jakarta Servlet / Faces sobre una {@link AuraPage}.
     * Si es una petición AJAX de JettraAura, escribe la respuesta JSON directamente y retorna {@code true}.
     * Si es un POST o GET con redirección, envía el redirect y retorna {@code true}.
     * Si debe continuar el renderizado normal de la vista .xhtml, retorna {@code false}.
     */
    public static boolean processServletRequest(AuraPage page, HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (page == null || req == null || res == null) return false;

        AuraRequest auraReq = new AuraRequest(req.getMethod(), req.getRequestURI());
        Map<String, String> params = req.getParameterMap();
        if (params != null) {
            auraReq.setParameters(params);
        }
        String xReqWith = req.getHeader("X-Requested-With");
        if (xReqWith != null) {
            auraReq.setHeader("X-Requested-With", xReqWith);
        }

        AuraResponse auraRes = new AuraResponse();
        page.setRequest(auraReq);
        page.setResponse(auraRes);
        page.autoBindAnnotatedComponents();

        if (auraReq.isAjax()) {
            String json = AuraHandler.processAjaxRequest(page, auraReq, auraRes);
            res.setStatus(200);
            res.setContentType("application/json; charset=UTF-8");
            PrintWriter out = res.getWriter();
            out.print(json);
            out.flush();
            return true;
        }

        if (auraReq.isPost() && (auraReq.getParameter("aura:target") != null || auraReq.getParameter("flux:target") != null)) {
            AuraHandler.processFormPost(page, auraReq, auraRes);
            if (auraRes.isRedirect()) {
                res.sendRedirect(auraRes.getRedirectLocation());
                return true;
            }
        } else if (auraReq.getParameter("aura:target") != null || auraReq.getParameter("flux:target") != null) {
            AuraHandler.processGetEvent(page, auraReq, auraRes);
            if (auraRes.isRedirect()) {
                res.sendRedirect(auraRes.getRedirectLocation());
                return true;
            }
        }

        return false;
    }

    /**
     * Sincroniza atributos del {@link FacesContext} hacia los parámetros de una {@link AuraPage}.
     */
    public static void syncFacesContext(FacesContext facesContext, AuraPage page) {
        if (facesContext == null || page == null) return;
        for (Map.Entry<String, Object> entry : facesContext.getViewMap().entrySet()) {
            if (entry.getValue() != null) {
                page.setParameter(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
    }
}

