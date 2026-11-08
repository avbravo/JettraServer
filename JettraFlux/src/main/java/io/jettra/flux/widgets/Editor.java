package io.jettra.flux.widgets;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
public class Editor extends Widget {
    private String content = "";
    private Editor(String content) { this.content = content; }
    public static Editor of(String content) { return new Editor(content); }
    @Override public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div ").append(renderCommonAttributes(theme, "espresso-editor")).append(" style=\"border:1px solid rgba(128,128,128,0.2); border-radius:6px; overflow:hidden; display:flex; flex-direction:column; background:var(--surface-color);\">");
        // Toolbar
        sb.append("<div style=\"display:flex; gap:10px; padding:10px; background:rgba(128,128,128,0.05); border-bottom:1px solid rgba(128,128,128,0.2);\">");
        sb.append("<button style=\"border:none; background:transparent; cursor:pointer; color:var(--text-color);\"><b>B</b></button>");
        sb.append("<button style=\"border:none; background:transparent; cursor:pointer; color:var(--text-color);\"><i>I</i></button>");
        sb.append("<button style=\"border:none; background:transparent; cursor:pointer; color:var(--text-color);\"><u>U</u></button>");
        sb.append("<div style=\"width:1px; background:rgba(128,128,128,0.2);\"></div>");
        sb.append("<button style=\"border:none; background:transparent; cursor:pointer; color:var(--text-color);\">≡</button>");
        sb.append("</div>");
        // Editor content
        sb.append("<textarea style=\"border:none; outline:none; padding:15px; min-height:150px; background:transparent; color:var(--text-color); font-family:inherit; resize:vertical;\">").append(content).append("</textarea>");
        sb.append("</div>");
        return sb.toString();
    }
}
