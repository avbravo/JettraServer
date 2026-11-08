package io.jettra.flux.widgets;

import io.jettra.flux.core.Modifier;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

import java.util.ArrayList;
import java.util.List;

/**
 * JettraFluxModal - High-performance reactive modal dialog component in JettraFlux.
 * Built with Java 25 standards, supporting fluid builder syntax, backdrop blur,
 * custom header badges, dynamic polymorphic body swapping, and integrated open()/close() lifecycle.
 */
public class JettraFluxModal extends Widget {

    private String modalId;
    private String title = "";
    private String subtitle = "";
    private String icon = "fas fa-window-maximize";
    private String badgeText = null;
    private String badgeColor = "#38bdf8";
    private String maxWidth = "820px";
    private String maxHeight = "90vh";
    private boolean isOpen = false;
    private boolean closeOnEsc = true;
    private boolean closeOnClickOutside = true;
    private String borderColor = "rgba(56, 189, 248, 0.3)";
    private String glowColor = "rgba(56, 189, 248, 0.15)";

    private Widget customHeader;
    private Widget customBody;
    private Widget customFooter;
    private final List<Widget> bodyChildren = new ArrayList<>();
    private final List<Widget> footerActions = new ArrayList<>();

    private JettraFluxModal(String modalId) {
        this.modalId = modalId;
        this.id = modalId;
    }

    public static JettraFluxModal of(String modalId) {
        return new JettraFluxModal(modalId);
    }

    public static JettraFluxModal of(String modalId, String title) {
        JettraFluxModal m = new JettraFluxModal(modalId);
        m.title = title;
        return m;
    }

    public JettraFluxModal title(String title) {
        this.title = title;
        return this;
    }

    public JettraFluxModal subtitle(String subtitle) {
        this.subtitle = subtitle;
        return this;
    }

    public JettraFluxModal icon(String icon) {
        this.icon = icon;
        return this;
    }

    public JettraFluxModal badge(String text, String color) {
        this.badgeText = text;
        this.badgeColor = color;
        return this;
    }

    public JettraFluxModal maxWidth(String maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }

    public JettraFluxModal maxHeight(String maxHeight) {
        this.maxHeight = maxHeight;
        return this;
    }

    public JettraFluxModal borderColor(String borderColor) {
        this.borderColor = borderColor;
        return this;
    }

    public JettraFluxModal glowColor(String glowColor) {
        this.glowColor = glowColor;
        return this;
    }

    public JettraFluxModal open(boolean open) {
        this.isOpen = open;
        return this;
    }

    public JettraFluxModal closeOnEsc(boolean closeOnEsc) {
        this.closeOnEsc = closeOnEsc;
        return this;
    }

    public JettraFluxModal closeOnClickOutside(boolean closeOnClickOutside) {
        this.closeOnClickOutside = closeOnClickOutside;
        return this;
    }

    public JettraFluxModal header(Widget header) {
        this.customHeader = header;
        return this;
    }

    public JettraFluxModal body(Widget body) {
        this.customBody = body;
        return this;
    }

    public JettraFluxModal footer(Widget footer) {
        this.customFooter = footer;
        return this;
    }

    public JettraFluxModal addBody(Widget widget) {
        if (widget != null) bodyChildren.add(widget);
        return this;
    }

