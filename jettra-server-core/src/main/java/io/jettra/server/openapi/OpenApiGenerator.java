package io.jettra.server.openapi;

import io.jettra.server.openapi.annotations.RequestBody;
import io.jettra.server.openapi.annotations.Schema;
import io.jettra.server.openapi.annotations.Tags;
import io.jettra.server.openapi.annotations.Tag;
import io.jettra.server.openapi.annotations.ApiResponse;
import io.jettra.server.openapi.annotations.Operation;
import io.jettra.server.openapi.annotations.OpenApi;
import io.jettra.rest.annotations.POST;
import io.jettra.rest.annotations.DELETE;
import io.jettra.rest.annotations.GET;
import io.jettra.rest.annotations.PathParam;
import io.jettra.rest.annotations.HeaderParam;
import io.jettra.rest.annotations.Path;
import io.jettra.rest.annotations.QueryParam;
import io.jettra.rest.annotations.PUT;
import io.jettra.json.JettraJson;
import io.jettra.json.JettraJsonBuilder;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public class OpenApiGenerator {

    public static String generate(List<Class<?>> controllers) {
        Map<String, Object> openapi = new LinkedHashMap<>();
        openapi.put("openapi", "3.0.0");

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("title", "Jettra API");
        info.put("version", "1.0.0");
        info.put("description", "Auto-generated OpenAPI documentation");

        openapi.put("info", info);

        Map<String, Object> paths = new LinkedHashMap<>();

        for (Class<?> clazz : controllers) {
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
            }

            String basePath = "";
            if (clazz.isAnnotationPresent(Path.class)) {
                basePath = clazz.getAnnotation(Path.class).value();
                if (!basePath.startsWith("/")) basePath = "/" + basePath;
            }

            for (Method method : clazz.getDeclaredMethods()) {
                String httpMethod = null;
                if (method.isAnnotationPresent(GET.class)) httpMethod = "get";
                else if (method.isAnnotationPresent(POST.class)) httpMethod = "post";
                else if (method.isAnnotationPresent(PUT.class)) httpMethod = "put";
                else if (method.isAnnotationPresent(DELETE.class)) httpMethod = "delete";

                if (httpMethod != null) {
                    String methodPath = basePath;
                    if (method.isAnnotationPresent(Path.class)) {
                        String mp = method.getAnnotation(Path.class).value();
                        if (!mp.startsWith("/")) mp = "/" + mp;
                        methodPath += mp;
                    }

                    Map<String, Object> pathItem = (Map<String, Object>) paths.computeIfAbsent(methodPath, k -> new LinkedHashMap<>());
                    Map<String, Object> operation = new LinkedHashMap<>();
                    
                    if (method.isAnnotationPresent(Operation.class)) {
                        Operation op = method.getAnnotation(Operation.class);
                        if (!op.summary().isEmpty()) operation.put("summary", op.summary());
                        if (!op.description().isEmpty()) operation.put("description", op.description());
                    } else {
                        operation.put("summary", method.getName());
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
                        Map<String, Object> param = new LinkedHashMap<>();
                        if (p.isAnnotationPresent(PathParam.class)) {
                            param.put("name", p.getAnnotation(PathParam.class).value());
                            param.put("in", "path");
                            param.put("required", true);
                        } else if (p.isAnnotationPresent(QueryParam.class)) {
                            param.put("name", p.getAnnotation(QueryParam.class).value());
                            param.put("in", "query");
                        } else if (p.isAnnotationPresent(HeaderParam.class)) {
                            param.put("name", p.getAnnotation(HeaderParam.class).value());
                            param.put("in", "header");
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
                            
                            continue; // Ignored in parameters list
                        }

                        if (p.isAnnotationPresent(io.jettra.server.openapi.annotations.Parameter.class)) {
                            io.jettra.server.openapi.annotations.Parameter pAnnot = p.getAnnotation(io.jettra.server.openapi.annotations.Parameter.class);
                            if (!pAnnot.description().isEmpty()) param.put("description", pAnnot.description());
                            param.put("required", pAnnot.required() || "path".equals(param.get("in")));
                        }

                        Map<String, Object> schema = new LinkedHashMap<>();
                        schema.put("type", "string");
                        if (p.isAnnotationPresent(Schema.class)) {
                            Schema s = p.getAnnotation(Schema.class);
                            if (!s.description().isEmpty()) schema.put("description", s.description());
                            if (!s.example().isEmpty()) schema.put("example", s.example());
                        }
                        param.put("schema", schema);

                        parameters.add(param);
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
                        defaultResp.put("description", "Successful operation");
                        responses.put("200", defaultResp);
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
        securitySchemes.put("bearerAuth", bearerAuth);
        components.put("securitySchemes", securitySchemes);
        openapi.put("components", components);

        List<Map<String, List<String>>> globalSecurity = new ArrayList<>();
        Map<String, List<String>> req = new LinkedHashMap<>();
        req.put("bearerAuth", new ArrayList<>());
        globalSecurity.add(req);
        openapi.put("security", globalSecurity);

        JettraJson gson = new JettraJsonBuilder().setPrettyPrinting().create();
        return gson.toJson(openapi);
    }
}
