package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

/**
 * Native JettraFlux Tree Widget for hierarchical JSON and structured object inspection.
 * Formats JSON nodes with typed badges, interactive collapsible branches, and clean typography.
 * Completely self-contained without external JSON library dependencies.
 */
public class FluxJsonTree extends Widget {

    private final String rawJson;
    private final Object parsedRoot;
    private final String treeId;
    private boolean defaultExpanded = false;
    private int maxPreviewLength = 80;

    private FluxJsonTree(String json, boolean defaultExpanded) {
        this.rawJson = json != null ? json.trim() : "{}";
        this.defaultExpanded = defaultExpanded;
        this.treeId = "fjtree_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        this.parsedRoot = parseJson(this.rawJson);
    }

    public static FluxJsonTree of(String json) {
        return new FluxJsonTree(json, false);
    }

    public static FluxJsonTree of(String json, boolean defaultExpanded) {
        return new FluxJsonTree(json, defaultExpanded);
    }

    public static FluxJsonTree of(java.util.Map<String, Object> map) {
        return new FluxJsonTree(map != null ? map.toString() : "{}", false);
    }

    public FluxJsonTree defaultExpanded(boolean expanded) {
        this.defaultExpanded = expanded;
        return this;
    }

    public FluxJsonTree maxPreviewLength(int length) {
        this.maxPreviewLength = length;
        return this;
    }

    public String getRawJson() {
        return rawJson;
    }

    public Object getParsedRoot() {
        return parsedRoot;
    }

    /**
     * Lightweight recursive JSON parser without external dependencies.
     */
    public static Object parseJson(String json) {
        if (json == null || json.isBlank()) return Collections.emptyMap();
        String s = json.trim();
        try {
            int[] pos = new int[]{0};
            return parseValue(s, pos);
        } catch (Exception e) {
            java.util.Map<String, Object> fallback = new LinkedHashMap<>();
            fallback.put("raw", json);
            return fallback;
        }
    }

    private static Object parseValue(String s, int[] pos) {
        skipWhitespace(s, pos);
        if (pos[0] >= s.length()) return null;
        char c = s.charAt(pos[0]);
        if (c == '{') return parseObject(s, pos);
        if (c == '[') return parseArray(s, pos);
        if (c == '"' || c == '\'') return parseString(s, pos);
        if (c == 't' || c == 'T' || c == 'f' || c == 'F') return parseBoolean(s, pos);
        if (c == 'n' || c == 'N') return parseNull(s, pos);
        if (c == '-' || Character.isDigit(c)) return parseNumber(s, pos);

        // Fallback token
        StringBuilder token = new StringBuilder();
        while (pos[0] < s.length() && s.charAt(pos[0]) != ',' && s.charAt(pos[0]) != '}' && s.charAt(pos[0]) != ']') {
            token.append(s.charAt(pos[0]++));
        }
        return token.toString().trim();
    }

    private static java.util.Map<String, Object> parseObject(String s, int[] pos) {
        java.util.Map<String, Object> map = new LinkedHashMap<>();
        pos[0]++; // skip '{'
        skipWhitespace(s, pos);
        while (pos[0] < s.length() && s.charAt(pos[0]) != '}') {
            skipWhitespace(s, pos);
            if (pos[0] >= s.length() || s.charAt(pos[0]) == '}') break;
            String key = parseString(s, pos);
            skipWhitespace(s, pos);
            if (pos[0] < s.length() && s.charAt(pos[0]) == ':') {
                pos[0]++; // skip ':'
            }
            Object val = parseValue(s, pos);
            map.put(key, val);
            skipWhitespace(s, pos);
            if (pos[0] < s.length() && s.charAt(pos[0]) == ',') {
                pos[0]++; // skip ','
            }
            skipWhitespace(s, pos);
        }
        if (pos[0] < s.length() && s.charAt(pos[0]) == '}') {
            pos[0]++; // skip '}'
        }
        return map;
    }

    private static List<Object> parseArray(String s, int[] pos) {
        List<Object> list = new ArrayList<>();
        pos[0]++; // skip '['
        skipWhitespace(s, pos);
        while (pos[0] < s.length() && s.charAt(pos[0]) != ']') {
            skipWhitespace(s, pos);
            if (pos[0] >= s.length() || s.charAt(pos[0]) == ']') break;
            Object val = parseValue(s, pos);
            list.add(val);
            skipWhitespace(s, pos);
            if (pos[0] < s.length() && s.charAt(pos[0]) == ',') {
                pos[0]++; // skip ','
            }
            skipWhitespace(s, pos);
        }
        if (pos[0] < s.length() && s.charAt(pos[0]) == ']') {
            pos[0]++; // skip ']'
        }
        return list;
    }

