package io.jettra.studio.i18n;

import java.util.Locale;

/**
 * Supported standard languages in JettraStudio.
 */
public enum LanguageStudio {
    ES("es", "Español", Locale.of("es")),
    EN("en", "English", Locale.of("en")),
    PT("pt", "Português", Locale.of("pt")),
    FR("fr", "Français", Locale.of("fr")),
    DE("de", "Deutsch", Locale.of("de")),
    IT("it", "Italiano", Locale.of("it"));

    private final String code;
    private final String displayName;
    private final Locale locale;

    LanguageStudio(String code, String displayName, Locale locale) {
        this.code = code;
        this.displayName = displayName;
        this.locale = locale;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Locale getLocale() {
        return locale;
    }

    public static LanguageStudio fromCode(String code) {
        if (code == null) return ES;
        String clean = code.trim().toLowerCase();
        for (LanguageStudio lang : values()) {
            if (lang.code.equalsIgnoreCase(clean)) {
                return lang;
            }
        }
        return ES;
    }
}
