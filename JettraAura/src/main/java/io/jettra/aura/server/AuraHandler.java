package io.jettra.aura.server;

import io.jettra.aura.ajax.AuraAjaxButton;
import io.jettra.aura.ajax.AuraAjaxLink;
import io.jettra.aura.ajax.AuraAjaxRequestTarget;
import io.jettra.aura.annotation.AuraAuthorize;
import io.jettra.aura.components.AuraButton;
import io.jettra.aura.components.AuraForm;
import io.jettra.aura.components.AuraLink;
import io.jettra.aura.components.AuraModal;
import io.jettra.aura.core.AuraComponent;
import io.jettra.aura.core.AuraContainer;
import io.jettra.aura.core.AuraEvent;
import io.jettra.aura.core.AuraPage;
import io.jettra.aura.core.AuraRequest;
import io.jettra.aura.core.AuraResponse;
import io.jettra.aura.core.AuraSession;
import io.jettra.server.core.http.HttpRequest;
import io.jettra.server.core.http.HttpResponse;
import io.jettra.server.core.http.HttpStatus;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Despachador HTTP principal de JettraAura para JettraServer.
 * Ejecuta el ciclo de vida de peticiones, eventos de componentes, peticiones Ajax y seguridad.
 */
public class AuraHandler {

    private static final String SESSION_COOKIE = "AURA_SESSION_ID";

    private final AuraApplication application;

    public AuraHandler() {
        this(AuraApplication.get());
    }

    public AuraHandler(AuraApplication application) {
        this.application = application != null ? application : AuraApplication.get();
    }

    public void handle(HttpRequest req, HttpResponse res) {
        String uri = req.getPath();
        Class<? extends AuraPage> pageClass = application.resolvePage(uri);

        if (pageClass == null) {
            res.status(HttpStatus.NOT_FOUND).html("<h1>404 - Página Aura no encontrada</h1><p>Ruta: " + uri + "</p>");
            return;
        }

        // 1. Crear contexto de Petición, Respuesta y Sesión Aura
        AuraRequest auraReq = buildAuraRequest(req);
        AuraResponse auraRes = new AuraResponse();
        String cookieSessionId = extractSessionId(req);
        AuraSession session = AuraSession.resolve(cookieSessionId);
        AuraSession.set(session);
        if (cookieSessionId == null || cookieSessionId.isBlank()) {
            res.header("Set-Cookie", SESSION_COOKIE + "=" + session.getId() + "; Path=/; HttpOnly; SameSite=Lax");
        }

        // 2. Control de Autorización y Seguridad (@AuraAuthorize)
        AuraAuthorize auth = pageClass.getAnnotation(AuraAuthorize.class);
        if (auth != null) {
            if (!session.isAuthenticated()) {
                res.status(HttpStatus.FOUND).header("Location", auth.redirectOnDenied()).body("");
                return;
            }
            if (auth.roles().length > 0) {
                boolean hasRole = false;
                for (String r : auth.roles()) {
                    if (session.hasRole(r)) {
                        hasRole = true;
                        break;
                    }
                }
                if (!hasRole) {
                    res.status(HttpStatus.FORBIDDEN).html("<h1>403 - Acceso Denegado</h1><p>No cuenta con los roles requeridos para acceder a esta página Aura.</p>");
                    return;
                }
            }
        }

        // 3. Obtener o instanciar la página manteniendo estado de componentes en la sesión
        String pageKey = normalizePageKey(uri, pageClass);
        AuraPage page = session.getPage(pageKey);
        if (page == null || !pageClass.isInstance(page)) {
            page = application.newPageInstance(pageClass);
            session.putPage(pageKey, page);
            session.putPage(uri, page);
        }
        page.setRequest(auraReq);
        page.setResponse(auraRes);
        page.setSession(session);
        page.autoBindAnnotatedComponents();

        try {
            // 4. Manejo de Peticiones Ajax
            if (auraReq.isAjax()) {
                String jsonResponse = processAjaxRequest(page, auraReq, auraRes);
                res.status(HttpStatus.OK).contentType("application/json; charset=UTF-8").body(jsonResponse);
                return;
            }

            // 5. Manejo de Peticiones POST (Formularios y Botones)
            if (auraReq.isPost()) {
                processFormPost(page, auraReq, auraRes);
                if (auraRes.isRedirect()) {
                    res.status(HttpStatus.FOUND).header("Location", auraRes.getRedirectLocation()).body("");
                    return;
                }
            } else {
                processGetEvent(page, auraReq, auraRes);
                if (auraRes.isRedirect()) {
                    res.status(HttpStatus.FOUND).header("Location", auraRes.getRedirectLocation()).body("");
                    return;
                }
            }

            // 6. Renderizado de página completa
            String html = page.renderPage();
            res.status(HttpStatus.OK).html(html);

        } finally {
            page.detach();
        }
    }

