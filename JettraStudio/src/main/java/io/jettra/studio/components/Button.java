package io.jettra.studio.components;

import io.jettra.studio.core.Component;
import io.jettra.studio.markup.MarkupTag;
import io.jettra.studio.model.IModel;
import io.jettra.studio.model.Model;

/**
 * Interactive button component for JettraStudio with action handling and theme variants.
 */
public class Button extends Component {

    public enum Variant {
        DEFAULT("espresso-button"),
        PRIMARY("espresso-button btn-primary"),
        GOLD("games-btn-gold police-btn-gold"),
        BLUE("games-btn-blue police-btn-blue"),
        LIME("games-btn-lime police-btn-lime"),
        RED("games-btn-red police-btn-red"),
        PURPLE("games-btn-purple"),
        DARK("games-btn-dark");

        private final String cssClass;

        Variant(String cssClass) {
            this.cssClass = cssClass;
        }

        public String getCssClass() {
            return cssClass;
        }
    }

    private Runnable clickAction;
    private Variant variant = Variant.DEFAULT;

    public Button(String id) {
        super(id, new Model<>(""));
    }

    public Button(String id, String label) {
        super(id, new Model<>(label));
    }

    public Button(String id, IModel<String> labelModel) {
        super(id, labelModel);
    }

    public Button(String id, Runnable clickAction) {
        this(id);
        this.clickAction = clickAction;
    }

    public Button(String id, String label, Runnable clickAction) {
        this(id, label);
        this.clickAction = clickAction;
    }

    public static Button of(String id, String label) {
        return new Button(id, label);
    }

    public static Button of(String id, String label, Runnable clickAction) {
        return new Button(id, label, clickAction);
    }

    public Button variant(Variant variant) {
        if (variant != null) {
            this.variant = variant;
        }
        return this;
    }

    public Button onClick(Runnable clickAction) {
        this.clickAction = clickAction;
        return this;
    }

    /**
     * Action hook invoked when button is clicked / submitted.
     */
    public void onClick() {
        if (clickAction != null) {
            clickAction.run();
        }
    }

    @Override
    public void onComponentTag(MarkupTag tag) {
        super.onComponentTag(tag);
        if (!tag.getTagName().equalsIgnoreCase("button") && !tag.getTagName().equalsIgnoreCase("input")) {
            // Can decorate an <a> or <div> as button
            tag.setAttribute("role", "button");
        }
        if (tag.getTagName().equalsIgnoreCase("button") && tag.getAttribute("type") == null) {
            tag.setAttribute("type", "submit");
        }
        addCssClass(variant.getCssClass());
        tag.setAttribute("name", getId());
    }

    @Override
    protected void onComponentTagBody(MarkupTag tag, StringBuilder buffer) {
        Object val = getModelObject();
        if (val != null && !val.toString().isEmpty()) {
            buffer.append(escapeHtml(val.toString()));
        } else if (!tag.getBodyContent().isEmpty()) {
            buffer.append(tag.getBodyContent()); // preserve template button label
        }
    }
}
