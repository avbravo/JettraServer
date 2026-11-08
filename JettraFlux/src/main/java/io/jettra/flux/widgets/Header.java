package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.Arrays;
import java.util.List;

public class Header extends Widget {
    private final int level;
    private final List<Widget> children;

    private Header(int level, List<Widget> children) {
        this.level = Math.max(1, Math.min(6, level));
        this.children = children;
    }

    public static Header of(int level, Widget... children) {
        return new Header(level, Arrays.asList(children));
    }
    
    public static Header of(int level, String text) {
        return new Header(level, Arrays.asList(Text.of(text)));
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        String tag = "h" + level;
        sb.append("<").append(tag).append(" ").append(renderCommonAttributes(theme, "espresso-header")).append(">\n");
        for (Widget child : children) {
            sb.append(child.render(theme));
        }
        sb.append("</").append(tag).append(">\n");
        return sb.toString();
    }
}
