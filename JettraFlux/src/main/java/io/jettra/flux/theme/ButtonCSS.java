package io.jettra.flux.theme;

public class ButtonCSS {
    public static String get() {
        return "<style>\n"
            + ".espresso-btn { display: inline-flex; align-items: center; justify-content: center; gap: 0.5rem; padding: 0.75rem 1.25rem; font-size: 1rem; font-weight: 600; border-radius: 6px; cursor: pointer; transition: background-color 0.2s, color 0.2s, border-color 0.2s, box-shadow 0.2s; outline-color: transparent; border: 1px solid transparent; }\n"
            
            // Severities (Solid)
            + ".espresso-btn-primary { background-color: #3b82f6; color: #ffffff; border-color: #3b82f6; }\n"
            + ".espresso-btn-primary:hover { background-color: #2563eb; border-color: #2563eb; }\n"
            
            + ".espresso-btn-secondary { background-color: #64748b; color: #ffffff; border-color: #64748b; }\n"
            + ".espresso-btn-secondary:hover { background-color: #475569; border-color: #475569; }\n"
            
            + ".espresso-btn-success { background-color: #22c55e; color: #ffffff; border-color: #22c55e; }\n"
            + ".espresso-btn-success:hover { background-color: #16a34a; border-color: #16a34a; }\n"
            
            + ".espresso-btn-info { background-color: #0ea5e9; color: #ffffff; border-color: #0ea5e9; }\n"
            + ".espresso-btn-info:hover { background-color: #0284c7; border-color: #0284c7; }\n"
            
            + ".espresso-btn-warning { background-color: #f59e0b; color: #ffffff; border-color: #f59e0b; }\n"
            + ".espresso-btn-warning:hover { background-color: #d97706; border-color: #d97706; }\n"
            
            + ".espresso-btn-help { background-color: #a855f7; color: #ffffff; border-color: #a855f7; }\n"
            + ".espresso-btn-help:hover { background-color: #9333ea; border-color: #9333ea; }\n"
            
            + ".espresso-btn-danger { background-color: #ef4444; color: #ffffff; border-color: #ef4444; }\n"
            + ".espresso-btn-danger:hover { background-color: #dc2626; border-color: #dc2626; }\n"
            
            // Outlined
            + ".espresso-btn-outlined { background-color: transparent; }\n"
            + ".espresso-btn-outlined.espresso-btn-primary { color: #3b82f6; border-color: #3b82f6; }\n"
            + ".espresso-btn-outlined.espresso-btn-primary:hover { background-color: rgba(59, 130, 246, 0.04); }\n"
            + ".espresso-btn-outlined.espresso-btn-secondary { color: #64748b; border-color: #64748b; }\n"
            + ".espresso-btn-outlined.espresso-btn-success { color: #22c55e; border-color: #22c55e; }\n"
            + ".espresso-btn-outlined.espresso-btn-info { color: #0ea5e9; border-color: #0ea5e9; }\n"
            + ".espresso-btn-outlined.espresso-btn-warning { color: #f59e0b; border-color: #f59e0b; }\n"
            + ".espresso-btn-outlined.espresso-btn-help { color: #a855f7; border-color: #a855f7; }\n"
            + ".espresso-btn-outlined.espresso-btn-danger { color: #ef4444; border-color: #ef4444; }\n"
            
            // Text
            + ".espresso-btn-text { background-color: transparent; border-color: transparent; }\n"
            + ".espresso-btn-text.espresso-btn-primary { color: #3b82f6; }\n"
            + ".espresso-btn-text.espresso-btn-primary:hover { background-color: rgba(59, 130, 246, 0.04); border-color: transparent; }\n"
            
            // Rounded
            + ".espresso-btn-rounded { border-radius: 2rem; }\n"
            + "</style>";
    }
}
