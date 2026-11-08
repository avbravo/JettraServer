package io.jettra.flux.widgets;

import io.jettra.flux.core.Modifier;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * JettraUserEditModal - Dedicated, high-performance modal component for editing user profiles,
 * RBAC roles, account status, and multi-database access assignments in JettraDB.
 * Built with pure Java 25 standards adhering to JettraFlux component specifications.
 * Features:
 * - Immutable username identity enforcement with visual security lock
 * - Role selection across system RBAC roles (DB_ADMIN, READ_WRITE, READ_ONLY, MANAGER)
 * - Integrated MultiSelect component pre-populated with authorized database scopes
 * - Non-destructive credential handling: password hash is preserved unless explicitly overridden
 * - Client-side state hydration supporting both instantaneous local binding and asynchronous server fetching
 */
public class JettraUserEditModal extends Widget {

    private final String modalId;
    private String title = "Edit User Profile & Permissions";
    private String subtitle = "Modify role assignments, database authorizations, and account status";
    private String icon = "fas fa-user-edit";
    private String badgeText = "SECURITY AUDITED";
    private String formAction = "";
    private String actionName = "update_user";
    private String submitText = "Guardar Cambios";
    private String cancelText = "Cancelar";
    private String maxWidth = "660px";
    private final Set<String> availableRoles = new LinkedHashSet<>(List.of("DB_ADMIN", "READ_WRITE", "READ_ONLY", "MANAGER"));
    private final Set<String> availableDatabases = new LinkedHashSet<>();

    protected JettraUserEditModal(String modalId) {
        this.modalId = modalId;
        this.id = modalId;
    }

    public static JettraUserEditModal of(String modalId) {
        return new JettraUserEditModal(modalId);
    }

    public static JettraUserEditModal builder(String modalId) {
        return new JettraUserEditModal(modalId);
    }

    public JettraUserEditModal title(String title) {
        if (title != null) this.title = title;
        return this;
    }

    public JettraUserEditModal subtitle(String subtitle) {
        if (subtitle != null) this.subtitle = subtitle;
        return this;
    }

    public JettraUserEditModal icon(String icon) {
        if (icon != null) this.icon = icon;
        return this;
    }

    public JettraUserEditModal badgeText(String badgeText) {
        this.badgeText = badgeText;
        return this;
    }

    public JettraUserEditModal formAction(String formAction) {
        if (formAction != null) this.formAction = formAction;
        return this;
    }

    public JettraUserEditModal actionName(String actionName) {
        if (actionName != null) this.actionName = actionName;
        return this;
    }

    public JettraUserEditModal submitText(String submitText) {
        if (submitText != null) this.submitText = submitText;
        return this;
    }

    public JettraUserEditModal cancelText(String cancelText) {
        if (cancelText != null) this.cancelText = cancelText;
        return this;
    }

    public JettraUserEditModal maxWidth(String maxWidth) {
        if (maxWidth != null) this.maxWidth = maxWidth;
        return this;
    }

    public JettraUserEditModal roles(Collection<String> roles) {
        if (roles != null && !roles.isEmpty()) {
            this.availableRoles.clear();
            this.availableRoles.addAll(roles);
        }
        return this;
    }

    public JettraUserEditModal roles(String... roles) {
        if (roles != null && roles.length > 0) {
            return roles(Arrays.asList(roles));
        }
        return this;
    }

    public JettraUserEditModal databases(Collection<String> databases) {
        if (databases != null) {
            this.availableDatabases.clear();
            this.availableDatabases.addAll(databases);
        }
        return this;
    }

