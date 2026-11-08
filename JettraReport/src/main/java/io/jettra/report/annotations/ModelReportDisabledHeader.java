package io.jettra.report.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to disable the inclusion of the automatic class name header in generated reports.
 * Typically used when the user designs custom headers manually using @ReportLabel or @ModelReportHeader.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ModelReportDisabledHeader {
}
