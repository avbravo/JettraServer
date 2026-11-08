package io.jettra.flux.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to draw an editable DataTable inside a Model.
 * It allows inserting, editing, and deleting records in the form of a table.
 * It loads stored data, commonly used for master-details forms.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ViewDataTable {
    /**
     * Comma-separated list of rows to display in the table.
     * Example: "id, name"
     */
    String row() default "";

    /**
     * Comma-separated list of columns that will be editable.
     * Example: "name"
     */
    String editablerow() default "";
    /**
     * Cuando se usa en un formulario Master-details en la tabla maestra donde
     * se anexa de manera predeterminada no se muestra. Si lo cambia a true
     * se muestra la tabla dentro de la otra tabla
     * Example: false
     */
    boolean showRowInMasterTable() default false;

    /**
     * The class managing the data source (Repository, Controller, Service)
     * returning the list of objects.
     */
    String source() default "";

    /**
     * Method inside the source that returns the list of objects to select.
     */
    String method() default "";

    /**
     * Allows applying a filter to the query operation.
     */
    String filter() default "";

    /**
     * Determines whether the columns of the master datatable can be edited.
     * Default is true. E.g. if false, inline editing in the master datatable is disabled.
     */
    boolean editableRowMaster() default true;
}
