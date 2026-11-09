package io.jettra.aura.components;

import io.jettra.aura.core.AuraComponent;
import io.jettra.aura.core.AuraTag;
import io.jettra.aura.model.IModel;
import io.jettra.aura.model.Model;

/**
 * Componente insignia o badge visual (ej. Activo, Pendiente, UP, DOWN).
 */
public class AuraBadge extends AuraComponent {

    public enum Severity {
        PRIMARY, SUCCESS, WARNING, DANGER, INFO, NEUTRAL
    }

    private Severity severity = Severity.PRIMARY;

    public AuraBadge(String id) {
        super(id);
    }

    public AuraBadge(String id, String text) {
        super(id, Model.of(text));
    }

    public AuraBadge(String id, IModel<String> model) {
        super(id, model);
    }

    public AuraBadge(String id, String text, Severity severity) {
        super(id, Model.of(text));
        this.severity = severity != null ? severity : Severity.PRIMARY;
    }

    public Severity getSeverity() {
        return severity;
    }

    public AuraBadge setSeverity(Severity severity) {
        this.severity = severity != null ? severity : Severity.PRIMARY;
        return this;
    }

    @Override
    protected void onComponentTag(AuraTag tag) {
        super.onComponentTag(tag);
        String badgeStyle = switch (severity) {
            case SUCCESS -> "background: #ecfdf5; color: #059669; border: 1px solid #a7f3d0;";
            case DANGER -> "background: #fef2f2; color: #dc2626; border: 1px solid #fecaca;";
            case WARNING -> "background: #fffbeb; color: #d97706; border: 1px solid #fde68a;";
            case INFO -> "background: #eff6ff; color: #2563eb; border: 1px solid #bfdbfe;";
            case NEUTRAL -> "background: #f1f5f9; color: #64748b; border: 1px solid #cbd5e1;";
            default -> "background: #eff6ff; color: #1d4ed8; border: 1px solid #93c5fd;";
        };
        tag.appendAttribute("style", badgeStyle + " display: inline-flex; align-items: center; padding: 0.2rem 0.6rem; border-radius: 9999px; font-size: 0.75rem; font-weight: 700;", ";");
    }

    @Override
    protected void onComponentTagBody(AuraTag tag, String markupBody, StringBuilder output) {
        Object val = getModelObject();
        if (val != null) {
            output.append(isEscapeModelStrings() ? escapeHtml(val.toString()) : val.toString());
        } else if (markupBody != null) {
            output.append(markupBody);
        }
    }
}

