package io.jettra.flux.widgets;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.UUID;
public class ConfirmPopup extends Widget {
    private Widget target;
    private String message;
    private final String id;
    private String actionMethod;
    private ConfirmPopup(Widget target, String message) {
        this.target = target;
        this.message = message;
        this.id = "cpop_" + UUID.randomUUID().toString().replace("-", "");
    }
    public static ConfirmPopup of(Widget target, String message) { return new ConfirmPopup(target, message); }
    public ConfirmPopup actionMethod(String actionMethod) { this.actionMethod = actionMethod; return this; }
    
    @Override public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div style=\"position: relative; display: inline-block;\">");
        sb.append("<div onclick=\"var p=document.getElementById('").append(id).append("'); p.style.display=(p.style.display==='none'?'block':'none');\">");
        if (target != null) sb.append(target.render(theme));
        sb.append("</div>");
        
        sb.append("<div id=\"").append(id).append("\" style=\"display:none; position:absolute; top:100%; left:0; margin-top:8px; background:var(--surface-color, #fff); padding:15px; border-radius:8px; box-shadow:0 4px 6px rgba(0,0,0,0.1); border:1px solid rgba(0,0,0,0.1); z-index:1000; min-width: 250px;\">");
        sb.append("<div style=\"display:flex; align-items:center; gap:10px; margin-bottom:15px;\">");
        sb.append("<i class=\"fas fa-exclamation-triangle\" style=\"color:#eab308;\"></i><span>").append(message).append("</span></div>");
        sb.append("<div style=\"display:flex; justify-content:flex-end; gap:10px;\">");
        sb.append("<button type=\"button\" onclick=\"document.getElementById('").append(id).append("').style.display='none'\" style=\"background:transparent; border:1px solid #ccc; padding:6px 12px; border-radius:4px; cursor:pointer;\">Reject</button>");
        String acceptAction = actionMethod != null ? "window.location.href='" + actionMethod + "'" : "document.getElementById('" + id + "').style.display='none'; document.getElementById('" + id + "_confirmed').showModal();";
        sb.append("<button type=\"button\" onclick=\"").append(acceptAction).append("\" style=\"background:var(--primary-color, #3b82f6); color:white; border:none; padding:6px 12px; border-radius:4px; cursor:pointer;\">Accept</button>");
        sb.append("</div></div></div>");
        
        String confirmedDialogHtml = "<dialog id='" + id + "_confirmed' style='border:none; border-radius:8px; padding:20px; box-shadow:0 10px 15px rgba(0,0,0,0.1); background:var(--surface-color, #fff); color:var(--text-color, #333);'> " +
                                     "<h4 style=\"margin-top:0; margin-bottom:15px;\"><i class=\"fas fa-check-circle\" style=\"color:#10b981; margin-right:8px;\"></i>Confirmed</h4>" + 
                                     "<p style=\"margin-bottom:20px;\">Action completed successfully.</p>" +
                                     "<div style=\"text-align:right;\"><button type=\"button\" onclick=\"this.closest('dialog').close();\" style=\"background:var(--primary-color, #3b82f6); color:white; border:none; padding:8px 16px; border-radius:6px; cursor:pointer;\">OK</button></div>" + 
                                     "</dialog>";
        sb.append(confirmedDialogHtml);
        return sb.toString();
    }
}
