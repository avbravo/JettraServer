package io.jettra.flux.widgets;

import io.jettra.flux.core.Modifier;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

/**
 * JettraFluxJsonEditor - Structured reactive JSON editor component in JettraFlux.
 * Includes interactive JSON syntax validation, real-time error detection,
 * formatting (prettify), minification, copy-to-clipboard, and status badge.
 */
public class JettraFluxJsonEditor extends Widget {

    private String editorId;
    private String name;
    private String initialValue = "{\n  \n}";
    private String height = "220px";
    private boolean readOnly = false;
    private String label = "JSON Payload";
    private boolean showToolbar = true;
    private String placeholder = "{\n  \"key\": \"value\"\n}";

    private JettraFluxJsonEditor(String editorId) {
        this.editorId = editorId;
        this.id = editorId;
        this.name = editorId;
    }

    public static JettraFluxJsonEditor of(String editorId) {
        return new JettraFluxJsonEditor(editorId);
    }

    public static JettraFluxJsonEditor of(String editorId, String label, String initialValue) {
        JettraFluxJsonEditor ed = new JettraFluxJsonEditor(editorId);
        ed.label = label;
        if (initialValue != null) ed.initialValue = initialValue;
        return ed;
    }

    public JettraFluxJsonEditor name(String name) {
        this.name = name;
        return this;
    }

    public JettraFluxJsonEditor label(String label) {
        this.label = label;
        return this;
    }

    public JettraFluxJsonEditor value(String value) {
        if (value != null) this.initialValue = value;
        return this;
    }

    public JettraFluxJsonEditor height(String height) {
        this.height = height;
        return this;
    }

    public JettraFluxJsonEditor readOnly(boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }

    public JettraFluxJsonEditor showToolbar(boolean showToolbar) {
        this.showToolbar = showToolbar;
        return this;
    }

