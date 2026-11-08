package io.jettra.flux.security;

import io.jettra.core.login.NoLoginRequired;
import jcf.AppRole;
import jcf.annotation.PageWidgetAllow;
import io.jettra.test.annotation.AfterEach;
import io.jettra.test.annotation.DisplayName;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.Test;

import java.util.Set;

import static io.jettra.test.core.JettraAssert.*;

/**
 * Unit test suite for JettraFlux Declarative RBAC and PageSecurityGuard.
 */
@NotRequiresRunningServer
class PageSecurityGuardTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clear();
    }

    // Dummy test classes for annotation inspection
    @NoLoginRequired
    static class PublicSamplePage {}

    @PageWidgetAllow(role = {AppRole.ADMIN, AppRole.MANAGER})
    static class ManagerAdminPage {}

    @PageWidgetAllow(role = {AppRole.ADMIN}, department = "IT")
    static class ItAdminPage {}

    static class DefaultUnannotatedPage {}

    @Test
    @DisplayName("SecurityPrincipal parses roles and enforces role checking")
    void testSecurityPrincipal() {
        SecurityPrincipal principal = SecurityPrincipal.of("alice", "ADMIN, MANAGER", "Engineering");
        assertEquals("alice", principal.username());
        assertEquals("Engineering", principal.department());
        assertTrue(principal.hasRole("ADMIN"));
        assertTrue(principal.hasRole("admin")); // case-insensitive
        assertTrue(principal.hasRole("MANAGER"));
        assertFalse(principal.hasRole("USER"));
        assertTrue(principal.hasAnyRole(Set.of("USER", "ADMIN")));
        assertFalse(principal.hasAnyRole(Set.of("GUEST")));
    }

    @Test
    @DisplayName("SecurityContextHolder isolates context and cleans up properly")
    void testSecurityContextHolder() {
        SecurityPrincipal principal = SecurityPrincipal.of("bob", "USER", "");
        SecurityContext ctx = SecurityContext.authenticated(principal);
        SecurityContextHolder.setContext(ctx);

        assertEquals(ctx, SecurityContextHolder.getContext());
        assertTrue(SecurityContextHolder.getContext().isAuthenticated());

        SecurityContextHolder.clear();
        assertFalse(SecurityContextHolder.getContext().isAuthenticated());
    }

    @Test
    @DisplayName("Public page with @NoLoginRequired grants access immediately")
    void testPublicPageAccess() {
        SecurityContext unauth = SecurityContext.unauthenticated();
        SecurityDecision decision = PageSecurityGuard.evaluate(PublicSamplePage.class, unauth);

        assertInstanceOf(SecurityDecision.Granted.class, decision);
    }

    @Test
    @DisplayName("Protected page without authentication redirects to login")
    void testUnauthenticatedAccessRedirectsToLogin() {
        SecurityContext unauth = SecurityContext.unauthenticated();
        SecurityDecision decision = PageSecurityGuard.evaluate(ManagerAdminPage.class, unauth);

        assertInstanceOf(SecurityDecision.RedirectToLogin.class, decision);
        SecurityDecision.RedirectToLogin redirect = (SecurityDecision.RedirectToLogin) decision;
        assertEquals("/login", redirect.loginPath());
    }

    @Test
    @DisplayName("Protected page with matching role grants access")
    void testAuthorizedRoleGrantsAccess() {
        SecurityPrincipal principal = SecurityPrincipal.of("adminUser", "ADMIN", "IT");
        SecurityContext ctx = SecurityContext.authenticated(principal);

        SecurityDecision decision = PageSecurityGuard.evaluate(ManagerAdminPage.class, ctx);
        assertInstanceOf(SecurityDecision.Granted.class, decision);
    }

    @Test
    @DisplayName("Protected page with insufficient role yields 403 Denied")
    void testUnauthorizedRoleDenied() {
        SecurityPrincipal principal = SecurityPrincipal.of("regularUser", "USER", "Sales");
        SecurityContext ctx = SecurityContext.authenticated(principal);

        SecurityDecision decision = PageSecurityGuard.evaluate(ManagerAdminPage.class, ctx);
        assertInstanceOf(SecurityDecision.Denied.class, decision);
        SecurityDecision.Denied denied = (SecurityDecision.Denied) decision;
        assertEquals(403, denied.statusCode());
        assertTrue(denied.requiredRoles().contains("ADMIN"));
        assertTrue(denied.requiredRoles().contains("MANAGER"));
    }

    @Test
    @DisplayName("Protected page with mismatched department yields 403 Denied")
    void testMismatchedDepartmentDenied() {
        SecurityPrincipal principal = SecurityPrincipal.of("adminFinance", "ADMIN", "Finance");
        SecurityContext ctx = SecurityContext.authenticated(principal);

        SecurityDecision decision = PageSecurityGuard.evaluate(ItAdminPage.class, ctx);
        assertInstanceOf(SecurityDecision.Denied.class, decision);
        SecurityDecision.Denied denied = (SecurityDecision.Denied) decision;
        assertEquals(403, denied.statusCode());
    }

    @Test
    @DisplayName("Unannotated default page requires authentication")
    void testUnannotatedDefaultPage() {
        SecurityDecision unauthDecision = PageSecurityGuard.evaluate(DefaultUnannotatedPage.class, SecurityContext.unauthenticated());
        assertInstanceOf(SecurityDecision.RedirectToLogin.class, unauthDecision);

        SecurityPrincipal principal = SecurityPrincipal.of("anyUser", "USER", "");
        SecurityDecision authDecision = PageSecurityGuard.evaluate(DefaultUnannotatedPage.class, SecurityContext.authenticated(principal));
        assertInstanceOf(SecurityDecision.Granted.class, authDecision);
    }
}
