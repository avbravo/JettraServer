package io.jettra.flux.widgets;

import java.time.Year;
import java.util.List;

/**
 * Immutable Java 25 record encapsulating metadata displayed in the global AppFooter.
 * Automatically resolves current copyright year dynamically via {@link Year#now()}.
 */
public record FooterMetadata(
    String appName,
    String version,
    int copyrightYear,
    String copyrightHolder,
    String connectionStatus,
    List<FooterLink> links
) {
    public FooterMetadata {
        appName = (appName == null || appName.isBlank()) ? "JettraDB" : appName.trim();
        version = (version == null || version.isBlank()) ? "1.0.0" : version.trim();
        copyrightYear = copyrightYear <= 0 ? Year.now().getValue() : copyrightYear;
        copyrightHolder = (copyrightHolder == null || copyrightHolder.isBlank()) ? "JettraStack" : copyrightHolder.trim();
        connectionStatus = (connectionStatus == null || connectionStatus.isBlank()) ? "Online" : connectionStatus.trim();
        links = (links == null) ? List.of() : List.copyOf(links);
    }

    public static FooterMetadata ofDefault(String appName, String version) {
        return new FooterMetadata(
            appName,
            version,
            Year.now().getValue(),
            "JettraStack",
            "Connected",
            List.of()
        );
    }

    public static FooterMetadata of(String appName, String version, String copyrightHolder, String connectionStatus, List<FooterLink> links) {
        return new FooterMetadata(
            appName,
            version,
            Year.now().getValue(),
            copyrightHolder,
            connectionStatus,
            links
        );
    }
}
