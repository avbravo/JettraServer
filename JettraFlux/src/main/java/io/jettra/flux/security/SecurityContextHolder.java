package io.jettra.flux.security;

/**
 * Thread-safe holder for the current execution thread's SecurityContext.
 */
public final class SecurityContextHolder {

    private static final ThreadLocal<SecurityContext> CONTEXT =
            ThreadLocal.withInitial(SecurityContext::unauthenticated);

    private SecurityContextHolder() {}

    public static SecurityContext getContext() {
        return CONTEXT.get();
    }

    public static void setContext(SecurityContext context) {
        if (context == null || !context.isAuthenticated()) {
            CONTEXT.set(SecurityContext.unauthenticated());
        } else {
            CONTEXT.set(context);
        }
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
