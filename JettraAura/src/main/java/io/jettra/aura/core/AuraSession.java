package io.jettra.aura.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sesión de usuario de JettraAura. Mantiene estado de autenticación, atributos de sesión,
 * instancias de páginas con estado y mensajes flash (feedback: info, success, warning, error).
 */
public class AuraSession implements Serializable {

    public enum FeedbackLevel {
        INFO, SUCCESS, WARNING, ERROR
    }

    public record FeedbackMessage(FeedbackLevel level, String text) implements Serializable {}

    private static final ThreadLocal<AuraSession> CURRENT = new ThreadLocal<>();
    private static final Map<String, AuraSession> SESSION_STORE = new ConcurrentHashMap<>();
    private static final AuraSession DEFAULT_SHARED_SESSION = new AuraSession("default-aura-session");

    private final String id;
    private String user;
    private Set<String> roles = Collections.emptySet();
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();
    private final Map<String, AuraPage> pageStore = new ConcurrentHashMap<>();
    private final List<FeedbackMessage> feedbackMessages = new ArrayList<>();

    public AuraSession() {
        this(UUID.randomUUID().toString());
    }

    public AuraSession(String id) {
        this.id = (id != null && !id.isBlank()) ? id : UUID.randomUUID().toString();
    }

    public static AuraSession get() {
        AuraSession s = CURRENT.get();
        if (s == null) {
            s = new AuraSession();
            CURRENT.set(s);
        }
        return s;
    }

    public static AuraSession resolve(String sessionId) {
        if (sessionId != null && !sessionId.isBlank()) {
            return SESSION_STORE.computeIfAbsent(sessionId, AuraSession::new);
        }
        return DEFAULT_SHARED_SESSION;
    }

    public static void registerSession(AuraSession session) {
        if (session != null) {
            SESSION_STORE.put(session.getId(), session);
        }
    }

    public static void set(AuraSession session) {
        if (session != null) {
            CURRENT.set(session);
        } else {
            CURRENT.remove();
        }
    }

    public String getId() {
        return id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public boolean isAuthenticated() {
        return user != null && !user.isBlank();
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles != null ? roles : Collections.emptySet();
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public void setAttribute(String key, Object val) {
        if (key != null && val != null) {
            attributes.put(key, val);
        } else if (key != null) {
            attributes.remove(key);
        }
    }

    public void removeAttribute(String key) {
        if (key != null) {
            attributes.remove(key);
        }
    }

    public AuraPage getPage(String uri) {
        return uri != null ? pageStore.get(uri) : null;
    }

    public void putPage(String uri, AuraPage page) {
        if (uri != null && page != null) {
            pageStore.put(uri, page);
        }
    }

    public void removePage(String uri) {
        if (uri != null) {
            pageStore.remove(uri);
        }
    }

    public void clearPages() {
        pageStore.clear();
    }

    public static void clearAllSessions() {
        SESSION_STORE.clear();
        DEFAULT_SHARED_SESSION.clearPages();
        synchronized (DEFAULT_SHARED_SESSION) {
            DEFAULT_SHARED_SESSION.feedbackMessages.clear();
        }
    }

    public void invalidate() {
        user = null;
        roles = Collections.emptySet();
        attributes.clear();
        pageStore.clear();
        synchronized (this) {
            feedbackMessages.clear();
        }
        SESSION_STORE.remove(id);
    }

    public void info(String message) {
        addFeedback(FeedbackLevel.INFO, message);
    }

    public void success(String message) {
        addFeedback(FeedbackLevel.SUCCESS, message);
    }

    public void warn(String message) {
        addFeedback(FeedbackLevel.WARNING, message);
    }

    public void error(String message) {
        addFeedback(FeedbackLevel.ERROR, message);
    }

    public synchronized void addFeedback(FeedbackLevel level, String message) {
        feedbackMessages.add(new FeedbackMessage(level, message));
    }

    public synchronized List<FeedbackMessage> getFeedbackMessages() {
        return new ArrayList<>(feedbackMessages);
    }

    public synchronized List<FeedbackMessage> consumeFeedbackMessages() {
        List<FeedbackMessage> copy = new ArrayList<>(feedbackMessages);
        feedbackMessages.clear();
        return copy;
    }
}
