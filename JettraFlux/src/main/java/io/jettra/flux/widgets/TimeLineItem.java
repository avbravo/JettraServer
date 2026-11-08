package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

public class TimeLineItem extends Widget {
    private Widget content;
    private Widget opposite;
    private String markerIcon;
    private String markerColor = "#3b82f6";
    private String connectorColor = "#e2e8f0";
    private String layout = "vertical";

    private TimeLineItem() {}

    public static TimeLineItem of() {
        return new TimeLineItem();
    }

    public TimeLineItem content(Widget content) {
        this.content = content;
        return this;
    }

    public TimeLineItem opposite(Widget opposite) {
        this.opposite = opposite;
        return this;
    }

    public TimeLineItem markerIcon(String icon) {
        this.markerIcon = icon;
        return this;
    }

    public TimeLineItem markerColor(String color) {
        this.markerColor = color;
        return this;
    }
    
    public TimeLineItem connectorColor(String color) {
        this.connectorColor = color;
        return this;
    }

    public TimeLineItem layout(String layout) {
        this.layout = layout;
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        
        boolean isHorizontal = "horizontal".equalsIgnoreCase(layout);
        
        String eventStyle = isHorizontal ? "display: flex; flex-direction: column; min-width: 120px; flex: 1; position: relative;" : "display: flex; min-height: 70px; position: relative;";
        sb.append("<div class=\"jettra-timeline-event\" style=\"").append(eventStyle).append("\">\n");
        
        // Opposite
        String oppositeStyle = isHorizontal ? "flex: 1; padding: 0 0 1rem 0; text-align: center; display: flex; align-items: flex-end; justify-content: center;" : "flex: 1; padding: 0 1rem; text-align: right; display: flex; align-items: center; justify-content: flex-end;";
        sb.append("<div class=\"jettra-timeline-event-opposite\" style=\"").append(oppositeStyle).append("\">\n");
        if (opposite != null) {
            sb.append(opposite.render(theme));
        }
        sb.append("</div>\n");

        // Separator
        String separatorStyle = isHorizontal ? "display: flex; flex-direction: row; align-items: center; z-index: 1;" : "display: flex; flex-direction: column; align-items: center; z-index: 1;";
        sb.append("<div class=\"jettra-timeline-event-separator\" style=\"").append(separatorStyle).append("\">\n");
        
        String markerStyle = "display: flex; align-items: center; justify-content: center; width: 2.5rem; height: 2.5rem; border-radius: 50%; border: 2px solid " + markerColor + "; background-color: var(--surface-color, #ffffff); box-shadow: 0 2px 4px rgba(0,0,0,0.1); flex-shrink: 0;";
        sb.append("<div class=\"jettra-timeline-event-marker\" style=\"").append(markerStyle).append("\">\n");
        if (markerIcon != null && !markerIcon.isEmpty()) {
            sb.append("<span style=\"display: flex; align-items: center; justify-content: center;\"><i class=\"").append(markerIcon).append("\"></i></span>\n");
        }
        sb.append("</div>\n");
        
        String connectorStyle = isHorizontal ? "flex-grow: 1; height: 2px; background-color: " + connectorColor + "; margin: 0 4px;" : "flex-grow: 1; width: 2px; background-color: " + connectorColor + "; margin: 4px 0;";
        sb.append("<div class=\"jettra-timeline-event-connector\" style=\"").append(connectorStyle).append("\"></div>\n");
        sb.append("</div>\n");

        // Content
        String contentStyle = isHorizontal ? "flex: 1; padding: 1rem 0 0 0; display: flex; flex-direction: column; align-items: center; text-align: center;" : "flex: 1; padding: 0 1rem; display: flex; align-items: center;";
        sb.append("<div class=\"jettra-timeline-event-content\" style=\"").append(contentStyle).append("\">\n");
        if (content != null) {
            sb.append(content.render(theme));
        }
        sb.append("</div>\n");
        
        sb.append("</div>\n");
        
        return sb.toString();
    }
}
