package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.Arrays;
import java.util.List;

public class Carousel extends Widget {
    private final List<Widget> items;
    private String orientation = "horizontal";

    private Carousel(List<Widget> items) {
        this.items = items;
    }

    public static Carousel of(Widget... items) {
        return new Carousel(Arrays.asList(items));
    }
    
    public Carousel orientation(String orientation) {
        this.orientation = orientation;
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        String id = "carousel_" + java.util.UUID.randomUUID().toString().replace("-", "");
        
        boolean isVert = "vertical".equalsIgnoreCase(orientation);
        String containerStyle = isVert ? "position: relative; display: flex; flex-direction: column; align-items: center; width: 100%; height: 400px;" : "position: relative; display: flex; align-items: center; width: 100%;";
        String trackStyle = isVert ? "display: flex; flex-direction: column; overflow-y: auto; scroll-snap-type: y mandatory; scroll-behavior: smooth; gap: 16px; height: 100%; width: 100%; padding: 40px 10px; " : "display: flex; overflow-x: auto; scroll-snap-type: x mandatory; scroll-behavior: smooth; gap: 16px; width: 100%; padding: 10px 40px; ";
        
        String prevBtnStyle = isVert ? "position: absolute; top: 10px; z-index: 10; background: rgba(0,0,0,0.5); color: white; border: none; border-radius: 50%; width: 40px; height: 40px; cursor: pointer; display: flex; align-items: center; justify-content: center;" : "position: absolute; left: 10px; z-index: 10; background: rgba(0,0,0,0.5); color: white; border: none; border-radius: 50%; width: 40px; height: 40px; cursor: pointer; display: flex; align-items: center; justify-content: center;";
        String nextBtnStyle = isVert ? "position: absolute; bottom: 10px; z-index: 10; background: rgba(0,0,0,0.5); color: white; border: none; border-radius: 50%; width: 40px; height: 40px; cursor: pointer; display: flex; align-items: center; justify-content: center;" : "position: absolute; right: 10px; z-index: 10; background: rgba(0,0,0,0.5); color: white; border: none; border-radius: 50%; width: 40px; height: 40px; cursor: pointer; display: flex; align-items: center; justify-content: center;";
        
        String prevIcon = isVert ? "fas fa-chevron-up" : "fas fa-chevron-left";
        String nextIcon = isVert ? "fas fa-chevron-down" : "fas fa-chevron-right";
        String prevAction = isVert ? "{top: -250, behavior: 'smooth'}" : "{left: -250, behavior: 'smooth'}";
        String nextAction = isVert ? "{top: 250, behavior: 'smooth'}" : "{left: 250, behavior: 'smooth'}";
        
        sb.append("<div style=\"").append(containerStyle).append("\">\n");
        sb.append("<button onclick=\"document.getElementById('").append(id).append("').scrollBy(").append(prevAction).append(")\" style=\"").append(prevBtnStyle).append("\"><i class=\"").append(prevIcon).append("\"></i></button>\n");
        
        sb.append("<div id=\"").append(id).append("\" ").append(renderCommonAttributes(theme, "espresso-carousel"))
          .append(" style=\"").append(trackStyle).append(modifier.getStyles()).append("\">\n");
        
        for (Widget item : items) {
            // Updated to ensure child items maintain their own dimensions
            sb.append("  <div style=\"scroll-snap-align: start; flex: 0 0 auto; display: flex; justify-content: center; align-items: center; width: max-content;\">\n");
            sb.append(item.render(theme));
            sb.append("  </div>\n");
        }
        sb.append("</div>\n");
        
        sb.append("<button onclick=\"document.getElementById('").append(id).append("').scrollBy(").append(nextAction).append(")\" style=\"").append(nextBtnStyle).append("\"><i class=\"").append(nextIcon).append("\"></i></button>\n");
        sb.append("</div>\n");
        return sb.toString();
    }
}
