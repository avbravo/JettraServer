package io.jettra.report.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to define a custom report footer at the class level.
 * Configures font style, orientation/alignment, text color, size, and layout decorations.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Repeatable(ModelReportFooters.class)
public @interface ModelReportFooter {
    /**
     * The custom text/label of the footer.
     */
    String value() default "";

    /**
     * Alternative to value() for specifying the footer text/label.
     */
    String label() default "";

    enum Orientation {
        LEFT, CENTER, RIGHT
    }

    enum Style {
        BOLD, ITALIC, SUBLINE, STRIKETHROUGH
    }

    enum ReportType {
        NORMAL, MASTER, DETAILS
    }

    /**
     * Alignment/Orientation of the footer text (LEFT, CENTER, RIGHT).
     */
    Orientation orientation() default Orientation.LEFT;

    /**
     * Font family name (e.g. Helvetica, Times-Roman, Courier).
     */
    String font() default "Helvetica";

    /**
     * Font size of the custom footer.
     */
    int size() default 10;

    /**
     * Hexadecimal text color (e.g. #000000, #ff0000).
     */
    String textColor() default "#000000";

    /**
     * Style decorations (BOLD, ITALIC, SUBLINE, STRIKETHROUGH).
     */
    Style[] style() default {};

    /**
     * Defines which report type this footer applies to (NORMAL, MASTER, DETAILS).
     */
    ReportType type() default ReportType.NORMAL;
}
