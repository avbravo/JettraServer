package io.jettra.flux.theme;

public class MessageCSS {
    public static String get() {
        return "<style>\n"
            + ".espresso-message { margin: 1rem 0; padding: 1rem 1.25rem; border: 1px solid; border-radius: 6px; display: flex; align-items: center; font-size: 1rem; }\n"
            + ".espresso-message-icon { margin-right: 0.5rem; font-size: 1.25rem; }\n"
            + ".espresso-message-success { background-color: #f0fdf4; color: #166534; border-color: #bbf7d0; }\n"
            + ".espresso-message-info { background-color: #eff6ff; color: #1e40af; border-color: #bfdbfe; }\n"
            + ".espresso-message-warning { background-color: #fffbeb; color: #92400e; border-color: #fde68a; }\n"
            + ".espresso-message-error { background-color: #fef2f2; color: #991b1b; border-color: #fecaca; }\n"
            + "</style>";
    }
}
