package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

/**
 * Widget to render raw inline script blocks in JettraFlux widget trees.
 */
public class RawScript extends Widget {
    private final String jsCode;

    private RawScript(String jsCode) {
        this.jsCode = jsCode;
    }

    public static RawScript of(String jsCode) {
        return new RawScript(jsCode);
    }

    @Override
    public String render(ThemeData theme) {
        if (jsCode == null || jsCode.trim().isEmpty()) return "";
        String trimmed = jsCode.trim();
        if (trimmed.startsWith("<script>")) {
            return trimmed + "\n";
        }
        return "<script>\n" + trimmed + "\n</script>\n";
    }
}
