package io.jettra.studio.core;

import io.jettra.studio.markup.HtmlResourceLoader;
import java.util.regex.Pattern;

/**
 * BasePage provides out-of-the-box layout templating for JettraStudio.
 * When a child page extends BasePage:
 * 1. The parent template (BasePage.html or MasterPage.html) provides the overall scaffolding (head, navbar, sidebar, footer).
 * 2. The placeholder tag <jettrat:child/> or <jettra:child/> in the parent template is replaced with the child page's HTML body.
 * 3. Components added in the child class bind seamlessly to both the parent layout and the child content.
 */
public abstract class BasePage extends WebPage {

    private static final Pattern CHILD_TAG_PATTERN = Pattern.compile(
        "<(?:jettrat:child|jettra:child|jettras:child)\\s*(?:/>|>.*?</(?:jettrat:child|jettra:child|jettras:child)>)",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    public BasePage() {
        super();
    }

    public BasePage(PageParameters parameters) {
        super(parameters);
    }

    @Override
    protected String loadMarkupForPage() {
        // 1. Load child page markup (either customMarkup or from resource)
        String childMarkup = (getCustomMarkup() != null)
            ? getCustomMarkup()
            : HtmlResourceLoader.getInstance().loadMarkup(this.getClass());

        // 2. Load parent base template markup (e.g. BasePage.html)
        String baseMarkup = HtmlResourceLoader.getInstance().loadMarkup(BasePage.class);
        if (baseMarkup == null || baseMarkup.isBlank()) {
            baseMarkup = generateDefaultBaseTemplateHtml();
        }

        if (childMarkup == null || childMarkup.isBlank()) {
            return baseMarkup;
        }

        // If child markup contains a full <html> document, extract its <body> content for the slot
        String childContent = extractBodyContent(childMarkup);

        // Replace <jettrat:child/> in parent layout with child page content
        if (CHILD_TAG_PATTERN.matcher(baseMarkup).find()) {
            return CHILD_TAG_PATTERN.matcher(baseMarkup).replaceFirst(java.util.regex.Matcher.quoteReplacement(childContent));
        }

        // If no <jettrat:child/> tag found, append child content to main container
        return baseMarkup.replace("</body>", childContent + "\n</body>");
    }

    private String extractBodyContent(String markup) {
        if (markup.contains("<body") && markup.contains("</body>")) {
            int bodyStart = markup.indexOf('>', markup.indexOf("<body")) + 1;
            int bodyEnd = markup.indexOf("</body>");
            if (bodyStart != -1 && bodyEnd != -1 && bodyEnd > bodyStart) {
                return markup.substring(bodyStart, bodyEnd).trim();
            }
        }
        return markup;
    }

    protected String generateDefaultBaseTemplateHtml() {
        return "<!DOCTYPE html>\n"
            + "<html lang=\"es\">\n"
            + "<head>\n"
            + "  <meta charset=\"utf-8\" />\n"
            + "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />\n"
            + "  <title>JettraStudio Application</title>\n"
            + "</head>\n"
            + "<body>\n"
            + "  <header class=\"jettra-base-header\" style=\"display:flex; justify-content:space-between; align-items:center; padding:12px 24px; border-bottom:1px solid var(--jf-border, #333); background:var(--jf-surface, #111);\">\n"
            + "    <div style=\"font-weight:800; font-size:1.15rem; color:var(--jf-accent, #ffd700);\">\n"
            + "      ⚡ JettraStudio\n"
            + "    </div>\n"
            + "    <div style=\"display:flex; gap:12px; align-items:center;\">\n"
            + "      <jettras:theme-selector/>\n"
            + "    </div>\n"
            + "  </header>\n"
            + "  <main class=\"jettra-base-main\" style=\"padding:24px; min-height:calc(100vh - 120px);\">\n"
            + "    <jettras:child/>\n"
            + "  </main>\n"
            + "  <footer class=\"jettra-base-footer\" style=\"text-align:center; padding:16px; font-size:0.85rem; color:var(--jf-text-secondary, #888); border-top:1px solid var(--jf-border, #333);\">\n"
            + "    JettraStudio &copy; 2026 Ecosistema Jettra (Java 25+)\n"
            + "  </footer>\n"
            + "</body>\n"
            + "</html>";
    }
}
