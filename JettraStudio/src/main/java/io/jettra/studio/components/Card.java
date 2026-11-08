package io.jettra.studio.components;

import io.jettra.studio.core.MarkupContainer;
import io.jettra.studio.markup.MarkupTag;
import io.jettra.studio.model.IModel;
import io.jettra.studio.model.Model;

/**
 * Card surface container component with header, title, and theme adaptations.
 */
public class Card extends MarkupContainer {

    private String title;
    private String subtitle;
    private boolean blueBorder = false;

    public Card(String id) {
        super(id);
    }

    public Card(String id, String title) {
        super(id);
        this.title = title;
    }

    public Card(String id, IModel<?> model) {
        super(id, model);
    }

    public static Card of(String id) {
        return new Card(id);
    }

    public static Card of(String id, String title) {
        return new Card(id, title);
    }

    public Card title(String title) {
        this.title = title;
        return this;
    }

    public Card subtitle(String subtitle) {
        this.subtitle = subtitle;
        return this;
    }

    public Card blueBorder(boolean blueBorder) {
        this.blueBorder = blueBorder;
        return this;
    }

    @Override
    public void onComponentTag(MarkupTag tag) {
        super.onComponentTag(tag);
        addCssClass("espresso-card");
        if (blueBorder) {
            addCssClass("games-card-blue");
        }
    }

    @Override
    protected void onComponentTagBody(MarkupTag tag, StringBuilder buffer) {
        if (title != null && !title.isBlank()) {
            buffer.append("<div class=\"espresso-card-header\" style=\"margin-bottom:12px; border-bottom:1px solid rgba(255,215,0,0.3); padding-bottom:8px;\">\n");
            buffer.append("  <h3 style=\"margin:0; font-size:1.1rem; color:var(--jf-accent, #ffd700); font-weight:800;\">")
                  .append(escapeHtml(title)).append("</h3>\n");
            if (subtitle != null && !subtitle.isBlank()) {
                buffer.append("  <span style=\"font-size:0.8rem; color:var(--jf-text-secondary, #94a3b8);\">")
                      .append(escapeHtml(subtitle)).append("</span>\n");
            }
            buffer.append("</div>\n");
        }
        super.onComponentTagBody(tag, buffer);
    }
}
