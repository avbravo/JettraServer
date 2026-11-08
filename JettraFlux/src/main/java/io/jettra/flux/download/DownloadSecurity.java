package io.jettra.flux.download;

import java.nio.file.Path;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Security and sanitization utility in Java 25+ for file download requests.
 * Prevents Path Traversal, null-byte injection, and invalid character abuse.
 */
public final class DownloadSecurity {

    private static final Pattern SAFE_FILENAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+$");

    private DownloadSecurity() {}

    /**
     * Sanitizes and validates a requested file name.
     * Prevents Path Traversal ("..", "/", "\\"), control characters, and null bytes.
     *
     * @param rawFileName raw file name requested by client or caller
     * @return validated, clean file name
     * @throws SecurityException if path traversal, directory navigation, or malicious characters are detected
     * @throws IllegalArgumentException if the file name is null or blank
     */
    public static String sanitizeFileName(String rawFileName) {
        if (rawFileName == null || rawFileName.isBlank()) {
            throw new IllegalArgumentException("File name must not be null or blank");
        }

        String trimmed = rawFileName.trim();

        // Check for null-bytes or control characters
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (c < 32 || c == 127) {
                throw new SecurityException("Malformed file name contains control character: " + rawFileName);
            }
        }

        // Check for path traversal sequences
        if (trimmed.contains("..") || trimmed.contains("/") || trimmed.contains("\\") || trimmed.contains("%")) {
            throw new SecurityException("Path traversal attempt detected in file name: " + rawFileName);
        }

        // Validate strictly against safe characters
        if (!SAFE_FILENAME_PATTERN.matcher(trimmed).matches()) {
            throw new SecurityException("File name contains unsupported or potentially unsafe characters: " + rawFileName);
        }

        // Ensure name does not resolve to empty or root-like designations
        if (".".equals(trimmed) || "..".equals(trimmed)) {
            throw new SecurityException("Invalid relative path token as file name: " + rawFileName);
        }

        return trimmed;
    }

    /**
     * Verifies whether a candidate string is a safe, valid file name.
     */
    public static boolean isSafeFileName(String rawFileName) {
        if (rawFileName == null || rawFileName.isBlank()) {
            return false;
        }
        String trimmed = rawFileName.trim();
        if (trimmed.contains("..") || trimmed.contains("/") || trimmed.contains("\\") || trimmed.contains("%")) {
            return false;
        }
        return SAFE_FILENAME_PATTERN.matcher(trimmed).matches();
    }

    /**
     * Strictly verifies that candidatePath is confined within baseDirectory.
     * Prevents path traversal outside of the allowed root storage boundary.
     *
     * @param baseDirectory allowed root directory
     * @param candidatePath target file path to validate
     * @throws SecurityException if candidatePath escapes baseDirectory
     */
    public static void validatePathWithinDirectory(Path baseDirectory, Path candidatePath) {
        Objects.requireNonNull(baseDirectory, "baseDirectory must not be null");
        Objects.requireNonNull(candidatePath, "candidatePath must not be null");

        Path normalizedBase = baseDirectory.normalize().toAbsolutePath();
        Path normalizedCandidate = candidatePath.normalize().toAbsolutePath();

        if (!normalizedCandidate.startsWith(normalizedBase)) {
            throw new SecurityException("Path traversal violation: Target path '"
                + normalizedCandidate + "' is outside allowed directory '" + normalizedBase + "'");
        }
    }
}