    public static String normalizePageKey(String uri, Class<? extends AuraPage> pageClass) {
        if (pageClass != null) {
            return pageClass.getName();
        }
        if (uri == null) return "";
        if (uri.endsWith(".xhtml")) return uri.substring(0, uri.length() - 6);
        if (uri.endsWith(".html")) return uri.substring(0, uri.length() - 5);
        return uri;
    }

    public static void processGetEvent(AuraPage page, AuraRequest auraReq, AuraResponse auraRes) {
        String targetId = auraReq.getParameter("aura:target");
        if (targetId == null) {
            targetId = auraReq.getParameter("flux:target");
        }
        if (targetId != null) {
            String eventType = auraReq.getParameter("aura:event");
            if (eventType == null || eventType.isBlank()) {
                eventType = auraReq.getParameter("flux:event");
            }
            if (eventType == null || eventType.isBlank()) eventType = "click";
            AuraComponent comp = page.get(targetId);
            if (comp != null) {
                if (comp instanceof AuraLink link) {
                    link.onClick();
                } else if (comp instanceof AuraButton btn) {
                    btn.onSubmit();
                }
                comp.fireEvent(new AuraEvent(comp, eventType, auraReq, auraRes, null));
            }
            page.invokeAuraAction(targetId, auraReq, auraRes, null);
        }
    }

    public static String processAjaxRequest(AuraPage page, AuraRequest auraReq, AuraResponse auraRes) {
        AuraAjaxRequestTarget target = new AuraAjaxRequestTarget(page);
        String targetId = auraReq.getParameter("aura:target");
        if (targetId == null) {
            targetId = auraReq.getParameter("flux:target");
        }
        String eventType = auraReq.getParameter("aura:event");
        if (eventType == null || eventType.isBlank()) {
            eventType = auraReq.getParameter("flux:event");
        }
        if (eventType == null || eventType.isBlank()) {
            eventType = "click";
        }

        if (targetId != null) {
            AuraComponent comp = page.get(targetId);
            if (comp != null) {
                if ("close".equalsIgnoreCase(eventType) && comp instanceof AuraModal modal) {
                    modal.hide();
                } else if (comp instanceof AuraAjaxButton ajaxBtn) {
                    if (ajaxBtn.isDefaultFormProcessing()) {
                        AuraForm<?> parentForm = ajaxBtn.findParent(AuraForm.class);
                        if (parentForm != null) {
                            parentForm.processFormInputs(auraReq);
                        }
                    }
                    ajaxBtn.onAjaxSubmit(target);
                } else if (comp instanceof AuraAjaxLink ajaxLink) {
                    ajaxLink.onAjaxClick(target);
                } else if (comp instanceof AuraButton btn) {
                    if (btn.isDefaultFormProcessing()) {
                        AuraForm<?> parentForm = btn.findParent(AuraForm.class);
                        if (parentForm != null) {
                            parentForm.processFormInputs(auraReq);
                        }
                    }
                    btn.onSubmit();
                } else if (comp instanceof AuraLink link) {
                    link.onClick();
                }

                comp.fireEvent(new AuraEvent(comp, eventType, auraReq, auraRes, target));
            }
            page.invokeAuraAction(targetId, auraReq, auraRes, target);
        }

        if (auraRes.isRedirect() && target.getRedirectUrl() == null) {
            target.redirect(auraRes.getRedirectLocation());
        }

        return target.toJson();
    }

