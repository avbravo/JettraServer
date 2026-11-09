package io.jettra.aura.components;

import io.jettra.aura.core.AuraComponent;
import io.jettra.aura.core.AuraSession;
import io.jettra.aura.core.AuraTag;
import java.util.List;

/**
 * Panel de retroalimentación de mensajes .
 * Renderiza los mensajes informativos, de éxito, advertencia o error acumulados en la sesión.
 */
public class AuraFeedbackPanel extends AuraComponent {

    public AuraFeedbackPanel(String id) {
        super(id);
        setOutputMarkupId(true);
    }

    @Override
    protected void onComponentTagBody(AuraTag tag, String markupBody, StringBuilder output) {
        AuraSession session = AuraSession.get();
        List<AuraSession.FeedbackMessage> messages = (session != null)
                ? session.consumeFeedbackMessages()
                : List.of();

        if (messages.isEmpty()) {
            return;
        }

        output.append("<div class=\"aura-feedback-container\" style=\"display: flex; flex-direction: column; gap: 0.5rem; margin-bottom: 1rem;\">\n");
        for (AuraSession.FeedbackMessage msg : messages) {
            String alertClass = switch (msg.level()) {
                case SUCCESS -> "aura-alert-success";
                case ERROR -> "aura-alert-danger";
                case WARNING -> "aura-alert-warning";
                default -> "aura-alert-info";
            };

            String icon = switch (msg.level()) {
                case SUCCESS -> "✔";
                case ERROR -> "✖";
                case WARNING -> "⚠";
                default -> "ℹ";
            };

            String style = switch (msg.level()) {
                case SUCCESS -> "background: #ecfdf5; color: #065f46; border: 1px solid #a7f3d0; padding: 0.75rem 1rem; border-radius: 8px;";
                case ERROR -> "background: #fef2f2; color: #991b1b; border: 1px solid #fecaca; padding: 0.75rem 1rem; border-radius: 8px;";
                case WARNING -> "background: #fffbeb; color: #92400e; border: 1px solid #fde68a; padding: 0.75rem 1rem; border-radius: 8px;";
                default -> "background: #eff6ff; color: #1e40af; border: 1px solid #bfdbfe; padding: 0.75rem 1rem; border-radius: 8px;";
            };

            output.append("  <div class=\"aura-alert ").append(alertClass).append("\" style=\"").append(style).append("\">\n");
            output.append("    <span style=\"font-weight: bold; margin-right: 0.5rem;\">").append(icon).append("</span>\n");
            output.append("    <span>").append(escapeHtml(msg.text())).append("</span>\n");
            output.append("  </div>\n");
        }
        output.append("</div>\n");
    }
}

