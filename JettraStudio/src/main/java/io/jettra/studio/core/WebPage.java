package io.jettra.studio.core;

import io.jettra.flux.theme.ColorMode;
import io.jettra.flux.theme.ThemeData;
import io.jettra.studio.i18n.LocalizationManager;
import io.jettra.studio.markup.HtmlResourceLoader;
import io.jettra.studio.theme.StudioThemeManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * WebPage represents an entire HTML page bound to a Java class in JettraStudio.
 * Automatically loads its paired HTML file (e.g. HomePage.html for HomePage.java)
 * and orchestrates component binding, i18n interpolation, and theme injection.
 */
public abstract class WebPage extends MarkupContainer {

    private PageParameters pageParameters = new PageParameters();
    private String customMarkup;
    private String pageTitle;
    private String currentThemeName;
    private ColorMode currentColorMode = ColorMode.DARK;
    private String redirectUrl;
    private final java.util.List<String> responseCookies = new java.util.ArrayList<>();

    private static final Pattern I18N_TAG_PATTERN = Pattern.compile(
        "<(?:jettrat:message|jettra:message|jettras:message)\\s+key=[\"']([^\"']+)[\"'][^>]*?(?:/>|>.*?</(?:jettrat:message|jettra:message|jettras:message)>)",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    private static final Pattern THEME_SELECTOR_TAG_PATTERN = Pattern.compile(
        "<(?:jettras:theme-selector|jettrat:theme-selector|jettra:theme-selector)[^>]*?(?:/>|>.*?</(?:jettras:theme-selector|jettrat:theme-selector|jettra:theme-selector)>)",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    public WebPage() {
        this(new PageParameters());
    }

    public WebPage(PageParameters parameters) {
        super("page");
        if (parameters != null) {
            this.pageParameters = parameters;
        }
        onInitialize();
    }

    /**
     * Component initialization hook. Override to add child components via add(...).
     */
    protected void onInitialize() {}

    /**
     * Invoked immediately before HTML rendering begins.
     */
    protected void onBeforeRender() {}

    public PageParameters getPageParameters() {
        return pageParameters;
    }

    public void setPageParameters(PageParameters parameters) {
        if (parameters != null) {
            this.pageParameters = parameters;
        }
    }

    
    public void redirect(String url) {
        this.redirectUrl = url;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void addCookie(String name, String value, String path) {
        responseCookies.add(name + "=" + value + "; Path=" + (path != null ? path : "/") + "; HttpOnly");
    }

    public void clearCookie(String name, String path) {
        responseCookies.add(name + "=; Path=" + (path != null ? path : "/") + "; Max-Age=0; HttpOnly");
    }

    public java.util.List<String> getResponseCookies() {
        return responseCookies;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    public String getCurrentThemeName() {
        return currentThemeName;
    }

    public void setCurrentThemeName(String themeName) {
        this.currentThemeName = themeName;
    }

    public ColorMode getCurrentColorMode() {
        return currentColorMode;
    }

    public void setCurrentColorMode(ColorMode mode) {
        if (mode != null) {
            this.currentColorMode = mode;
        }
    }

    /**
     * Override to supply programmatic HTML markup instead of loading from classpath.
     */
    public String getCustomMarkup() {
        return customMarkup;
    }

    public void setCustomMarkup(String customMarkup) {
        this.customMarkup = customMarkup;
    }

    /**
     * Renders the complete HTML response for this page.
     */
    public String renderPage() {
        onBeforeRender();

        // 1. Resolve raw HTML template
        String rawHtml = loadMarkupForPage();
        if (rawHtml == null || rawHtml.isBlank()) {
            rawHtml = generateDefaultScaffoldHtml();
        }

        // 2. Process i18n tags: <jettrat:message key="..."/>
        String processed = processI18nTags(rawHtml);

        // 3. Process Theme Selector tags: <jettrat:theme-selector/>
        processed = processThemeSelectorTags(processed);

        // 4. Render child components with jettrat:id
        StringBuilder bodyBuffer = new StringBuilder();
        renderMarkupContent(processed, bodyBuffer);
        String finalHtml = bodyBuffer.toString();

        // 5. Inject title if configured
        if (pageTitle != null && !pageTitle.isBlank()) {
            if (finalHtml.contains("<title>")) {
                finalHtml = finalHtml.replaceFirst("<title>.*?</title>", "<title>" + escapeHtml(pageTitle) + "</title>");
            }
        }

        // 6. Inactive / Active Theme CSS and JS Injection into <head>
        ThemeData theme = StudioThemeManager.getInstance().resolveTheme(currentThemeName, currentColorMode);
        finalHtml = StudioThemeManager.getInstance().injectThemeIntoHead(finalHtml, theme);

        // 7. Detach transient state
        detach();

        return finalHtml;
    }

    protected String loadMarkupForPage() {
        if (customMarkup != null) {
            return customMarkup;
        }
        return HtmlResourceLoader.getInstance().loadMarkup(this.getClass());
    }

    private String processI18nTags(String html) {
        if (html == null) return "";
        Matcher m = I18N_TAG_PATTERN.matcher(html);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String key = m.group(1);
            String localized = LocalizationManager.getInstance().getString(this.getClass(), key, key);
            m.appendReplacement(sb, Matcher.quoteReplacement(localized));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private String processThemeSelectorTags(String html) {
        if (html == null) return "";
        Matcher m = THEME_SELECTOR_TAG_PATTERN.matcher(html);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String selectorHtml = renderThemeSelectorWidget();
            m.appendReplacement(sb, Matcher.quoteReplacement(selectorHtml));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private String renderThemeSelectorWidget() {
        io.jettra.flux.widgets.ThemeSelectDropdown dropdown = io.jettra.flux.widgets.ThemeSelectDropdown.of()
            .current(currentThemeName != null ? currentThemeName : "Games")
            .asNativeSelect(true);
        ThemeData theme = StudioThemeManager.getInstance().resolveTheme(currentThemeName, currentColorMode);
        return dropdown.render(theme);
    }

    private String generateDefaultScaffoldHtml() {
        return "<!DOCTYPE html>\n"
            + "<html>\n"
            + "<head>\n"
            + "  <meta charset=\"utf-8\" />\n"
            + "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />\n"
            + "  <title>" + (pageTitle != null ? pageTitle : "JettraStudio Page") + "</title>\n"
            + "</head>\n"
            + "<body>\n"
            + "  <div class=\"jettra-page-content\">\n"
            + "  </div>\n"
            + "</body>\n"
            + "</html>";
    }
}
