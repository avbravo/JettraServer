package io.jettra.studio.markup;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents an HTML element identified by jettrat:id="..." or jettra:id="..."
 * along with its opening tag, attributes, placeholder body content, and closing tag.
 */
public class MarkupTag {

    private final String tagName;
    private final String id;
    private final Map<String, String> attributes = new LinkedHashMap<>();
    private String bodyContent = "";
    private boolean selfClosing = false;
    private int openTagStart;
    private int openTagEnd;
    private int closeTagStart;
    private int closeTagEnd;

    public MarkupTag(String tagName, String id) {
        this.tagName = Objects.requireNonNull(tagName, "tagName cannot be null").toLowerCase();
        this.id = Objects.requireNonNull(id, "id cannot be null");
    }

    public String getTagName() {
        return tagName;
    }

    public String getId() {
        return id;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public String getAttribute(String name) {
        return attributes.get(name);
    }

    public MarkupTag setAttribute(String name, String value) {
        if (value == null) {
            attributes.remove(name);
        } else {
            attributes.put(name, value);
        }
        return this;
    }

    public String getBodyContent() {
        return bodyContent;
    }

    public void setBodyContent(String bodyContent) {
        this.bodyContent = (bodyContent != null) ? bodyContent : "";
    }

    public boolean isSelfClosing() {
        return selfClosing;
    }

    public void setSelfClosing(boolean selfClosing) {
        this.selfClosing = selfClosing;
    }

    public int getOpenTagStart() {
        return openTagStart;
    }

    public void setOpenTagStart(int openTagStart) {
        this.openTagStart = openTagStart;
    }

    public int getOpenTagEnd() {
        return openTagEnd;
    }

    public void setOpenTagEnd(int openTagEnd) {
        this.openTagEnd = openTagEnd;
    }

    public int getCloseTagStart() {
        return closeTagStart;
    }

    public void setCloseTagStart(int closeTagStart) {
        this.closeTagStart = closeTagStart;
    }

    public int getCloseTagEnd() {
        return closeTagEnd;
    }

    public void setCloseTagEnd(int closeTagEnd) {
        this.closeTagEnd = closeTagEnd;
    }

    /**
     * Renders the opening HTML tag with all preserved and injected attributes,
     * stripping out the internal jettrat:id attribute from client output.
     */
    public String renderOpeningTag() {
        StringBuilder sb = new StringBuilder("<").append(tagName);
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            String k = entry.getKey();
            if ("jettrat:id".equalsIgnoreCase(k) || "jettra:id".equalsIgnoreCase(k) || "jettras:id".equalsIgnoreCase(k)) {
                continue; // Do not emit component ID attribute to public DOM
            }
            sb.append(" ").append(k).append("=\"").append(escapeAttribute(entry.getValue())).append("\"");
        }
        if (selfClosing) {
            sb.append(" />");
        } else {
            sb.append(">");
        }
        return sb.toString();
    }

    /**
     * Renders the closing HTML tag unless self-closing.
     */
    public String renderClosingTag() {
        return selfClosing ? "" : "</" + tagName + ">";
    }

    private String escapeAttribute(String val) {
        if (val == null) return "";
        return val.replace("&", "&amp;")
                  .replace("\"", "&quot;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;");
    }

    @Override
    public String toString() {
        return "MarkupTag{" + tagName + " id='" + id + "'}";
    }
}
