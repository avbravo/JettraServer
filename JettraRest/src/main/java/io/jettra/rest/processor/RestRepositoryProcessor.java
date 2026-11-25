package io.jettra.rest.processor;

import io.jettra.rest.annotations.RestRepository;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Procesador de anotaciones en tiempo de compilación para {@link RestRepository}.
 * <p>
 * A partir de una interfaz anotada con {@code @Repository} y {@code @RestRepository},
 * genera la clase de recurso REST correspondiente (ej. {@code ProductRest} a partir de
 * {@code ProductRepository}), exponiendo los endpoints HTTP de cada método e integrando
 * la seguridad declarativa ({@code @Secured}, {@code @RolesAllowed}, {@code @DeclareRoles},
 * {@code @PermitAll}, {@code @DenyAll}) compatible con {@code JettraRestServer} y {@code JettraJWT}.
 */
@SupportedAnnotationTypes("io.jettra.rest.annotations.RestRepository")
public class RestRepositoryProcessor extends AbstractProcessor {

    private static final String ANN_PKG = "io.jettra.rest.annotations.";
    private static final String ACCRED_PKG = "io.jettra.rest.annotations.accreditation.";
    private static final Pattern PATH_PARAM_PATTERN = Pattern.compile("\\{([^}]+)\\}");

