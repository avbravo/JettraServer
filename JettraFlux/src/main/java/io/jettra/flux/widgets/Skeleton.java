package io.jettra.flux.widgets;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
public class Skeleton extends Widget {
    public enum Shape { RECTANGLE, CIRCLE }
    private final String width;
    private final String height;
    private final Shape shape;
    private Skeleton(String width, String height, Shape shape) { this.width = width; this.height = height; this.shape = shape; }
    public static Skeleton of(String width, String height) { return new Skeleton(width, height, Shape.RECTANGLE); }
    public static Skeleton circle(String size) { return new Skeleton(size, size, Shape.CIRCLE); }
    @Override
    public String render(ThemeData theme) {
        String baseClass = "espresso-skeleton";
        String br = shape == Shape.CIRCLE ? "50%" : "6px";
        String customStyle = "width: " + width + "; height: " + height + "; border-radius: " + br + "; background-color: rgba(128,128,128,0.2);";
        return "<div " + renderCommonAttributes(theme, baseClass) + " style=\"" + customStyle + " " + (modifier!=null?modifier.getStyles():"") + "\"></div>\n";
    }
}
