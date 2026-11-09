package io.jettra.aura.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Especifica la ruta explícita al archivo de plantilla HTML asociado a la página o panel Aura.
 * Si no se especifica, JettraAura buscará por convención una plantilla con el mismo nombre y
 * extensión .html (ej. Index.java -> Index.html o index.html).
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AuraTemplate {
    String value();
}

