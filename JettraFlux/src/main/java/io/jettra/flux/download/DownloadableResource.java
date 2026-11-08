package io.jettra.flux.download;

/**
 * Functional interface implementing the Resource Locator / Provider Pattern.
 * Encapsulates the deferred provisioning and streaming lifecycle of a DownloadResource.
 */
@FunctionalInterface
public interface DownloadableResource {

    /**
     * Provides or resolves the underlying DownloadResource.
     *
     * @return non-null DownloadResource instance
     * @throws Exception if resolution or resource preparation fails
     */
    DownloadResource getDownloadResource() throws Exception;
}