    private static String parseString(String s, int[] pos) {
        skipWhitespace(s, pos);
        if (pos[0] >= s.length()) return "";
        char quote = s.charAt(pos[0]);
        if (quote != '"' && quote != '\'') {
            StringBuilder sb = new StringBuilder();
            while (pos[0] < s.length() && s.charAt(pos[0]) != ':' && s.charAt(pos[0]) != ',' && s.charAt(pos[0]) != '}' && s.charAt(pos[0]) != ']') {
                sb.append(s.charAt(pos[0]++));
            }
            return sb.toString().trim();
        }
        pos[0]++; // skip open quote
        StringBuilder sb = new StringBuilder();
        boolean escape = false;
        while (pos[0] < s.length()) {
            char c = s.charAt(pos[0]++);
            if (escape) {
                if (c == 'n') sb.append('\n');
                else if (c == 't') sb.append('\t');
                else if (c == 'r') sb.append('\r');
                else sb.append(c);
                escape = false;
            } else if (c == '\\') {
                escape = true;
            } else if (c == quote) {
                break;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static Boolean parseBoolean(String s, int[] pos) {
        skipWhitespace(s, pos);
        if (s.startsWith("true", pos[0]) || s.startsWith("TRUE", pos[0])) {
            pos[0] += 4;
            return true;
        }
        if (s.startsWith("false", pos[0]) || s.startsWith("FALSE", pos[0])) {
            pos[0] += 5;
            return false;
        }
        return false;
    }

    private static Object parseNull(String s, int[] pos) {
        skipWhitespace(s, pos);
        if (s.startsWith("null", pos[0]) || s.startsWith("NULL", pos[0])) {
            pos[0] += 4;
        }
        return null;
    }

    private static Number parseNumber(String s, int[] pos) {
        skipWhitespace(s, pos);
        int start = pos[0];
        while (pos[0] < s.length()) {
            char c = s.charAt(pos[0]);
            if (Character.isDigit(c) || c == '.' || c == '-' || c == '+' || c == 'e' || c == 'E') {
                pos[0]++;
            } else {
                break;
            }
        }
        String numStr = s.substring(start, pos[0]);
        if (numStr.contains(".")) {
            try { return Double.parseDouble(numStr); } catch (Exception ignored) {}
        }
        try { return Long.parseLong(numStr); } catch (Exception ignored) {}
        return 0;
    }

    private static void skipWhitespace(String s, int[] pos) {
        while (pos[0] < s.length() && Character.isWhitespace(s.charAt(pos[0]))) {
            pos[0]++;
        }
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div id=\"").append(treeId).append("\" class=\"jettra-flux-json-tree\" style=\"font-family:ui-monospace,SFMono-Regular,Menlo,Monaco,Consolas,monospace; font-size:11.5px; line-height:1.5;\">\n");
        renderNode(sb, null, parsedRoot, 0, treeId + "_root", defaultExpanded);
        sb.append("</div>\n");
        return sb.toString();
    }

    private void renderNode(StringBuilder sb, String key, Object element, int depth, String nodeId, boolean isExpanded) {
        int paddingLeft = depth * 14;

        if (element == null) {
            renderLeaf(sb, key, "null", "#94a3b8", paddingLeft);
            return;
        }

        if (element instanceof java.util.Map<?, ?> map) {
            int size = map.size();
            String label = (key != null ? "<span style=\"color:#38bdf8; font-weight:600;\">\"" + escapeHtml(key) + "\"</span>: " : "")
                + "<span style=\"color:var(--j-text-secondary,#94a3b8); font-size:10px;\">{ " + size + " field" + (size == 1 ? "" : "s") + " }</span>";

            String toggleIconId = nodeId + "_icon";
            String childrenId = nodeId + "_children";
            String displayStyle = isExpanded ? "block" : "none";
            String iconClass = isExpanded ? "fas fa-chevron-down" : "fas fa-chevron-right";

            sb.append("<div style=\"margin-left:").append(paddingLeft).append("px; margin-top:2px; margin-bottom:2px;\">\n");
            sb.append("  <div style=\"cursor:pointer; display:inline-flex; align-items:center; gap:5px; user-select:none;\" onclick=\"")
              .append("var c=document.getElementById('").append(childrenId).append("');")
              .append("var ic=document.getElementById('").append(toggleIconId).append("');")
              .append("if(c){if(c.style.display==='none'){c.style.display='block';if(ic)ic.className='fas fa-chevron-down';}")
              .append("else{c.style.display='none';if(ic)ic.className='fas fa-chevron-right';}}\">\n");
            sb.append("    <i id=\"").append(toggleIconId).append("\" class=\"").append(iconClass).append("\" style=\"font-size:9px; color:var(--j-text-muted,#64748b); width:12px; text-align:center;\"></i>\n");
            sb.append("    ").append(label).append("\n");
            sb.append("  </div>\n");

            sb.append("  <div id=\"").append(childrenId).append("\" style=\"display:").append(displayStyle).append("; border-left:1px dashed var(--j-border,rgba(255,255,255,0.12)); margin-left:5px; padding-left:6px;\">\n");
            int idx = 0;
            for (java.util.Map.Entry<?, ?> entry : map.entrySet()) {
                String k = entry.getKey() != null ? entry.getKey().toString() : "";
                renderNode(sb, k, entry.getValue(), 0, nodeId + "_" + idx++, defaultExpanded);
            }
            sb.append("  </div>\n");
            sb.append("</div>\n");

        } else if (element instanceof List<?> list) {
            int size = list.size();
            String label = (key != null ? "<span style=\"color:#38bdf8; font-weight:600;\">\"" + escapeHtml(key) + "\"</span>: " : "")
                + "<span style=\"color:var(--j-text-secondary,#94a3b8); font-size:10px;\">[ " + size + " item" + (size == 1 ? "" : "s") + " ]</span>";

            String toggleIconId = nodeId + "_icon";
            String childrenId = nodeId + "_children";
            String displayStyle = isExpanded ? "block" : "none";
            String iconClass = isExpanded ? "fas fa-chevron-down" : "fas fa-chevron-right";

            sb.append("<div style=\"margin-left:").append(paddingLeft).append("px; margin-top:2px; margin-bottom:2px;\">\n");
            sb.append("  <div style=\"cursor:pointer; display:inline-flex; align-items:center; gap:5px; user-select:none;\" onclick=\"")
              .append("var c=document.getElementById('").append(childrenId).append("');")
              .append("var ic=document.getElementById('").append(toggleIconId).append("');")
              .append("if(c){if(c.style.display==='none'){c.style.display='block';if(ic)ic.className='fas fa-chevron-down';}")
              .append("else{c.style.display='none';if(ic)ic.className='fas fa-chevron-right';}}\">\n");
            sb.append("    <i id=\"").append(toggleIconId).append("\" class=\"").append(iconClass).append("\" style=\"font-size:9px; color:var(--j-text-muted,#64748b); width:12px; text-align:center;\"></i>\n");
            sb.append("    ").append(label).append("\n");
            sb.append("  </div>\n");

            sb.append("  <div id=\"").append(childrenId).append("\" style=\"display:").append(displayStyle).append("; border-left:1px dashed var(--j-border,rgba(255,255,255,0.12)); margin-left:5px; padding-left:6px;\">\n");
            for (int i = 0; i < list.size(); i++) {
                renderNode(sb, String.valueOf(i), list.get(i), 0, nodeId + "_" + i, defaultExpanded);
            }
            sb.append("  </div>\n");
            sb.append("</div>\n");

        } else if (element instanceof Boolean b) {
            renderLeaf(sb, key, b.toString(), "#a855f7", paddingLeft);
        } else if (element instanceof Number n) {
            renderLeaf(sb, key, n.toString(), "#fbbf24", paddingLeft);
        } else {
            renderLeaf(sb, key, "\"" + escapeHtml(element.toString()) + "\"", "#4ade80", paddingLeft);
        }
    }

    private void renderLeaf(StringBuilder sb, String key, String value, String color, int paddingLeft) {
        sb.append("<div style=\"margin-left:").append(paddingLeft + 16).append("px; margin-top:1px; margin-bottom:1px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;\">\n");
        if (key != null) {
            sb.append("<span style=\"color:#38bdf8; font-weight:600;\">\"").append(escapeHtml(key)).append("\"</span>: ");
        }
        sb.append("<span style=\"color:").append(color).append(";\">").append(value).append("</span>\n");
        sb.append("</div>\n");
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
