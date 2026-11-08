package io.jettra.flux.widgets;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.Arrays;
import java.util.List;
public class AvatarGroup extends Widget {
    private final List<Widget> children;
    private AvatarGroup(List<Widget> children) { this.children = children; }
    public static AvatarGroup of(Widget... children) { return new AvatarGroup(Arrays.asList(children)); }
    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div ").append(renderCommonAttributes(theme, "espresso-avatargroup")).append(" style=\"display: flex; align-items: center;\">\n");
        for (int i = 0; i < children.size(); i++) {
            String childHtml = children.get(i).render(theme);
            if (i > 0) {
                childHtml = childHtml.replace("style=\"", "style=\"margin-left: -1rem; border: 2px solid var(--surface-color); ");
            } else {
                childHtml = childHtml.replace("style=\"", "style=\"border: 2px solid var(--surface-color); ");
            }
            sb.append(childHtml);
        }
        sb.append("</div>\n");
        return sb.toString();
    }
}
