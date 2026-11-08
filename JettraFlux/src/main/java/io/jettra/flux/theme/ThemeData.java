package io.jettra.flux.theme;

/**
 * ThemeData defines the visual properties for the application's components.
 */
public class ThemeData {
    public final String primaryColor;
    public final String secondaryColor;
    public final String backgroundColor;
    public final String surfaceColor;
    public final String onPrimaryColor;
    public final String onSurfaceColor;
    
    public final String buttonStyle;
    public final String cardStyle;
    public final String containerStyle;
    public final String textStyle;
    
    public final String customCss;
    public final String customJs;

    public final ThemeTokens tokens;
    public final ColorMode colorMode;
    
    public ThemeData(
            String primaryColor, String secondaryColor, String backgroundColor, 
            String surfaceColor, String onPrimaryColor, String onSurfaceColor,
            String buttonStyle, String cardStyle, String containerStyle, String textStyle) {
        this(primaryColor, secondaryColor, backgroundColor, surfaceColor, onPrimaryColor, onSurfaceColor, buttonStyle, cardStyle, containerStyle, textStyle, "", "", null, null);
    }

    public ThemeData(
            String primaryColor, String secondaryColor, String backgroundColor, 
            String surfaceColor, String onPrimaryColor, String onSurfaceColor,
            String buttonStyle, String cardStyle, String containerStyle, String textStyle,
            String customCss, String customJs) {
        this(primaryColor, secondaryColor, backgroundColor, surfaceColor, onPrimaryColor, onSurfaceColor, buttonStyle, cardStyle, containerStyle, textStyle, customCss, customJs, null, null);
    }

    public ThemeData(
            String primaryColor, String secondaryColor, String backgroundColor, 
            String surfaceColor, String onPrimaryColor, String onSurfaceColor,
            String buttonStyle, String cardStyle, String containerStyle, String textStyle,
            String customCss, String customJs,
            ThemeTokens tokens, ColorMode colorMode) {
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
        this.backgroundColor = backgroundColor;
        this.surfaceColor = surfaceColor;
        this.onPrimaryColor = onPrimaryColor;
        this.onSurfaceColor = onSurfaceColor;
        this.buttonStyle = buttonStyle;
        this.cardStyle = cardStyle;
        this.containerStyle = containerStyle;
        this.textStyle = textStyle;
        this.customCss = customCss;
        this.customJs = customJs;
        this.tokens = tokens != null ? tokens : new ThemeTokens(
            backgroundColor != null ? backgroundColor : "#121212",
            surfaceColor != null ? surfaceColor : "#1e1e1e",
            onSurfaceColor != null ? onSurfaceColor : "#ffffff",
            secondaryColor != null ? secondaryColor : "#94a3b8",
            "rgba(128, 128, 128, 0.25)",
            primaryColor != null ? primaryColor : "#2563eb",
            secondaryColor != null ? secondaryColor : "#64748b",
            primaryColor != null ? primaryColor : "#2563eb",
            onSurfaceColor != null ? onSurfaceColor : "#ffffff"
        );
        this.colorMode = colorMode != null ? colorMode : ColorMode.DARK;
    }

    public ThemeTokens getTokens() {
        return tokens;
    }

    public ColorMode getColorMode() {
        return colorMode;
    }

    public ThemeData withTokens(ThemeTokens tokens, ColorMode mode) {
        return new ThemeData(
            primaryColor, secondaryColor, backgroundColor, surfaceColor, onPrimaryColor, onSurfaceColor,
            buttonStyle, cardStyle, containerStyle, textStyle, customCss, customJs, tokens, mode
        );
    }
    
