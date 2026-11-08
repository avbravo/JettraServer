package io.jettra.flux.theme;

public class PanelCSS {
    public static String get() {
        return "<style>\n"
            + ".espresso-panel { border: 1px solid var(--surface-color); border-radius: 6px; margin-bottom: 1rem; background-color: var(--surface-color); }\n"
            + ".espresso-panel-header { padding: 1.25rem; background-color: var(--background-color); border-bottom: 1px solid var(--surface-color); border-radius: 6px 6px 0 0; display: flex; justify-content: space-between; align-items: center; font-weight: 700; color: var(--on-surface-color); }\n"
            + ".espresso-panel-content { padding: 1.25rem; color: var(--on-surface-color); line-height: 1.5; }\n"
            + ".espresso-panel-icons { color: #64748b; cursor: pointer; transition: color 0.2s; }\n"
            + ".espresso-panel-icons:hover { color: #3b82f6; }\n"
            + "</style>";
    }
}
