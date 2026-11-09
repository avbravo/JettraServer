package io.jettra.aura.server;

import io.jettra.aura.ajax.AuraAjaxButton;
import io.jettra.aura.ajax.AuraAjaxLink;
import io.jettra.aura.ajax.AuraAjaxRequestTarget;
import io.jettra.aura.annotation.AuraAuthorize;
import io.jettra.aura.components.AuraButton;
import io.jettra.aura.components.AuraForm;
import io.jettra.aura.components.AuraLink;
import io.jettra.aura.core.AuraComponent;
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

        // 1. Crear contexto de Petición y Respuesta Aura
        AuraRequest auraReq = buildAuraRequest(req);
        AuraResponse auraRes = new AuraResponse();
        AuraSession session = AuraSession.get();

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

        // 3. Instanciar la página
        AuraPage page = application.newPageInstance(pageClass);
        page.setRequest(auraReq);
        page.setResponse(auraRes);
        page.setSession(session);

        try {
            // 4. Manejo de Peticiones Ajax
            if (auraReq.isAjax()) {
                handleAjaxRequest(page, auraReq, res);
                return;
            }

            // 5. Manejo de Peticiones POST (Formularios y Botones)
            if (auraReq.isPost()) {
                handleFormPost(page, auraReq, auraRes);
                if (auraRes.isRedirect()) {
                    res.status(HttpStatus.FOUND).header("Location", auraRes.getRedirectLocation()).body("");
                    return;
                }
            } else {
                // Manejo de eventos de click por GET (enlaces con target)
                String targetId = auraReq.getParameter("aura:target");
                if (targetId != null) {
                    AuraComponent comp = page.get(targetId);
                    if (comp instanceof AuraLink link) {
                        link.onClick();
                        if (auraRes.isRedirect()) {
                            res.status(HttpStatus.FOUND).header("Location", auraRes.getRedirectLocation()).body("");
                            return;
                        }
                    }
                }
            }

            // 6. Renderizado de página completa
            String html = page.renderPage();
            res.status(HttpStatus.OK).html(html);

        } finally {
            page.detach();
        }
    }

    private void handleAjaxRequest(AuraPage page, AuraRequest auraReq, HttpResponse res) {
        AuraAjaxRequestTarget target = new AuraAjaxRequestTarget(page);
        String targetId = auraReq.getParameter("aura:target");

        if (targetId != null) {
            AuraComponent comp = page.get(targetId);
            if (comp instanceof AuraAjaxButton ajaxBtn) {
                ajaxBtn.onAjaxSubmit(target);
            } else if (comp instanceof AuraAjaxLink ajaxLink) {
                ajaxLink.onAjaxClick(target);
            }
        }

        String jsonResponse = target.toJson();
        res.status(HttpStatus.OK).json(jsonResponse);
    }

    private void handleFormPost(AuraPage page, AuraRequest auraReq, AuraResponse auraRes) {
        // Localizar si hay un formulario o botón específico accionado
        String targetId = auraReq.getParameter("aura:target");
        if (targetId != null) {
            AuraComponent comp = page.get(targetId);
            if (comp instanceof AuraForm<?> form) {
                form.processSubmit(auraReq);
                return;
            } else if (comp instanceof AuraButton btn) {
                btn.onSubmit();
                return;
            }
        }

        // Si no se especificó targetId, buscar el primer formulario registrado en la página
        for (AuraComponent comp : page.getChildren()) {
            if (comp instanceof AuraForm<?> form) {
                form.processSubmit(auraReq);
                return;
            }
        }
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
