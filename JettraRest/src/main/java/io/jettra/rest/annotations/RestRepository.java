package io.jettra.rest.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca una interfaz de repositorio (anotada también con {@code @Repository}) para que
 * {@code RestRepositoryProcessor} genere en tiempo de compilación su clase de recurso REST
 * correspondiente (por ejemplo, {@code ProductRest} a partir de {@code ProductRepository}),
 * exponiendo los endpoints de cada método e integrando la configuración de seguridad de JettraRest/JettraJWT.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RestRepository {

    /**
     * Ruta base del recurso REST generado (ej. {@code "/products"}).
     * Si está vacío, se deriva automáticamente en kebab-case a partir del nombre del repositorio
     * (por ejemplo, {@code ProductRepository} -> {@code "/product"}).
     */
    String path() default "";

    /**
     * Nombre simple de la clase REST generada (ej. {@code "ProductRest"}).
     * Si está vacío, reemplaza el sufijo {@code Repository} por {@code Rest} o añade {@code Rest}.
     */
    String resourceName() default "";

    /**
     * Paquete destino donde se generará la clase REST.
     * Si está vacío, se genera en el mismo paquete de la interfaz de repositorio.
     */
    String targetPackage() default "";

    /**
     * Indica si el recurso REST generado requiere autenticación JWT ({@code @Secured}) por defecto.
     * Puede sobreescribirse colocando {@code @PermitAll} o {@code @DenyAll} en la interfaz o en métodos concretos.
     */
    boolean secured() default true;

    /**
     * Roles autorizados a nivel de clase ({@code @RolesAllowed}) para acceder a los endpoints generados.
     */
    String[] rolesAllowed() default {};

    /**
     * Roles declarados a nivel de clase ({@code @DeclareRoles}).
     */
    String[] declareRoles() default {};

    /**
     * Tipo MIME producido y consumido por el recurso generado (por defecto {@code application/json}).
     */
    String mediaType() default "application/json";
}
