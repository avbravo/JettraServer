package io.jettra.flux.widgets;

import io.jettra.flux.core.Modifier;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

/**
 * JettraConfirmDialog - Reusable, native JettraFlux confirmation modal dialog.
 * Designed for destructive or critical actions (such as database dropping, user revocation, or partition removal).
 * Features:
 * - Backdrop context blocking with blur filter
 * - Explicit target item highlight and customizable icon
 * - Warning icon with pulsing danger accents
 * - Native form integration or typed callback dispatch
 * - Pure Java 25 standards without raw external script dependencies
 */
public class JettraConfirmDialog extends Widget {

    private final String dialogId;
    private String title = "Confirm Destructive Action";
    private String warningMessage = "Are you sure you want to proceed? This action cannot be undone.";
    private String targetItemLabel = "Target Item:";
    private String targetItemValue = "";
    private String targetItemIcon = "fas fa-database";
    private String headerIcon = "fas fa-exclamation-triangle";
    private String headerBadgeText = "Destructive Operation";
    private String confirmText = "Confirm Deletion";
    private String confirmIcon = "fas fa-trash-alt";
    private String cancelText = "Cancel";
    private String confirmButtonColor = "#f43f5e"; // Rose/Danger
    private String formAction = "";
    private String actionName = "drop_db";
    private String targetParamName = "target_db";
    private String onConfirmJs = "";

    private JettraConfirmDialog(String dialogId) {
        this.dialogId = dialogId;
        this.id = dialogId;
    }

    public static JettraConfirmDialog of(String dialogId) {
        return new JettraConfirmDialog(dialogId);
    }

    public static JettraConfirmDialog builder(String dialogId) {
        return new JettraConfirmDialog(dialogId);
    }

    public JettraConfirmDialog title(String title) {
        this.title = title;
        return this;
    }

    public JettraConfirmDialog warningMessage(String warningMessage) {
        this.warningMessage = warningMessage;
        return this;
    }

    public JettraConfirmDialog targetItemLabel(String targetItemLabel) {
        this.targetItemLabel = targetItemLabel;
        return this;
    }

    public JettraConfirmDialog targetItemValue(String targetItemValue) {
        this.targetItemValue = targetItemValue;
        return this;
    }

    public JettraConfirmDialog targetItemIcon(String targetItemIcon) {
        this.targetItemIcon = targetItemIcon != null ? targetItemIcon : "fas fa-database";
        return this;
    }

    public JettraConfirmDialog headerIcon(String headerIcon) {
        this.headerIcon = headerIcon != null ? headerIcon : "fas fa-exclamation-triangle";
        return this;
    }

    public JettraConfirmDialog headerBadgeText(String headerBadgeText) {
        this.headerBadgeText = headerBadgeText;
        return this;
    }

    public JettraConfirmDialog confirmText(String confirmText) {
        this.confirmText = confirmText;
        return this;
    }

    public JettraConfirmDialog confirmIcon(String confirmIcon) {
        this.confirmIcon = confirmIcon != null ? confirmIcon : "fas fa-trash-alt";
        return this;
    }

    public JettraConfirmDialog cancelText(String cancelText) {
        this.cancelText = cancelText;
        return this;
    }

    public JettraConfirmDialog confirmButtonColor(String color) {
        this.confirmButtonColor = color;
        return this;
    }

    public JettraConfirmDialog formAction(String formAction) {
        this.formAction = formAction;
        return this;
    }

    public JettraConfirmDialog actionName(String actionName) {
        this.actionName = actionName;
        return this;
    }

    public JettraConfirmDialog targetParamName(String targetParamName) {
        this.targetParamName = targetParamName;
        return this;
    }