    public JettraFluxModal addFooterAction(Widget action) {
        if (action != null) footerActions.add(action);
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        String display = isOpen ? "flex" : "none";

        String outsideClickJs = closeOnClickOutside ? "if(event.target===this){JettraFluxModal.close('" + modalId + "');}" : "";

        sb.append("<!-- JettraFluxModal: ").append(modalId).append(" -->\n");
        sb.append("<div id=\"").append(modalId).append("\" ");
        sb.append("class=\"jettra-flux-modal-overlay ").append(modifier != null ? modifier.getClasses() : "").append("\" ");
        sb.append("onclick=\"").append(outsideClickJs).append("\" ");
        sb.append("style=\"display:").append(display).append("; position:fixed; top:0; left:0; width:100vw; height:100vh; ")
          .append("background:rgba(10, 15, 29, 0.78); backdrop-filter:blur(8px); -webkit-backdrop-filter:blur(8px); ")
          .append("z-index:99999; align-items:center; justify-content:center; opacity:1; transition:opacity 0.2s ease-in-out; ")
          .append(modifier != null ? modifier.getStyles() : "").append("\">\n");

        sb.append("  <div class=\"jettra-flux-modal-dialog\" role=\"dialog\" aria-modal=\"true\" style=\"width:")
          .append(maxWidth).append("; max-width:95vw; max-height:").append(maxHeight).append("; ")
          .append("background:var(--j-bg-surface, #0f172a); border:1px solid ").append(borderColor).append("; ")
          .append("box-shadow:0 25px 50px -12px rgba(0,0,0,0.6), 0 0 25px ").append(glowColor).append("; ")
          .append("border-radius:14px; display:flex; flex-direction:column; overflow:hidden; position:relative; ")
          .append("animation:jettraModalZoomIn 0.22s cubic-bezier(0.16, 1, 0.3, 1);\">\n");

        // Header
        if (customHeader != null) {
            sb.append("    <div class=\"jettra-flux-modal-header\">")
              .append(customHeader.render(theme))
              .append("</div>\n");
        } else {
            sb.append("    <div class=\"jettra-flux-modal-header\" style=\"padding:16px 20px; border-bottom:1px solid var(--j-border, rgba(255,255,255,0.08)); ")
              .append("background:var(--j-bg-subsurface, #1e293b); display:flex; justify-content:space-between; align-items:center;\">\n");
            sb.append("      <div style=\"display:flex; align-items:center; gap:10px;\">\n");
            if (icon != null && !icon.isBlank()) {
                sb.append("        <i class=\"").append(icon).append("\" style=\"color:").append(badgeColor).append("; font-size:16px;\"></i>\n");
            }
            sb.append("        <div>\n");
            sb.append("          <h3 style=\"margin:0; font-size:15px; font-weight:700; color:var(--j-text-primary, #f8fafc); display:flex; align-items:center; gap:8px;\">")
              .append(title);
            if (badgeText != null && !badgeText.isBlank()) {
                sb.append("            <span style=\"font-size:10px; font-weight:700; padding:2px 8px; border-radius:12px; background:rgba(56,189,248,0.15); color:")
                  .append(badgeColor).append("; border:1px solid ").append(borderColor).append(";\">")
                  .append(badgeText).append("</span>\n");
            }
            sb.append("          </h3>\n");
            if (subtitle != null && !subtitle.isBlank()) {
                sb.append("          <p style=\"margin:2px 0 0 0; font-size:11.5px; color:var(--j-text-muted, #94a3b8);\">").append(subtitle).append("</p>\n");
            }
            sb.append("        </div>\n");
            sb.append("      </div>\n");
            sb.append("      <button type=\"button\" class=\"jettra-flux-modal-close\" onclick=\"JettraFluxModal.close('").append(modalId).append("')\" ")
              .append("style=\"background:none; border:none; color:var(--j-text-muted, #94a3b8); font-size:18px; cursor:pointer; padding:6px; border-radius:6px; transition:color 0.15s, background 0.15s;\" ")
              .append("onmouseover=\"this.style.color='#f8fafc'; this.style.background='rgba(255,255,255,0.06)';\" ")
              .append("onmouseout=\"this.style.color='var(--j-text-muted, #94a3b8)'; this.style.background='none';\">\n")
              .append("        <i class=\"fas fa-times\"></i>\n")
              .append("      </button>\n");
            sb.append("    </div>\n");
        }

        // Body
        sb.append("    <div class=\"jettra-flux-modal-body\" style=\"padding:18px 22px; overflow-y:auto; flex:1; display:flex; flex-direction:column; gap:14px;\">\n");
        if (customBody != null) {
            sb.append(customBody.render(theme));
        }
        for (Widget w : bodyChildren) {
            sb.append(w.render(theme));
        }
        sb.append("    </div>\n");

        // Footer
        if (customFooter != null) {
            sb.append("    <div class=\"jettra-flux-modal-footer\">")
              .append(customFooter.render(theme))
              .append("</div>\n");
        } else if (!footerActions.isEmpty()) {
            sb.append("    <div class=\"jettra-flux-modal-footer\" style=\"padding:12px 20px; border-top:1px solid var(--j-border, rgba(255,255,255,0.08)); ")
              .append("background:var(--j-bg-subsurface, #1e293b); display:flex; justify-content:flex-end; align-items:center; gap:10px;\">\n");
            for (Widget btn : footerActions) {
                sb.append(btn.render(theme));
            }
            sb.append("    </div>\n");
        }

        sb.append("  </div>\n");

        // Client lifecycle script helper
        sb.append("  <script>\n")
          .append("    if (!window.JettraFluxModal) {\n")
          .append("      window.JettraFluxModal = {\n")
          .append("        open: function(id) {\n")
          .append("          var m = document.getElementById(id);\n")
          .append("          if (m) {\n")
          .append("            if (m.parentElement && m.parentElement !== document.body) {\n")
          .append("              document.body.appendChild(m);\n")
          .append("            }\n")
          .append("            m.style.display = 'flex';\n")
          .append("            document.body.style.overflow = 'hidden';\n")
          .append("            var firstInput = m.querySelector('input:not([type=hidden]), textarea, select');\n")
          .append("            if (firstInput) setTimeout(function(){ firstInput.focus(); }, 80);\n")
          .append("          }\n")
          .append("        },\n")
          .append("        close: function(id) {\n")
          .append("          var m = document.getElementById(id);\n")
          .append("          if (m) {\n")
          .append("            m.style.display = 'none';\n")
          .append("            document.body.style.overflow = '';\n")
          .append("          }\n")
          .append("        }\n")
          .append("      };\n")
          .append("      document.addEventListener('keydown', function(e) {\n")
          .append("        if (e.key === 'Escape') {\n")
          .append("          document.querySelectorAll('.jettra-flux-modal-overlay').forEach(function(modal) {\n")
          .append("            if (modal.style.display === 'flex') modal.style.display = 'none';\n")
          .append("          });\n")
          .append("          document.body.style.overflow = '';\n")
          .append("        }\n")
          .append("      });\n")
          .append("    }\n")
          .append("  </script>\n");

        sb.append("</div>\n");
        return sb.toString();
    }
}
