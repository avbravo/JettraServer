package io.jettra.flux.widgets;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
public class Tooltip extends Widget {
    private Widget target;
    private String text;
    private Tooltip(Widget target, String text) { this.target = target; this.text = text; }
    public static Tooltip of(Widget target, String text) { return new Tooltip(target, text); }
    @Override public String render(ThemeData theme) {
        String targetHtml = target.render(theme);
        targetHtml = targetHtml.replaceFirst(">", " title=\"" + text + "\">");
        return targetHtml;
    }
}
