package io.jettra.studio.core;

import io.jettra.studio.markup.HtmlParser;
import io.jettra.studio.markup.MarkupTag;
import io.jettra.studio.model.IModel;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Component that can contain other components, markup container component in JettraStudio.
 * Handles child component registration and recursive markup parsing/rendering.
 */
public class MarkupContainer extends Component implements Iterable<Component> {

    private final Map<String, Component> children = new LinkedHashMap<>();

    public MarkupContainer(String id) {
        super(id);
    }

    public MarkupContainer(String id, IModel<?> model) {
        super(id, model);
    }

    /**
     * Adds one or more child components to this container.
     */
    public MarkupContainer add(Component... components) {
        if (components != null) {
            for (Component c : components) {
                if (c == null) continue;
                if (c == this) {
                    throw new IllegalArgumentException("Cannot add container to itself");
                }
                c.setParent(this);
                children.put(c.getId(), c);
            }
        }
        return this;
    }

    public Component get(String id) {
        if (id == null) return null;
        return children.get(id.trim());
    }

    @SuppressWarnings("unchecked")
    public <T extends Component> T get(String id, Class<T> expectedType) {
        Component c = get(id);
        return (c != null && expectedType.isInstance(c)) ? (T) c : null;
    }

    public boolean contains(String id) {
        return children.containsKey(id);
    }

    public MarkupContainer remove(String id) {
        if (id != null) {
            Component c = children.remove(id);
            if (c != null) {
                c.setParent(null);
            }
        }
        return this;
    }

    public int size() {
        return children.size();
    }

    public Map<String, Component> getChildren() {
        return Collections.unmodifiableMap(children);
    }

    @Override
    public Iterator<Component> iterator() {
        return children.values().iterator();
    }

    @Override
    protected void onComponentTagBody(MarkupTag tag, StringBuilder buffer) {
        renderMarkupContent(tag.getBodyContent(), buffer);
    }

    /**
     * Scans body markup for child component tags and renders them with their respective Java components.
     */
    public void renderMarkupContent(String markup, StringBuilder buffer) {
        if (markup == null || markup.isEmpty()) return;

        HtmlParser parser = new HtmlParser();
        List<MarkupTag> tags = parser.parseTags(markup);

        if (tags.isEmpty()) {
            buffer.append(markup);
            return;
        }

        int lastIndex = 0;
        for (MarkupTag childTag : tags) {
            // Append raw static HTML before this child component
            if (childTag.getOpenTagStart() > lastIndex) {
                buffer.append(markup, lastIndex, childTag.getOpenTagStart());
            }

            Component child = children.get(childTag.getId());
            if (child != null) {
                child.render(childTag, buffer);
            } else {
                // No Java component registered: preserve tag and its body as static HTML
                buffer.append(childTag.renderOpeningTag());
                if (!childTag.isSelfClosing()) {
                    buffer.append(childTag.getBodyContent());
                    buffer.append(childTag.renderClosingTag());
                }
            }

            lastIndex = childTag.getCloseTagEnd();
        }

        // Append trailing static HTML
        if (lastIndex < markup.length()) {
            buffer.append(markup.substring(lastIndex));
        }
    }

    @Override
    public void detach() {
        super.detach();
        for (Component child : children.values()) {
            child.detach();
        }
    }
}
