package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

import java.util.Map;
import java.util.UUID;

/**
 * Native JettraFlux Widget for structured object and snapshot inspection.
 * Provides:
 * - Compact single-line preview with typography styling.
 * - Interactive expand/collapse toggle to view complete payload attributes.
 * - Multi-view tab inspection: Structured Tree, Key-Value Table, and Formatted Raw JSON.
 * - Full clipboard copy helper.
 * - Slide-over Flyout Drawer Inspector integration (FluxSnapshotDrawer).
 * Completely self-contained without external JSON library dependencies.
 */
public class FluxObjectViewer extends Widget {

    private final String rawPayload;
    private final String viewerId;
    private String title = "Snapshot Attributes";
    private boolean expandable = true;
    private boolean defaultExpanded = false;
    private int maxPreviewLength = 65;
    private boolean showCopyButton = true;
    private boolean showBadge = true;
    private boolean showDrawerButton = true;
    private String version = "v1";
    private String timestamp = "";
    private String author = "system";

    private FluxObjectViewer(String rawPayload) {
        this.rawPayload = (rawPayload != null && !rawPayload.isBlank()) ? rawPayload.trim() : "{}";
        this.viewerId = "fov_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    public static FluxObjectViewer of(String payload) {
        return new FluxObjectViewer(payload);
    }

    public static FluxObjectViewer of(Object object) {
        if (object == null) return new FluxObjectViewer("{}");
        return new FluxObjectViewer(object.toString());
    }

    public FluxObjectViewer title(String title) {
        this.title = title;
        return this;
    }

    public FluxObjectViewer expandable(boolean expandable) {
        this.expandable = expandable;
        return this;
    }

    public FluxObjectViewer defaultExpanded(boolean expanded) {
        this.defaultExpanded = expanded;
        return this;
    }

    public FluxObjectViewer maxPreviewLength(int length) {
        this.maxPreviewLength = length;
        return this;
    }

    public FluxObjectViewer showCopyButton(boolean show) {
        this.showCopyButton = show;
        return this;
    }

    public FluxObjectViewer showBadge(boolean show) {
        this.showBadge = show;
        return this;
    }

    public FluxObjectViewer showDrawerButton(boolean show) {
        this.showDrawerButton = show;
        return this;
    }

    public FluxObjectViewer version(String version) {
        this.version = version;
        return this;
    }

