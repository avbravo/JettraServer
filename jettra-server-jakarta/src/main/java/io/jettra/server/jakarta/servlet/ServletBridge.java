package io.jettra.server.jakarta.servlet;

import io.jettra.server.core.http.HttpMethod;
import io.jettra.server.core.http.HttpRouter;

/**
 * Adaptador para registrar un HttpServlet tradicional dentro del HttpRouter asíncrono.
 */
public class ServletBridge {

    private final HttpRouter router;
    private final ServletContext servletContext;

    public ServletBridge(HttpRouter router, ServletContext servletContext) {
        this.router = router;
        this.servletContext = servletContext;
    }

    public void registerServlet(String urlPattern, HttpServlet servlet) {
        try {
            servlet.init();
        } catch (Exception e) {
            throw new RuntimeException("Error en init() del Servlet", e);
        }

        for (HttpMethod m : HttpMethod.values()) {
            router.add(m, urlPattern, (req, res) -> {
                HttpServletRequest servletReq = new HttpServletRequest(req, servletContext);
                HttpServletResponse servletRes = new HttpServletResponse(res);
                servlet.service(servletReq, servletRes);
                HttpSession sess = servletReq.getSession(false);
                if (sess != null && sess.isValid()) {
                    servletRes.setHeader("Set-Cookie", "JSESSIONID=" + sess.getId() + "; Path=/; HttpOnly; SameSite=Lax");
                }
                servletRes.flushBuffer();
            });
        }
    }
}
