package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

public class TabPanel extends Widget {
    private final String header;
    private final Widget content;
    private boolean active = false;

    private TabPanel(String header, Widget content) {
        this.header = header;
        this.content = content;
    }

    public static TabPanel of(String header, Widget content) {
        return new TabPanel(header, content);
    }
    
    public TabPanel active(boolean active) {
        this.active = active;
        return this;
    }
    
    public String getHeader() {
        return header;
    }
    
    public boolean isActive() {
        return active;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        String display = active ? "block" : "none";
        sb.append("<div ").append(renderCommonAttributes(theme, "espresso-tabpanel")).append(" style=\"display: ").append(display).append(";\">\n");
        if (content != null) {
            sb.append(content.render(theme));
        }
        sb.append("</div>\n");
        return sb.toString();
    }
}