    public JettraUserEditModal databases(String... databases) {
        if (databases != null) {
            return databases(Arrays.asList(databases));
        }
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        String dbMultiSelectId = modalId + "_dbs";

        // Build embedded MultiSelect for assigned databases
        MultiSelect dbsMultiSelect = MultiSelect.of(dbMultiSelectId, "target_dbs")
            .label("Authorized Databases Access")
            .selectAllOption(true, "* (All Databases)")
            .options(availableDatabases)
            .placeholder("Select one or more target databases...")
            .quickActions(true);

        StringBuilder sb = new StringBuilder();

        sb.append("<!-- JettraUserEditModal: ").append(modalId).append(" -->\n");
        sb.append("<div id=\"").append(modalId).append("\" ");
        sb.append("class=\"jettra-flux-modal-overlay ").append(modifier != null ? modifier.getClasses() : "").append("\" ");
        sb.append("style=\"display:none; position:fixed; top:0; left:0; width:100vw; height:100vh; ")
          .append("background:rgba(10, 15, 29, 0.82); backdrop-filter:blur(8px); -webkit-backdrop-filter:blur(8px); ")
          .append("z-index:999999; align-items:center; justify-content:center; ")
          .append(modifier != null ? modifier.getStyles() : "").append("\">\n");

        sb.append("  <div class=\"jettra-flux-modal-dialog\" role=\"dialog\" aria-modal=\"true\" aria-labelledby=\"")
          .append(modalId).append("_title\" ")
          .append("style=\"width:").append(maxWidth).append("; max-width:94vw; max-height:92vh; background:#0f172a; border:1px solid rgba(56, 189, 248, 0.35); ")
          .append("box-shadow:0 25px 50px -12px rgba(0,0,0,0.7), 0 0 30px rgba(56, 189, 248, 0.15); ")
          .append("border-radius:14px; overflow:hidden; display:flex; flex-direction:column; animation:jettraModalZoomIn 0.2s cubic-bezier(0.16, 1, 0.3, 1);\">\n");

        // Top accent line
        sb.append("    <div style=\"height:4px; width:100%; background:linear-gradient(90deg, #0284c7, #38bdf8, #818cf8);\"></div>\n");

        // Header
        sb.append("    <div style=\"padding:16px 22px; border-bottom:1px solid rgba(255,255,255,0.08); background:#1e293b; display:flex; justify-content:space-between; align-items:center;\">\n");
        sb.append("      <div style=\"display:flex; align-items:center; gap:12px;\">\n");
        sb.append("        <div style=\"width:38px; height:38px; border-radius:10px; background:rgba(56,189,248,0.15); display:flex; align-items:center; justify-content:center; color:#38bdf8; font-size:18px;\">\n");
        sb.append("          <i class=\"").append(icon).append("\"></i>\n");
        sb.append("        </div>\n");
        sb.append("        <div>\n");
        sb.append("          <h3 id=\"").append(modalId).append("_title\" style=\"margin:0; font-size:16px; font-weight:700; color:#f8fafc; display:flex; align-items:center; gap:8px;\">\n");
        sb.append("            ").append(title).append("\n");
        if (badgeText != null && !badgeText.isBlank()) {
            sb.append("            <span style=\"font-size:10px; font-weight:700; padding:2px 8px; border-radius:12px; background:rgba(56,189,248,0.15); color:#38bdf8; border:1px solid rgba(56,189,248,0.3);\">")
              .append(badgeText).append("</span>\n");
        }
        sb.append("          </h3>\n");
        if (subtitle != null && !subtitle.isBlank()) {
            sb.append("          <p style=\"margin:2px 0 0 0; font-size:12px; color:#94a3b8;\">").append(subtitle).append("</p>\n");
        }
        sb.append("        </div>\n");
        sb.append("      </div>\n");
        sb.append("      <button type=\"button\" onclick=\"JettraUserEditModal.close('").append(modalId).append("')\" ")
          .append("style=\"background:none; border:none; color:#94a3b8; font-size:18px; cursor:pointer; padding:6px; border-radius:6px; transition:all 0.15s;\" ")
          .append("onmouseover=\"this.style.color='#f8fafc'; this.style.background='rgba(255,255,255,0.06)';\" ")
          .append("onmouseout=\"this.style.color='#94a3b8'; this.style.background='none';\">\n");
        sb.append("        <i class=\"fas fa-times\"></i>\n");
        sb.append("      </button>\n");
        sb.append("    </div>\n");

        // Form Body
        sb.append("    <form id=\"").append(modalId).append("_form\" method=\"POST\" action=\"").append(formAction).append("\" style=\"margin:0; overflow-y:auto; display:flex; flex-direction:column; flex:1;\">\n");
        sb.append("      <input type=\"hidden\" name=\"action\" value=\"").append(actionName).append("\"/>\n");
        sb.append("      <input type=\"hidden\" id=\"").append(modalId).append("_userId\" name=\"user_id\" value=\"\"/>\n");

        sb.append("      <div style=\"padding:20px 22px; display:flex; flex-direction:column; gap:16px;\">\n");

        // Top Grid: Username (Immutable) & Email
        sb.append("        <div style=\"display:grid; grid-template-columns:1fr 1fr; gap:14px;\">\n");

        // Username (Immutable / Locked)
        sb.append("          <div style=\"display:flex; flex-direction:column; gap:6px;\">\n");
        sb.append("            <label style=\"font-size:11.5px; color:#94a3b8; font-weight:700; text-transform:uppercase; letter-spacing:0.5px;\">Username (Immutable)</label>\n");
        sb.append("            <div style=\"position:relative; display:flex; align-items:center;\">\n");
        sb.append("              <i class=\"fas fa-lock\" style=\"position:absolute; left:12px; color:#38bdf8; font-size:13px;\"></i>\n");
        sb.append("              <input type=\"text\" id=\"").append(modalId).append("_username\" name=\"username\" readonly ")
          .append("style=\"width:100%; box-sizing:border-box; padding:9px 12px 9px 34px; background:#0b0f19; border:1px solid rgba(56,189,248,0.3); border-radius:8px; color:#38bdf8; font-family:monospace; font-weight:700; font-size:13px; cursor:not-allowed;\"/>\n");
        sb.append("            </div>\n");
        sb.append("            <span style=\"font-size:10.5px; color:#64748b;\">Primary identity key cannot be modified.</span>\n");
        sb.append("          </div>\n");

        // Email
        sb.append("          <div style=\"display:flex; flex-direction:column; gap:6px;\">\n");
        sb.append("            <label style=\"font-size:11.5px; color:#94a3b8; font-weight:700; text-transform:uppercase; letter-spacing:0.5px;\">Email Address</label>\n");
        sb.append("            <div style=\"position:relative; display:flex; align-items:center;\">\n");
        sb.append("              <i class=\"fas fa-envelope\" style=\"position:absolute; left:12px; color:#94a3b8; font-size:13px;\"></i>\n");
        sb.append("              <input type=\"email\" id=\"").append(modalId).append("_email\" name=\"email\" placeholder=\"user@company.com\" ")
          .append("style=\"width:100%; box-sizing:border-box; padding:9px 12px 9px 34px; background:#0f172a; border:1px solid rgba(255,255,255,0.15); border-radius:8px; color:#f8fafc; font-size:13px;\"/>\n");
        sb.append("            </div>\n");
        sb.append("          </div>\n");

        sb.append("        </div>\n");

        // Second Grid: Role & Account Status
        sb.append("        <div style=\"display:grid; grid-template-columns:1fr 1fr; gap:14px;\">\n");

        // Role Dropdown
        sb.append("          <div style=\"display:flex; flex-direction:column; gap:6px;\">\n");
        sb.append("            <label style=\"font-size:11.5px; color:#94a3b8; font-weight:700; text-transform:uppercase; letter-spacing:0.5px;\">Assigned RBAC Role</label>\n");
        sb.append("            <select id=\"").append(modalId).append("_role\" name=\"role\" ")
          .append("style=\"width:100%; box-sizing:border-box; padding:9px 12px; background:#0f172a; border:1px solid rgba(255,255,255,0.15); border-radius:8px; color:#f8fafc; font-size:13px; cursor:pointer;\">\n");
        for (String role : availableRoles) {
            sb.append("              <option value=\"").append(role).append("\">").append(role).append("</option>\n");
        }
        sb.append("            </select>\n");
        sb.append("          </div>\n");

        // Status Dropdown
        sb.append("          <div style=\"display:flex; flex-direction:column; gap:6px;\">\n");
        sb.append("            <label style=\"font-size:11.5px; color:#94a3b8; font-weight:700; text-transform:uppercase; letter-spacing:0.5px;\">Account Status</label>\n");
        sb.append("            <select id=\"").append(modalId).append("_active\" name=\"active\" ")
          .append("style=\"width:100%; box-sizing:border-box; padding:9px 12px; background:#0f172a; border:1px solid rgba(255,255,255,0.15); border-radius:8px; color:#f8fafc; font-size:13px; cursor:pointer;\">\n");
        sb.append("              <option value=\"true\">ACTIVE (Access Granted)</option>\n");
        sb.append("              <option value=\"false\">DISABLED (Access Revoked)</option>\n");
        sb.append("            </select>\n");
        sb.append("          </div>\n");

        sb.append("        </div>\n");

        // Assigned Databases MultiSelect Component
        sb.append("        <div style=\"margin-top:2px;\">\n");
        sb.append(dbsMultiSelect.render(theme));
        sb.append("        </div>\n");

        // Optional Password Reset
        sb.append("        <div style=\"display:flex; flex-direction:column; gap:6px; margin-top:2px;\">\n");
        sb.append("          <label style=\"font-size:11.5px; color:#94a3b8; font-weight:700; text-transform:uppercase; letter-spacing:0.5px;\">Reset Password (Optional)</label>\n");
        sb.append("          <div style=\"position:relative; display:flex; align-items:center;\">\n");
        sb.append("            <i class=\"fas fa-key\" style=\"position:absolute; left:12px; color:#94a3b8; font-size:13px;\"></i>\n");
        sb.append("            <input type=\"password\" id=\"").append(modalId).append("_password\" name=\"password\" placeholder=\"••••••••  (Leave blank to keep unchanged)\" ")
          .append("style=\"width:100%; box-sizing:border-box; padding:9px 12px 9px 34px; background:#0f172a; border:1px solid rgba(255,255,255,0.15); border-radius:8px; color:#f8fafc; font-size:13px;\"/>\n");
        sb.append("          </div>\n");
        sb.append("          <span style=\"font-size:10.5px; color:#64748b;\">Existing SHA-256 hashed credentials and active tokens are securely preserved if left empty.</span>\n");
        sb.append("        </div>\n");

        sb.append("      </div>\n");

        // Footer Actions
        sb.append("      <div style=\"padding:14px 22px; border-top:1px solid rgba(255,255,255,0.08); background:#1e293b; display:flex; justify-content:flex-end; align-items:center; gap:10px;\">\n");
        sb.append("        <button type=\"button\" onclick=\"JettraUserEditModal.close('").append(modalId).append("')\" ")
          .append("style=\"padding:8px 16px; border-radius:8px; background:rgba(255,255,255,0.06); color:#cbd5e1; border:1px solid rgba(255,255,255,0.1); font-size:13px; font-weight:600; cursor:pointer; transition:all 0.15s;\" ")
          .append("onmouseover=\"this.style.background='rgba(255,255,255,0.12)'; this.style.color='#f8fafc';\" ")
          .append("onmouseout=\"this.style.background='rgba(255,255,255,0.06)'; this.style.color='#cbd5e1';\">\n");
        sb.append("          <i class=\"fas fa-times\" style=\"margin-right:6px;\"></i>").append(cancelText).append("\n");
        sb.append("        </button>\n");

        sb.append("        <button type=\"submit\" ")
          .append("style=\"padding:8px 20px; border-radius:8px; background:linear-gradient(135deg, #0284c7, #0369a1); color:#fff; border:none; font-size:13px; font-weight:700; cursor:pointer; display:flex; align-items:center; gap:8px; box-shadow:0 4px 14px rgba(2,132,199,0.35); transition:all 0.15s;\" ")
          .append("onmouseover=\"this.style.transform='translateY(-1px)'; this.style.boxShadow='0 6px 18px rgba(2,132,199,0.5)';\" ")
          .append("onmouseout=\"this.style.transform='none'; this.style.boxShadow='0 4px 14px rgba(2,132,199,0.35)';\">\n");
        sb.append("          <i class=\"fas fa-save\"></i> ").append(submitText).append("\n");
        sb.append("        </button>\n");
        sb.append("      </div>\n");

        sb.append("    </form>\n");
        sb.append("  </div>\n");

        // Client JavaScript Lifecycle Helper
        sb.append("  <script>\n");
        sb.append("    if (!window.JettraUserEditModal) {\n");
        sb.append("      window.JettraUserEditModal = {\n");
        sb.append("        open: function(modalId, data) {\n");
        sb.append("          var modal = document.getElementById(modalId);\n");
        sb.append("          if (!modal) return;\n");
        sb.append("          if (data) {\n");
        sb.append("            var uId = document.getElementById(modalId + '_userId');\n");
        sb.append("            if (uId && data.userId) uId.value = data.userId;\n");
        sb.append("            var uName = document.getElementById(modalId + '_username');\n");
        sb.append("            if (uName && data.username) uName.value = data.username;\n");
        sb.append("            var email = document.getElementById(modalId + '_email');\n");
        sb.append("            if (email) email.value = data.email || '';\n");
        sb.append("            var role = document.getElementById(modalId + '_role');\n");
        sb.append("            if (role && data.role) role.value = data.role;\n");
        sb.append("            var active = document.getElementById(modalId + '_active');\n");
        sb.append("            if (active) active.value = (data.active === false || data.active === 'false') ? 'false' : 'true';\n");
        sb.append("            var pwd = document.getElementById(modalId + '_password');\n");
        sb.append("            if (pwd) pwd.value = '';\n");
        sb.append("            var dbs = data.databases || data.assignedDatabases || '';\n");
        sb.append("            if (window.JettraMultiSelect) {\n");
        sb.append("              window.JettraMultiSelect.setSelectedValues(modalId + '_dbs', dbs);\n");
        sb.append("            }\n");
        sb.append("          }\n");
        sb.append("          modal.style.display = 'flex';\n");
        sb.append("          document.body.style.overflow = 'hidden';\n");
        sb.append("        },\n");
        sb.append("        close: function(modalId) {\n");
        sb.append("          var modal = document.getElementById(modalId);\n");
        sb.append("          if (!modal) return;\n");
        sb.append("          modal.style.display = 'none';\n");
        sb.append("          document.body.style.overflow = '';\n");
        sb.append("        },\n");
        sb.append("        loadAndOpen: function(modalId, fetchUrl) {\n");
        sb.append("          var self = this;\n");
        sb.append("          fetch(fetchUrl, { headers: { 'Accept': 'application/json' } })\n");
        sb.append("            .then(function(res) { return res.json(); })\n");
        sb.append("            .then(function(data) {\n");
        sb.append("              if (data && (data.status === 'SUCCESS' || data.username)) {\n");
        sb.append("                self.open(modalId, data);\n");
        sb.append("              } else {\n");
        sb.append("                alert(data.message || 'Unable to retrieve user details.');\n");
        sb.append("              }\n");
        sb.append("            })\n");
        sb.append("            .catch(function(err) {\n");
        sb.append("              console.error('Failed to load user details', err);\n");
        sb.append("            });\n");
        sb.append("        }\n");
        sb.append("      };\n");
        sb.append("    }\n");
        sb.append("  </script>\n");

        sb.append("</div>\n");

        return sb.toString();
    }
}
