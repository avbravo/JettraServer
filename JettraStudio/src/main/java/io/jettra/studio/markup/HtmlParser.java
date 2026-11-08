package io.jettra.studio.markup;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fast, tolerant HTML5 parser for JettraStudio.
 * Scans HTML markup to identify component hooks marked with jettrat:id="..." or jettra:id="..."
 * and extracts tag attributes, bounds, and placeholder body contents.
 */
public class HtmlParser {

    private static final Pattern COMPONENT_TAG_PATTERN = Pattern.compile(
        "<([a-zA-Z0-9\\-_:]+)\\s+([^>]*?(?:jettrat:id|jettra:id|jettras:id)\\s*=\\s*[\"']([^\"']+)[\"'][^>]*)>",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    private static final Pattern ATTR_PATTERN = Pattern.compile(
        "([a-zA-Z0-9\\-_:]+)(?:\\s*=\\s*[\"']([^\"']*)[\"'])?",
        Pattern.DOTALL
    );

    /**
     * Parses the given HTML content and discovers all top-level component tags.
     */
    public List<MarkupTag> parseTags(String html) {
        List<MarkupTag> tags = new ArrayList<>();
        if (html == null || html.isEmpty()) return tags;

        Matcher matcher = COMPONENT_TAG_PATTERN.matcher(html);
        int searchOffset = 0;

        while (matcher.find(searchOffset)) {
            String tagName = matcher.group(1).toLowerCase();
            String rawAttrs = matcher.group(2);
            String componentId = matcher.group(3);

            MarkupTag tag = new MarkupTag(tagName, componentId);
            tag.setOpenTagStart(matcher.start());
            tag.setOpenTagEnd(matcher.end());

            // Parse attributes
            parseAttributes(rawAttrs, tag);

            // Check if self-closing (e.g. <input ... /> or HTML void elements)
            boolean isVoidElement = isVoidHtmlElement(tagName);
            boolean isSelfClosingAttr = rawAttrs.trim().endsWith("/");

            if (isVoidElement || isSelfClosingAttr) {
                tag.setSelfClosing(true);
                tag.setCloseTagStart(tag.getOpenTagEnd());
                tag.setCloseTagEnd(tag.getOpenTagEnd());
                tag.setBodyContent("");
                tags.add(tag);
                searchOffset = tag.getOpenTagEnd();
            } else {
                // Find matching closing tag with depth tracking
                int closingTagIndex = findClosingTag(html, tagName, tag.getOpenTagEnd());
                if (closingTagIndex != -1) {
                    tag.setCloseTagStart(closingTagIndex);
                    tag.setCloseTagEnd(closingTagIndex + tagName.length() + 3); // </tagName>
                    tag.setBodyContent(html.substring(tag.getOpenTagEnd(), closingTagIndex));
                } else {
                    // Fallback if closing tag is missing
                    tag.setCloseTagStart(tag.getOpenTagEnd());
                    tag.setCloseTagEnd(tag.getOpenTagEnd());
                    tag.setBodyContent("");
                }
                tags.add(tag);
                // Advance past this entire component
                searchOffset = tag.getCloseTagEnd();
            }
        }

        return tags;
    }

    private void parseAttributes(String rawAttrs, MarkupTag tag) {
        if (rawAttrs == null || rawAttrs.isBlank()) return;
        Matcher m = ATTR_PATTERN.matcher(rawAttrs);
        while (m.find()) {
            String attrName = m.group(1);
            String attrValue = m.group(2);
            if (attrName != null && !attrName.isBlank() && !attrName.equals("/")) {
                tag.setAttribute(attrName, attrValue != null ? attrValue : "");
            }
        }
    }

    private boolean isVoidHtmlElement(String tag) {
        return switch (tag.toLowerCase()) {
            case "input", "img", "br", "hr", "meta", "link", "area", "base", "col", "embed", "param", "source", "track", "wbr" -> true;
            default -> false;
        };
    }

    /**
     * Finds the matching closing tag </tagName> for an opened <tagName>,
     * handling nested elements of the same tag name.
     */
    private int findClosingTag(String html, String tagName, int fromIndex) {
        int depth = 1;
        int index = fromIndex;
        int len = html.length();

        String openPrefix = "<" + tagName;
        String closeString = "</" + tagName + ">";

        while (index < len) {
            int nextOpen = indexOfIgnoreCase(html, openPrefix, index);
            int nextClose = indexOfIgnoreCase(html, closeString, index);

            if (nextClose == -1) {
                // No closing tag found
                return -1;
            }

            if (nextOpen != -1 && nextOpen < nextClose) {
                // Ensure it is an actual tag boundary, not <div-something
                char charAfter = (nextOpen + openPrefix.length() < len) ? html.charAt(nextOpen + openPrefix.length()) : ' ';
                if (Character.isWhitespace(charAfter) || charAfter == '>' || charAfter == '/') {
                    // Verify if it is not self closing
                    int openEnd = html.indexOf('>', nextOpen);
                    if (openEnd != -1 && html.substring(nextOpen, openEnd).trim().endsWith("/")) {
                        // self-closing, depth doesn't change
                        index = openEnd + 1;
                        continue;
                    }
                    depth++;
                }
                index = nextOpen + openPrefix.length();
            } else {
                depth--;
                if (depth == 0) {
                    return nextClose;
                }
                index = nextClose + closeString.length();
            }
        }

        return -1;
    }

    private int indexOfIgnoreCase(String src, String target, int fromIndex) {
        if (fromIndex >= src.length()) return -1;
        String s = src.toLowerCase();
        String t = target.toLowerCase();
        return s.indexOf(t, fromIndex);
    }
}
