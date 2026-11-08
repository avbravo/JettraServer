package io.jettra.studio.core;

import io.jettra.studio.markup.MarkupTag;
import io.jettra.studio.model.IModel;
import io.jettra.studio.model.Model;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Base class for all components in JettraStudio, base class for all components in JettraStudio.
 * Binds an HTML element identified by jettrat:id with Java lifecycle, model, and rendering.
 */
public abstract class Component implements Serializable {

    private final String id;
    private MarkupContainer parent;
    private IModel<?> model;
    private boolean visible = true;
    private boolean enabled = true;
    private boolean escapeModelStrings = true;
    private final Map<String, String> attributes = new LinkedHashMap<>();

    public Component(String id) {
        this(id, null);
    }

    public Component(String id, IModel<?> model) {
        this.id = Objects.requireNonNull(id, "Component id cannot be null").trim();
        this.model = model;
    }

    public String getId() {
        return id;
    }

    public MarkupContainer getParent() {
        return parent;
    }

    public void setParent(MarkupContainer parent) {
        this.parent = parent;
    }

    public IModel<?> getModel() {
        return model;
    }

    public Component setModel(IModel<?> model) {
        this.model = model;
        return this;
    }

    public Object getModelObject() {
        return model != null ? model.getObject() : null;
    }

    @SuppressWarnings("unchecked")
    public <T> T getModelObject(Class<T> type) {
        Object val = getModelObject();
        return (val != null && type.isInstance(val)) ? (T) val : null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public Component setModelObject(Object object) {
        if (model != null) {
            ((IModel) model).setObject(object);
        } else {
            this.model = new Model<>(object);
        }
        return this;
    }

    public boolean isVisible() {
        return visible;
    }

    public Component setVisible(boolean visible) {
        this.visible = visible;
        return this;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Component setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public boolean isEscapeModelStrings() {
        return escapeModelStrings;
    }

    public Component setEscapeModelStrings(boolean escape) {
        this.escapeModelStrings = escape;
        return this;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public String getAttribute(String name) {
        return attributes.get(name);
    }

    public Component setAttribute(String name, String value) {
        if (value == null) {
            attributes.remove(name);
        } else {
            attributes.put(name, value);
        }
        return this;
    }

    public Component addCssClass(String cssClass) {
        if (cssClass == null || cssClass.isBlank()) return this;
        String existing = attributes.get("class");
        if (existing == null || existing.isBlank()) {
            attributes.put("class", cssClass.trim());
        } else {
            attributes.put("class", existing + " " + cssClass.trim());
        }
        return this;
    }

    public Component addStyle(String style) {
        if (style == null || style.isBlank()) return this;
        String existing = attributes.get("style");
        if (existing == null || existing.isBlank()) {
            attributes.put("style", style.trim());
        } else {
            attributes.put("style", existing + "; " + style.trim());
        }
        return this;
    }

    /**
     * Traverses component hierarchy to locate the root WebPage.
     */
    public WebPage getPage() {
        if (this instanceof WebPage page) return page;
        return parent != null ? parent.getPage() : null;
    }

    /**
     * Called before rendering to modify HTML element attributes.
     */
    public void onComponentTag(MarkupTag tag) {
        syncAttributesToTag(tag);
    }

    /**
     * Synchronizes component attributes and state to the target MarkupTag.
     */
    public void syncAttributesToTag(MarkupTag tag) {
        if (tag == null) return;
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue();
            if (val == null || val.isBlank()) continue;

            String existing = tag.getAttribute(key);
            if ("class".equalsIgnoreCase(key)) {
                if (existing == null || existing.isBlank()) {
                    tag.setAttribute("class", val.trim());
                } else {
                    String[] tokens = val.trim().split("\\s+");
                    String current = existing;
                    for (String token : tokens) {
                        if (!token.isBlank() && !(" " + current + " ").contains(" " + token + " ")) {
                            current = current + " " + token;
                        }
                    }
                    tag.setAttribute("class", current.trim());
                }
            } else if ("style".equalsIgnoreCase(key)) {
                if (existing == null || existing.isBlank()) {
                    tag.setAttribute("style", val.trim());
                } else {
                    String[] rules = val.split(";");
                    String current = existing.endsWith(";") ? existing : existing + "; ";
                    for (String rule : rules) {
                        String r = rule.trim();
                        if (!r.isBlank() && !current.contains(r)) {
                            current = current + " " + r + ";";
                        }
                    }
                    tag.setAttribute("style", current.trim());
                }
            } else {
                tag.setAttribute(key, val);
            }
        }
        if (!enabled) {
            tag.setAttribute("disabled", "disabled");
        }
    }

    /**
     * Renders the internal body content of the component tag.
     */
    protected void onComponentTagBody(MarkupTag tag, StringBuilder buffer) {
        Object val = getModelObject();
        if (val != null) {
            String str = val.toString();
            buffer.append(escapeModelStrings ? escapeHtml(str) : str);
        }
    }

    /**
     * Main rendering hook for this component.
     */
    public void render(MarkupTag tag, StringBuilder buffer) {
        if (!isVisible()) {
            return; // Component is hidden: render nothing
        }
        onComponentTag(tag);
        syncAttributesToTag(tag);
        buffer.append(tag.renderOpeningTag());
        if (!tag.isSelfClosing()) {
            onComponentTagBody(tag, buffer);
            buffer.append(tag.renderClosingTag());
        }
    }

    protected String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#x27;");
    }

    /**
     * Called at the end of the request cycle to detach and release transient state.
     */
    public void detach() {
        if (model != null) {
            model.detach();
        }
    }
}
