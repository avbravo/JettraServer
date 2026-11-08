package io.jettra.cdi.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestor universal de ámbitos de ciclo de vida (Contexts) para Jettra.
 */
public class JettraContext {

    public enum Scope {
        REQUEST, SESSION, APPLICATION, VIEW, WINDOW, CLIENT;
    }

    private static final ThreadLocal<JettraContext> currentContext = new ThreadLocal<>();
    private static final Map<String, Map<String, Object>> applicationScope = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, Object>> sessions = new ConcurrentHashMap<>();

    private final Map<Scope, Map<String, Object>> localScopes = new ConcurrentHashMap<>();
    private final String sessionId;

    public JettraContext() {
        this("anonymous_" + System.nanoTime());
    }

    public JettraContext(String sessionId) {
        this.sessionId = sessionId != null ? sessionId : "anonymous_" + System.nanoTime();
        localScopes.put(Scope.REQUEST, new ConcurrentHashMap<>());
        localScopes.put(Scope.VIEW, new ConcurrentHashMap<>());
        sessions.computeIfAbsent(this.sessionId, k -> new ConcurrentHashMap<>());
    }

    public static void setCurrent(JettraContext context) {
        currentContext.set(context);
    }

    public static JettraContext getCurrent() {
        JettraContext ctx = currentContext.get();
        if (ctx == null) {
            ctx = new JettraContext();
            currentContext.set(ctx);
        }
        return ctx;
    }

    public static void clear() {
        currentContext.remove();
    }

    public Object get(Scope scope, String key) {
        switch (scope) {
            case APPLICATION:
                return applicationScope.computeIfAbsent("global", k -> new ConcurrentHashMap<>()).get(key);
            case SESSION:
                return sessions.computeIfAbsent(sessionId, k -> new ConcurrentHashMap<>()).get(key);
            default:
                return localScopes.computeIfAbsent(scope, k -> new ConcurrentHashMap<>()).get(key);
        }
    }

    public void set(Scope scope, String key, Object value) {
        if (value == null) {
            remove(scope, key);
            return;
        }
        switch (scope) {
            case APPLICATION:
                applicationScope.computeIfAbsent("global", k -> new ConcurrentHashMap<>()).put(key, value);
                break;
            case SESSION:
                sessions.computeIfAbsent(sessionId, k -> new ConcurrentHashMap<>()).put(key, value);
                break;
            default:
                localScopes.computeIfAbsent(scope, k -> new ConcurrentHashMap<>()).put(key, value);
                break;
        }
    }

    public void remove(Scope scope, String key) {
        switch (scope) {
            case APPLICATION:
                Map<String, Object> app = applicationScope.get("global");
                if (app != null) app.remove(key);
                break;
            case SESSION:
                Map<String, Object> sess = sessions.get(sessionId);
                if (sess != null) sess.remove(key);
                break;
            default:
                Map<String, Object> loc = localScopes.get(scope);
                if (loc != null) loc.remove(key);
                break;
        }
    }

    public String getSessionId() {
        return sessionId;
    }

    public void destroyRequest() {
        Map<String, Object> req = localScopes.remove(Scope.REQUEST);
        if (req != null) req.clear();
    }

    public void destroyView() {
        Map<String, Object> view = localScopes.remove(Scope.VIEW);
        if (view != null) view.clear();
    }

    public static void destroySession(String sid) {
        if (sid != null) {
            Map<String, Object> s = sessions.remove(sid);
            if (s != null) s.clear();
        }
    }

    public static void clearAll() {
        currentContext.remove();
        applicationScope.clear();
        sessions.clear();
    }
}
