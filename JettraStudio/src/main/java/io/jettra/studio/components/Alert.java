package io.jettra.studio.components;

import io.jettra.studio.core.Component;
import io.jettra.studio.markup.MarkupTag;
import io.jettra.studio.model.IModel;
import io.jettra.studio.model.Model;

/**
 * Visual Alert banner component supporting standard status types (SUCCESS, DANGER, INFO, WARNING).
 */
public class Alert extends Component {

    public enum Type {
        SUCCESS("espresso-alert-success", "✓"),
        DANGER("espresso-alert-danger", "⚠️"),
        INFO("espresso-alert-info", "ℹ️"),
        WARNING("espresso-alert-warning", "⚡");

        private final String cssClass;
        private final String icon;

        Type(String cssClass, String icon) {
            this.cssClass = cssClass;
            this.icon = icon;
        }

        public String getCssClass() {
            return cssClass;
        }

        public String getIcon() {
            return icon;
        }
    }

    private Type type = Type.INFO;

    public Alert(String id) {
        super(id, new Model<>(""));
    }

    public Alert(String id, String message, Type type) {
        super(id, new Model<>(message));
        this.type = (type != null) ? type : Type.INFO;
    }

    public Alert(String id, IModel<String> messageModel, Type type) {
        super(id, messageModel);
        this.type = (type != null) ? type : Type.INFO;
    }

    public static Alert success(String id, String message) {
        return new Alert(id, message, Type.SUCCESS);
    }

    public static Alert danger(String id, String message) {
        return new Alert(id, message, Type.DANGER);
    }

    public static Alert info(String id, String message) {
        return new Alert(id, message, Type.INFO);
    }

    public static Alert warning(String id, String message) {
        return new Alert(id, message, Type.WARNING);
    }

    public Alert type(Type type) {
        if (type != null) {
            this.type = type;
        }
        return this;
    }

    @Override
    public void onComponentTag(MarkupTag tag) {
        super.onComponentTag(tag);
        addCssClass("espresso-alert " + type.getCssClass());
        addStyle("display:flex; align-items:center; gap:10px; padding:12px 18px; border-radius:6px; margin-bottom:16px;");
    }

    @Override
    protected void onComponentTagBody(MarkupTag tag, StringBuilder buffer) {
        Object val = getModelObject();
        buffer.append("<span style=\"font-size:1.15rem;\">").append(type.getIcon()).append("</span> ");
        if (val != null && !val.toString().isEmpty()) {
            buffer.append("<span>").append(escapeHtml(val.toString())).append("</span>");
        } else {
            buffer.append("<span>").append(tag.getBodyContent()).append("</span>");
        }
    }
}
