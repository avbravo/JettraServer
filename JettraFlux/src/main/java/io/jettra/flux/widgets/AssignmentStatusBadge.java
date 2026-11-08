package io.jettra.flux.widgets;

import io.jettra.flux.core.Modifier;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

import java.util.Objects;

/**
 * AssignmentStatusBadge - Semantic status badge for JettraFlux representing
 * identity authorization, database scoping, and preservation state.
 *
 * Distinctly conveys that unassigned/revoked states preserve the underlying user identity,
 * preventing ambiguity between access revocation and entity deletion.
 */
public class AssignmentStatusBadge extends Widget {

    public enum StatusType {
        AUTHORIZED("Authorized", "fas fa-shield-check", "rgba(16,185,129,0.15)", "#34d399", "rgba(16,185,129,0.3)"),
        REVOKED_PRESERVED("Revoked (Preserved)", "fas fa-user-shield", "rgba(245,158,11,0.15)", "#fbbf24", "rgba(245,158,11,0.3)"),
        NOT_ASSIGNED("Not Assigned", "fas fa-shield-alt", "rgba(100,116,139,0.15)", "#94a3b8", "rgba(100,116,139,0.25)"),
        INACTIVE("Inactive", "fas fa-ban", "rgba(239,68,68,0.15)", "#f87171", "rgba(239,68,68,0.3)");

        private final String defaultLabel;
        private final String icon;
        private final String background;
        private final String color;
        private final String border;

        StatusType(String defaultLabel, String icon, String background, String color, String border) {
            this.defaultLabel = defaultLabel;
            this.icon = icon;
            this.background = background;
            this.color = color;
            this.border = border;
        }

        public String defaultLabel() { return defaultLabel; }
        public String icon() { return icon; }
        public String background() { return background; }
        public String color() { return color; }
        public String border() { return border; }
    }

    private StatusType statusType = StatusType.NOT_ASSIGNED;
    private String customLabel;
    private String databaseContext;

    protected AssignmentStatusBadge(StatusType type) {
        this.statusType = Objects.requireNonNull(type, "StatusType cannot be null");
        this.id = "statusBadge_" + Integer.toHexString(System.identityHashCode(this));
    }

    public static AssignmentStatusBadge of(StatusType type) {
        return new AssignmentStatusBadge(type);
    }

    public static AssignmentStatusBadge authorized() {
        return new AssignmentStatusBadge(StatusType.AUTHORIZED);
    }

    public static AssignmentStatusBadge revokedPreserved() {
        return new AssignmentStatusBadge(StatusType.REVOKED_PRESERVED);
    }

    public static AssignmentStatusBadge notAssigned() {
        return new AssignmentStatusBadge(StatusType.NOT_ASSIGNED);
    }

    public static AssignmentStatusBadge inactive() {
        return new AssignmentStatusBadge(StatusType.INACTIVE);
    }

    public AssignmentStatusBadge customLabel(String customLabel) {
        this.customLabel = customLabel;
        return this;
    }

    public AssignmentStatusBadge databaseContext(String databaseContext) {
        this.databaseContext = databaseContext;
        return this;
    }

    public StatusType statusType() {
        return statusType;
    }

    public String label() {
        if (customLabel != null && !customLabel.isBlank()) {
            return customLabel;
        }
        if (databaseContext != null && !databaseContext.isBlank()) {
            return statusType.defaultLabel() + " (" + databaseContext + ")";
        }
        return statusType.defaultLabel();
    }

    @Override
    public AssignmentStatusBadge id(String id) {
        super.id(id);
        return this;
    }

    @Override
    public AssignmentStatusBadge modifier(Modifier modifier) {
        super.modifier(modifier);
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        String badgeId = (id != null && !id.isBlank()) ? id : "statusBadge_" + System.identityHashCode(this);
        String displayLabel = escapeHtml(label());

        sb.append("<span id=\"").append(badgeId).append("\" class=\"jettra-assignment-status-badge badge-")
          .append(statusType.name().toLowerCase());

        if (modifier != null && modifier.getClasses() != null && !modifier.getClasses().isBlank()) {
            sb.append(" ").append(modifier.getClasses().trim());
        }

        sb.append("\" style=\"display:inline-flex; align-items:center; gap:5px; font-size:11px; font-weight:600; ")
          .append("padding:3px 9px; border-radius:12px; letter-spacing:0.3px; user-select:none; ")
          .append("background:").append(statusType.background()).append("; ")
          .append("color:").append(statusType.color()).append("; ")
          .append("border:1px solid ").append(statusType.border()).append("; ");

        if (modifier != null && modifier.getStyles() != null && !modifier.getStyles().isBlank()) {
            sb.append(modifier.getStyles().trim());
        }
        sb.append("\">");

        sb.append("<i class=\"").append(statusType.icon()).append("\" style=\"font-size:11px;\"></i>");
        sb.append("<span>").append(displayLabel).append("</span>");
        sb.append("</span>\n");

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
