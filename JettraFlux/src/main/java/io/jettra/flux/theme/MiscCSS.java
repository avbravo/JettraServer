package io.jettra.flux.theme;

public class MiscCSS {
    public static String get() {
        return "<style>\n"
            + ".espresso-chip { display: inline-flex; align-items: center; background-color: #e2e8f0; color: #475569; border-radius: 16px; padding: 0 0.75rem; height: 2rem; font-size: 0.875rem; font-weight: 600; }\n"
            + ".espresso-chip-icon { margin-right: 0.5rem; }\n"
            + ".espresso-chip-image { width: 2rem; height: 2rem; border-radius: 50%; margin-left: -0.75rem; margin-right: 0.5rem; }\n"
            + ".espresso-chip i, .espresso-tag i { font-family: 'Font Awesome 5 Free', 'Font Awesome 6 Free', FontAwesome !important; font-weight: 900 !important; }\n"
            
            + ".espresso-skeleton { background-color: #e2e8f0; border-radius: 6px; animation: p-skeleton-animation 1.2s infinite linear; }\n"
            + ".espresso-skeleton-circle { border-radius: 50%; }\n"
            + "@keyframes p-skeleton-animation { 0% { opacity: 1; } 50% { opacity: 0.4; } 100% { opacity: 1; } }\n"
            + "</style>";
    }
}
