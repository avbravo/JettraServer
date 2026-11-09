package io.jettra.aura.components;

import io.jettra.aura.core.AuraComponent;
import io.jettra.aura.core.AuraTag;
import io.jettra.flux.core.FluxView;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import io.jettra.flux.theme.Themes;
import java.util.function.Supplier;

/**
 * Componente puente de JettraAura que permite incrustar cualquier {@link Widget}
 * o {@link FluxView} de JettraFlux directamente dentro de una plantilla HTML (.html)
 * o Jakarta Faces (.xhtml) mediante {@code aura:id} o {@code flux:id}.
 */
public class AuraFluxWidget extends AuraComponent {

    private transient Supplier<Widget> widgetSupplier;
    private transient ThemeData theme;

    public AuraFluxWidget(String id, Widget widget) {
        this(id, () -> widget, Themes.Core());
    }

    public AuraFluxWidget(String id, FluxView view) {
        this(id, view != null ? view::build : () -> null, Themes.Core());
    }

    public AuraFluxWidget(String id, Supplier<Widget> widgetSupplier) {
        this(id, widgetSupplier, Themes.Core());
    }

    public AuraFluxWidget(String id, Supplier<Widget> widgetSupplier, ThemeData theme) {
        super(id);
        this.widgetSupplier = widgetSupplier;
        this.theme = theme != null ? theme : Themes.Core();
        setOutputMarkupId(true);
    }

    public AuraFluxWidget setWidget(Widget widget) {
        this.widgetSupplier = () -> widget;
        return this;
    }

    public AuraFluxWidget setWidgetSupplier(Supplier<Widget> widgetSupplier) {
        this.widgetSupplier = widgetSupplier;
        return this;
    }

    public ThemeData getTheme() {
        return theme != null ? theme : Themes.Core();
    }

    public AuraFluxWidget setTheme(ThemeData theme) {
        this.theme = theme != null ? theme : Themes.Core();
        return this;
    }

    @Override
    protected void onComponentTagBody(AuraTag tag, String markupBody, StringBuilder output) {
        if (widgetSupplier != null) {
            Widget widget = widgetSupplier.get();
            if (widget != null) {
                output.append(widget.render(getTheme()));
                return;
            }
        }
        super.onComponentTagBody(tag, markupBody, output);
    }
}
