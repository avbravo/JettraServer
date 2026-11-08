package jcf.component;

import io.jettra.flux.widgets.AppFooter;
import io.jettra.flux.widgets.FooterMetadata;

/**
 * Aliased entry-point for AppFooter in the jcf.component namespace.
 */
public class AppFooterComponent extends AppFooter {

    public AppFooterComponent() {
        super();
    }

    public AppFooterComponent(FooterMetadata metadata) {
        super(metadata);
    }

    public static AppFooter of() {
        return new AppFooter();
    }

    public static AppFooter of(FooterMetadata metadata) {
        return new AppFooter(metadata);
    }
}
