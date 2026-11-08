package io.jettra.flux.theme;

public class DividerCSS {
    public static String get() {
        return "<style>\n"
            + ".espresso-divider { position: relative; border-color: #e2e8f0; }\n"
            + ".espresso-divider-horizontal { border-top: 1px solid #e2e8f0; margin: 1rem 0; width: 100%; }\n"
            + ".espresso-divider-vertical { border-left: 1px solid #e2e8f0; margin: 0 1rem; min-height: 100%; }\n"
            + ".espresso-divider-with-content::before { content: ''; position: absolute; top: 50%; width: 100%; border-top: 1px solid #e2e8f0; left: 0; z-index: -1; }\n"
            + ".espresso-divider-with-content { border: none; text-align: center; margin: 1rem 0; z-index: 1; }\n"
            + ".espresso-divider-content { background-color: #ffffff; padding: 0 0.5rem; color: #475569; font-weight: 500; font-size: 0.875rem; }\n"
            + "</style>";
    }
}
