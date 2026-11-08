package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

public class ScrollTop extends Widget {
    
    private ScrollTop() {}

    public static ScrollTop of() {
        return new ScrollTop();
    }

    @Override
    public String render(ThemeData theme) {
        String style = "position: fixed; bottom: 20px; right: 20px; display: flex; align-items: center; justify-content: center; width: 40px; height: 40px; border-radius: 50%; background-color: var(--primary-color); color: white; cursor: pointer; box-shadow: 0 2px 5px rgba(0,0,0,0.2); z-index: 1000;";
        StringBuilder sb = new StringBuilder();
        sb.append("<div ").append(renderCommonAttributes(theme, "espresso-scrolltop")).append(" style=\"").append(style).append("\" onclick=\"window.scrollTo({top: 0, behavior: 'smooth'});\">");
        sb.append("<svg style=\"width: 20px; height: 20px;\" fill=\"none\" stroke=\"currentColor\" viewBox=\"0 0 24 24\"><path stroke-linecap=\"round\" stroke-linejoin=\"round\" stroke-width=\"2\" d=\"M5 10l7-7m0 0l7 7m-7-7v18\"></path></svg>");
        sb.append("</div>");
        return sb.toString();
    }
}
