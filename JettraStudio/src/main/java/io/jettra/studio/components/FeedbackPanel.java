package io.jettra.studio.components;

import io.jettra.studio.core.Component;
import io.jettra.studio.markup.MarkupTag;

import java.util.ArrayList;
import java.util.List;

/**
 * FeedbackPanel presents validation error, warning, and success messages on the page.
 */
public class FeedbackPanel extends Component {

    public record Message(String text, Alert.Type type) {}

    private final List<Message> messages = new ArrayList<>();

    public FeedbackPanel(String id) {
        super(id);
    }

    public static FeedbackPanel of(String id) {
        return new FeedbackPanel(id);
    }

    public FeedbackPanel info(String text) {
        messages.add(new Message(text, Alert.Type.INFO));
        return this;
    }

    public FeedbackPanel error(String text) {
        messages.add(new Message(text, Alert.Type.DANGER));
        return this;
    }

    public FeedbackPanel success(String text) {
        messages.add(new Message(text, Alert.Type.SUCCESS));
        return this;
    }

    public FeedbackPanel warning(String text) {
        messages.add(new Message(text, Alert.Type.WARNING));
        return this;
    }

    public boolean hasMessages() {
        return !messages.isEmpty();
    }

    public void clear() {
        messages.clear();
    }

    @Override
    public boolean isVisible() {
        return super.isVisible() && !messages.isEmpty();
    }

    @Override
    public void onComponentTag(MarkupTag tag) {
        super.onComponentTag(tag);
        addCssClass("jettra-feedback-panel");
    }

    @Override
    protected void onComponentTagBody(MarkupTag tag, StringBuilder buffer) {
        buffer.append("<div style=\"display:flex; flex-direction:column; gap:8px; margin-bottom:16px;\">\n");
        for (Message msg : messages) {
            buffer.append("  <div class=\"espresso-alert ").append(msg.type().getCssClass())
                  .append("\" style=\"padding:10px 16px; border-radius:6px; display:flex; align-items:center; gap:8px;\">")
                  .append("<span>").append(msg.type().getIcon()).append("</span> ")
                  .append("<span>").append(escapeHtml(msg.text())).append("</span>")
                  .append("</div>\n");
        }
        buffer.append("</div>\n");
    }
}
