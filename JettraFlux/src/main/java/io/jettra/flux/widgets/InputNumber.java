package io.jettra.flux.widgets;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
public class InputNumber extends Widget {
    private int value = 0;
    private InputNumber(int value) { this.value = value; }
    public static InputNumber of(int value) { return new InputNumber(value); }
    @Override public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div ").append(renderCommonAttributes(theme, "espresso-inputnumber")).append(" style=\"display:inline-flex; border:1px solid rgba(128,128,128,0.2); border-radius:6px; overflow:hidden; align-items:stretch; background:var(--surface-color);\">");
        sb.append("<button style=\"border:none; background:transparent; padding:0 12px; cursor:pointer; color:var(--text-color); font-weight:bold; font-size:1.2rem; border-right:1px solid rgba(128,128,128,0.1);\">-</button>");
        sb.append("<input type=\"number\" value=\"").append(value).append("\" style=\"border:none; padding:10px; width:50px; text-align:center; background:transparent; color:var(--text-color); outline:none; -moz-appearance:textfield;\" />");
        sb.append("<button style=\"border:none; background:transparent; padding:0 12px; cursor:pointer; color:var(--text-color); font-weight:bold; font-size:1.2rem; border-left:1px solid rgba(128,128,128,0.1);\">+</button>");
        sb.append("</div>");
        // add css to hide spin buttons
        sb.append("<style>.espresso-inputnumber input::-webkit-outer-spin-button, .espresso-inputnumber input::-webkit-inner-spin-button { -webkit-appearance: none; margin: 0; }</style>");
        return sb.toString();
    }
}