    public JettraConfirmDialog onConfirmJs(String onConfirmJs) {
        this.onConfirmJs = onConfirmJs;
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();

        sb.append("<!-- JettraConfirmDialog: ").append(dialogId).append(" -->\n");
        sb.append("<div id=\"").append(dialogId).append("\" ");
        sb.append("class=\"jettra-flux-modal-overlay ").append(modifier != null ? modifier.getClasses() : "").append("\" ");
        sb.append("style=\"display:none; position:fixed; top:0; left:0; width:100vw; height:100vh; ")
          .append("background:rgba(10, 15, 29, 0.82); backdrop-filter:blur(8px); -webkit-backdrop-filter:blur(8px); ")
          .append("z-index:999999; align-items:center; justify-content:center; ")
          .append(modifier != null ? modifier.getStyles() : "").append("\">\n");

        sb.append("  <div class=\"jettra-flux-modal-dialog\" role=\"alertdialog\" aria-modal=\"true\" aria-labelledby=\"")
          .append(dialogId).append("_title\" aria-describedby=\"").append(dialogId).append("_desc\" ")
          .append("style=\"width:520px; max-width:92vw; background:#0f172a; border:1px solid rgba(244, 63, 94, 0.4); ")
          .append("box-shadow:0 25px 50px -12px rgba(0,0,0,0.7), 0 0 30px rgba(244, 63, 94, 0.18); ")
          .append("border-radius:14px; overflow:hidden; display:flex; flex-direction:column; animation:jettraModalZoomIn 0.2s cubic-bezier(0.16, 1, 0.3, 1);\">\n");

        // Top danger accent banner
        sb.append("    <div style=\"height:4px; width:100%; background:linear-gradient(90deg, #f43f5e, #fb7185, #f43f5e);\"></div>\n");

        // Header
        sb.append("    <div style=\"padding:18px 24px; border-bottom:1px solid rgba(255,255,255,0.08); background:#1e293b; display:flex; justify-content:space-between; align-items:center;\">\n");
        sb.append("      <div style=\"display:flex; align-items:center; gap:12px;\">\n");
        sb.append("        <div style=\"width:38px; height:38px; border-radius:10px; background:rgba(244,63,94,0.15); display:flex; align-items:center; justify-content:center; color:#f43f5e; font-size:18px;\">\n");
        sb.append("          <i class=\"").append(headerIcon).append("\"></i>\n");
        sb.append("        </div>\n");
        sb.append("        <div>\n");
        sb.append("          <h3 id=\"").append(dialogId).append("_title\" style=\"margin:0; font-size:16px; font-weight:700; color:#f8fafc;\">")
          .append(title).append("</h3>\n");
        if (headerBadgeText != null && !headerBadgeText.isBlank()) {
            sb.append("          <span style=\"font-size:11px; color:#f43f5e; font-weight:600; text-transform:uppercase; letter-spacing:0.5px;\">").append(headerBadgeText).append("</span>\n");
        }
        sb.append("        </div>\n");
        sb.append("      </div>\n");
        sb.append("      <button type=\"button\" onclick=\"JettraConfirmDialog.close('").append(dialogId).append("')\" ")
          .append("style=\"background:none; border:none; color:#94a3b8; font-size:18px; cursor:pointer; padding:6px; border-radius:6px; transition:all 0.15s;\" ")
          .append("onmouseover=\"this.style.color='#f8fafc'; this.style.background='rgba(255,255,255,0.06)';\" ")
          .append("onmouseout=\"this.style.color='#94a3b8'; this.style.background='none';\">\n");
        sb.append("        <i class=\"fas fa-times\"></i>\n");
        sb.append("      </button>\n");
        sb.append("    </div>\n");

        // Body with Form
        sb.append("    <form id=\"").append(dialogId).append("_form\" method=\"POST\" action=\"").append(formAction).append("\" style=\"margin:0;\">\n");
        sb.append("      <input type=\"hidden\" name=\"action\" value=\"").append(actionName).append("\"/>\n");
        sb.append("      <input type=\"hidden\" id=\"").append(dialogId).append("_targetInput\" name=\"").append(targetParamName).append("\" value=\"").append(targetItemValue).append("\"/>\n");

        sb.append("      <div style=\"padding:22px 24px; display:flex; flex-direction:column; gap:16px;\">\n");
        sb.append("        <p id=\"").append(dialogId).append("_desc\" style=\"margin:0; font-size:13.5px; color:#cbd5e1; line-height:1.5;\">")
          .append(warningMessage).append("</p>\n");

        // Target Item Box
        sb.append("        <div style=\"background:rgba(15,23,42,0.8); border:1px solid rgba(244,63,94,0.25); border-radius:10px; padding:12px 16px; display:flex; align-items:center; gap:12px;\">\n");
        sb.append("          <i class=\"").append(targetItemIcon).append("\" style=\"color:#f43f5e; font-size:18px;\"></i>\n");
        sb.append("          <div style=\"flex:1; overflow:hidden;\">\n");
        sb.append("            <div style=\"font-size:11px; color:#94a3b8; text-transform:uppercase; font-weight:600;\">").append(targetItemLabel).append("</div>\n");
        sb.append("            <div id=\"").append(dialogId).append("_targetDisplay\" style=\"font-size:15px; font-weight:700; color:#f8fafc; font-family:monospace; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;\">")
          .append(targetItemValue.isEmpty() ? "None selected" : targetItemValue).append("</div>\n");
        sb.append("          </div>\n");
        sb.append("        </div>\n");
        sb.append("      </div>\n");

        // Footer Actions
        sb.append("      <div style=\"padding:14px 24px; border-top:1px solid rgba(255,255,255,0.08); background:#1e293b; display:flex; justify-content:flex-end; align-items:center; gap:10px;\">\n");
        sb.append("        <button type=\"button\" onclick=\"JettraConfirmDialog.close('").append(dialogId).append("')\" ")
          .append("style=\"padding:8px 16px; border-radius:8px; background:rgba(255,255,255,0.06); color:#cbd5e1; border:1px solid rgba(255,255,255,0.1); font-size:13px; font-weight:600; cursor:pointer; transition:all 0.15s;\" ")
          .append("onmouseover=\"this.style.background='rgba(255,255,255,0.12)'; this.style.color='#f8fafc';\" ")
          .append("onmouseout=\"this.style.background='rgba(255,255,255,0.06)'; this.style.color='#cbd5e1';\">\n");
        sb.append("          ").append(cancelText).append("\n");
        sb.append("        </button>\n");

        String confirmClick = (onConfirmJs != null && !onConfirmJs.isBlank())
            ? onConfirmJs
            : "document.getElementById('" + dialogId + "_form').submit();";

        sb.append("        <button type=\"button\" onclick=\"").append(confirmClick).append("\" ")
          .append("style=\"padding:8px 18px; border-radius:8px; background:linear-gradient(135deg, ").append(confirmButtonColor).append(", #be123c); color:#fff; border:none; font-size:13px; font-weight:700; cursor:pointer; display:flex; align-items:center; gap:8px; box-shadow:0 4px 12px rgba(244,63,94,0.3); transition:all 0.15s;\" ")
          .append("onmouseover=\"this.style.transform='translateY(-1px)'; this.style.boxShadow='0 6px 16px rgba(244,63,94,0.45)';\" ")
          .append("onmouseout=\"this.style.transform='none'; this.style.boxShadow='0 4px 12px rgba(244,63,94,0.3)';\">\n");
        sb.append("          <i class=\"").append(confirmIcon).append("\"></i> ").append(confirmText).append("\n");
        sb.append("        </button>\n");
        sb.append("      </div>\n");

        sb.append("    </form>\n");
        sb.append("  </div>\n");

        // Client JavaScript helper
        sb.append("  <script>\n");
        sb.append("    if (!window.JettraConfirmDialog) {\n");
        sb.append("      window.JettraConfirmDialog = {\n");
        sb.append("        open: function(id, targetValue, targetLabel) {\n");
        sb.append("          var modal = document.getElementById(id);\n");
        sb.append("          if (!modal) return;\n");
        sb.append("          var input = document.getElementById(id + '_targetInput');\n");
        sb.append("          var display = document.getElementById(id + '_targetDisplay');\n");
        sb.append("          if (input && targetValue) input.value = targetValue;\n");
        sb.append("          if (display && targetValue) display.innerText = targetLabel ? (targetLabel + ' (' + targetValue + ')') : targetValue;\n");
        sb.append("          modal.style.display = 'flex';\n");
        sb.append("          document.body.style.overflow = 'hidden';\n");
        sb.append("        },\n");
        sb.append("        close: function(id) {\n");
        sb.append("          var modal = document.getElementById(id);\n");
        sb.append("          if (!modal) return;\n");
        sb.append("          modal.style.display = 'none';\n");
        sb.append("          document.body.style.overflow = '';\n");
        sb.append("        }\n");
        sb.append("      };\n");
        sb.append("    }\n");
        sb.append("  </script>\n");

        sb.append("</div>\n");

        return sb.toString();
    }
}
