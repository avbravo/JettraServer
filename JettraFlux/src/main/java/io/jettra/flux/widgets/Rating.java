package io.jettra.flux.widgets;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
public class Rating extends Widget {
    private int value = 0;
    private Rating(int value) { this.value = value; }
    public static Rating of(int value) { return new Rating(value); }
    @Override public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div ").append(renderCommonAttributes(theme, "espresso-rating")).append(" style=\"display:inline-flex; gap:4px; align-items:center;\">");
        for (int i = 1; i <= 5; i++) {
            String color = (i <= value) ? "#eab308" : "rgba(128,128,128,0.3)";
            sb.append("<svg style=\"width:20px; height:20px; color:").append(color).append("; cursor:pointer;\" fill=\"currentColor\" viewBox=\"0 0 20 20\"><path d=\"M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z\"/></svg>");
        }
        sb.append("</div>");
        return sb.toString();
    }
}
