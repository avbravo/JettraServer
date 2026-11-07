package io.jettra.server.openapi;

import io.jettra.server.openapi.annotations.RequestBody;
import io.jettra.server.openapi.annotations.Schema;
import io.jettra.server.openapi.annotations.Tags;
import io.jettra.server.openapi.annotations.Tag;
import io.jettra.server.openapi.annotations.ApiResponse;
import io.jettra.server.openapi.annotations.Operation;
import io.jettra.server.openapi.annotations.OpenApi;
import io.jettra.json.JettraJson;
import io.jettra.json.JettraJsonBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * Generador automático de especificación OpenAPI 3.0.0.
 * Compatible tanto con anotaciones nativas JettraRest como con especificaciones estándar Jakarta REST (JAX-RS).
 */
public class OpenApiGenerator {

    public static String generate(List<Class<?>> controllers) {
        Map<String, Object> openapi = generateModel(controllers);
        JettraJson gson = new JettraJsonBuilder().setPrettyPrinting().create();
        return gson.toJson(openapi);
    }

    public static Map<String, Object> generateModel(List<Class<?>> controllers) {
        Map<String, Object> openapi = new LinkedHashMap<>();
        openapi.put("openapi", "3.0.0");

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("title", "JettraServer API");
        info.put("version", "1.0.0");
        info.put("description", "Documentación interactiva de microservicios con seguridad JWT y JettraSecurityDB");

        openapi.put("info", info);

        Map<String, Object> paths = new LinkedHashMap<>();

        if (controllers != null) {
            for (Class<?> clazz : controllers) {
                if (clazz == null) continue;

                if (clazz.isAnnotationPresent(OpenApi.class)) {
                    OpenApi apiConfig = clazz.getAnnotation(OpenApi.class);
                    if (!apiConfig.title().isEmpty()) info.put("title", apiConfig.title());
                    if (!apiConfig.version().isEmpty()) info.put("version", apiConfig.version());
                    if (!apiConfig.description().isEmpty()) info.put("description", apiConfig.description());
                }

                List<String> classTags = new ArrayList<>();
                if (clazz.isAnnotationPresent(Tag.class)) {
                    Tag tag = clazz.getAnnotation(Tag.class);
                    classTags.add(tag.name());
                } else if (clazz.isAnnotationPresent(Tags.class)) {
                    for (Tag t : clazz.getAnnotation(Tags.class).value()) {
                        classTags.add(t.name());
                    }
                } else {
                    classTags.add(clazz.getSimpleName().replace("Controller", "").replace("Resource", ""));
                }

                List<String> classRoles = getRolesAllowed(clazz);

                String basePath = getPathValue(clazz);
                if (basePath == null) basePath = "";
                if (!basePath.isEmpty() && !basePath.startsWith("/")) basePath = "/" + basePath;

                for (Method method : clazz.getDeclaredMethods()) {
                    String httpMethod = getHttpMethod(method);
                    if (httpMethod == null) continue;

                    String methodPath = basePath;
                    String mp = getPathValue(method);
                    if (mp != null) {
                        if (!mp.startsWith("/")) mp = "/" + mp;
                        methodPath += mp;
                    }
                    if (methodPath.isEmpty()) methodPath = "/";

                    Map<String, Object> pathItem = (Map<String, Object>) paths.computeIfAbsent(methodPath, k -> new LinkedHashMap<>());
                    Map<String, Object> operation = new LinkedHashMap<>();

                    List<String> methodRoles = getRolesAllowed(method);
                    List<String> effectiveRoles = methodRoles != null ? methodRoles : classRoles;

                    if (effectiveRoles != null && !effectiveRoles.isEmpty()) {
                        operation.put("x-roles-allowed", effectiveRoles);
                    }

                    if (method.isAnnotationPresent(Operation.class)) {
                        Operation op = method.getAnnotation(Operation.class);
                        if (!op.summary().isEmpty()) operation.put("summary", op.summary());
                        if (!op.description().isEmpty()) operation.put("description", op.description());
                    } else {
                        String summary = method.getName();
                        if (effectiveRoles != null && !effectiveRoles.isEmpty()) {
                            summary += " [🔒 " + String.join(", ", effectiveRoles) + "]";
                        }
                        operation.put("summary", summary);
                    }

                    List<String> methodTags = new ArrayList<>(classTags);
                    if (method.isAnnotationPresent(Tag.class)) {
                        Tag tag = method.getAnnotation(Tag.class);
                        if (!methodTags.contains(tag.name())) methodTags.add(tag.name());
                    }
                    if (!methodTags.isEmpty()) {
                        operation.put("tags", methodTags);
                    }

                    List<Map<String, Object>> parameters = new ArrayList<>();
                    for (Parameter p : method.getParameters()) {
                        String paramIn = null;
                        String paramName = null;

                        for (Annotation a : p.getAnnotations()) {
                            String aName = a.annotationType().getSimpleName();
                            if ("PathParam".equals(aName)) {
                                paramIn = "path";
                                paramName = getAnnotationStringValue(a);
                            } else if ("QueryParam".equals(aName)) {
                                paramIn = "query";
                                paramName = getAnnotationStringValue(a);
                            } else if ("HeaderParam".equals(aName)) {
                                paramIn = "header";
                                paramName = getAnnotationStringValue(a);
                            }
                        }

                        if (paramIn != null) {
                            Map<String, Object> param = new LinkedHashMap<>();
                            param.put("name", paramName != null ? paramName : p.getName());
                            param.put("in", paramIn);
                            param.put("required", "path".equals(paramIn));

                            if (p.isAnnotationPresent(io.jettra.server.openapi.annotations.Parameter.class)) {
                                var pAnnot = p.getAnnotation(io.jettra.server.openapi.annotations.Parameter.class);
                                if (!pAnnot.description().isEmpty()) param.put("description", pAnnot.description());
                                if (pAnnot.required()) param.put("required", true);
                            }

                            Map<String, Object> schema = new LinkedHashMap<>();
                            schema.put("type", getJsonType(p.getType()));
                            if (p.isAnnotationPresent(Schema.class)) {
                                Schema s = p.getAnnotation(Schema.class);
                                if (!s.description().isEmpty()) schema.put("description", s.description());
                                if (!s.example().isEmpty()) schema.put("example", s.example());
                            }
                            param.put("schema", schema);
                            parameters.add(param);
                        } else {
                            // Body parameter
                            Map<String, Object> requestBodyMap = new LinkedHashMap<>();
                            if (p.isAnnotationPresent(RequestBody.class)) {
                                RequestBody reqBody = p.getAnnotation(RequestBody.class);
                                if (!reqBody.description().isEmpty()) requestBodyMap.put("description", reqBody.description());
                                requestBodyMap.put("required", reqBody.required());
                            } else {
                                requestBodyMap.put("required", true);
                            }

                            Map<String, Object> content = new LinkedHashMap<>();
                            Map<String, Object> mediaType = new LinkedHashMap<>();
                            Map<String, Object> bodySchema = new LinkedHashMap<>();
                            bodySchema.put("type", "object");
                            if (p.isAnnotationPresent(Schema.class)) {
                                Schema s = p.getAnnotation(Schema.class);
                                if (!s.description().isEmpty()) bodySchema.put("description", s.description());
                                if (!s.example().isEmpty()) bodySchema.put("example", s.example());
                            }
                            mediaType.put("schema", bodySchema);
                            content.put("application/json", mediaType);
                            requestBodyMap.put("content", content);

                            operation.put("requestBody", requestBodyMap);
                        }
                    }

                    if (!parameters.isEmpty()) {
                        operation.put("parameters", parameters);
                    }

                    Map<String, Object> responses = new LinkedHashMap<>();
                    if (method.isAnnotationPresent(ApiResponse.class)) {
                        for (ApiResponse r : method.getAnnotationsByType(ApiResponse.class)) {
                            Map<String, Object> respDesc = new LinkedHashMap<>();
                            respDesc.put("description", r.description());
                            responses.put(r.responseCode(), respDesc);
                        }
                    } else {
                        Map<String, Object> defaultResp = new LinkedHashMap<>();
                        defaultResp.put("description", "Operación exitosa");
                        responses.put("200", defaultResp);
                        if (effectiveRoles != null && !effectiveRoles.isEmpty()) {
                            Map<String, Object> r401 = new LinkedHashMap<>();
                            r401.put("description", "No autorizado (Requiere Bearer JWT)");
                            responses.put("401", r401);
                            Map<String, Object> r403 = new LinkedHashMap<>();
                            r403.put("description", "Prohibido (Requiere roles: " + String.join(", ", effectiveRoles) + ")");
                            responses.put("403", r403);
                        }
                    }
                    operation.put("responses", responses);

                    pathItem.put(httpMethod, operation);
                }
            }
        }

        openapi.put("paths", paths);

        Map<String, Object> components = new LinkedHashMap<>();
        Map<String, Object> securitySchemes = new LinkedHashMap<>();
        Map<String, Object> bearerAuth = new LinkedHashMap<>();
        bearerAuth.put("type", "http");
        bearerAuth.put("scheme", "bearer");
        bearerAuth.put("bearerFormat", "JWT");
        bearerAuth.put("description", "Acreditación mediante JWT Bearer token emitido por JettraSecurityDB.");
        securitySchemes.put("bearerAuth", bearerAuth);
        components.put("securitySchemes", securitySchemes);
        openapi.put("components", components);

        List<Map<String, List<String>>> globalSecurity = new ArrayList<>();
        Map<String, List<String>> req = new LinkedHashMap<>();
        req.put("bearerAuth", new ArrayList<>());
        globalSecurity.add(req);
        openapi.put("security", globalSecurity);

        return openapi;
    }

