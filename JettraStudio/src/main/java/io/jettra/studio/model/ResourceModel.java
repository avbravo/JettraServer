package io.jettra.studio.model;

import io.jettra.studio.i18n.LocalizationManager;
import java.util.Objects;

/**
 * ResourceModel dynamically looks up localized text by message key
 * from the active localization bundle / language.
 */
public class ResourceModel implements IModel<String> {

    private final String resourceKey;
    private final String defaultValue;

    public ResourceModel(String resourceKey) {
        this(resourceKey, resourceKey);
    }

    public ResourceModel(String resourceKey, String defaultValue) {
        this.resourceKey = Objects.requireNonNull(resourceKey, "resourceKey cannot be null");
        this.defaultValue = defaultValue;
    }

    public static ResourceModel of(String resourceKey) {
        return new ResourceModel(resourceKey);
    }

    public static ResourceModel of(String resourceKey, String defaultValue) {
        return new ResourceModel(resourceKey, defaultValue);
    }

    @Override
    public String getObject() {
        return LocalizationManager.getInstance().getString(resourceKey, defaultValue);
    }

    public String getResourceKey() {
        return resourceKey;
    }
}