    public JettraFluxJsonEditor placeholder(String placeholder) {
        this.placeholder = placeholder;
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        String textareaId = editorId + "_input";
        String statusId = editorId + "_status";

        sb.append("<div id=\"").append(editorId).append("_container\" class=\"jettra-flux-json-editor-container ")
          .append(modifier != null ? modifier.getClasses() : "").append("\" ")
          .append("style=\"display:flex; flex-direction:column; border:1px solid var(--j-border, rgba(255,255,255,0.12)); ")
          .append("border-radius:8px; overflow:hidden; background:var(--j-bg-body, #0a0f1d); ")
          .append(modifier != null ? modifier.getStyles() : "").append("\">\n");

        if (showToolbar) {
            sb.append("  <div class=\"json-editor-toolbar\" style=\"display:flex; justify-content:space-between; align-items:center; ")
              .append("padding:6px 12px; background:var(--j-bg-subsurface, #1e293b); border-bottom:1px solid var(--j-border, rgba(255,255,255,0.08));\">\n");
            sb.append("    <div style=\"display:flex; align-items:center; gap:8px;\">\n");
            sb.append("      <i class=\"fas fa-code\" style=\"color:#38bdf8; font-size:11px;\"></i>\n");
            sb.append("      <span style=\"font-size:11px; font-weight:700; color:var(--j-text-secondary, #94a3b8); text-transform:uppercase; letter-spacing:0.5px;\">")
              .append(label).append("</span>\n");
            sb.append("      <span id=\"").append(statusId).append("\" style=\"font-size:9.5px; font-weight:700; padding:1px 6px; border-radius:10px; background:rgba(16,185,129,0.15); color:#10b981; border:1px solid rgba(16,185,129,0.3);\">VALID JSON</span>\n");
            sb.append("    </div>\n");

            sb.append("    <div style=\"display:flex; align-items:center; gap:4px;\">\n");
            sb.append("      <button type=\"button\" onclick=\"JettraFluxJsonEditor.format('").append(textareaId).append("','").append(statusId).append("')\" ")
              .append("style=\"background:none; border:none; color:var(--j-text-muted, #94a3b8); font-size:10.5px; cursor:pointer; padding:3px 6px; border-radius:4px;\" ")
              .append("title=\"Format / Prettify JSON\"><i class=\"fas fa-magic\"></i> Prettify</button>\n");
            sb.append("      <button type=\"button\" onclick=\"JettraFluxJsonEditor.minify('").append(textareaId).append("','").append(statusId).append("')\" ")
              .append("style=\"background:none; border:none; color:var(--j-text-muted, #94a3b8); font-size:10.5px; cursor:pointer; padding:3px 6px; border-radius:4px;\" ")
              .append("title=\"Compact / Minify JSON\"><i class=\"fas fa-compress-alt\"></i> Minify</button>\n");
            sb.append("      <button type=\"button\" onclick=\"JettraFluxJsonEditor.copy('").append(textareaId).append("')\" ")
              .append("style=\"background:none; border:none; color:var(--j-text-muted, #94a3b8); font-size:10.5px; cursor:pointer; padding:3px 6px; border-radius:4px;\" ")
              .append("title=\"Copy JSON to Clipboard\"><i class=\"fas fa-copy\"></i></button>\n");
            sb.append("    </div>\n");
            sb.append("  </div>\n");
        }

        sb.append("  <textarea id=\"").append(textareaId).append("\" name=\"").append(name).append("\" ");
        if (readOnly) sb.append("readonly=\"readonly\" ");
        sb.append("placeholder=\"").append(placeholder.replace("\"", "&quot;")).append("\" ");
        sb.append("oninput=\"JettraFluxJsonEditor.validate('").append(textareaId).append("','").append(statusId).append("')\" ");
        sb.append("style=\"width:100%; height:").append(height).append("; padding:10px 12px; font-family:'Fira Code', Consolas, Monaco, monospace; ")
          .append("font-size:12px; line-height:1.5; color:#38bdf8; background:transparent; border:none; outline:none; resize:vertical; ")
          .append("box-sizing:border-box;\">").append(initialValue.replace("<", "&lt;").replace(">", "&gt;")).append("</textarea>\n");

        sb.append("</div>\n");

        sb.append("<script>\n")
          .append("if (!window.JettraFluxJsonEditor) {\n")
          .append("  window.JettraFluxJsonEditor = {\n")
          .append("    validate: function(textareaId, statusId) {\n")
          .append("      var ta = document.getElementById(textareaId);\n")
          .append("      var st = document.getElementById(statusId);\n")
          .append("      if (!ta || !st) return true;\n")
          .append("      var val = ta.value.trim();\n")
          .append("      if (!val) { st.textContent = 'EMPTY'; st.style.color = '#94a3b8'; st.style.borderColor = 'rgba(148,163,184,0.3)'; return true; }\n")
          .append("      try {\n")
          .append("        JSON.parse(val);\n")
          .append("        st.textContent = 'VALID JSON';\n")
          .append("        st.style.color = '#10b981';\n")
          .append("        st.style.background = 'rgba(16,185,129,0.15)';\n")
          .append("        st.style.borderColor = 'rgba(16,185,129,0.3)';\n")
          .append("        return true;\n")
          .append("      } catch (err) {\n")
          .append("        st.textContent = 'SYNTAX ERROR';\n")
          .append("        st.style.color = '#ef4444';\n")
          .append("        st.style.background = 'rgba(239,68,68,0.15)';\n")
          .append("        st.style.borderColor = 'rgba(239,68,68,0.3)';\n")
          .append("        return false;\n")
          .append("      }\n")
          .append("    },\n")
          .append("    format: function(textareaId, statusId) {\n")
          .append("      var ta = document.getElementById(textareaId);\n")
          .append("      if (!ta) return;\n")
          .append("      try {\n")
          .append("        var parsed = JSON.parse(ta.value);\n")
          .append("        ta.value = JSON.stringify(parsed, null, 2);\n")
          .append("        this.validate(textareaId, statusId);\n")
          .append("      } catch(e){ alert('Invalid JSON: ' + e.message); }\n")
          .append("    },\n")
          .append("    minify: function(textareaId, statusId) {\n")
          .append("      var ta = document.getElementById(textareaId);\n")
          .append("      if (!ta) return;\n")
          .append("      try {\n")
          .append("        var parsed = JSON.parse(ta.value);\n")
          .append("        ta.value = JSON.stringify(parsed);\n")
          .append("        this.validate(textareaId, statusId);\n")
          .append("      } catch(e){ alert('Invalid JSON: ' + e.message); }\n")
          .append("    },\n")
          .append("    copy: function(textareaId) {\n")
          .append("      var ta = document.getElementById(textareaId);\n")
          .append("      if (ta) {\n")
          .append("        navigator.clipboard.writeText(ta.value);\n")
          .append("      }\n")
          .append("    }\n")
          .append("  };\n")
          .append("}\n")
          .append("</script>\n");

        return sb.toString();
    }
}
