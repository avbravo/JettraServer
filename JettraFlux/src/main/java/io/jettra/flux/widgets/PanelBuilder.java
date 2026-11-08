package io.jettra.flux.widgets;

import io.jettra.flux.core.Modifier;
import io.jettra.flux.core.Widget;

import java.util.List;

/**
 * Fluent builder for constructing customizable, responsive JettraFlux Panel containers.
 */
public class PanelBuilder {
    private PanelHeader header;
    private PanelBody body;
    private Widget footer;
    private boolean toggleable = false;
    private Modifier modifier;
    private String id;

    public PanelBuilder() {}

    public PanelBuilder header(PanelHeader header) {
        this.header = header;
        return this;
    }

    public PanelBuilder header(String title) {
        if (this.header == null) {
            this.header = PanelHeader.of(title);
        } else {
            this.header.title(title);
        }
        return this;
    }

    public PanelBuilder title(String title) {
        return header(title);
    }

    public PanelBuilder subtitle(String subtitle) {
        if (this.header == null) this.header = new PanelHeader();
        this.header.subtitle(subtitle);
        return this;
    }

    public PanelBuilder icon(String icon) {
        if (this.header == null) this.header = new PanelHeader();
        this.header.icon(icon);
        return this;
    }

    public PanelBuilder icon(String icon, String color) {
        if (this.header == null) this.header = new PanelHeader();
        this.header.icon(icon, color);
        return this;
    }

    public PanelBuilder badge(Badge badge) {
        if (this.header == null) this.header = new PanelHeader();
        this.header.badge(badge);
        return this;
    }

    public PanelBuilder badge(String text, String severity) {
        if (this.header == null) this.header = new PanelHeader();
        this.header.badge(text, severity);
        return this;
    }

    public PanelBuilder addAction(Widget action) {
        if (this.header == null) this.header = new PanelHeader();
        this.header.addAction(action);
        return this;
    }

    public PanelBuilder body(PanelBody body) {
        this.body = body;
        return this;
    }

    public PanelBuilder grid(int columns, Widget... items) {
        this.body = PanelBody.grid(columns, items);
        return this;
    }

    public PanelBuilder add(Widget child) {
        if (this.body == null) this.body = new PanelBody();
        this.body.add(child);
        return this;
    }

    public PanelBuilder addAll(List<Widget> children) {
        if (this.body == null) this.body = new PanelBody();
        this.body.addAll(children);
        return this;
    }

    public PanelBuilder footer(Widget footer) {
        this.footer = footer;
        return this;
    }

    public PanelBuilder toggleable(boolean toggleable) {
        this.toggleable = toggleable;
        return this;
    }

    public PanelBuilder id(String id) {
        this.id = id;
        return this;
    }

    public PanelBuilder modifier(Modifier modifier) {
        this.modifier = modifier;
        return this;
    }

    public Panel build() {
        Panel panel = Panel.of(this.header, this.body);
        if (this.footer != null) panel.footer(this.footer);
        if (this.toggleable) panel.toggleable(true);
        if (this.id != null) panel.id(this.id);
        if (this.modifier != null) panel.modifier(this.modifier);
        return panel;
    }
}
