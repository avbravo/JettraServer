package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.ArrayList;
import java.util.List;

public class MeterGroup extends Widget {
    
    public static class MeterItem {
        public String label;
        public int value;
        public String color;
        public MeterItem(String label, int value, String color) { this.label = label; this.value = value; this.color = color; }
    }
    
    private List<MeterItem> items = new ArrayList<>();
    
    private MeterGroup() {}

    public static MeterGroup of() {
        return new MeterGroup();
    }
    
    public MeterGroup add(String label, int value, String color) {
        items.add(new MeterItem(label, value, color));
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        int total = 0;
        for (MeterItem item : items) total += item.value;
        if (total == 0) total = 100;
        
        StringBuilder sb = new StringBuilder();
        sb.append("<div ").append(renderCommonAttributes(theme, "espresso-metergroup")).append(" style=\"width: 100%;\">");
        
        // Progress bar container
        sb.append("<div style=\"display: flex; width: 100%; height: 10px; border-radius: 6px; overflow: hidden; background: rgba(128,128,128,0.2); margin-bottom: 15px;\">");
        for (MeterItem item : items) {
            double percent = ((double) item.value / total) * 100;
            sb.append("<div style=\"height: 100%; width: ").append(percent).append("%; background-color: ").append(item.color).append(";\"></div>");
        }
        sb.append("</div>");
        
        // Legends
        sb.append("<div style=\"display: flex; gap: 15px; flex-wrap: wrap;\">");
        for (MeterItem item : items) {
            sb.append("<div style=\"display: flex; align-items: center; font-size: 0.85rem; color: var(--text-color);\"><span style=\"display: inline-block; width: 12px; height: 12px; border-radius: 50%; background-color: ").append(item.color).append("; margin-right: 8px;\"></span>").append(item.label).append(" (").append(item.value).append("%)</div>");
        }
        sb.append("</div>");
        
        sb.append("</div>");
        return sb.toString();
    }
}
