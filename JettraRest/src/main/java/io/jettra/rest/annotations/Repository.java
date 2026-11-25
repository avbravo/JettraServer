package io.jettra.rest.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación marcadora de repositorio del ecosistema JettraRest.
 * {@code RestRepositoryProcessor} acepta tanto esta anotación como cualquier otra anotación
 * cuyo nombre simple sea {@code Repository} (por ejemplo, Jakarta Data {@code @Repository}).
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Repository {
}
