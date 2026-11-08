package io.jettra.flux.widgets;

import io.jettra.flux.core.Modifier;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

/**
 * IdentityPreservationNotice - JettraFlux informational banner component.
 * Clearly articulates the strict identity preservation guarantee in JettraDB:
 * user identities in system_db are permanent, historical entities.
 * Deselecting or revoking privileges only modifies database scoping,
 * while physical deletion is strictly prohibited.
 */
public class IdentityPreservationNotice extends Widget {

    private String title = "Identity Preservation Policy Active";
    private String description = "All system user identities are historical and permanent in system_db. " +
        "De-assigning a database revokes scoped permissions without deleting the user account. " +
        "Physical user deletion (DROP/DELETE) is strictly blocked across Web UI, drivers, and shell.";
    private boolean dismissible = false;

    protected IdentityPreservationNotice() {
        this.id = "identityNotice_" + Integer.toHexString(System.identityHashCode(this));
    }

    public static IdentityPreservationNotice create() {
        return new IdentityPreservationNotice();
    }

    public IdentityPreservationNotice title(String title) {
        this.title = title;
        return this;
    }

    public IdentityPreservationNotice description(String description) {
        this.description = description;
        return this;
    }

    public IdentityPreservationNotice dismissible(boolean dismissible) {
        this.dismissible = dismissible;
        return this;
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }

    @Override
    public IdentityPreservationNotice id(String id) {
        super.id(id);
        return this;
    }

    @Override
    public IdentityPreservationNotice modifier(Modifier modifier) {
        super.modifier(modifier);
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        String noticeId = (id != null && !id.isBlank()) ? id : "identityNotice_" + System.identityHashCode(this);

        sb.append("<div id=\"").append(noticeId).append("\" class=\"jettra-identity-preservation-notice");
        if (modifier != null && modifier.getClasses() != null && !modifier.getClasses().isBlank()) {
            sb.append(" ").append(modifier.getClasses().trim());
        }

        sb.append("\" style=\"display:flex; align-items:flex-start; gap:12px; padding:12px 16px; ")
          .append("background:rgba(56,189,248,0.08); border:1px solid rgba(56,189,248,0.25); ")
          .append("border-radius:10px; margin-bottom:16px; color:#cbd5e1; font-size:12.5px; line-height:1.45; ");

        if (modifier != null && modifier.getStyles() != null && !modifier.getStyles().isBlank()) {
            sb.append(modifier.getStyles().trim());
        }
        sb.append("\">\n");

        sb.append("  <i class=\"fas fa-shield-alt\" style=\"color:#38bdf8; font-size:18px; margin-top:2px; flex-shrink:0;\"></i>\n");
        sb.append("  <div style=\"flex:1;\">\n");
        sb.append("    <span style=\"display:block; font-weight:700; color:#f8fafc; font-size:13px; margin-bottom:2px;\">")
          .append(escapeHtml(title)).append("</span>\n");
        sb.append("    <span style=\"color:#94a3b8;\">").append(escapeHtml(description)).append("</span>\n");
        sb.append("  </div>\n");

        if (dismissible) {
            sb.append("  <button type=\"button\" onclick=\"document.getElementById('").append(noticeId).append("').style.display='none';\" ")
              .append("style=\"background:none; border:none; color:#64748b; font-size:14px; cursor:pointer; padding:2px;\">")
              .append("<i class=\"fas fa-times\"></i></button>\n");
        }

        sb.append("</div>\n");
        return sb.toString();
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
