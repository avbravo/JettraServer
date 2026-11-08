package io.jettra.flux.security;

import io.jettra.core.login.NoLoginRequired;

import java.io.InputStream;
import java.lang.annotation.AnnotationTypeMismatchException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Navigation interceptor and role-based access evaluator for JettraFlux pages.
 * Validates authentication and authorization declaratively before any widget lifecycle begins.
 */
public final class PageSecurityGuard {

    private PageSecurityGuard() {}

    public record PageSecurityMetadata(
        boolean secured,
        boolean noLoginRequired,
        Set<String> requiredRoles,
        String requiredDepartment
    ) {
        public static final PageSecurityMetadata UNRESTRICTED =
                new PageSecurityMetadata(false, true, Set.of(), "");
    }

    /**
     * Resolves declarative security metadata from the specified class by reflection.
     */
    public static PageSecurityMetadata resolveMetadata(Class<?> pageClass) {
        if (pageClass == null) {
            return PageSecurityMetadata.UNRESTRICTED;
        }

        // 1. Inspect @PageWidgetAllow from jcf.annotation
        if (pageClass.isAnnotationPresent(jcf.annotation.PageWidgetAllow.class)) {
            jcf.annotation.PageWidgetAllow ann = pageClass.getAnnotation(jcf.annotation.PageWidgetAllow.class);
            Set<String> roles = extractRoles(ann.role());
            String dept = ann.department() != null ? ann.department().trim() : "";
            return new PageSecurityMetadata(true, false, roles, dept);
        }

        // 2. Inspect @PageWidgetAllow from io.jettra.core.security.widget (legacy / core)
        if (pageClass.isAnnotationPresent(io.jettra.core.security.widget.PageWidgetAllow.class)) {
            io.jettra.core.security.widget.PageWidgetAllow ann =
                    pageClass.getAnnotation(io.jettra.core.security.widget.PageWidgetAllow.class);
            Set<String> roles = extractCoreRoles(ann);
            String dept = ann.department() != null ? ann.department().trim() : "";
            return new PageSecurityMetadata(true, false, roles, dept);
        }

        // 3. Inspect @NoLoginRequired
        if (pageClass.isAnnotationPresent(NoLoginRequired.class)) {
            return new PageSecurityMetadata(false, true, Set.of(), "");
        }

        // 4. Default: authenticated access required, but no specific role restrictions
        return new PageSecurityMetadata(true, false, Set.of(), "");
    }

    /**
     * Evaluates whether the current security context is permitted to access the page class.
     */
    public static SecurityDecision evaluate(Class<?> pageClass, SecurityContext context) {
        PageSecurityMetadata meta = resolveMetadata(pageClass);

        // Explicitly public via @NoLoginRequired
        if (meta.noLoginRequired()) {
            return new SecurityDecision.Granted();
        }

        // Target page requires security: verify authentication first
        if (!context.isAuthenticated() || context.principal() == null) {
            return new SecurityDecision.RedirectToLogin("/login");
        }

        SecurityPrincipal principal = context.principal();

        // Verify role authorization if required roles are specified
        if (!meta.requiredRoles().isEmpty()) {
            boolean authorized = meta.requiredRoles().stream()
                    .anyMatch(r -> principal.hasRole(r) || checkSynonymForPrincipal(r, principal));

            if (!authorized) {
                String rolesStr = String.join(", ", meta.requiredRoles());
                return new SecurityDecision.Denied(
                    403,
                    "No cuentas con los privilegios necesarios (" + rolesStr + ") para ver esta página.",
                    meta.requiredRoles()
                );
            }
        }

        // Verify department restriction if specified
        if (!meta.requiredDepartment().isEmpty()) {
            if (!meta.requiredDepartment().equalsIgnoreCase(principal.department())) {
                return new SecurityDecision.Denied(
                    403,
                    "No perteneces al departamento requerido para ver esta página.",
                    Set.of()
                );
            }
        }

        return new SecurityDecision.Granted();
    }

    private static Set<String> extractRoles(Object[] roleArray) {
        if (roleArray == null || roleArray.length == 0) {
            return Set.of();
        }
        return Arrays.stream(roleArray)
                .filter(Objects::nonNull)
                .map(r -> (r instanceof Enum<?> e) ? e.name() : String.valueOf(r))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }

    private static Set<String> extractCoreRoles(io.jettra.core.security.widget.PageWidgetAllow ann) {
        try {
            return extractRoles((Object[]) ann.role());
        } catch (AnnotationTypeMismatchException e) {
            // Safe fallback if different AppRole classloader occurs
            Set<String> matched = new HashSet<>();
            Matcher m = Pattern.compile("AppRole\\.([A-Z0-9_]+)").matcher(e.getMessage());
            while (m.find()) {
                matched.add(m.group(1));
            }
            return Collections.unmodifiableSet(matched);
        } catch (Exception e) {
            return Set.of();
        }
    }

    private static boolean checkSynonymForPrincipal(String requiredRole, SecurityPrincipal principal) {
        for (String userRole : principal.roles()) {
            if (checkSynonym(requiredRole, userRole)) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkSynonym(String pluginRole, String userRole) {
        if (userRole == null || userRole.isEmpty()) return false;
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("plugin-config.json")) {
            if (is != null) {
                String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                String pRoleStr = "\"plugin-role\"\\s*:\\s*\"" + Pattern.quote(pluginRole) + "\"";
                String pSecRoleStr = "\"plugin-security-role\"\\s*:\\s*\"" + Pattern.quote(pluginRole) + "\"";
                String aRoleStr = "\"applicative-role\"\\s*:\\s*\"" + Pattern.quote(userRole) + "\"";
                String aRoleStrLegacy = "\"application-role\"\\s*:\\s*\"" + Pattern.quote(userRole) + "\"";
                String aSecRoleStr = "\"applicative-security-role\"\\s*:\\s*\"" + Pattern.quote(userRole) + "\"";

                String[] blocks = content.split("\\{");
                for (String block : blocks) {
                    boolean matchPlugin = Pattern.compile(pRoleStr).matcher(block).find() ||
                                          Pattern.compile(pSecRoleStr).matcher(block).find();
                    boolean matchApp = Pattern.compile(aRoleStr).matcher(block).find() ||
                                       Pattern.compile(aRoleStrLegacy).matcher(block).find() ||
                                       Pattern.compile(aSecRoleStr).matcher(block).find();
                    if (matchPlugin && matchApp) {
                        return true;
                    }
                }
            }
        } catch (Exception ignored) {}
        return false;
    }
}
