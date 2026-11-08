package io.jettra.flux.theme;

public class AccordionCSS {
    public static String get() {
        return "<style>\n"
            + ".espresso-accordion { width: 100%; display: flex; flex-direction: column; gap: 0; }\n"
            + ".espresso-accordion-tab { border: 1px solid var(--surface-color); margin-bottom: -1px; background-color: var(--surface-color); }\n"
            + ".espresso-accordion-tab:first-child { border-top-left-radius: 6px; border-top-right-radius: 6px; }\n"
            + ".espresso-accordion-tab:last-child { border-bottom-left-radius: 6px; border-bottom-right-radius: 6px; margin-bottom: 0; }\n"
            + ".espresso-accordion-header { padding: 1.25rem; background-color: var(--background-color); cursor: pointer; display: flex; align-items: center; color: var(--on-surface-color); font-weight: 600; transition: background-color 0.2s, color 0.2s; }\n"
            + ".espresso-accordion-header:hover { background-color: var(--surface-color); color: var(--primary-color); }\n"
            + ".espresso-accordion-tab-active .espresso-accordion-header { color: var(--primary-color); }\n"
            + ".espresso-accordion-header-icon { margin-right: 0.5rem; transition: transform 0.2s; }\n"
            + ".espresso-accordion-content { padding: 1.25rem; color: var(--on-surface-color); border-top: 1px solid var(--surface-color); }\n"
            + "</style>";
    }
}
