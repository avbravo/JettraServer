package io.jettra.server.jakarta.servlet;

import java.io.IOException;

public abstract class HttpServlet {

    public void init() throws Exception {}

    public void service(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String method = req.getMethod();
        switch (method.toUpperCase()) {
            case "GET" -> doGet(req, resp);
            case "POST" -> doPost(req, resp);
            case "PUT" -> doPut(req, resp);
            case "DELETE" -> doDelete(req, resp);
            default -> resp.sendError(405, "Method Not Allowed");
        }
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.sendError(405, "HTTP GET not supported");
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.sendError(405, "HTTP POST not supported");
    }

    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.sendError(405, "HTTP PUT not supported");
    }

    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.sendError(405, "HTTP DELETE not supported");
    }

    public void destroy() {}
}