    // Method to generate a base CSS for the page using this theme
    public String generateGlobalCss() {
        StringBuilder css = new StringBuilder("<style>\n");
        css.append(":root {\n")
           .append("  --primary-color: ").append(primaryColor).append(";\n")
           .append("  --secondary-color: ").append(secondaryColor).append(";\n")
           .append("  --background-color: ").append(backgroundColor).append(";\n")
           .append("  --surface-color: ").append(surfaceColor).append(";\n")
           .append("  --on-primary-color: ").append(onPrimaryColor).append(";\n")
           .append("  --on-surface-color: ").append(onSurfaceColor).append(";\n")
           .append("  --text-color: ").append(onSurfaceColor).append(";\n")
           .append("  --text-color-secondary: ").append(secondaryColor).append(";\n")
           .append("  --border-color: ").append(tokens != null ? tokens.border() : "rgba(128, 128, 128, 0.25)").append(";\n");
        if (tokens != null) {
            css.append(tokens.toCssVariables());
        }
        css.append("}\n")
           .append("body { background-color: var(--jf-bg, var(--background-color)); color: var(--jf-text-primary, var(--on-surface-color)); font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; margin: 0; padding: 0; transition: background-color 0.15s ease, color 0.15s ease; }\n")
           .append(".jettra-icon-svg { display: inline-flex; align-items: center; justify-content: center; }\n")
           .append(".jettra-icon-svg svg { width: 100%; height: 100%; }\n")
           .append("/* JettraFlux Professional Dashboard Layout */\n")
           .append(".espresso-dashboard {\n")
           .append("  display: grid;\n")
           .append("  grid-template-areas: 'top top' 'left center' 'left footer';\n")
           .append("  grid-template-columns: 280px 1fr;\n")
           .append("  grid-template-rows: 70px minmax(0, 1fr) auto;\n")
           .append("  height: 100vh;\n")
           .append("  overflow: hidden;\n")
           .append("  background-color: var(--jf-bg, var(--background-color));\n")
           .append("}\n")
           .append(".espresso-top { grid-area: top; width: 100%; box-sizing: border-box; background-color: var(--jf-surface, var(--surface-color)); color: var(--jf-text-primary, var(--on-surface-color)); box-shadow: 0 2px 10px rgba(0,0,0,0.05); z-index: 10; display: flex; align-items: center; padding: 0 1.5rem; transition: background-color 0.15s ease, color 0.15s ease; }\n")
           .append(".espresso-left { grid-area: left; height: 100%; box-sizing: border-box; background-color: var(--jf-surface, var(--surface-color)); color: var(--jf-text-primary, var(--on-surface-color)); border-right: 1px solid var(--jf-border, var(--border-color)); z-index: 5; overflow-y: auto; padding: 1.5rem 1rem; transition: background-color 0.15s ease, color 0.15s ease; }\n")
           .append(".espresso-center { grid-area: center; overflow-y: auto; width: 100%; box-sizing: border-box; padding: 2rem; background-color: var(--jf-bg, var(--background-color)); color: var(--jf-text-primary, var(--on-surface-color)); transition: background-color 0.15s ease, color 0.15s ease; }\n")
           .append(".espresso-footer { grid-area: footer; width: 100%; box-sizing: border-box; padding: 1rem 2rem; text-align: center; background-color: var(--jf-surface, var(--surface-color)); font-size: 0.875rem; color: var(--jf-text-secondary, var(--text-color-secondary)); transition: background-color 0.15s ease, color 0.15s ease; }\n")
           .append("@media (max-width: 992px) {\n")
           .append("  .espresso-dashboard {\n")
           .append("    grid-template-areas: 'top' 'center' 'footer';\n")
           .append("    grid-template-columns: 1fr;\n")
           .append("    grid-template-rows: 70px minmax(0, 1fr) auto;\n")
           .append("  }\n")
           .append("  .espresso-left {\n")
           .append("    position: fixed;\n")
           .append("    left: -280px;\n")
           .append("    top: 70px;\n")
           .append("    height: calc(100vh - 70px);\n")
           .append("    width: 280px;\n")
           .append("    z-index: 1000;\n")
           .append("    box-shadow: 2px 0 10px rgba(0,0,0,0.5);\n")
           .append("    transition: left 0.3s ease;\n")
           .append("    display: block;\n")
           .append("  }\n")
           .append("  .espresso-left.open {\n")
           .append("    left: 0;\n")
           .append("  }\n")
           .append("}\n")
           .append("/* JettraAlert Styling */\n")
           .append(".espresso-alert {\n")
           .append("  padding: 14px 18px;\n")
           .append("  border-radius: 8px;\n")
           .append("  margin-bottom: 20px;\n")
           .append("  font-weight: 500;\n")
           .append("  display: flex;\n")
           .append("  flex-direction: column;\n")
           .append("  align-items: flex-start;\n")
           .append("  gap: 4px;\n")
           .append("  width: 100%;\n")
           .append("  box-sizing: border-box;\n")
           .append("}\n")
           .append(".espresso-alert-danger {\n")
           .append("  background-color: rgba(239, 68, 68, 0.15) !important;\n")
           .append("  border: 1px solid rgba(239, 68, 68, 0.4) !important;\n")
           .append("  color: #f87171 !important;\n")
           .append("}\n")
           .append(".espresso-alert-danger h1, .espresso-alert-danger h2, .espresso-alert-danger h3, .espresso-alert-danger h4, .espresso-alert-danger p, .espresso-alert-danger span, .espresso-alert-danger div {\n")
           .append("  color: #f87171 !important;\n")
           .append("}\n")
           .append(".espresso-alert-success {\n")
           .append("  background-color: rgba(16, 185, 129, 0.15) !important;\n")
           .append("  border: 1px solid rgba(16, 185, 129, 0.4) !important;\n")
           .append("  color: #34d399 !important;\n")
           .append("}\n")
           .append(".espresso-alert-success h1, .espresso-alert-success h2, .espresso-alert-success h3, .espresso-alert-success h4, .espresso-alert-success p, .espresso-alert-success span, .espresso-alert-success div {\n")
           .append("  color: #34d399 !important;\n")
           .append("}\n")
           .append(".espresso-alert-info {\n")
           .append("  background-color: rgba(59, 130, 246, 0.15) !important;\n")
           .append("  border: 1px solid rgba(59, 130, 246, 0.4) !important;\n")
           .append("  color: #60a5fa !important;\n")
           .append("}\n")
           .append(".espresso-alert-info h1, .espresso-alert-info h2, .espresso-alert-info h3, .espresso-alert-info h4, .espresso-alert-info p, .espresso-alert-info span, .espresso-alert-info div {\n")
           .append("  color: #60a5fa !important;\n")
           .append("}\n")
           .append("/* JettraFlux Universal Component Theme Adaptation */\n")
           .append(".espresso-textfield, .espresso-textarea, .espresso-select, .espresso-input, .espresso-inputnumber, .form-control, .form-select, input[type=\"text\"], input[type=\"number\"], input[type=\"password\"], input[type=\"email\"], textarea, select {\n")
           .append("  background-color: var(--surface-color) !important;\n")
           .append("  color: var(--on-surface-color) !important;\n")
           .append("  border: 1px solid var(--border-color, rgba(128, 128, 128, 0.3)) !important;\n")
           .append("  border-radius: 6px;\n")
           .append("  padding: 10px 14px;\n")
           .append("  font-size: 15px;\n")
           .append("  width: 100%;\n")
           .append("  box-sizing: border-box;\n")
           .append("  transition: border-color 0.2s, box-shadow 0.2s;\n")
           .append("}\n")
           .append(".espresso-textfield::placeholder, .espresso-textarea::placeholder, .form-control::placeholder, input::placeholder, textarea::placeholder {\n")
           .append("  color: var(--text-color-secondary, #94a3b8) !important;\n")
           .append("  opacity: 0.7;\n")
           .append("}\n")
           .append(".espresso-textfield:focus, .espresso-textarea:focus, .espresso-select:focus, .espresso-input:focus, .form-control:focus, .form-select:focus, input:focus, textarea:focus, select:focus {\n")
           .append("  border-color: var(--primary-color) !important;\n")
           .append("  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.25) !important;\n")
           .append("  outline: none !important;\n")
           .append("}\n")
           .append("option, select option {\n")
           .append("  background-color: var(--surface-color) !important;\n")
           .append("  color: var(--on-surface-color) !important;\n")
           .append("}\n")
           .append(".espresso-button, .espresso-btn, .btn-primary {\n")
           .append("  background-color: var(--jf-accent, var(--primary-color));\n")
           .append("  color: var(--on-primary-color, #ffffff);\n")
           .append("  border: none;\n")
           .append("  border-radius: 6px;\n")
           .append("  padding: 10px 20px;\n")
           .append("  font-weight: 500;\n")
           .append("  cursor: pointer;\n")
           .append("  transition: all 0.2s ease;\n")
           .append("}\n")
           .append(".espresso-card, .card {\n")
           .append("  background-color: var(--jf-surface, var(--surface-color));\n")
           .append("  color: var(--jf-text-primary, var(--on-surface-color));\n")
           .append("  border: 1px solid var(--jf-border, var(--border-color, rgba(128, 128, 128, 0.2)));\n")
           .append("  border-radius: 8px;\n")
           .append("  padding: 20px;\n")
           .append("  box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1);\n")
           .append("  transition: background-color 0.15s ease, color 0.15s ease, border-color 0.15s ease;\n")
           .append("}\n")
           .append(".espresso-table, .espresso-datatable, .table {\n")
           .append("  background-color: var(--jf-surface, var(--surface-color));\n")
           .append("  color: var(--jf-text-primary, var(--on-surface-color));\n")
           .append("  border-color: var(--jf-border, var(--border-color));\n")
           .append("}\n")
           .append(".espresso-table th, .espresso-datatable th, .table th {\n")
           .append("  background-color: var(--jf-bg, var(--background-color));\n")
           .append("  color: var(--jf-text-primary, var(--on-surface-color));\n")
           .append("  border-bottom: 1px solid var(--jf-border, var(--border-color));\n")
           .append("}\n")
           .append(".espresso-table td, .espresso-datatable td, .table td {\n")
           .append("  border-bottom: 1px solid var(--border-color);\n")
           .append("  color: var(--on-surface-color);\n")
           .append("}\n");
        if (customCss != null && !customCss.isEmpty()) {
            String cleanCss = customCss.replaceAll("(?i)^\\s*<style[^>]*>", "").replaceAll("(?i)</style>\\s*$", "");
            css.append(cleanCss).append("\n");
        }
        css.append("</style>\n");
        if (customJs != null && !customJs.isEmpty()) {
            String cleanJs = customJs.replaceAll("(?i)^\\s*<script[^>]*>", "").replaceAll("(?i)</script>\\s*$", "");
            css.append("<script>\n").append(cleanJs).append("\n</script>\n");
        }
        return css.toString();
    }
}
