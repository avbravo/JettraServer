package io.jettra.aura.server;

import io.jettra.aura.core.AuraPage;
import io.jettra.aura.core.AuraRequest;
import io.jettra.aura.core.AuraResponse;
import io.jettra.aura.core.AuraSession;
import io.jettra.server.jakarta.servlet.HttpServlet;
import io.jettra.server.jakarta.servlet.HttpServletRequest;
import io.jettra.server.jakarta.servlet.HttpServletResponse;
import io.jettra.server.jakarta.servlet.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Servlet compatible con la especificación Jakarta Servlet 6.1 para desplegar
 * aplicaciones JettraAura dentro del contenedor Jakarta EE de JettraServer.
 */
public class AuraServlet extends HttpServlet {

    private final AuraApplication application;

    public AuraServlet() {
        this(AuraApplication.get());
    }

    public AuraServlet(AuraApplication application) {
        this.application = application != null ? application : AuraApplication.get();
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        processRequest(req, res);
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        processRequest(req, res);
    }

    private void processRequest(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String uri = req.getRequestURI();
        Class<? extends AuraPage> pageClass = application.resolvePage(uri);

        if (pageClass == null) {
            res.setStatus(404);
            res.setContentType("text/html; charset=UTF-8");
            res.getWriter().println("<h1>404 - Página JettraAura no encontrada</h1><p>URI: " + uri + "</p>");
            return;
        }

        // Sincronizar sesión de Jakarta Servlet con AuraSession
        HttpSession httpSession = req.getSession(true);
        AuraSession auraSession = (AuraSession) httpSession.getAttribute("aura_session");
        if (auraSession == null) {
            auraSession = new AuraSession();
            httpSession.setAttribute("aura_session", auraSession);
        }
        AuraSession.set(auraSession);

        // Construir AuraRequest
        AuraRequest auraReq = new AuraRequest(req.getMethod(), uri);
        Map<String, String> params = req.getParameterMap();
        if (params != null) {
            auraReq.setParameters(params);
        }

        AuraResponse auraRes = new AuraResponse();
        AuraPage page = application.newPageInstance(pageClass);
        page.setRequest(auraReq);
        page.setResponse(auraRes);
        page.setSession(auraSession);

        try {
            if (auraReq.isPost()) {
                // Manejar submit
                for (io.jettra.aura.core.AuraComponent comp : page.getChildren()) {
                    if (comp instanceof io.jettra.aura.components.AuraForm<?> form) {
                        form.processSubmit(auraReq);
                        break;
                    }
                }
                if (auraRes.isRedirect()) {
                    res.sendRedirect(auraRes.getRedirectLocation());
                    return;
                }
            }

            String html = page.renderPage();
            res.setContentType(auraRes.getContentType());
            res.setStatus(auraRes.getStatus());
            PrintWriter out = res.getWriter();
            out.print(html);
            out.flush();
        } finally {
            page.detach();
            AuraSession.set(null);
        }
    }
}
