package io.jettra.aura.template;

import io.jettra.aura.core.AuraContainer;
import io.jettra.aura.core.AuraTag;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Nodo en el árbol de sintaxis de marcado HTML pre-compilado de JettraAura.
 */
public sealed interface AuraMarkupNode extends Serializable
    permits AuraMarkupNode.LiteralNode, AuraMarkupNode.ComponentNode {

    void render(AuraContainer container, StringBuilder output);

    /**
     * Nodo que representa texto HTML literal entre etiquetas de componentes.
     */
    record LiteralNode(String html) implements AuraMarkupNode {
        @Override
        public void render(AuraContainer container, StringBuilder output) {
            if (html != null) output.append(html);
        }
    }

    /**
     * Nodo que representa una etiqueta vinculada a un componente Aura (aura:id).
     */
    final class ComponentNode implements AuraMarkupNode {
        private final AuraTag tag;
        private final List<AuraMarkupNode> children = new ArrayList<>();
        private String rawInnerMarkup;

        public ComponentNode(AuraTag tag, String rawInnerMarkup) {
            this.tag = tag;
            this.rawInnerMarkup = rawInnerMarkup != null ? rawInnerMarkup : "";
        }

        public AuraTag getTag() {
            return tag;
        }

        public List<AuraMarkupNode> getChildren() {
            return children;
        }

        public String getRawInnerMarkup() {
            return rawInnerMarkup;
        }

        public void setRawInnerMarkup(String rawInnerMarkup) {
            this.rawInnerMarkup = rawInnerMarkup != null ? rawInnerMarkup : "";
        }

        public void addChild(AuraMarkupNode child) {
            if (child != null) children.add(child);
        }

        @Override
        public void render(AuraContainer container, StringBuilder output) {
            String auraId = tag.getAuraId();
            io.jettra.aura.core.AuraComponent comp = (container != null && auraId != null)
                    ? container.get(auraId)
                    : null;

            if (comp != null) {
                // Clonar la etiqueta para que cada renderizado sea inmutable respecto a modificaciones de atributos
                AuraTag activeTag = new AuraTag(tag.getName());
                activeTag.setAuraId(tag.getAuraId());
                activeTag.setSelfClosing(tag.isSelfClosing());
                activeTag.getAttributes().putAll(tag.getAttributes());

                if (comp instanceof AuraContainer childContainer && !(comp instanceof io.jettra.aura.components.AuraListView)) {
                    // Contenedor que renderiza sus hijos anidados
                    comp.renderContainer(activeTag, out -> {
                        for (AuraMarkupNode child : children) {
                            child.render(childContainer, out);
                        }
                    }, output);
                } else {
                    comp.render(activeTag, rawInnerMarkup, output);
                }
            } else {
                // Fallback: Si no hay componente Java registrado, emitir el marcado HTML original (Preview amigable)
                output.append(tag.renderOpen());
                if (!tag.isSelfClosing()) {
                    for (AuraMarkupNode child : children) {
                        child.render(container, output);
                    }
                    output.append(tag.renderClose());
                }
            }
        }

        public void renderChildren(AuraContainer container, StringBuilder output) {
            for (AuraMarkupNode child : children) {
                child.render(container, output);
            }
        }
    }
}