    public static void processFormPost(AuraPage page, AuraRequest auraReq, AuraResponse auraRes) {
        String targetId = auraReq.getParameter("aura:target");
        if (targetId == null) {
            targetId = auraReq.getParameter("flux:target");
        }
        String eventType = auraReq.getParameter("aura:event");
        if (eventType == null || eventType.isBlank()) {
            eventType = auraReq.getParameter("flux:event");
        }
        if (eventType == null || eventType.isBlank()) {
            eventType = "submit";
        }

        if (targetId != null) {
            AuraComponent comp = page.get(targetId);
            if (comp instanceof AuraForm<?> form) {
                form.processSubmit(auraReq);
                form.fireEvent(new AuraEvent(form, eventType, auraReq, auraRes, null));
                page.invokeAuraAction(targetId, auraReq, auraRes, null);
                return;
            } else if (comp instanceof AuraButton btn) {
                AuraForm<?> parentForm = btn.findParent(AuraForm.class);
                if (parentForm != null) {
                    if (btn.isDefaultFormProcessing()) {
                        parentForm.processFormInputs(auraReq);
                        btn.onSubmit();
                        parentForm.onSubmit();
                    } else {
                        btn.onSubmit();
                    }
                } else {
                    btn.onSubmit();
                }
                btn.fireEvent(new AuraEvent(btn, eventType, auraReq, auraRes, null));
                page.invokeAuraAction(targetId, auraReq, auraRes, null);
                return;
            } else if (comp instanceof AuraLink link) {
                link.onClick();
                link.fireEvent(new AuraEvent(link, eventType, auraReq, auraRes, null));
                page.invokeAuraAction(targetId, auraReq, auraRes, null);
                return;
            } else if (comp != null) {
                comp.fireEvent(new AuraEvent(comp, eventType, auraReq, auraRes, null));
                page.invokeAuraAction(targetId, auraReq, auraRes, null);
                return;
            }
        }

        // Si no se especificó targetId, buscar formulario cuyo botón fue accionado o el primer formulario visible
        AuraForm<?> form = findSubmittedForm(page, auraReq);
        if (form != null) {
            form.processSubmit(auraReq);
            form.fireEvent(new AuraEvent(form, eventType, auraReq, auraRes, null));
        }
    }

    private static AuraForm<?> findSubmittedForm(AuraContainer container, AuraRequest req) {
        AuraForm<?> firstForm = null;
        for (AuraComponent comp : container.getChildren()) {
            if (comp instanceof AuraForm<?> form) {
                if (firstForm == null) firstForm = form;
                if (hasMatchingInput(form, req)) {
                    return form;
                }
            } else if (comp instanceof AuraContainer sub) {
                AuraForm<?> nested = findSubmittedForm(sub, req);
                if (nested != null) return nested;
            }
        }
        return firstForm;
    }

    private static boolean hasMatchingInput(AuraContainer container, AuraRequest req) {
        for (AuraComponent child : container.getChildren()) {
            if (req.getParameters().containsKey(child.getPath()) || req.getParameters().containsKey(child.getId())) {
                return true;
            }
            if (child instanceof AuraContainer sub && hasMatchingInput(sub, req)) {
                return true;
            }
        }
        return false;
    }

    private String extractSessionId(HttpRequest req) {
        Map<String, String> headers = req.getHeaders();
        if (headers == null) return null;
        String cookieHeader = headers.get("Cookie");
        if (cookieHeader == null) cookieHeader = headers.get("cookie");
        if (cookieHeader == null || cookieHeader.isBlank()) return null;

        String[] cookies = cookieHeader.split(";");
        for (String c : cookies) {
            String[] kv = c.trim().split("=", 2);
            if (kv.length == 2 && SESSION_COOKIE.equals(kv[0].trim())) {
                return kv[1].trim();
            }
        }
        return null;
    }

    private AuraRequest buildAuraRequest(HttpRequest req) {
        AuraRequest auraReq = new AuraRequest(req.getMethod().name(), req.getPath());
        auraReq.setParameters(req.getQueryParams());

        // Parsear formulario si es POST urlencoded
        String body = req.getBodyAsString();
        if (body != null && !body.isBlank() && req.getMethod().name().equalsIgnoreCase("POST")) {
            Map<String, String> formParams = parseUrlEncoded(body);
            auraReq.setParameters(formParams);
            auraReq.setBody(body);
        }

        return auraReq;
    }

    private Map<String, String> parseUrlEncoded(String data) {
        Map<String, String> map = new LinkedHashMap<>();
        if (data == null || data.isBlank()) return map;

        String[] pairs = data.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            String k = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String v = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
            map.put(k, v);
        }
        return map;
    }
}
