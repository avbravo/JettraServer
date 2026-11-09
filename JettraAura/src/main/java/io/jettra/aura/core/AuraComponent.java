package io.jettra.aura.core;

import io.jettra.aura.model.CompoundPropertyModel;
import io.jettra.aura.model.IModel;
import io.jettra.aura.model.Model;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Clase base para todos los componentes de la interfaz de usuario en JettraAura.
 * Cada componente se asocia con un elemento del archivo HTML marcado con aura:id.
 * Gestiona ciclo de vida, modelos, atributos HTML, visibilidad y eventos.
 */
public abstract class AuraComponent implements Serializable {

    private final String id;
    private AuraContainer parent;
    private IModel<?> model;
    private boolean visible = true;
    private boolean enabled = true;
    private boolean escapeModelStrings = true;
    private boolean outputMarkupId = false;
    private String markupId;
    private final List<AuraTagModifier> modifiers = new ArrayList<>();

    public AuraComponent(String id) {
        this(id, null);
    }

    public AuraComponent(String id, IModel<?> model) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El id del componente Aura no puede ser nulo ni vacío");
        }
        this.id = id.trim();
        this.model = model;
    }

    public String getId() {
        return id;
    }

    public AuraContainer getParent() {
        return parent;
    }

    public void setParent(AuraContainer parent) {
        this.parent = parent;
    }

    @SuppressWarnings("unchecked")
    public <T> IModel<T> getModel() {
        if (this.model == null && parent != null) {
            // Soporte para CompoundPropertyModel heredado del contenedor padre
            IModel<?> parentModel = parent.getModel();
            if (parentModel instanceof CompoundPropertyModel<?> cpm) {
                this.model = cpm.bind(this.id);
            }
        }
        return (IModel<T>) this.model;
    }

    public AuraComponent setModel(IModel<?> model) {
        this.model = model;
        return this;
    }

    public Object getModelObject() {
        IModel<?> m = getModel();
        return m != null ? m.getObject() : null;
    }

    @SuppressWarnings("unchecked")
    public void setModelObject(Object object) {
        IModel<Object> m = (IModel<Object>) getModel();
        if (m == null) {
            m = new Model<>(object);
            this.model = m;
        } else {
            m.setObject(object);
        }
    }

    public boolean isVisible() {
        return visible;
    }

    public AuraComponent setVisible(boolean visible) {
        this.visible = visible;
        return this;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public AuraComponent setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public boolean isEscapeModelStrings() {
        return escapeModelStrings;
    }

    public AuraComponent setEscapeModelStrings(boolean escapeModelStrings) {
        this.escapeModelStrings = escapeModelStrings;
        return this;
    }

    public boolean isOutputMarkupId() {
        return outputMarkupId;
    }

    public AuraComponent setOutputMarkupId(boolean outputMarkupId) {
        this.outputMarkupId = outputMarkupId;
        return this;
    }

    public String getMarkupId() {
        if (markupId == null) {
            markupId = "aura-" + id + "-" + Integer.toHexString(UUID.randomUUID().hashCode());
        }
        return markupId;
    }

    public AuraComponent setMarkupId(String markupId) {
        this.markupId = markupId;
        return this;
    }

    public AuraComponent addModifier(AuraTagModifier modifier) {
        if (modifier != null) modifiers.add(modifier);
        return this;
    }

    public List<AuraTagModifier> getModifiers() {
        return modifiers;
    }

    /**
     * Ciclo de vida: Invocado una vez al vincular el componente a su contenedor padre.
     */
    protected void onInitialize() {}

    /**
     * Ciclo de vida: Invocado antes de iniciar el renderizado para configurar estado dinámico.
     */
    protected void onConfigure() {}

    /**
     * Ciclo de vida: Invocado inmediatamente antes de procesar el marcado HTML del componente.
     */
    protected void onBeforeRender() {}

    /**
     * Ciclo de vida: Permite enriquecer o modificar la etiqueta HTML (atributos class, style, id, etc.).
     */
    protected void onComponentTag(AuraTag tag) {
        if (outputMarkupId) {
            tag.setAttribute("id", getMarkupId());
        }
        if (!enabled) {
            tag.setAttribute("disabled", "disabled");
        }
        for (AuraTagModifier modifier : modifiers) {
            modifier.modify(tag);
        }
    }

    /**
     * Ciclo de vida: Renderiza el contenido interno de la etiqueta HTML.
     */
    protected void onComponentTagBody(AuraTag tag, String markupBody, StringBuilder output) {
        // Por defecto conserva el cuerpo original
        if (markupBody != null) {
            output.append(markupBody);
        }
    }

    /**
     * Renderizado completo de la etiqueta: apertura, cuerpo y cierre.
     */
    public void render(AuraTag tag, String markupBody, StringBuilder output) {
        onConfigure();
        if (!isVisible()) {
            return;
        }

        onBeforeRender();
        onComponentTag(tag);

        output.append(tag.renderOpen());
        if (!tag.isSelfClosing()) {
            onComponentTagBody(tag, markupBody, output);
            output.append(tag.renderClose());
        }
    }

    /**
     * Renderizado de componentes contenedores que delegan el cuerpo a sus hijos anidados.
     */
    public void renderContainer(AuraTag tag, java.util.function.Consumer<StringBuilder> bodyRenderer, StringBuilder output) {
        onConfigure();
        if (!isVisible()) {
            return;
        }

        onBeforeRender();
        onComponentTag(tag);

        output.append(tag.renderOpen());
        if (!tag.isSelfClosing()) {
            if (bodyRenderer != null) {
                bodyRenderer.accept(output);
            }
            output.append(tag.renderClose());
        }
    }

    /**
     * Ciclo de vida: Desacopla modelos y libera referencias temporales.
     */
    public void detach() {
        if (model != null) {
            model.detach();
        }
    }

    public AuraPage getPage() {
        AuraComponent current = this;
        while (current != null) {
            if (current instanceof AuraPage p) return p;
            current = current.getParent();
        }
        return null;
    }

    public String getPath() {
        if (parent == null || parent instanceof AuraPage) {
            return id;
        }
        return parent.getPath() + ":" + id;
    }

    protected static String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[id=" + id + ", path=" + getPath() + "]";
    }
}
