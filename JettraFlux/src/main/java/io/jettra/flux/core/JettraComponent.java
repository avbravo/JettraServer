package io.jettra.flux.core;

import io.jettra.flux.theme.ThemeData;

/**
 * Universal declarative component interface for the JettraFlux ecosystem.
 * Implemented by Widget and reactive component abstractions.
 */
public interface JettraComponent {
    /**
     * Renders this component to an HTML string using the provided theme.
     *
     * @param theme current active theme data
     * @return rendered HTML string
     */
    String render(ThemeData theme);

    /**
     * Returns the unique identifier of this component.
     *
     * @return component ID
     */
    String getId();
}
