package io.jettra.server.jakarta.rest;

import io.jettra.server.core.http.HttpMethod;
import io.jettra.server.core.http.HttpRequest;
import io.jettra.server.core.http.HttpResponse;
import io.jettra.server.core.http.HttpRouter;
import io.jettra.server.core.http.HttpStatus;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

/**
 * Despachador para recursos Jakarta REST (JAX-RS 3.1).
 * Descubre métodos anotados, resuelve parámetros de ruta, query y cuerpo, y formatea la respuesta en JSON.
 */
public class RestDispatcher {

    private final HttpRouter router;
    private final Map<Class<?>, Object> singletonInstances = new HashMap<>();

    public RestDispatcher(HttpRouter router) {
        this.router = router;
    }

    public synchronized void registerResource(Class<?> resourceClass) {
        try {
            Object instance = resourceClass.getDeclaredConstructor().newInstance();
            registerResourceInstance(instance);
        } catch (Exception e) {
            throw new RuntimeException("Error instanciando recurso REST: " + resourceClass.getName(), e);
        }
    }

    public synchronized void registerResource(Object instance) {
        if (instance instanceof Class<?> clazz) {
            registerResource(clazz);
        } else {
            registerResourceInstance(instance);
        }
    }

    public synchronized void registerResourceInstance(Object instance) {
        Class<?> clazz = instance.getClass();
        singletonInstances.put(clazz, instance);

        String basePath = "";
        RestAnnotations.Path classPath = clazz.getAnnotation(RestAnnotations.Path.class);
        if (classPath != null) {
            basePath = sanitizePath(classPath.value());
        }

        for (Method method : clazz.getDeclaredMethods()) {
            HttpMethod httpMethod = resolveHttpMethod(method);
            if (httpMethod == null) continue;

            String subPath = "";
            RestAnnotations.Path methodPath = method.getAnnotation(RestAnnotations.Path.class);
            if (methodPath != null) {
                subPath = sanitizePath(methodPath.value());
            }

            String fullPath = combinePaths(basePath, subPath);

            router.add(httpMethod, fullPath, (req, res) -> handleRestInvocation(instance, method, req, res));
        }
    }

    private void handleRestInvocation(Object instance, Method method, HttpRequest req, HttpResponse res) {
        try {
            Parameter[] parameters = method.getParameters();
            Object[] args = new Object[parameters.length];

            for (int i = 0; i < parameters.length; i++) {
                Parameter param = parameters[i];
                Class<?> paramType = param.getType();

                RestAnnotations.PathParam pathParam = param.getAnnotation(RestAnnotations.PathParam.class);
                if (pathParam != null) {
                    String val = req.getPathParam(pathParam.value());
                    args[i] = convertPrimitive(val, paramType);
                    continue;
                }

                RestAnnotations.QueryParam queryParam = param.getAnnotation(RestAnnotations.QueryParam.class);
                if (queryParam != null) {
                    String val = req.getQueryParam(queryParam.value());
                    args[i] = convertPrimitive(val, paramType);
                    continue;
                }

                RestAnnotations.HeaderParam headerParam = param.getAnnotation(RestAnnotations.HeaderParam.class);
                if (headerParam != null) {
                    String val = req.getHeader(headerParam.value());
                    args[i] = convertPrimitive(val, paramType);
                    continue;
                }

                if (paramType == HttpRequest.class) {
                    args[i] = req;
                    continue;
                }
                if (paramType == HttpResponse.class) {
                    args[i] = res;
                    continue;
                }

                // Si no tiene anotación de parámetro, es el cuerpo de la petición (JSON)
                if (req.getBody().length > 0) {
                    if (paramType == String.class) {
                        args[i] = req.getBodyAsString();
                    } else if (paramType == byte[].class) {
                        args[i] = req.getBody();
                    } else {
                        args[i] = req.getBodyAsJson(paramType);
                    }
                }
            }

            method.setAccessible(true);
            Object result = method.invoke(instance, args);

            if (res.isCommitted()) return;

            if (result == null) {
                res.status(HttpStatus.NO_CONTENT);
            } else if (result instanceof String s) {
                res.body(s);
            } else if (result instanceof byte[] b) {
                res.body(b);
            } else {
                res.json(result);
            }

        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            res.status(HttpStatus.INTERNAL_SERVER_ERROR)
               .json(Map.of("error", "REST invocation failed", "message", cause.getMessage() != null ? cause.getMessage() : "Internal Error"));
        }
    }

    private static HttpMethod resolveHttpMethod(Method method) {
        if (method.isAnnotationPresent(RestAnnotations.GET.class)) return HttpMethod.GET;
        if (method.isAnnotationPresent(RestAnnotations.POST.class)) return HttpMethod.POST;
        if (method.isAnnotationPresent(RestAnnotations.PUT.class)) return HttpMethod.PUT;
        if (method.isAnnotationPresent(RestAnnotations.DELETE.class)) return HttpMethod.DELETE;
        if (method.isAnnotationPresent(RestAnnotations.PATCH.class)) return HttpMethod.PATCH;
        return null;
    }

    private static String sanitizePath(String path) {
        if (path == null || path.isBlank()) return "";
        path = path.trim();
        if (!path.startsWith("/")) path = "/" + path;
        if (path.endsWith("/") && path.length() > 1) path = path.substring(0, path.length() - 1);
        return path;
    }

    private static String combinePaths(String base, String sub) {
        if (base.isEmpty()) return sub.isEmpty() ? "/" : sub;
        if (sub.isEmpty() || sub.equals("/")) return base;
        return base + sub;
    }

    private static Object convertPrimitive(String val, Class<?> type) {
        if (val == null) return null;
        if (type == String.class) return val;
        if (type == int.class || type == Integer.class) return Integer.parseInt(val);
        if (type == long.class || type == Long.class) return Long.parseLong(val);
        if (type == double.class || type == Double.class) return Double.parseDouble(val);
        if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(val);
        return val;
    }
}
