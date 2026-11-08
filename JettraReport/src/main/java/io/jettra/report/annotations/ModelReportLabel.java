package io.jettra.report.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to define fields representing report labels with specific section placement and alignment.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface ModelReportLabel {
    String label() default "";

    enum Section {
        HEADER, FOOTER, LASTPAGE, FIRSTPAGE
    }

    enum Orientation {
        LEFT, CENTER, RIGHT
    }

    enum Style {
        BOLD, ITALIC, SUBLINE, STRIKETHROUGH
    }

    Section section() default Section.HEADER;
    Orientation orientation() default Orientation.LEFT;

    String font() default "Helvetica";
    int size() default 10;
    String textColor() default "#000000";
    Style[] style() default {};
}
