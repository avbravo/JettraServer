package io.jettra.report.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to define a custom report header at the class level.
 * Configures font style, orientation/alignment, text color, size, layout, and report type.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Repeatable(ModelReportHeaders.class)
public @interface ModelReportHeader {
    /**
     * The custom text/label of the header. Replaces the default class name header.
     */
    String value() default "";

    /**
     * Alternative to value() for specifying the header text/label.
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
     * Alignment/Orientation of the header text (LEFT, CENTER, RIGHT).
     */
    Orientation orientation() default Orientation.LEFT;

    /**
     * Font family name (e.g. Helvetica, Times-Roman, Courier).
     */
    String font() default "Helvetica";

    /**
     * Font size of the custom header.
     */
    int size() default 14;

    /**
     * Hexadecimal text color (e.g. #000000, #ff0000).
     */
    String textColor() default "#000000";

    /**
     * Style decorations (BOLD, ITALIC, SUBLINE, STRIKETHROUGH).
     */
    Style[] style() default {};

    /**
     * Defines which report type this header applies to (NORMAL, MASTER, DETAILS).
     */
    ReportType type() default ReportType.NORMAL;
}
