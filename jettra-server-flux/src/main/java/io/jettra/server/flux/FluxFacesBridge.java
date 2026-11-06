package io.jettra.server.flux;

import io.jettra.flux.core.Widget;
import io.jettra.flux.widgets.Div;
import io.jettra.flux.widgets.Text;
import io.jettra.server.jakarta.faces.FacesContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Puente de interoperabilidad entre Jakarta Faces 4.0 y JettraFlux.
 * Permite renderizar componentes tipados en Java puro dentro del árbol de componentes de Faces.
 */
public class FluxFacesBridge {

    public static Widget fromFacesContext(FacesContext context) {
        if (context == null) {
            return Text.of("FacesContext is null");
        }

        List<Widget> children = new ArrayList<>();
        children.add(Text.of("Jakarta Faces View: " + context.getViewId()));

        Map<String, Object> viewMap = context.getViewMap();
        for (Map.Entry<String, Object> entry : viewMap.entrySet()) {
            children.add(Div.of(Text.of(entry.getKey() + " = " + entry.getValue())));
        }

        return Div.of(children.toArray(new Widget[0]));
    }
}
