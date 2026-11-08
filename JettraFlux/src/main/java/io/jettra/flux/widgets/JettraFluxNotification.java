package io.jettra.flux.widgets;

import io.jettra.flux.core.Modifier;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

/**
 * JettraFluxNotification - Reactive visual notification / alert banner component in JettraFlux.
 * Provides rich error, warning, success, and info feedback with dismiss actions,
 * animations, and dynamic JavaScript client controls (show, hide, clear).
 */
public class JettraFluxNotification extends Widget {

    public enum Type {
        SUCCESS("#10b981", "rgba(16,185,129,0.12)", "rgba(16,185,129,0.35)", "fas fa-check-circle"),
        ERROR("#ef4444", "rgba(239,68,68,0.12)", "rgba(239,68,68,0.35)", "fas fa-exclamation-triangle"),
        WARNING("#f59e0b", "rgba(245,158,11,0.12)", "rgba(245,158,11,0.35)", "fas fa-exclamation-circle"),
        INFO("#38bdf8", "rgba(56,189,248,0.12)", "rgba(56,189,248,0.35)", "fas fa-info-circle");

        private final String color;
        private final String bg;
        private final String border;
        private final String defaultIcon;

        Type(String color, String bg, String border, String defaultIcon) {
            this.color = color;
            this.bg = bg;
            this.border = border;
            this.defaultIcon = defaultIcon;
        }

        public String color() { return color; }
        public String bg() { return bg; }
        public String border() { return border; }
        public String defaultIcon() { return defaultIcon; }
    }

    private String notificationId;
    private String title = null;
    private String message = "";
    private Type type = Type.INFO;
    private String customIcon = null;
    private boolean dismissible = true;
    private boolean visible = false;

    private JettraFluxNotification(String notificationId) {
        this.notificationId = notificationId;
        this.id = notificationId;
    }

    public static JettraFluxNotification of(String notificationId) {
        return new JettraFluxNotification(notificationId);
    }

    public static JettraFluxNotification of(String notificationId, String message, Type type) {
        JettraFluxNotification n = new JettraFluxNotification(notificationId);
        n.message = message;
        n.type = type != null ? type : Type.INFO;
        return n;
    }

    public JettraFluxNotification title(String title) {
        this.title = title;
        return this;
    }

    public JettraFluxNotification message(String message) {
        this.message = message;
        return this;
    }

    public JettraFluxNotification type(Type type) {
        if (type != null) this.type = type;
        return this;
    }

    public JettraFluxNotification icon(String icon) {
        this.customIcon = icon;
        return this;
    }

    public JettraFluxNotification dismissible(boolean dismissible) {
        this.dismissible = dismissible;
        return this;
    }

    public JettraFluxNotification visible(boolean visible) {
        this.visible = visible;
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        String display = visible ? "flex" : "none";
        String iconClass = customIcon != null ? customIcon : type.defaultIcon();

        sb.append("<div id=\"").append(notificationId).append("\" ");
        sb.append("class=\"jettra-flux-notification ").append(modifier != null ? modifier.getClasses() : "").append("\" ");
        sb.append("role=\"alert\" ");
        sb.append("style=\"display:").append(display).append("; align-items:flex-start; gap:10px; padding:10px 14px; ")
          .append("border-radius:8px; background:").append(type.bg()).append("; border:1px solid ").append(type.border()).append("; ")
          .append("color:var(--j-text-primary, #f8fafc); font-size:12px; transition:all 0.2s ease; ")
          .append(modifier != null ? modifier.getStyles() : "").append("\">\n");

        sb.append("  <i class=\"").append(iconClass).append("\" id=\"").append(notificationId).append("_icon\" ")
          .append("style=\"color:").append(type.color()).append("; font-size:15px; margin-top:2px; flex-shrink:0;\"></i>\n");

        sb.append("  <div style=\"flex:1; line-height:1.4;\">\n");
        sb.append("    <div id=\"").append(notificationId).append("_title\" style=\"font-weight:700; color:")
          .append(type.color()).append("; ").append(title == null || title.isBlank() ? "display:none;" : "")
          .append("\">").append(title != null ? title : "").append("</div>\n");
        sb.append("    <div id=\"").append(notificationId).append("_msg\" style=\"color:var(--j-text-primary, #f8fafc); font-size:12px;\">")
          .append(message != null ? message : "").append("</div>\n");
        sb.append("  </div>\n");

        if (dismissible) {
            sb.append("  <button type=\"button\" onclick=\"JettraFluxNotification.hide('").append(notificationId).append("')\" ")
              .append("style=\"background:none; border:none; color:var(--j-text-muted, #94a3b8); font-size:14px; cursor:pointer; padding:2px 4px; flex-shrink:0;\" ")
              .append("title=\"Cerrar\"><i class=\"fas fa-times\"></i></button>\n");
        }

        sb.append("</div>\n");

        sb.append("<script>\n")
          .append("if (!window.JettraFluxNotification) {\n")
          .append("  window.JettraFluxNotification = {\n")
          .append("    show: function(id, title, message, type) {\n")
          .append("      var el = document.getElementById(id);\n")
          .append("      if (!el) return;\n")
          .append("      var colorMap = {\n")
          .append("        SUCCESS: { color:'#10b981', bg:'rgba(16,185,129,0.12)', border:'rgba(16,185,129,0.35)', icon:'fas fa-check-circle' },\n")
          .append("        ERROR: { color:'#ef4444', bg:'rgba(239,68,68,0.12)', border:'rgba(239,68,68,0.35)', icon:'fas fa-exclamation-triangle' },\n")
          .append("        WARNING: { color:'#f59e0b', bg:'rgba(245,158,11,0.12)', border:'rgba(245,158,11,0.35)', icon:'fas fa-exclamation-circle' },\n")
          .append("        INFO: { color:'#38bdf8', bg:'rgba(56,189,248,0.12)', border:'rgba(56,189,248,0.35)', icon:'fas fa-info-circle' }\n")
          .append("      };\n")
          .append("      var t = colorMap[(type || 'INFO').toUpperCase()] || colorMap.INFO;\n")
          .append("      el.style.background = t.bg;\n")
          .append("      el.style.borderColor = t.border;\n")
          .append("      var iconEl = document.getElementById(id + '_icon');\n")
          .append("      if (iconEl) { iconEl.className = t.icon; iconEl.style.color = t.color; }\n")
          .append("      var titleEl = document.getElementById(id + '_title');\n")
          .append("      if (titleEl) {\n")
          .append("        titleEl.textContent = title || '';\n")
          .append("        titleEl.style.color = t.color;\n")
          .append("        titleEl.style.display = title ? 'block' : 'none';\n")
          .append("      }\n")
          .append("      var msgEl = document.getElementById(id + '_msg');\n")
          .append("      if (msgEl) msgEl.textContent = message || '';\n")
          .append("      el.style.display = 'flex';\n")
          .append("    },\n")
          .append("    hide: function(id) {\n")
          .append("      var el = document.getElementById(id);\n")
          .append("      if (el) el.style.display = 'none';\n")
          .append("    }\n")
          .append("  };\n")
          .append("}\n")
          .append("</script>\n");

        return sb.toString();
    }
}