    private static String getPathValue(AnnotatedElement element) {
        for (Annotation a : element.getAnnotations()) {
            if ("Path".equals(a.annotationType().getSimpleName())) {
                return getAnnotationStringValue(a);
            }
        }
        return null;
    }

    private static String getHttpMethod(Method method) {
        for (Annotation a : method.getAnnotations()) {
            String name = a.annotationType().getSimpleName();
            if ("GET".equals(name)) return "get";
            if ("POST".equals(name)) return "post";
            if ("PUT".equals(name)) return "put";
            if ("DELETE".equals(name)) return "delete";
            if ("PATCH".equals(name)) return "patch";
        }
        return null;
    }

    private static List<String> getRolesAllowed(AnnotatedElement element) {
        for (Annotation a : element.getAnnotations()) {
            if ("RolesAllowed".equals(a.annotationType().getSimpleName())) {
                try {
                    Object val = a.annotationType().getMethod("value").invoke(a);
                    if (val instanceof String[] arr) {
                        return Arrays.asList(arr);
                    } else if (val instanceof String str) {
                        return List.of(str);
                    }
                } catch (Exception ignored) {}
            }
        }
        return null;
    }

    private static String getAnnotationStringValue(Annotation a) {
        try {
            Object val = a.annotationType().getMethod("value").invoke(a);
            if (val != null) return val.toString();
        } catch (Exception ignored) {}
        return null;
    }

    private static String getJsonType(Class<?> type) {
        if (type == int.class || type == Integer.class || type == long.class || type == Long.class) return "integer";
        if (type == double.class || type == Double.class || type == float.class || type == Float.class) return "number";
        if (type == boolean.class || type == Boolean.class) return "boolean";
        return "string";
    }
}
