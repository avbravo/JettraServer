package io.jettra.flux.theme;

public class TabViewCSS {
    public static String get() {
        return "<style>\n"
            + ".espresso-tabview { display: flex; flex-direction: column; width: 100%; }\n"
            + ".espresso-tabview-nav { list-style-type: none; margin: 0; padding: 0; display: flex; border-bottom: 2px solid var(--surface-color); }\n"
            + ".espresso-tabview-nav-item { margin-bottom: -2px; }\n"
            + ".espresso-tabview-nav-link { display: block; padding: 1rem 1.5rem; font-weight: 600; color: var(--on-surface-color); cursor: pointer; border-bottom: 2px solid transparent; transition: color 0.2s, border-color 0.2s; }\n"
            + ".espresso-tabview-nav-link:hover { color: var(--primary-color); border-color: var(--surface-color); }\n"
            + ".espresso-tabview-nav-active .espresso-tabview-nav-link { color: var(--primary-color); border-color: var(--primary-color); }\n"
            + ".espresso-tabview-panels { padding: 1.5rem 0; }\n"
            + "</style>";
    }
}
