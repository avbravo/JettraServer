package io.jettra.aura.template;

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
}

