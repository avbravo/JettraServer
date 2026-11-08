package io.jettra.flux.widgets;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.UUID;
public class ConfirmDialog extends Widget {
    private Widget trigger;
    private String header = "Confirmation";
    private String message;
    private String actionMethod;
    private final String id;
    
    private ConfirmDialog(Widget trigger, String message) {
        this.trigger = trigger;
        this.message = message;
        this.id = "cdlg_" + UUID.randomUUID().toString().replace("-", "");
    }
    public static ConfirmDialog of(Widget trigger, String message) { return new ConfirmDialog(trigger, message); }
    public ConfirmDialog header(String header) { this.header = header; return this; }
    public ConfirmDialog actionMethod(String actionMethod) { this.actionMethod = actionMethod; return this; }
    
    @Override public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div style=\"display:inline-block;\">");
        sb.append("<div onclick=\"document.getElementById('").append(id).append("').showModal();\">");
        if (trigger != null) sb.append(trigger.render(theme));
        sb.append("</div>");
        
        sb.append("<dialog id=\"").append(id).append("\" style=\"padding:0; border:none; border-radius:8px; box-shadow:0 10px 15px rgba(0,0,0,0.1); max-width:400px; width:100%;\">");
        sb.append("<div style=\"padding:15px 20px; border-bottom:1px solid rgba(0,0,0,0.1); display:flex; justify-content:space-between; align-items:center;\">");
        sb.append("<h3 style=\"margin:0; font-size:1.1rem;\">").append(header).append("</h3>");
        sb.append("<button onclick=\"document.getElementById('").append(id).append("').close()\" style=\"background:none; border:none; font-size:1.2rem; cursor:pointer;\">&times;</button>");
        sb.append("</div>");
        sb.append("<div style=\"padding:20px;\">").append(message).append("</div>");
        sb.append("<div style=\"padding:15px 20px; border-top:1px solid rgba(0,0,0,0.1); display:flex; justify-content:flex-end; gap:10px;\">");
        sb.append("<button type=\"button\" onclick=\"document.getElementById('").append(id).append("').close()\" style=\"background:transparent; border:1px solid #ccc; padding:8px 16px; border-radius:6px; cursor:pointer;\">Reject</button>");
        String acceptAction = actionMethod != null ? "window.location.href='" + actionMethod + "'" : "document.getElementById('"+id+"').close(); document.getElementById('" + id + "_confirmed').showModal();";
        sb.append("<button type=\"button\" onclick=\"").append(acceptAction).append("\" style=\"background:var(--primary-color, #3b82f6); color:white; border:none; padding:8px 16px; border-radius:6px; cursor:pointer;\">Accept</button>");
        sb.append("</div></dialog></div>");
        
        String confirmedDialogHtml = "<dialog id='" + id + "_confirmed' style='border:none; border-radius:8px; padding:20px; box-shadow:0 10px 15px rgba(0,0,0,0.1); background:var(--surface-color, #fff); color:var(--text-color, #333); max-width:400px; width:100%;'> " +
                                     "<div style=\"display:flex; align-items:center; margin-bottom:15px;\"><i class=\"fas fa-check-circle\" style=\"color:#10b981; font-size:1.5rem; margin-right:10px;\"></i><h3 style=\"margin:0; font-size:1.2rem;\">Confirmed</h3></div>" + 
                                     "<p style=\"margin-bottom:20px; color:var(--text-color-secondary, #666);\">Action completed successfully.</p>" +
                                     "<div style=\"text-align:right;\"><button type=\"button\" onclick=\"this.closest('dialog').close();\" style=\"background:var(--primary-color, #3b82f6); color:white; border:none; padding:8px 16px; border-radius:6px; cursor:pointer;\">OK</button></div>" + 
                                     "</dialog>";
        sb.append(confirmedDialogHtml);
        
        return sb.toString();
    }
}
