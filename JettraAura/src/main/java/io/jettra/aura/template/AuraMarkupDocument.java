package io.jettra.aura.template;

import io.jettra.aura.core.AuraComponent;
import io.jettra.aura.core.AuraContainer;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Documento HTML estructurado y pre-procesado en nodos Aura.
 */
public class AuraMarkupDocument implements Serializable {

    private final List<AuraMarkupNode> rootNodes = new ArrayList<>();

    public AuraMarkupDocument() {}

    public void addNode(AuraMarkupNode node) {
        if (node != null) {
            rootNodes.add(node);
        }
    }

    public List<AuraMarkupNode> getRootNodes() {
        return rootNodes;
    }

    public String render(AuraContainer rootContainer) {
        StringBuilder sb = new StringBuilder(4096);
        for (AuraMarkupNode node : rootNodes) {
            node.render(rootContainer, sb);
        }
        return sb.toString();
    }

    /**
     * Busca el nodo correspondiente al componente indicado dentro del árbol de marcado
     * y lo renderiza con su contenedor padre e hijos anidados.
     */
    public boolean renderComponent(AuraContainer rootContainer, AuraComponent target, StringBuilder output) {
        if (rootContainer == null || target == null) return false;
        return findAndRenderNode(rootNodes, rootContainer, target, output);
    }

    private boolean findAndRenderNode(List<AuraMarkupNode> nodes, AuraContainer currentContainer, AuraComponent target, StringBuilder output) {
        for (AuraMarkupNode node : nodes) {
            if (node instanceof AuraMarkupNode.ComponentNode compNode) {
                String auraId = compNode.getTag().getAuraId();
                AuraComponent resolved = (currentContainer != null && auraId != null)
                        ? currentContainer.get(auraId)
                        : null;

                if (resolved == target || (auraId != null && auraId.equals(target.getId()) && currentContainer == target.getParent())) {
                    compNode.render(currentContainer, output);
                    return true;
                }

                AuraContainer nextContainer = (resolved instanceof AuraContainer c) ? c : currentContainer;
                if (findAndRenderNode(compNode.getChildren(), nextContainer, target, output)) {
                    return true;
                }
            }
        }
        return false;
    }
}
