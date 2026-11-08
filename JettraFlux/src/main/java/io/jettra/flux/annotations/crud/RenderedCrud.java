package io.jettra.flux.annotations.crud;

import io.jettra.flux.widgets.Page;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to simplify the creation of CRUD interfaces.
 * When applied to a Page, the framework will automatically generate the CRUD UI.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface RenderedCrud {
    /**
     * The ViewModel class.
     */
    Class<?> model();

    /**
     * The Repository class.
     */
    Class<?> repository() default void.class;

    /**
     * The Controller class for endpoint integration.
     */
    Class<?> controller() default void.class;

    /**
     * Enable reporting for this view.
     */
    boolean report() default false;

    /**
     * Show report viewer UI before exporting/printing.
     */
    boolean reportShowViewer() default true;

    /**
     * Enable Print option in report viewer.
     */
    boolean reportAllowPrint() default true;

    /**
     * Enable PDF option in report viewer.
     */
    boolean reportAllowPdf() default true;

    /**
     * Enable Excel option in report viewer.
     */
    boolean reportAllowExcel() default true;

    /**
     * Enable CSV option in report viewer.
     */
    boolean reportAllowCsv() default true;

    /**
     * Enable Word option in report viewer.
     */
    boolean reportAllowWord() default true;

    /**
     * Report orientation (PORTRAIT or LANDSCAPE).
     */
    String reportOrientation() default "PORTRAIT";

    /**
     * Custom title for the report.
     */
    String reportTitle() default "";

    /**
     * Header text color in hex format.
     */
    String reportHeaderColor() default "#000000";

    /**
     * Enable inline editable datatable.
     */
    boolean editable() default false;

    /**
     * Automatically render the CrudView. If false, the page must manually build and add the component.
     */
    boolean autoRender() default true;

    /**
     * Base class to extend when generating the page class.
     */
    Class<?> extendsClass() default Page.class;

    /**
     * The title of the page to be generated.
     */
    String title() default "";
}
