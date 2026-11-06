package io.jettra.server.jakarta.websocket;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestor de endpoints WebSocket anotados con @ServerEndpoint.
 */
public class WebSocketManager {

    private final Map<String, Object> endpoints = new ConcurrentHashMap<>();

    public void registerEndpoint(Class<?> endpointClass) {
        WebSocketAnnotations.ServerEndpoint anno = endpointClass.getAnnotation(WebSocketAnnotations.ServerEndpoint.class);
        if (anno == null) {
            throw new IllegalArgumentException("La clase " + endpointClass.getName() + " debe tener @ServerEndpoint");
        }
        try {
            Object instance = endpointClass.getDeclaredConstructor().newInstance();
            endpoints.put(anno.value(), instance);
        } catch (Exception e) {
            throw new RuntimeException("Error registrando WebSocket Endpoint", e);
        }
    }

    public Object getEndpoint(String path) {
        return endpoints.get(path);
    }

    public void dispatchMessage(Object endpointInstance, WebSocketSession session, String message) {
        for (Method m : endpointInstance.getClass().getDeclaredMethods()) {
            if (m.isAnnotationPresent(WebSocketAnnotations.OnMessage.class)) {
                m.setAccessible(true);
                try {
                    Class<?>[] pTypes = m.getParameterTypes();
                    if (pTypes.length == 1 && pTypes[0] == String.class) {
                        m.invoke(endpointInstance, message);
                    } else if (pTypes.length == 2 && pTypes[0] == String.class && pTypes[1] == WebSocketSession.class) {
                        m.invoke(endpointInstance, message, session);
                    } else if (pTypes.length == 2 && pTypes[0] == WebSocketSession.class && pTypes[1] == String.class) {
                        m.invoke(endpointInstance, session, message);
                    }
                } catch (Exception e) {
                    dispatchError(endpointInstance, session, e);
                }
            }
        }
    }

    public void dispatchOpen(Object endpointInstance, WebSocketSession session) {
        for (Method m : endpointInstance.getClass().getDeclaredMethods()) {
            if (m.isAnnotationPresent(WebSocketAnnotations.OnOpen.class)) {
                m.setAccessible(true);
                try {
                    if (m.getParameterCount() == 1 && m.getParameterTypes()[0] == WebSocketSession.class) {
                        m.invoke(endpointInstance, session);
                    } else if (m.getParameterCount() == 0) {
                        m.invoke(endpointInstance);
                    }
                } catch (Exception e) {
                    dispatchError(endpointInstance, session, e);
                }
            }
        }
    }

    public void dispatchClose(Object endpointInstance, WebSocketSession session) {
        for (Method m : endpointInstance.getClass().getDeclaredMethods()) {
            if (m.isAnnotationPresent(WebSocketAnnotations.OnClose.class)) {
                m.setAccessible(true);
                try {
                    if (m.getParameterCount() == 1 && m.getParameterTypes()[0] == WebSocketSession.class) {
                        m.invoke(endpointInstance, session);
                    } else if (m.getParameterCount() == 0) {
                        m.invoke(endpointInstance);
                    }
                } catch (Exception ignored) {}
            }
        }
    }

    public void dispatchError(Object endpointInstance, WebSocketSession session, Throwable error) {
        for (Method m : endpointInstance.getClass().getDeclaredMethods()) {
            if (m.isAnnotationPresent(WebSocketAnnotations.OnError.class)) {
                m.setAccessible(true);
                try {
                    m.invoke(endpointInstance, session, error);
                } catch (Exception ignored) {}
            }
        }
    }
}
