package io.jettra.flux.download;

import io.jettra.test.annotation.DisplayName;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.TempDir;
import io.jettra.test.annotation.Test;

import java.nio.file.Path;

import static io.jettra.test.core.JettraAssert.*;

@NotRequiresRunningServer
class DownloadSecurityTest {

    @Test
    @DisplayName("sanitizeFileName accepts valid names and rejects traversal attempts")
    void testSanitizeFileName() {
        assertEquals("snapshot-2026-09-03-12-00-00.md",
            DownloadSecurity.sanitizeFileName("snapshot-2026-09-03-12-00-00.md"));
        assertEquals("report_backup.md",
            DownloadSecurity.sanitizeFileName("report_backup.md"));
        assertEquals("archive.json",
            DownloadSecurity.sanitizeFileName("archive.json"));

        // Path traversal attempts
        assertThrows(SecurityException.class, () -> DownloadSecurity.sanitizeFileName("../etc/passwd"));
        assertThrows(SecurityException.class, () -> DownloadSecurity.sanitizeFileName("..\\windows\\win.ini"));
        assertThrows(SecurityException.class, () -> DownloadSecurity.sanitizeFileName("/absolute/path.md"));
        assertThrows(SecurityException.class, () -> DownloadSecurity.sanitizeFileName("snapshot/sub/file.md"));
        assertThrows(SecurityException.class, () -> DownloadSecurity.sanitizeFileName("snapshot..md"));
        assertThrows(SecurityException.class, () -> DownloadSecurity.sanitizeFileName("snapshot%2e%2e.md"));
        assertThrows(SecurityException.class, () -> DownloadSecurity.sanitizeFileName("test\0bad.md"));
        assertThrows(SecurityException.class, () -> DownloadSecurity.sanitizeFileName("."));
        assertThrows(SecurityException.class, () -> DownloadSecurity.sanitizeFileName(".."));

        // Null or blank
        assertThrows(IllegalArgumentException.class, () -> DownloadSecurity.sanitizeFileName(null));
        assertThrows(IllegalArgumentException.class, () -> DownloadSecurity.sanitizeFileName("   "));
    }

    @Test
    @DisplayName("isSafeFileName correctly evaluates candidate names")
    void testIsSafeFileName() {
        assertTrue(DownloadSecurity.isSafeFileName("valid-file.md"));
        assertTrue(DownloadSecurity.isSafeFileName("Snapshot_2026.MD"));
        assertFalse(DownloadSecurity.isSafeFileName("../evil.md"));
        assertFalse(DownloadSecurity.isSafeFileName("dir/file.md"));
        assertFalse(DownloadSecurity.isSafeFileName(""));
        assertFalse(DownloadSecurity.isSafeFileName(null));
    }

    @Test
    @DisplayName("validatePathWithinDirectory strictly confines paths")
    void testValidatePathWithinDirectory(@TempDir Path tempDir) {
        Path inside = tempDir.resolve("snapshot.md");
        Path nested = tempDir.resolve("sub").resolve("snapshot.md");
        Path outside = tempDir.getParent().resolve("secret.txt");

        assertDoesNotThrow(() -> DownloadSecurity.validatePathWithinDirectory(tempDir, inside));
        assertDoesNotThrow(() -> DownloadSecurity.validatePathWithinDirectory(tempDir, nested));
        assertThrows(SecurityException.class, () -> DownloadSecurity.validatePathWithinDirectory(tempDir, outside));
    }
}
