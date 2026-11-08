package io.jettra.flux.download;

import io.jettra.flux.core.Widget;
import io.jettra.flux.widgets.RawScript;

import java.util.Objects;

/**
 * Native JettraFlux client driver and helper in Java 25+.
 * Encapsulates client-side file download triggering mechanisms (transient <a> tags
 * with download attribute, Blob-based downloads, and reactive streaming listeners)
 * without external JS/HTML files.
 */
public final class FluxDownload {

    private FluxDownload() {}

    /**
     * Generates a reusable JavaScript snippet that defines standard client-side download drivers:
     * - window.jettraTriggerDownload(url, filename)
     * - window.jettraDownloadBlob(data, filename, mimeType)
     * - window.jettraFetchAndDownload(fetchUrl, postBody, fallbackFilename)
     */
    public static String renderClientDriverScript() {
        return """
            if (!window.jettraTriggerDownload) {
              window.jettraTriggerDownload = function(url, filename) {
                if (!url) return;
                var a = document.createElement('a');
                a.href = url;
                if (filename) {
                  a.setAttribute('download', filename);
                }
                a.style.display = 'none';
                document.body.appendChild(a);
                a.click();
                setTimeout(function() {
                  if (a.parentNode) {
                    a.parentNode.removeChild(a);
                  }
                }, 200);
              };
            }

            if (!window.jettraDownloadBlob) {
              window.jettraDownloadBlob = function(content, filename, mimeType) {
                var mime = mimeType || 'text/markdown;charset=utf-8';
                var blob = (content instanceof Blob) ? content : new Blob([content], { type: mime });
                var url = window.URL.createObjectURL(blob);
                window.jettraTriggerDownload(url, filename);
                setTimeout(function() {
                  window.URL.revokeObjectURL(url);
                }, 1000);
              };
            }

            if (!window.jettraFetchAndDownload) {
              window.jettraFetchAndDownload = async function(fetchUrl, options, fallbackFilename) {
                var res = await fetch(fetchUrl, options || {});
                if (!res.ok) {
                  throw new Error('Download request failed with status ' + res.status);
                }
                var disp = res.headers.get('Content-Disposition') || '';
                var filename = fallbackFilename || 'download';
                var match = disp.match(/filename[^;=\\n]*=((['"]).*?\\2|[^;\\n]*)/);
                if (match && match[1]) {
                  filename = match[1].replace(/['"]/g, '').trim();
                }
                var blob = await res.blob();
                window.jettraDownloadBlob(blob, filename, res.headers.get('Content-Type'));
                return { success: true, fileName: filename, size: blob.size };
              };
            }
            """;
    }

    /**
     * Returns a JettraFlux Widget containing the download client driver script.
     */
    public static Widget asWidget() {
        return RawScript.of(renderClientDriverScript());
    }

    /**
     * Generates an immediate JavaScript expression to trigger a transient download of a URL.
     *
     * @param url download endpoint URL
     * @param fileName desired client file name
     * @return JavaScript snippet
     */
    public static String triggerDownloadScript(String url, String fileName) {
        Objects.requireNonNull(url, "url must not be null");
        String safeName = (fileName != null && !fileName.isBlank())
            ? DownloadSecurity.sanitizeFileName(fileName)
            : "";

        return "(function() {\n" +
               "  var a = document.createElement('a');\n" +
               "  a.href = '" + escapeJs(url) + "';\n" +
               (safeName.isEmpty() ? "" : "  a.setAttribute('download', '" + escapeJs(safeName) + "');\n") +
               "  a.style.display = 'none';\n" +
               "  document.body.appendChild(a);\n" +
               "  a.click();\n" +
               "  setTimeout(function() { if (a.parentNode) a.parentNode.removeChild(a); }, 200);\n" +
               "})();";
    }

    /**
     * Generates an immediate JavaScript expression to download a text/markdown string as a client Blob.
     *
     * @param content text or markdown content
     * @param fileName desired client file name
     * @param mimeType MIME content type (e.g. "text/markdown; charset=UTF-8")
     * @return JavaScript snippet
     */
    public static String triggerBlobScript(String content, String fileName, String mimeType) {
        Objects.requireNonNull(content, "content must not be null");
        String safeName = DownloadSecurity.sanitizeFileName(fileName);
        String safeMime = (mimeType != null && !mimeType.isBlank()) ? mimeType : "text/markdown;charset=utf-8";

        return "(function() {\n" +
               "  var blob = new Blob([" + quoteJsString(content) + "], { type: '" + escapeJs(safeMime) + "' });\n" +
               "  var url = window.URL.createObjectURL(blob);\n" +
               "  var a = document.createElement('a');\n" +
               "  a.href = url;\n" +
               "  a.setAttribute('download', '" + escapeJs(safeName) + "');\n" +
               "  a.style.display = 'none';\n" +
               "  document.body.appendChild(a);\n" +
               "  a.click();\n" +
               "  setTimeout(function() {\n" +
               "    if (a.parentNode) a.parentNode.removeChild(a);\n" +
               "    window.URL.revokeObjectURL(url);\n" +
               "  }, 1000);\n" +
               "})();";
    }

    private static String escapeJs(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("'", "\\'")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r");
    }

    private static String quoteJsString(String text) {
        if (text == null) return "''";
        return "\"" + text.replace("\\", "\\\\")
                          .replace("\"", "\\\"")
                          .replace("\n", "\\n")
                          .replace("\r", "\\r")
                          .replace("\t", "\\t") + "\"";
    }
}
