package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

public class AccordionTab extends Widget {
    private final String header;
    private final Widget content;
    private boolean active = false;

    private AccordionTab(String header, Widget content) {
        this.header = header;
        this.content = content;
    }

    public static AccordionTab of(String header, Widget content) {
        return new AccordionTab(header, content);
    }
    
    public AccordionTab active(boolean active) {
        this.active = active;
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        String activeClass = active ? " espresso-accordion-tab-active" : "";
        sb.append("<div ").append(renderCommonAttributes(theme, "espresso-accordion-tab" + activeClass)).append(">\n");
        
        // Header
        sb.append("  <div class=\"espresso-accordion-header\" onclick=\"toggleAccordion(this)\" style=\"cursor: pointer;\">\n");
        sb.append("    <span class=\"espresso-accordion-header-icon\"><i class=\"fas fa-chevron-").append(active ? "down" : "right").append("\"></i></span>\n");
        sb.append("    <span class=\"espresso-accordion-header-text\">").append(header).append("</span>\n");
        sb.append("  </div>\n");
        
        // Content
        String display = active ? "block" : "none";
        sb.append("  <div class=\"espresso-accordion-content\" style=\"display: ").append(display).append(";\">\n");
        if (content != null) {
            sb.append(content.render(theme));
        }
        sb.append("  </div>\n");
        
        sb.append("</div>\n");
        return sb.toString();
    }
}
