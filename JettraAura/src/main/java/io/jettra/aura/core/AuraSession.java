package io.jettra.aura.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Sesión de usuario de JettraAura. Mantiene estado de autenticación, atributos de sesión
 * y mensajes flash (feedback: info, success, warning, error).
 */
public class AuraSession implements Serializable {

    public enum FeedbackLevel {
        INFO, SUCCESS, WARNING, ERROR
    }

    public record FeedbackMessage(FeedbackLevel level, String text) implements Serializable {}

    private static final ThreadLocal<AuraSession> CURRENT = new ThreadLocal<>();

    private final String id = UUID.randomUUID().toString();
    private String user;
    private Set<String> roles = Collections.emptySet();
    private final Map<String, Object> attributes = new LinkedHashMap<>();
    private final List<FeedbackMessage> feedbackMessages = new ArrayList<>();

    public AuraSession() {}

    public static AuraSession get() {
        AuraSession s = CURRENT.get();
        if (s == null) {
            s = new AuraSession();
            CURRENT.set(s);
        }
        return s;
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
        attributes.put(key, val);
    }

    public void removeAttribute(String key) {
        attributes.remove(key);
    }

    public void invalidate() {
        user = null;
        roles = Collections.emptySet();
        attributes.clear();
        feedbackMessages.clear();
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

