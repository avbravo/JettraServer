package io.jettra.aura.template;

import io.jettra.aura.core.AuraTag;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Analizador sintáctico HTML de alto rendimiento para JettraAura.
 * Detecta etiquetas con identificadores (aura:id, j:id, wicket:id, data-aura-id) y construye
 * un árbol de nodos reutilizable en memoria para máxima velocidad bajo Java 25 y Project Loom.
 */
public class AuraTemplateParser {

    private static final Map<String, AuraMarkupDocument> PARSED_CACHE = new ConcurrentHashMap<>();

    // Expresión regular para localizar atributos de componentes Aura
    private static final Pattern TAG_PATTERN = Pattern.compile(
        "<(/?[a-zA-Z0-9\\-_:]+)([^>]*?)(/?)>",
        Pattern.DOTALL
    );

    private static final Pattern ATTR_PATTERN = Pattern.compile(
        "([a-zA-Z0-9\\-_:]+)(?:\\s*=\\s*(?:\"([^\"]*)\"|'([^']*)'|([^\\s>]+)))?"
    );

    private static final Set<String> VOID_TAGS = Set.of(
        "area", "base", "br", "col", "embed", "hr", "img", "input",
        "link", "meta", "param", "source", "track", "wbr"
    );

    public static AuraMarkupDocument parse(String html) {
        if (html == null || html.isBlank()) {
            return new AuraMarkupDocument();
        }

        return PARSED_CACHE.computeIfAbsent(html, AuraTemplateParser::doParse);
    }

    public static void clearCache() {
        PARSED_CACHE.clear();
    }

    private static class OpenElement {
        final String tagName;
        final AuraMarkupNode.ComponentNode compNode;
        final int bodyStartIndex;

        OpenElement(String tagName, AuraMarkupNode.ComponentNode compNode, int bodyStartIndex) {
            this.tagName = tagName;
            this.compNode = compNode;
            this.bodyStartIndex = bodyStartIndex;
        }
    }

    private static AuraMarkupDocument doParse(String html) {
        AuraMarkupDocument doc = new AuraMarkupDocument();
        Matcher matcher = TAG_PATTERN.matcher(html);

        Deque<OpenElement> stack = new ArrayDeque<>();
        int lastLiteralEnd = 0;

        while (matcher.find()) {
            int tagStart = matcher.start();
            int tagEnd = matcher.end();

            String rawTagName = matcher.group(1);
            String rawAttrs = matcher.group(2);
            String rawSlash = matcher.group(3);

            boolean isClosingTag = rawTagName.startsWith("/");
            String tagName = isClosingTag ? rawTagName.substring(1).trim() : rawTagName.trim();
            boolean isSelfClosing = "/".equals(rawSlash) || VOID_TAGS.contains(tagName.toLowerCase());

            // 1. Manejar texto literal previo al tag
            if (tagStart > lastLiteralEnd) {
                String literalText = html.substring(lastLiteralEnd, tagStart);
                appendNode(doc, stack, new AuraMarkupNode.LiteralNode(literalText));
            }
            lastLiteralEnd = tagEnd;

            // 2. Si es una etiqueta de cierre
            if (isClosingTag) {
                OpenElement matched = null;
                for (OpenElement el : stack) {
                    if (el.tagName.equalsIgnoreCase(tagName)) {
                        matched = el;
                        break;
                    }
                }

                if (matched != null) {
                    while (!stack.isEmpty()) {
                        OpenElement popped = stack.pop();
                        if (popped == matched) {
                            if (popped.compNode != null) {
                                String innerMarkup = html.substring(popped.bodyStartIndex, tagStart);
                                popped.compNode.setRawInnerMarkup(innerMarkup);
                            } else {
                                appendNode(doc, stack, new AuraMarkupNode.LiteralNode("</" + tagName + ">"));
                            }
                            break;
                        } else {
                            if (popped.compNode == null) {
                                appendNode(doc, stack, new AuraMarkupNode.LiteralNode("</" + popped.tagName + ">"));
                            }
                        }
                    }
                } else {
                    appendNode(doc, stack, new AuraMarkupNode.LiteralNode("</" + tagName + ">"));
                }
                continue;
            }

            // 3. Etiqueta de apertura o autocerrada: Analizar atributos
            AuraTag tag = parseTagAttributes(tagName, rawAttrs, isSelfClosing);
            String auraId = extractAuraId(tag);

            if (auraId != null && !auraId.isBlank()) {
                tag.setAuraId(auraId);
                AuraMarkupNode.ComponentNode compNode = new AuraMarkupNode.ComponentNode(tag, "");
                appendNode(doc, stack, compNode);

                if (!isSelfClosing) {
                    stack.push(new OpenElement(tagName, compNode, tagEnd));
                }
            } else {
                String rawTagHtml = html.substring(tagStart, tagEnd);
                appendNode(doc, stack, new AuraMarkupNode.LiteralNode(rawTagHtml));

                if (!isSelfClosing) {
                    stack.push(new OpenElement(tagName, null, tagEnd));
                }
            }
        }

        // 4. Texto literal final después de la última etiqueta
        if (lastLiteralEnd < html.length()) {
            String trailing = html.substring(lastLiteralEnd);
            appendNode(doc, stack, new AuraMarkupNode.LiteralNode(trailing));
        }

        return doc;
    }

    private static void appendNode(AuraMarkupDocument doc, Deque<OpenElement> stack, AuraMarkupNode node) {
        AuraMarkupNode.ComponentNode parentComp = null;
        for (OpenElement el : stack) {
            if (el.compNode != null) {
                parentComp = el.compNode;
                break;
            }
        }
        if (parentComp != null) {
            parentComp.addChild(node);
        } else {
            doc.addNode(node);
        }
    }

    private static AuraTag parseTagAttributes(String tagName, String rawAttrs, boolean selfClosing) {
        AuraTag tag = new AuraTag(tagName);
        tag.setSelfClosing(selfClosing);

        if (rawAttrs != null && !rawAttrs.isBlank()) {
            Matcher attrMatcher = ATTR_PATTERN.matcher(rawAttrs);
            while (attrMatcher.find()) {
                String name = attrMatcher.group(1);
                if (name == null || name.isBlank() || name.equals("/")) continue;

                String val = attrMatcher.group(2); // con comillas dobles
                if (val == null) val = attrMatcher.group(3); // con comillas simples
                if (val == null) val = attrMatcher.group(4); // sin comillas
                if (val == null) val = "";

                tag.setAttribute(name, val);
            }
        }

        return tag;
    }

    private static String extractAuraId(AuraTag tag) {
        String val = tag.getAttribute("aura:id");
        if (val == null) val = tag.getAttribute("j:id");
        if (val == null) val = tag.getAttribute("wicket:id");
        if (val == null) val = tag.getAttribute("data-aura-id");
        return val;
    }
}
