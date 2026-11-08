package io.jettra.test.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to designate the setup class for tests.
 * The annotated class must have public methods:
 * - public void startServer(int port)
 * - public void stopServer()
 * 
 * JettraTestRunner will use these to manage the server lifecycle.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JettraTestLauncher {
}
