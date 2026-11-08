package io.jettra.flux.theme;

public class DataTableCSS {
    public static String get() {
        return "<style>\n"
            + ".espresso-datatable { width: 100%; border-collapse: separate; border-spacing: 0; margin-top: 10px; font-size: 14px; }\n"
            + ".espresso-datatable th { padding: 12px 16px; text-align: left; background-color: #f8fafc; color: #475569; font-weight: 600; border-bottom: 1px solid #e2e8f0; border-top: 1px solid #e2e8f0; }\n"
            + ".espresso-datatable td { padding: 16px; border-bottom: 1px solid #e2e8f0; color: #334155; }\n"
            + ".espresso-datatable tr:hover td { background-color: #f1f5f9; }\n"
            + ".badge { padding: 4px 8px; border-radius: 6px; font-weight: 700; font-size: 11px; text-transform: uppercase; letter-spacing: 0.3px; }\n"
            + ".badge.qualified { background-color: #dcfce7; color: #16a34a; }\n"
            + ".badge.new { background-color: #dbeafe; color: #2563eb; }\n"
            + ".badge.unqualified { background-color: #fee2e2; color: #dc2626; }\n"
            + ".badge.negotiation { background-color: #fef9c3; color: #ca8a04; }\n"
            + ".avatar-group { display: flex; align-items: center; gap: 8px; font-weight: 600; }\n"
            + ".avatar-img { width: 32px; height: 32px; border-radius: 50%; object-fit: cover; border: 2px solid white; box-shadow: 0 0 0 1px #e2e8f0; }\n"
            + ".progress-track { width: 100%; height: 8px; background-color: #e2e8f0; border-radius: 4px; overflow: hidden; }\n"
            + ".progress-bar { height: 100%; background-color: #3b82f6; border-radius: 4px; }\n"
            + ".activity-cell { display: flex; align-items: center; gap: 10px; }\n"
            + "</style>";
    }
}
