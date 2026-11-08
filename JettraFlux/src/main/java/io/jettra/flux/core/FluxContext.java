package io.jettra.flux.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contexto y gestión de ámbitos (Request, Session, Application, etc.) para JettraFlux.
 * Diseñado para ser autónomo y completamente interoperable con servidores como JettraEE.
 */
public class FluxContext {

    public enum Scope {
        REQUEST, SESSION, APPLICATION, VIEW, WINDOW, CLIENT, CACHE;

        public String getRole() {
            if (this == SESSION) {
                FluxContext ctx = FluxContext.getCurrent();
                if (ctx != null) {
                    Object roleObj = ctx.get(SESSION, "role");
                    return roleObj != null ? roleObj.toString() : "";
                }
            }
            return "";
        }
    }

    protected static final ThreadLocal<FluxContext> currentContext = new ThreadLocal<>();
    protected static final Map<String, Map<String, Object>> applicationScope = new ConcurrentHashMap<>();
    protected static final Map<String, Map<String, Object>> sessions = new ConcurrentHashMap<>();

    protected final Map<Scope, Map<String, Object>> localScopes = new ConcurrentHashMap<>();
    protected final String sessionId;

    public FluxContext(String sessionId) {
        this.sessionId = sessionId != null ? sessionId : "default-session";
        Map<String, Object> reqMap = new ConcurrentHashMap<>();
        reqMap.put("MAP", new ConcurrentHashMap<String, Object>());
        localScopes.put(Scope.REQUEST, reqMap);

        sessions.computeIfAbsent(this.sessionId, k -> {
            Map<String, Object> sessMap = new ConcurrentHashMap<>();
            sessMap.put("MAP", new ConcurrentHashMap<String, Object>());
            return sessMap;
        });
    }

    public static void setCurrent(FluxContext context) {
        currentContext.set(context);
    }

    public static FluxContext getCurrent() {
        return currentContext.get();
    }

    public static void clear() {
        currentContext.remove();
    }

    public static Map<String, Map<String, Object>> getSessions() {
        return sessions;
    }

    public static Map<String, Map<String, Object>> getApplicationScope() {
        return applicationScope;
    }

    public Object get(Scope scope, String key) {
        switch (scope) {
            case APPLICATION:
                return applicationScope.getOrDefault("global", new ConcurrentHashMap<>()).get(key);
            case SESSION:
                return sessions.computeIfAbsent(sessionId, k -> new ConcurrentHashMap<>()).get(key);
            default:
                return localScopes.getOrDefault(scope, new ConcurrentHashMap<>()).get(key);
        }
    }

    public void set(Scope scope, String key, Object value) {
        if (value == null) {
            switch (scope) {
                case APPLICATION:
                    Map<String, Object> appMap = applicationScope.get("global");
                    if (appMap != null) appMap.remove(key);
                    break;
                case SESSION:
                    Map<String, Object> sessMap = sessions.get(sessionId);
                    if (sessMap != null) sessMap.remove(key);
                    break;
                default:
                    Map<String, Object> lMap = localScopes.get(scope);
                    if (lMap != null) lMap.remove(key);
                    break;
            }
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

    public String getSessionId() {
        return sessionId;
    }

    public void destroyRequest() {
        Map<String, Object> reqMap = localScopes.remove(Scope.REQUEST);
        if (reqMap != null) {
            reqMap.clear();
        }
    }

    public void destroyView() {
        Map<String, Object> viewMap = localScopes.remove(Scope.VIEW);
        if (viewMap != null) {
            viewMap.clear();
        }
    }
}
