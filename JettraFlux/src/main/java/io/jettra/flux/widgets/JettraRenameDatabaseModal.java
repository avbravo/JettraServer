package io.jettra.flux.widgets;

import io.jettra.flux.core.Modifier;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

/**
 * JettraRenameDatabaseModal - Reusable, native JettraFlux modal dialog
 * for renaming databases with complete cloning, user migration, and safe source drop.
 * <p>
 * Built with pure Java 25 standards without raw script embedding in business views.
 * Supports:
 * - Fluid animations and backdrop blur
 * - High-contrast accessible form elements
 * - Explicit source database display and new target name input validation
 * - Native form submission with action="rename_db"
 * </p>
 */
public class JettraRenameDatabaseModal extends Widget {

    private final String dialogId;
    private String title = "Rename Database";
    private String subtitle = "Create a cloned database replica with all multi-model data and user roles, then safely retire the source.";
    private String headerIcon = "fas fa-pen";
    private String headerBadge = "Migration Flow";
    private String currentDbLabel = "Current Database Name:";
    private String newDbLabel = "New Database Name:";
    private String newDbPlaceholder = "e.g. inventory_prod_db";
    private String confirmButtonText = "RENAME DATABASE";
    private String confirmButtonIcon = "fas fa-save";
    private String cancelButtonText = "Cancel";
    private String accentColor = "#38bdf8"; // Sky primary
    private String formAction = "/databases";
    private String actionName = "rename_db";
    private String oldDbParamName = "old_db";
    private String newDbParamName = "new_db";

    private JettraRenameDatabaseModal(String dialogId) {
        this.dialogId = dialogId;
        this.id = dialogId;
    }

    public static JettraRenameDatabaseModal of(String dialogId) {
        return new JettraRenameDatabaseModal(dialogId);
    }

    public static JettraRenameDatabaseModal builder(String dialogId) {
        return new JettraRenameDatabaseModal(dialogId);
    }

    public JettraRenameDatabaseModal title(String title) {
        this.title = title;
        return this;
    }

    public JettraRenameDatabaseModal subtitle(String subtitle) {
        this.subtitle = subtitle;
        return this;
    }

    public JettraRenameDatabaseModal headerIcon(String headerIcon) {
        this.headerIcon = headerIcon != null ? headerIcon : "fas fa-pen";
        return this;
    }

    public JettraRenameDatabaseModal headerBadge(String headerBadge) {
        this.headerBadge = headerBadge;
        return this;
    }

    public JettraRenameDatabaseModal currentDbLabel(String currentDbLabel) {
        this.currentDbLabel = currentDbLabel;
        return this;
    }

    public JettraRenameDatabaseModal newDbLabel(String newDbLabel) {
        this.newDbLabel = newDbLabel;
        return this;
    }

    public JettraRenameDatabaseModal newDbPlaceholder(String newDbPlaceholder) {
        this.newDbPlaceholder = newDbPlaceholder;
        return this;
    }

    public JettraRenameDatabaseModal confirmButtonText(String confirmButtonText) {
        this.confirmButtonText = confirmButtonText;
        return this;
    }

    public JettraRenameDatabaseModal confirmButtonIcon(String confirmButtonIcon) {
        this.confirmButtonIcon = confirmButtonIcon;
        return this;
    }

    public JettraRenameDatabaseModal cancelButtonText(String cancelButtonText) {
        this.cancelButtonText = cancelButtonText;
        return this;
    }

    public JettraRenameDatabaseModal accentColor(String accentColor) {
        this.accentColor = accentColor;
        return this;
    }

    public JettraRenameDatabaseModal formAction(String formAction) {
        this.formAction = formAction;
        return this;
    }

    public JettraRenameDatabaseModal actionName(String actionName) {
        this.actionName = actionName;
        return this;
    }

    public JettraRenameDatabaseModal oldDbParamName(String oldDbParamName) {
        this.oldDbParamName = oldDbParamName;
        return this;
    }

    public JettraRenameDatabaseModal newDbParamName(String newDbParamName) {
        this.newDbParamName = newDbParamName;
        return this;
    }

    public String getDialogId() {
        return dialogId;
    }

    public String getTitle() {
        return title;
    }