    public FluxObjectViewer timestamp(String timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public FluxObjectViewer author(String author) {
        this.author = author;
        return this;
    }

    public String getRawPayload() {
        return rawPayload;
    }

    public String getCompactPreview() {
        String clean = rawPayload.replaceAll("[\\r\\n\\t]+", " ");
        if (clean.length() > maxPreviewLength) {
            return clean.substring(0, maxPreviewLength) + "...";
        }
        return clean;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        String compact = getCompactPreview();
        String detailId = viewerId + "_detail";
        String iconId = viewerId + "_icon";
        String tabTreeId = viewerId + "_tab_tree";
        String tabTableId = viewerId + "_tab_table";
        String tabRawId = viewerId + "_tab_raw";

        sb.append("<div id=\"").append(viewerId).append("\" class=\"jettra-flux-object-viewer\" style=\"display:flex; flex-direction:column; gap:4px; width:100%;\">\n");

        // Row preview
        sb.append("  <div style=\"display:flex; align-items:center; gap:6px; min-width:0;\">\n");

        if (expandable) {
            String initialIcon = defaultExpanded ? "fas fa-chevron-down" : "fas fa-chevron-right";
            sb.append("    <button type=\"button\" onclick=\"")
              .append("var d=document.getElementById('").append(detailId).append("');")
              .append("var ic=document.getElementById('").append(iconId).append("');")
              .append("if(d){if(d.style.display==='none'){d.style.display='block';if(ic)ic.className='fas fa-chevron-down';}")
              .append("else{d.style.display='none';if(ic)ic.className='fas fa-chevron-right';}}\" ")
              .append("title=\"Expand full snapshot details\" ")
              .append("style=\"background:none; border:none; color:var(--j-text-muted,#94a3b8); cursor:pointer; padding:2px 4px; font-size:10px; display:inline-flex; align-items:center; justify-content:center;\">")
              .append("<i id=\"").append(iconId).append("\" class=\"").append(initialIcon).append("\"></i>")
              .append("</button>\n");
        }

        // Preview snippet
        sb.append("    <span style=\"flex:1; font-family:ui-monospace,SFMono-Regular,Menlo,Monaco,Consolas,monospace; font-size:11px; color:var(--j-text-secondary); overflow:hidden; text-overflow:ellipsis; white-space:nowrap; cursor:pointer;\" ")
          .append(expandable ? "onclick=\"var d=document.getElementById('" + detailId + "');var ic=document.getElementById('" + iconId + "');if(d){if(d.style.display==='none'){d.style.display='block';if(ic)ic.className='fas fa-chevron-down';}else{d.style.display='none';if(ic)ic.className='fas fa-chevron-right';}}\"" : "")
          .append(">")
          .append(escapeHtml(compact))
          .append("</span>\n");

        if (showCopyButton) {
            String b64 = java.util.Base64.getEncoder().encodeToString(rawPayload.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            sb.append("    <button type=\"button\" onclick=\"navigator.clipboard.writeText(atob('").append(b64).append("'));")
              .append("this.innerHTML='<i class=\\\'fas fa-check\\\' style=\\\'color:#10b981;\\\'></i>';")
              .append("var b=this;setTimeout(function(){b.innerHTML='<i class=\\\'fas fa-copy\\\'></i>';},1500);\" ")
              .append("title=\"Copy JSON Payload\" ")
              .append("style=\"background:none; border:none; color:var(--j-text-muted,#94a3b8); cursor:pointer; font-size:10px; padding:2px 4px;\">")
              .append("<i class=\"fas fa-copy\"></i>")
              .append("</button>\n");
        }

        sb.append("  </div>\n");

        // Expandable Detail Drawer / Multi-View Panel
        if (expandable) {
            String display = defaultExpanded ? "block" : "none";
            sb.append("  <div id=\"").append(detailId).append("\" style=\"display:").append(display).append("; margin-top:4px; padding:10px 12px; background:var(--j-bg-body,#0f172a); border:1px solid var(--j-border,rgba(255,255,255,0.08)); border-radius:6px; box-shadow:inset 0 1px 4px rgba(0,0,0,0.2);\">\n");

            // Header bar in detail view with View Switcher Tabs
            sb.append("    <div style=\"display:flex; justify-content:space-between; align-items:center; margin-bottom:8px; padding-bottom:6px; border-bottom:1px solid var(--j-border,rgba(255,255,255,0.08));\">\n");
            sb.append("      <div style=\"display:flex; align-items:center; gap:6px;\">\n");
            sb.append("        <span style=\"font-size:11px; font-weight:700; color:var(--j-text-primary,#f8fafc); text-transform:uppercase; letter-spacing:0.5px;\">").append(escapeHtml(title)).append("</span>\n");
            sb.append("        <span style=\"font-size:9.5px; padding:1px 6px; border-radius:3px; background:rgba(56,189,248,0.15); color:#38bdf8; font-weight:600;\">").append(rawPayload.length()).append(" bytes</span>\n");
            sb.append("      </div>\n");

            // View Mode Selector Tabs
            sb.append("      <div style=\"display:flex; align-items:center; gap:4px; font-size:10px;\">\n");
            sb.append("        <button type=\"button\" onclick=\"")
              .append("document.getElementById('").append(tabTreeId).append("').style.display='block';")
              .append("document.getElementById('").append(tabTableId).append("').style.display='none';")
              .append("document.getElementById('").append(tabRawId).append("').style.display='none';")
              .append("\" style=\"background:rgba(255,255,255,0.08); border:none; color:var(--j-text-primary,#f8fafc); padding:2px 7px; border-radius:3px; cursor:pointer; font-size:10px;\">")
              .append("<i class=\"fas fa-sitemap\" style=\"margin-right:3px;\"></i>Tree</button>\n");

            sb.append("        <button type=\"button\" onclick=\"")
              .append("document.getElementById('").append(tabTreeId).append("').style.display='none';")
              .append("document.getElementById('").append(tabTableId).append("').style.display='block';")
              .append("document.getElementById('").append(tabRawId).append("').style.display='none';")
              .append("\" style=\"background:rgba(255,255,255,0.08); border:none; color:var(--j-text-primary,#f8fafc); padding:2px 7px; border-radius:3px; cursor:pointer; font-size:10px;\">")
              .append("<i class=\"fas fa-table\" style=\"margin-right:3px;\"></i>Table</button>\n");

            sb.append("        <button type=\"button\" onclick=\"")
              .append("document.getElementById('").append(tabTreeId).append("').style.display='none';")
              .append("document.getElementById('").append(tabTableId).append("').style.display='none';")
              .append("document.getElementById('").append(tabRawId).append("').style.display='block';")
              .append("\" style=\"background:rgba(255,255,255,0.08); border:none; color:var(--j-text-primary,#f8fafc); padding:2px 7px; border-radius:3px; cursor:pointer; font-size:10px;\">")
              .append("<i class=\"fas fa-code\" style=\"margin-right:3px;\"></i>Raw</button>\n");
            sb.append("      </div>\n");
            sb.append("    </div>\n");

            // Tab 1: Interactive JSON Tree
            sb.append("    <div id=\"").append(tabTreeId).append("\" style=\"display:block; overflow-x:auto;\">\n");
            FluxJsonTree tree = FluxJsonTree.of(rawPayload, true);
            sb.append(tree.render(theme));
            sb.append("    </div>\n");

            // Tab 2: Key-Value Attributes Table
            sb.append("    <div id=\"").append(tabTableId).append("\" style=\"display:none; max-height:220px; overflow-y:auto;\">\n");
            renderKeyValueTable(sb);
            sb.append("    </div>\n");

            // Tab 3: Formatted Raw JSON
            sb.append("    <div id=\"").append(tabRawId).append("\" style=\"display:none; max-height:220px; overflow-y:auto;\">\n");
            sb.append("      <pre style=\"margin:0; padding:8px; background:rgba(0,0,0,0.3); border-radius:4px; font-family:ui-monospace,SFMono-Regular,Menlo,Monaco,Consolas,monospace; font-size:10.5px; color:#e2e8f0; white-space:pre-wrap; word-break:break-all;\">");
            sb.append(escapeHtml(formatPrettyJson(rawPayload)));
            sb.append("</pre>\n");
            sb.append("    </div>\n");

            sb.append("  </div>\n");
        }

        sb.append("</div>\n");
        return sb.toString();
    }

    private void renderKeyValueTable(StringBuilder sb) {
        sb.append("<table style=\"width:100%; border-collapse:collapse; font-size:11px; font-family:ui-monospace,SFMono-Regular,Menlo,Monaco,Consolas,monospace;\">\n");
        sb.append("  <thead><tr style=\"background:rgba(255,255,255,0.04); border-bottom:1px solid var(--j-border,rgba(255,255,255,0.1)); color:var(--j-text-muted,#94a3b8); text-align:left;\">");
        sb.append("<th style=\"padding:4px 8px; width:35%;\">Attribute</th><th style=\"padding:4px 8px;\">Value</th></tr></thead>\n");
        sb.append("  <tbody>\n");

        Object parsed = FluxJsonTree.parseJson(rawPayload);
        if (parsed instanceof Map<?, ?> map && !map.isEmpty()) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String k = String.valueOf(entry.getKey());
                Object v = entry.getValue();
                String vStr = (v != null) ? v.toString() : "null";
                String badgeColor = "#38bdf8";
                if (v instanceof Number) badgeColor = "#f59e0b";
                else if (v instanceof Boolean) badgeColor = "#10b981";
                else if (v instanceof Map || v instanceof java.util.List) badgeColor = "#a855f7";

                sb.append("    <tr style=\"border-bottom:1px solid rgba(255,255,255,0.04);\">\n");
                sb.append("      <td style=\"padding:4px 8px; font-weight:700; color:#38bdf8; word-break:break-all;\">").append(escapeHtml(k)).append("</td>\n");
                sb.append("      <td style=\"padding:4px 8px; color:#e2e8f0; word-break:break-all;\">")
                  .append("<span style=\"color:").append(badgeColor).append(";\">").append(escapeHtml(vStr)).append("</span>")
                  .append("</td>\n");
                sb.append("    </tr>\n");
            }
        } else {
            sb.append("    <tr><td colspan=\"2\" style=\"padding:8px; text-align:center; color:var(--j-text-muted,#94a3b8);\">").append(escapeHtml(rawPayload)).append("</td></tr>\n");
        }

        sb.append("  </tbody>\n");
        sb.append("</table>\n");
    }

    private String formatPrettyJson(String json) {
        if (json == null || json.isBlank()) return "{}";
        StringBuilder sb = new StringBuilder();
        int indent = 0;
        boolean inQuotes = false;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '\"' && (i == 0 || json.charAt(i - 1) != '\\')) {
                inQuotes = !inQuotes;
                sb.append(c);
            } else if (!inQuotes) {
                switch (c) {
                    case '{', '[' -> {
                        sb.append(c).append("\n");
                        indent += 2;
                        sb.append(" ".repeat(indent));
                    }
                    case '}', ']' -> {
                        sb.append("\n");
                        indent = Math.max(0, indent - 2);
                        sb.append(" ".repeat(indent));
                        sb.append(c);
                    }
                    case ',' -> {
                        sb.append(c).append("\n");
                        sb.append(" ".repeat(indent));
                    }
                    case ':' -> sb.append(": ");
                    case ' ', '\t', '\r', '\n' -> {}
                    default -> sb.append(c);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
