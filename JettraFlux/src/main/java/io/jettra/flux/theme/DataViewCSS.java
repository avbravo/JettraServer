package io.jettra.flux.theme;

public class DataViewCSS {
    public static String get() {
        return "<style>\n"
            + ".espresso-dataview { width: 100%; display: flex; flex-direction: column; gap: 1rem; }\n"
            + ".espresso-dataview-header { padding: 1rem; background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px 8px 0 0; display: flex; justify-content: space-between; align-items: center; }\n"
            + ".espresso-dataview-content { padding: 1rem; border: 1px solid #e2e8f0; border-top: none; border-radius: 0 0 8px 8px; }\n"
            + ".espresso-dataview-list { display: flex; flex-direction: column; gap: 1rem; }\n"
            + ".espresso-dataview-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 1rem; }\n"
            + ".dataview-list-item { display: flex; flex-direction: row; align-items: center; justify-content: space-between; padding: 1.5rem; border: 1px solid #e2e8f0; border-radius: 8px; background-color: var(--surface-color); transition: box-shadow 0.2s; }\n"
            + ".dataview-list-item:hover { box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1); }\n"
            + ".dataview-grid-item { display: flex; flex-direction: column; padding: 1.5rem; border: 1px solid #e2e8f0; border-radius: 8px; background-color: var(--surface-color); transition: box-shadow 0.2s; height: 100%; }\n"
            + ".dataview-grid-item:hover { box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1); }\n"
            + "@media (max-width: 768px) { .dataview-list-item { flex-direction: column; gap: 1rem; align-items: stretch; text-align: center; } }\n"
            + "</style>";
    }
}
