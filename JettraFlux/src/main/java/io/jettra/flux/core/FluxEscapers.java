package io.jettra.flux.core;

/**
 * Robust, deterministic HTML, attribute, and JavaScript escapers for JettraFlux components.
 * Prevents delimiter breakage, XSS, and syntax errors caused by naive string concatenations.
 */
public final class FluxEscapers {

    private FluxEscapers() {}

    /**
     * Escapes text for placement inside standard HTML body content.
     */
    public static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }

    /**
     * Escapes text for placement inside HTML attributes delimited by double quotes.
     */
    public static String escapeAttr(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;")
                   .replace("\n", "&#10;")
                   .replace("\r", "&#13;");
    }

    /**
     * Escapes text for placement inside a JavaScript string literal (e.g. '...' or "...").
     * Handles control characters, newlines, quotes, and HTML tag delimiters.
     */
    public static String escapeJs(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("'", "\\'")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t")
                   .replace("\b", "\\b")
                   .replace("\f", "\\f")
                   .replace("<", "\\u003c")
                   .replace(">", "\\u003e");
    }

    /**
     * Quotes a string as a safe JavaScript double-quoted literal, e.g. "foo".
     */
    public static String quoteJs(String text) {
        if (text == null) return "\"\"";
        return "\"" + escapeJs(text) + "\"";
    }
}
