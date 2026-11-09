package io.jettra.aura.core;

import io.jettra.aura.ajax.AuraAjaxRequestTarget;
import io.jettra.aura.model.CompoundPropertyModel;
import io.jettra.aura.model.IModel;
import io.jettra.aura.model.Model;
import io.jettra.flux.core.JettraComponent;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Clase base para todos los componentes de la interfaz de usuario en JettraAura.
 * Variante optimizada y simplificada de JettraFlux para vincular componentes Java 1:1
 * con elementos en plantillas HTML (.html) y Jakarta Faces (.xhtml) mediante aura:id o flux:id.
 * Gestiona ciclo de vida, modelos, atributos HTML, visibilidad y control de eventos.
 */
public abstract class AuraComponent implements JettraComponent, Serializable {

    private final String id;
    private AuraContainer parent;
    private IModel<?> model;
    private boolean visible = true;
    private boolean enabled = true;
    private boolean escapeModelStrings = true;
    private boolean outputMarkupId = false;
    private String markupId;
    private final List<AuraTagModifier> modifiers = new ArrayList<>();
    private final Map<String, List<AuraEventListener>> eventListeners = new LinkedHashMap<>();
    private final Map<String, List<Consumer<AuraAjaxRequestTarget>>> ajaxEventListeners = new LinkedHashMap<>();

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

    @Override
    public String getId() {
        return id;
    }

    public String getComponentType() {
        return getClass().getSimpleName();
    }

    @Override
    public String render(ThemeData theme) {
        if (this instanceof AuraPage auraPage) {
            return auraPage.renderSection();
        }
        AuraPage parentPage = getPage();
        if (parentPage != null) {
            String rendered = parentPage.renderComponent(this);
            if (rendered != null && !rendered.isBlank()) {
                return rendered;
            }
        }
        AuraTag tag = new AuraTag("div");
        tag.setAuraId(this.id);
        StringBuilder sb = new StringBuilder();
        render(tag, "", sb);
        return sb.toString();
    }

    /**
     * Convierte este componente Aura en un {@link Widget} de JettraFlux para integrarlo
     * de forma transparente dentro de vistas programáticas JettraFlux o puentes Jakarta Faces.
     */
    public Widget toFluxWidget() {
        return toFluxWidget("div");
    }

    /**
     * Convierte este componente Aura en un {@link Widget} de JettraFlux usando la etiqueta HTML indicada.
     */
    public Widget toFluxWidget(String tagName) {
        final AuraComponent self = this;
        return new Widget() {
            {
                this.id = self.getMarkupId();
            }

            @Override
            public String render(ThemeData theme) {
                if ("div".equalsIgnoreCase(tagName)) {
                    return self.render(theme);
                }
                if (self instanceof AuraPage auraPage) {
                    return auraPage.renderSection();
                }
                AuraPage parentPage = self.getPage();
                if (parentPage != null) {
                    String rendered = parentPage.renderComponent(self);
                    if (rendered != null && !rendered.isBlank()) {
                        return rendered;
                    }
                }
                AuraTag tag = new AuraTag(tagName != null && !tagName.isBlank() ? tagName : "div");
                tag.setAuraId(self.getId());
                StringBuilder sb = new StringBuilder();
                self.render(tag, "", sb);
                return sb.toString();
            }
        };
    }

    public AuraContainer getParent() {
        return parent;
    }

    public void setParent(AuraContainer parent) {
        this.parent = parent;
    }

    @SuppressWarnings("unchecked")
    public <C extends AuraContainer> C findParent(Class<C> containerType) {
        AuraContainer current = this.parent;
        while (current != null) {
            if (containerType.isInstance(current)) {
                return (C) current;
            }
            current = current.getParent();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> IModel<T> getModel() {
        if (this.model == null && parent != null) {
            // Soporte para CompoundPropertyModel heredado del contenedor padre o formulario ancestro
            AuraContainer current = parent;
            while (current != null) {
                IModel<?> parentModel = current.getModel();
                if (parentModel instanceof CompoundPropertyModel<?> cpm) {
                    this.model = cpm.bind(this.id);
                    break;
                }
                current = current.getParent();
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
        if (markupId != null && !markupId.isBlank()) {
            return markupId;
        }
        return "aura-" + getPath().replace(':', '-');
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
     * Registra un listener de eventos general (síncrono o asíncrono) para el tipo de evento indicado.
     */
    public AuraComponent onEvent(String eventType, AuraEventListener listener) {
        if (eventType != null && listener != null) {
            String key = eventType.trim().toLowerCase();
            eventListeners.computeIfAbsent(key, k -> new ArrayList<>()).add(listener);
        }
        return this;
    }

    /**
     * Registra un listener de evento AJAX en el componente (ej. "click", "change", "blur").
     */
    public AuraComponent addAjaxEvent(String eventType, Consumer<AuraAjaxRequestTarget> listener) {
        if (eventType != null && listener != null) {
            String key = eventType.trim().toLowerCase();
            this.outputMarkupId = true;
            ajaxEventListeners.computeIfAbsent(key, k -> new ArrayList<>()).add(listener);
        }
        return this;
    }

    /**
     * Dispara un evento sobre el componente ejecutando todos los listeners registrados.
     */
    public void fireEvent(AuraEvent event) {
        if (event == null) return;
        String key = event.getEventType().toLowerCase();

        List<AuraEventListener> listeners = eventListeners.get(key);
        if (listeners != null) {
            for (AuraEventListener l : listeners) {
                l.onEvent(event);
            }
        }
        List<AuraEventListener> wildcardListeners = eventListeners.get("*");
        if (wildcardListeners != null) {
            for (AuraEventListener l : wildcardListeners) {
                l.onEvent(event);
            }
        }

        if (event.getAjaxTarget() != null) {
            List<Consumer<AuraAjaxRequestTarget>> ajaxListeners = ajaxEventListeners.get(key);
            if (ajaxListeners != null) {
                for (Consumer<AuraAjaxRequestTarget> al : ajaxListeners) {
                    al.accept(event.getAjaxTarget());
                }
            }
        }
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
        tag.setAttribute("data-aura-path", getPath());
        if (!enabled) {
            tag.setAttribute("disabled", "disabled");
        }
        for (String evtType : ajaxEventListeners.keySet()) {
            String attrName = evtType.startsWith("on") ? evtType : "on" + evtType;
            if (tag.getAttribute(attrName) == null) {
                tag.setAttribute(attrName, "auraAjax('" + getPath() + "', '" + evtType + "', null, this); return false;");
            }
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