    public String getConfirmButtonText() {
        return confirmButtonText;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();

        sb.append("<!-- JettraRenameDatabaseModal: ").append(dialogId).append(" -->\n");
        sb.append("<div id=\"").append(dialogId).append("\" ");
        sb.append("class=\"jettra-flux-modal-overlay ").append(modifier != null ? modifier.getClasses() : "").append("\" ");
        sb.append("style=\"display:none; position:fixed; top:0; left:0; width:100vw; height:100vh; ")
          .append("background:rgba(10, 15, 29, 0.82); backdrop-filter:blur(8px); -webkit-backdrop-filter:blur(8px); ")
          .append("z-index:999999; align-items:center; justify-content:center; ")
          .append(modifier != null ? modifier.getStyles() : "").append("\">\n");

        sb.append("  <div class=\"jettra-flux-modal-dialog\" role=\"dialog\" aria-modal=\"true\" aria-labelledby=\"")
          .append(dialogId).append("_title\" ")
          .append("style=\"width:520px; max-width:92vw; background:#0f172a; border:1px solid rgba(56, 189, 248, 0.4); ")
          .append("box-shadow:0 25px 50px -12px rgba(0,0,0,0.7), 0 0 30px rgba(56, 189, 248, 0.18); ")
          .append("border-radius:14px; overflow:hidden; display:flex; flex-direction:column; animation:jettraModalZoomIn 0.2s cubic-bezier(0.16, 1, 0.3, 1);\">\n");

        // Top accent banner
        sb.append("    <div style=\"height:4px; width:100%; background:linear-gradient(90deg, ").append(accentColor).append(", #818cf8, ").append(accentColor).append(");\"></div>\n");

        // Header
        sb.append("    <div style=\"padding:18px 24px; border-bottom:1px solid rgba(255,255,255,0.08); background:#1e293b; display:flex; justify-content:space-between; align-items:center;\">\n");
        sb.append("      <div style=\"display:flex; align-items:center; gap:12px;\">\n");
        sb.append("        <div style=\"width:38px; height:38px; border-radius:10px; background:rgba(56,189,248,0.15); display:flex; align-items:center; justify-content:center; color:").append(accentColor).append("; font-size:18px;\">\n");
        sb.append("          <i class=\"").append(headerIcon).append("\"></i>\n");
        sb.append("        </div>\n");
        sb.append("        <div>\n");
        sb.append("          <h3 id=\"").append(dialogId).append("_title\" style=\"margin:0; font-size:16px; font-weight:700; color:#f8fafc;\">")
          .append(title).append("</h3>\n");
        if (headerBadge != null && !headerBadge.isBlank()) {
            sb.append("          <span style=\"font-size:11px; color:").append(accentColor).append("; font-weight:600; text-transform:uppercase; letter-spacing:0.5px;\">").append(headerBadge).append("</span>\n");
        }
        sb.append("        </div>\n");
        sb.append("      </div>\n");
        sb.append("      <button type=\"button\" onclick=\"JettraRenameDatabaseModal.close('").append(dialogId).append("')\" ")
          .append("style=\"background:none; border:none; color:#94a3b8; font-size:18px; cursor:pointer; padding:6px; border-radius:6px; transition:all 0.15s;\" ")
          .append("onmouseover=\"this.style.color='#f8fafc'; this.style.background='rgba(255,255,255,0.06)';\" ")
          .append("onmouseout=\"this.style.color='#94a3b8'; this.style.background='none';\">\n");
        sb.append("        <i class=\"fas fa-times\"></i>\n");
        sb.append("      </button>\n");
        sb.append("    </div>\n");

        // Body with Form
        sb.append("    <form id=\"").append(dialogId).append("_form\" method=\"POST\" action=\"").append(formAction).append("\" style=\"margin:0;\">\n");
        sb.append("      <input type=\"hidden\" name=\"action\" value=\"").append(actionName).append("\"/>\n");
        sb.append("      <input type=\"hidden\" id=\"").append(dialogId).append("_oldDbInput\" name=\"").append(oldDbParamName).append("\" value=\"\"/>\n");

        sb.append("      <div style=\"padding:22px 24px; display:flex; flex-direction:column; gap:16px;\">\n");
        if (subtitle != null && !subtitle.isBlank()) {
            sb.append("        <p style=\"margin:0; font-size:13px; color:#94a3b8; line-height:1.5;\">")
              .append(subtitle).append("</p>\n");
        }

        // Current Database Field (Disabled preview)
        sb.append("        <div>\n");
        sb.append("          <label style=\"display:block; font-size:13px; font-weight:600; color:#cbd5e1; margin-bottom:6px;\">")
          .append(currentDbLabel).append("</label>\n");
        sb.append("          <div style=\"position:relative; display:flex; align-items:center;\">\n");
        sb.append("            <i class=\"fas fa-database\" style=\"position:absolute; left:12px; color:#64748b; font-size:14px;\"></i>\n");
        sb.append("            <input type=\"text\" id=\"").append(dialogId).append("_oldDbDisplay\" disabled ")
          .append("style=\"width:100%; padding:10px 12px 10px 36px; background:#1e293b; border:1px solid rgba(255,255,255,0.1); border-radius:8px; color:#94a3b8; font-size:14px; font-weight:600; font-family:monospace; box-sizing:border-box;\"/>\n");
        sb.append("          </div>\n");
        sb.append("        </div>\n");

        // New Database Field (Editable input)
        sb.append("        <div>\n");
        sb.append("          <label for=\"").append(dialogId).append("_newDbInput\" style=\"display:block; font-size:13px; font-weight:600; color:#cbd5e1; margin-bottom:6px;\">")
          .append(newDbLabel).append("</label>\n");
        sb.append("          <div style=\"position:relative; display:flex; align-items:center;\">\n");
        sb.append("            <i class=\"fas fa-file-signature\" style=\"position:absolute; left:12px; color:").append(accentColor).append("; font-size:14px;\"></i>\n");
        sb.append("            <input type=\"text\" id=\"").append(dialogId).append("_newDbInput\" name=\"").append(newDbParamName).append("\" ")
          .append("placeholder=\"").append(newDbPlaceholder).append("\" required autocomplete=\"off\" ")
          .append("style=\"width:100%; padding:10px 12px 10px 36px; background:#0f172a; border:1px solid rgba(56,189,248,0.4); border-radius:8px; color:#f8fafc; font-size:14px; font-weight:600; font-family:monospace; box-sizing:border-box; outline:none; transition:border-color 0.15s;\" ")
          .append("onfocus=\"this.style.borderColor='#38bdf8'; this.style.boxShadow='0 0 0 3px rgba(56,189,248,0.2)';\" ")
          .append("onblur=\"this.style.borderColor='rgba(56,189,248,0.4)'; this.style.boxShadow='none';\"/>\n");
        sb.append("          </div>\n");
        sb.append("        </div>\n");
        sb.append("      </div>\n");

        // Footer Actions
        sb.append("      <div style=\"padding:14px 24px; border-top:1px solid rgba(255,255,255,0.08); background:#1e293b; display:flex; justify-content:flex-end; align-items:center; gap:10px;\">\n");
        sb.append("        <button type=\"button\" onclick=\"JettraRenameDatabaseModal.close('").append(dialogId).append("')\" ")
          .append("style=\"padding:8px 16px; border-radius:8px; background:rgba(255,255,255,0.06); color:#cbd5e1; border:1px solid rgba(255,255,255,0.1); font-size:13px; font-weight:600; cursor:pointer; transition:all 0.15s;\" ")
          .append("onmouseover=\"this.style.background='rgba(255,255,255,0.12)'; this.style.color='#f8fafc';\" ")
          .append("onmouseout=\"this.style.background='rgba(255,255,255,0.06)'; this.style.color='#cbd5e1';\">\n");
        sb.append("          ").append(cancelButtonText).append("\n");
        sb.append("        </button>\n");

        sb.append("        <button type=\"submit\" id=\"").append(dialogId).append("_submitBtn\" ")
          .append("style=\"padding:8px 18px; border-radius:8px; background:linear-gradient(135deg, ").append(accentColor).append(", #0284c7); color:#fff; border:none; font-size:13px; font-weight:700; cursor:pointer; display:flex; align-items:center; gap:8px; box-shadow:0 4px 12px rgba(56,189,248,0.3); transition:all 0.15s;\" ")
          .append("onmouseover=\"this.style.transform='translateY(-1px)'; this.style.boxShadow='0 6px 16px rgba(56,189,248,0.45)';\" ")
          .append("onmouseout=\"this.style.transform='none'; this.style.boxShadow='0 4px 12px rgba(56,189,248,0.3)';\">\n");
        sb.append("          <i class=\"").append(confirmButtonIcon).append("\"></i> ").append(confirmButtonText).append("\n");
        sb.append("        </button>\n");
        sb.append("      </div>\n");

        sb.append("    </form>\n");
        sb.append("  </div>\n");

        // Client JavaScript helper
        sb.append("  <script>\n");
        sb.append("    window.JettraRenameDatabaseModal = window.JettraRenameDatabaseModal || {};\n");
        sb.append("    window.JettraRenameDatabaseModal.open = function(id, oldDb) {\n");
        sb.append("      var modal = document.getElementById(id);\n");
        sb.append("      if (!modal) return;\n");
        sb.append("      var input = document.getElementById(id + '_oldDbInput');\n");
        sb.append("      var display = document.getElementById(id + '_oldDbDisplay');\n");
        sb.append("      var newInput = document.getElementById(id + '_newDbInput');\n");
        sb.append("      if (input && oldDb) input.value = oldDb;\n");
        sb.append("      if (display && oldDb) display.value = oldDb;\n");
        sb.append("      if (newInput) { newInput.value = ''; }\n");
        sb.append("      modal.style.display = 'flex';\n");
        sb.append("      setTimeout(function() { if (newInput) newInput.focus(); }, 100);\n");
        sb.append("    };\n");
        sb.append("    window.JettraRenameDatabaseModal.close = function(id) {\n");
        sb.append("      var modal = document.getElementById(id);\n");
        sb.append("      if (modal) modal.style.display = 'none';\n");
        sb.append("    };\n");
        sb.append("  </script>\n");
        sb.append("</div>\n");

        return sb.toString();
    }
}