    private static final Set<String> SCALAR_TYPES = Set.of(
            "java.lang.String",
            "java.lang.Integer", "int",
            "java.lang.Long", "long",
            "java.lang.Double", "double",
            "java.lang.Float", "float",
            "java.lang.Boolean", "boolean",
            "java.lang.Short", "short",
            "java.lang.Byte", "byte",
            "java.lang.Character", "char",
            "java.util.UUID",
            "java.math.BigDecimal",
            "java.math.BigInteger",
            "java.time.LocalDate",
            "java.time.LocalDateTime",
            "java.time.Instant"
    );

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }
        for (Element element : roundEnv.getElementsAnnotatedWith(RestRepository.class)) {
            if (element.getKind() != ElementKind.INTERFACE) {
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        "@RestRepository solo puede aplicarse sobre interfaces (encontrado en " + element.getSimpleName() + ").",
                        element
                );
                continue;
            }
            TypeElement repoInterface = (TypeElement) element;
            if (!hasAnnotationBySimpleName(repoInterface, "Repository")) {
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        "@RestRepository requiere que la interfaz '" + repoInterface.getSimpleName()
                                + "' también esté anotada con @Repository.",
                        repoInterface
                );
                continue;
            }
            generateRestResource(repoInterface);
        }
        return true;
    }

    private void generateRestResource(TypeElement repoInterface) {
        RestRepository cfg = repoInterface.getAnnotation(RestRepository.class);
        String interfaceSimpleName = repoInterface.getSimpleName().toString();
        String interfaceQualifiedName = repoInterface.getQualifiedName().toString();
        PackageElement pkgEl = processingEnv.getElementUtils().getPackageOf(repoInterface);
        String sourcePkg = pkgEl.isUnnamed() ? "" : pkgEl.getQualifiedName().toString();

        String targetPkg = (cfg.targetPackage() != null && !cfg.targetPackage().isBlank())
                ? cfg.targetPackage().trim()
                : sourcePkg;

        String resourceSimpleName = deriveResourceClassName(interfaceSimpleName, cfg.resourceName());
        String basePath = deriveBasePath(interfaceSimpleName, cfg.path());
        String mediaType = (cfg.mediaType() != null && !cfg.mediaType().isBlank())
                ? cfg.mediaType().trim()
                : "application/json";

        String qualifiedResourceName = targetPkg.isEmpty()
                ? resourceSimpleName
                : targetPkg + "." + resourceSimpleName;

        // Seguridad a nivel de clase
        boolean explicitPermitAll = hasAnnotationBySimpleName(repoInterface, "PermitAll");
        boolean explicitDenyAll = hasAnnotationBySimpleName(repoInterface, "DenyAll");
        boolean explicitSecured = hasAnnotationBySimpleName(repoInterface, "Secured");

        Set<String> classRoles = new LinkedHashSet<>();
        for (String r : cfg.rolesAllowed()) {
            if (r != null && !r.isBlank()) classRoles.add(r.trim());
        }
        classRoles.addAll(extractStringArrayAnnotationValue(repoInterface, "RolesAllowed"));

        Set<String> declaredRoles = new LinkedHashSet<>();
        for (String r : cfg.declareRoles()) {
            if (r != null && !r.isBlank()) declaredRoles.add(r.trim());
        }
        declaredRoles.addAll(extractStringArrayAnnotationValue(repoInterface, "DeclareRoles"));

        boolean emitClassSecured = !explicitPermitAll && !explicitDenyAll
                && (explicitSecured || cfg.secured() || !classRoles.isEmpty());

        try {
            JavaFileObject file = processingEnv.getFiler().createSourceFile(qualifiedResourceName, repoInterface);
            try (PrintWriter out = new PrintWriter(file.openWriter())) {
                if (!targetPkg.isEmpty()) {
                    out.println("package " + targetPkg + ";");
                    out.println();
                }

                if (!targetPkg.equals(sourcePkg) && !sourcePkg.isEmpty()) {
                    out.println("import " + interfaceQualifiedName + ";");
                }
                out.println("import io.jettra.core.inject.annotation.Inject;");
                out.println("import " + ANN_PKG + "Consumes;");
                out.println("import " + ANN_PKG + "DELETE;");
                out.println("import " + ANN_PKG + "DenyAll;");
                out.println("import " + ANN_PKG + "GET;");
                out.println("import " + ANN_PKG + "HeaderParam;");
                out.println("import " + ANN_PKG + "PATCH;");
                out.println("import " + ANN_PKG + "POST;");
                out.println("import " + ANN_PKG + "PUT;");
                out.println("import " + ANN_PKG + "Path;");
                out.println("import " + ANN_PKG + "PathParam;");
                out.println("import " + ANN_PKG + "PermitAll;");
                out.println("import " + ANN_PKG + "Produces;");
                out.println("import " + ANN_PKG + "QueryParam;");
                out.println("import " + ANN_PKG + "Secured;");
                out.println("import " + ACCRED_PKG + "DeclareRoles;");
                out.println("import " + ACCRED_PKG + "RolesAllowed;");
                out.println();
                out.println("/**");
                out.println(" * Recurso REST generado automáticamente en tiempo de compilación por RestRepositoryProcessor");
                out.println(" * a partir de {@link " + interfaceSimpleName + "}.");
                out.println(" */");
                out.println("@Path(\"" + escapeJava(basePath) + "\")");
                out.println("@Produces(\"" + escapeJava(mediaType) + "\")");
                out.println("@Consumes(\"" + escapeJava(mediaType) + "\")");
                if (explicitDenyAll) {
                    out.println("@DenyAll");
                } else if (explicitPermitAll) {
                    out.println("@PermitAll");
                } else if (emitClassSecured) {
                    out.println("@Secured");
                }
                if (!declaredRoles.isEmpty()) {
                    out.println("@DeclareRoles(" + formatStringArray(declaredRoles) + ")");
                }
                if (!classRoles.isEmpty()) {
                    out.println("@RolesAllowed(" + formatStringArray(classRoles) + ")");
                }
                out.println("public class " + resourceSimpleName + " {");
                out.println();
                out.println("    @Inject");
                out.println("    private " + interfaceSimpleName + " repository;");
                out.println();
                out.println("    public " + resourceSimpleName + "() {");
                out.println("    }");
                out.println();
                out.println("    public " + resourceSimpleName + "(" + interfaceSimpleName + " repository) {");
                out.println("        this.repository = repository;");
                out.println("    }");
                out.println();
                out.println("    public " + interfaceSimpleName + " getRepository() {");
                out.println("        return this.repository;");
                out.println("    }");
                out.println();
                out.println("    public void setRepository(" + interfaceSimpleName + " repository) {");
                out.println("        this.repository = repository;");
                out.println("    }");
                out.println();

                for (Element enclosed : repoInterface.getEnclosedElements()) {
                    if (enclosed.getKind() != ElementKind.METHOD) {
                        continue;
                    }
                    ExecutableElement method = (ExecutableElement) enclosed;
                    Set<Modifier> mods = method.getModifiers();
                    if (mods.contains(Modifier.STATIC) || mods.contains(Modifier.PRIVATE)) {
                        continue;
                    }
                    writeEndpointMethod(out, method);
                }

                out.println("}");
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    "No se pudo generar la clase REST '" + qualifiedResourceName + "': " + e.getMessage(),
                    repoInterface
            );
        }
    }

    private void writeEndpointMethod(PrintWriter out, ExecutableElement method) {
        String methodName = method.getSimpleName().toString();
        TypeMirror returnType = method.getReturnType();
        List<? extends VariableElement> params = method.getParameters();

        EndpointSpec spec = resolveEndpointSpec(method, methodName, params);

        // Anotaciones HTTP y ruta
        out.println("    @" + spec.httpMethod);
        if (spec.subPath != null && !spec.subPath.isEmpty()) {
            out.println("    @Path(\"" + escapeJava(spec.subPath) + "\")");
        }

        // Seguridad a nivel de método (propaga @PermitAll, @DenyAll, @Secured, @RolesAllowed)
        if (hasAnnotationBySimpleName(method, "DenyAll")) {
            out.println("    @DenyAll");
        } else if (hasAnnotationBySimpleName(method, "PermitAll")) {
            out.println("    @PermitAll");
        } else if (hasAnnotationBySimpleName(method, "Secured")) {
            out.println("    @Secured");
        }
        List<String> methodRoles = extractStringArrayAnnotationValue(method, "RolesAllowed");
        if (!methodRoles.isEmpty()) {
            out.println("    @RolesAllowed(" + formatStringArray(methodRoles) + ")");
        }

        // Firma de parámetros con anotaciones @PathParam / @QueryParam / @HeaderParam o body
        List<String> paramDecls = new ArrayList<>();
        List<String> argNames = new ArrayList<>();
        for (int i = 0; i < params.size(); i++) {
            VariableElement p = params.get(i);
            String pName = p.getSimpleName().toString();
            String pType = p.asType().toString();
            argNames.add(pName);

            String explicitHeader = extractSingleStringAnnotationValue(p, "HeaderParam");
            String explicitPath = extractSingleStringAnnotationValue(p, "PathParam");
            String explicitQuery = extractSingleStringAnnotationValue(p, "QueryParam");

            if (explicitHeader != null) {
                paramDecls.add("@HeaderParam(\"" + escapeJava(explicitHeader) + "\") " + pType + " " + pName);
            } else if (explicitPath != null) {
                paramDecls.add("@PathParam(\"" + escapeJava(explicitPath) + "\") " + pType + " " + pName);
            } else if (explicitQuery != null) {
                paramDecls.add("@QueryParam(\"" + escapeJava(explicitQuery) + "\") " + pType + " " + pName);
            } else if (spec.pathParamNames.contains(pName)) {
                paramDecls.add("@PathParam(\"" + escapeJava(pName) + "\") " + pType + " " + pName);
            } else if (isScalarType(p.asType())) {
                paramDecls.add("@QueryParam(\"" + escapeJava(pName) + "\") " + pType + " " + pName);
            } else {
                // Entidad / DTO / Record deserializado desde el cuerpo JSON
                paramDecls.add(pType + " " + pName);
            }
        }

        String returnTypeStr = returnType.toString();
        out.println("    public " + returnTypeStr + " " + methodName + "(" + String.join(", ", paramDecls) + ") {");
        out.println("        if (this.repository == null) {");
        out.println("            throw new IllegalStateException(\"Repositorio no inyectado en \" + getClass().getSimpleName());");
        out.println("        }");
        String call = "this.repository." + methodName + "(" + String.join(", ", argNames) + ");";
        if (returnType.getKind() == TypeKind.VOID) {
            out.println("        " + call);
        } else {
            out.println("        return " + call);
        }
        out.println("    }");
        out.println();
    }

    private record EndpointSpec(String httpMethod, String subPath, Set<String> pathParamNames) {
    }

    private EndpointSpec resolveEndpointSpec(ExecutableElement method, String methodName,
                                             List<? extends VariableElement> params) {
        String explicitHttp = detectExplicitHttpMethod(method);
        String explicitPath = extractSingleStringAnnotationValue(method, "Path");

        if (explicitHttp != null) {
            String normalized = normalizeSubPath(explicitPath);
            Set<String> pathParams = extractPathVariables(normalized);
            return new EndpointSpec(explicitHttp, normalized, pathParams);
        }

        String lower = methodName.toLowerCase(Locale.ROOT);
        int paramCount = params.size();

        // 1. Operaciones por ID (1 parámetro escalar)
        if (paramCount == 1 && isScalarType(params.get(0).asType())) {
            String pName = params.get(0).getSimpleName().toString();
            if (lower.equals("findbyid") || lower.equals("getbyid") || lower.equals("find")) {
                String path = normalizeSubPath(explicitPath != null ? explicitPath : "/{" + pName + "}");
                return new EndpointSpec("GET", path, extractPathVariables(path));
            }
            if (lower.equals("existsbyid")) {
                String path = normalizeSubPath(explicitPath != null ? explicitPath : "/{" + pName + "}/exists");
                return new EndpointSpec("GET", path, extractPathVariables(path));
            }
            if (lower.equals("deletebyid") || lower.equals("delete") || lower.equals("removebyid") || lower.equals("remove")) {
                String path = normalizeSubPath(explicitPath != null ? explicitPath : "/{" + pName + "}");
                return new EndpointSpec("DELETE", path, extractPathVariables(path));
            }
        }

        // 2. Colección completa / conteo / borrado masivo
        if (paramCount == 0) {
            if (lower.equals("findall") || lower.equals("list") || lower.equals("getall")) {
                String path = normalizeSubPath(explicitPath != null ? explicitPath : "");
                return new EndpointSpec("GET", path, extractPathVariables(path));
            }
            if (lower.equals("count")) {
                String path = normalizeSubPath(explicitPath != null ? explicitPath : "/count");
                return new EndpointSpec("GET", path, extractPathVariables(path));
            }
            if (lower.equals("deleteall") || lower.equals("clear")) {
                String path = normalizeSubPath(explicitPath != null ? explicitPath : "");
                return new EndpointSpec("DELETE", path, extractPathVariables(path));
            }
        }

        // 3. Creación (POST)
        if (lower.startsWith("save") || lower.startsWith("create") || lower.startsWith("insert") || lower.startsWith("add")) {
            String defaultPath = (paramCount == 1 && !isScalarType(params.get(0).asType()))
                    ? ""
                    : "/" + toKebabCase(methodName);
            String path = normalizeSubPath(explicitPath != null ? explicitPath : defaultPath);
            return new EndpointSpec("POST", path, extractPathVariables(path));
        }

        // 4. Actualización completa (PUT)
        if (lower.startsWith("update") || lower.startsWith("replace")) {
            String defaultPath;
            if (paramCount == 2 && isScalarType(params.get(0).asType()) && !isScalarType(params.get(1).asType())) {
                defaultPath = "/{" + params.get(0).getSimpleName() + "}";
            } else if (paramCount == 1 && !isScalarType(params.get(0).asType())) {
                defaultPath = "";
            } else {
                defaultPath = "/" + toKebabCase(methodName);
            }
            String path = normalizeSubPath(explicitPath != null ? explicitPath : defaultPath);
            return new EndpointSpec("PUT", path, extractPathVariables(path));
        }

        // 5. Actualización parcial (PATCH)
        if (lower.startsWith("patch") || lower.startsWith("partialupdate")) {
            String defaultPath = (paramCount >= 1 && isScalarType(params.get(0).asType()))
                    ? "/{" + params.get(0).getSimpleName() + "}"
                    : "";
            String path = normalizeSubPath(explicitPath != null ? explicitPath : defaultPath);
            return new EndpointSpec("PATCH", path, extractPathVariables(path));
        }

        // 6. Borrado personalizado (DELETE)
        if (lower.startsWith("delete") || lower.startsWith("remove")) {
            String path = normalizeSubPath(explicitPath != null ? explicitPath : "/" + toKebabCase(methodName));
            return new EndpointSpec("DELETE", path, extractPathVariables(path));
        }

        // 7. Consultas / búsquedas (GET)
        if (lower.startsWith("find") || lower.startsWith("get") || lower.startsWith("search")
                || lower.startsWith("list") || lower.startsWith("exists") || lower.startsWith("count")) {
            String path = normalizeSubPath(explicitPath != null ? explicitPath : "/" + toKebabCase(methodName));
            return new EndpointSpec("GET", path, extractPathVariables(path));
        }

        // 8. Respaldo general según presencia de body complejo
        boolean hasComplexParam = params.stream().anyMatch(p -> !isScalarType(p.asType()));
        String fallbackHttp = hasComplexParam ? "POST" : "GET";
        String path = normalizeSubPath(explicitPath != null ? explicitPath : "/" + toKebabCase(methodName));
        return new EndpointSpec(fallbackHttp, path, extractPathVariables(path));
    }

    private String detectExplicitHttpMethod(Element element) {
        if (hasAnnotationBySimpleName(element, "GET")) return "GET";
        if (hasAnnotationBySimpleName(element, "POST")) return "POST";
        if (hasAnnotationBySimpleName(element, "PUT")) return "PUT";
        if (hasAnnotationBySimpleName(element, "DELETE")) return "DELETE";
        if (hasAnnotationBySimpleName(element, "PATCH")) return "PATCH";
        return null;
    }

    private boolean isScalarType(TypeMirror type) {
        if (type.getKind().isPrimitive()) {
            return true;
        }
        String name = type.toString();
        int genericIdx = name.indexOf('<');
        if (genericIdx > 0) {
            name = name.substring(0, genericIdx);
        }
        return SCALAR_TYPES.contains(name);
    }

    private static String deriveResourceClassName(String interfaceSimpleName, String customName) {
        if (customName != null && !customName.isBlank()) {
            return customName.trim();
        }
        if (interfaceSimpleName.endsWith("Repository") && interfaceSimpleName.length() > "Repository".length()) {
            return interfaceSimpleName.substring(0, interfaceSimpleName.length() - "Repository".length()) + "Rest";
        }
        return interfaceSimpleName + "Rest";
    }

    private static String deriveBasePath(String interfaceSimpleName, String customPath) {
        if (customPath != null && !customPath.isBlank()) {
            String trimmed = customPath.trim();
            return trimmed.startsWith("/") ? trimmed : "/" + trimmed;
        }
        String stem = interfaceSimpleName.endsWith("Repository") && interfaceSimpleName.length() > "Repository".length()
                ? interfaceSimpleName.substring(0, interfaceSimpleName.length() - "Repository".length())
                : interfaceSimpleName;
        return "/" + toKebabCase(stem);
    }

    private static String normalizeSubPath(String rawPath) {
        if (rawPath == null || rawPath.isBlank() || "/".equals(rawPath.trim())) {
            return "";
        }
        String p = rawPath.trim();
        return p.startsWith("/") ? p : "/" + p;
    }

    private static Set<String> extractPathVariables(String path) {
        Set<String> vars = new LinkedHashSet<>();
        if (path == null || path.isEmpty()) return vars;
        Matcher m = PATH_PARAM_PATTERN.matcher(path);
        while (m.find()) {
            vars.add(m.group(1).trim());
        }
        return vars;
    }

    private static String toKebabCase(String camel) {
        if (camel == null || camel.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < camel.length(); i++) {
            char c = camel.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) sb.append('-');
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static boolean hasAnnotationBySimpleName(Element element, String simpleName) {
        for (AnnotationMirror am : element.getAnnotationMirrors()) {
            if (am.getAnnotationType().asElement().getSimpleName().toString().equals(simpleName)) {
                return true;
            }
        }
        return false;
    }

    private static String extractSingleStringAnnotationValue(Element element, String simpleName) {
        for (AnnotationMirror am : element.getAnnotationMirrors()) {
            if (am.getAnnotationType().asElement().getSimpleName().toString().equals(simpleName)) {
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : am.getElementValues().entrySet()) {
                    if ("value".equals(entry.getKey().getSimpleName().toString())) {
                        Object val = entry.getValue().getValue();
                        return val != null ? String.valueOf(val) : null;
                    }
                }
            }
        }
        return null;
    }

    private static List<String> extractStringArrayAnnotationValue(Element element, String simpleName) {
        List<String> result = new ArrayList<>();
        for (AnnotationMirror am : element.getAnnotationMirrors()) {
            if (am.getAnnotationType().asElement().getSimpleName().toString().equals(simpleName)) {
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : am.getElementValues().entrySet()) {
                    if ("value".equals(entry.getKey().getSimpleName().toString())) {
                        Object val = entry.getValue().getValue();
                        if (val instanceof List<?> list) {
                            for (Object item : list) {
                                if (item instanceof AnnotationValue av && av.getValue() != null) {
                                    String s = String.valueOf(av.getValue()).trim();
                                    if (!s.isEmpty()) result.add(s);
                                }
                            }
                        } else if (val != null) {
                            String s = String.valueOf(val).trim();
                            if (!s.isEmpty()) result.add(s);
                        }
                    }
                }
            }
        }
        return result;
    }

    private static String formatStringArray(Iterable<String> items) {
        List<String> quoted = new ArrayList<>();
        for (String item : items) {
            quoted.add("\"" + escapeJava(item) + "\"");
        }
        return "{" + String.join(", ", quoted) + "}";
    }

    private static String escapeJava(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
