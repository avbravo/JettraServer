package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.Arrays;
import java.util.List;

public class VisitorGraphCard extends Widget {
    private final String title;
    private final String selectedYear;
    private final String totalMrr;
    private final String mrrGrowthTitle;
    private final String avgMrr;
    private final String avgMrrTitle;
    private final List<Integer> barHeights;

    private VisitorGraphCard(String title, String selectedYear, String totalMrr, String mrrGrowthTitle, String avgMrr, String avgMrrTitle, List<Integer> barHeights) {
        this.title = title;
        this.selectedYear = selectedYear;
        this.totalMrr = totalMrr;
        this.mrrGrowthTitle = mrrGrowthTitle;
        this.avgMrr = avgMrr;
        this.avgMrrTitle = avgMrrTitle;
        this.barHeights = barHeights;
    }

    public static VisitorGraphCard of(String title, String selectedYear, String totalMrr, String mrrGrowthTitle, String avgMrr, String avgMrrTitle, Integer... barHeights) {
        return new VisitorGraphCard(title, selectedYear, totalMrr, mrrGrowthTitle, avgMrr, avgMrrTitle, Arrays.asList(barHeights));
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class='chart-card-header'>");
        sb.append("  <div class='stat-header' style='color: var(--on-surface-color); font-size: 1rem;'>").append(title).append("</div>");
        sb.append("  <select style='background: transparent; border: 1px solid rgba(128,128,128,0.3); color: var(--on-surface-color); padding: 5px 10px; border-radius: 4px;'><option>").append(selectedYear).append("</option></select>");
        sb.append("</div>");
        sb.append("<div style='display: flex; gap: 40px; margin-bottom: 30px;'>");
        sb.append("  <div>");
        sb.append("    <div class='stat-value'>").append(totalMrr).append("</div>");
        sb.append("    <div class='stat-header' style='margin-top: 5px; font-size: 0.75rem;'>").append(mrrGrowthTitle).append("</div>");
        sb.append("  </div>");
        sb.append("  <div>");
        sb.append("    <div class='stat-value'>").append(avgMrr).append("</div>");
        sb.append("    <div class='stat-header' style='margin-top: 5px; font-size: 0.75rem;'>").append(avgMrrTitle).append("</div>");
        sb.append("  </div>");
        sb.append("</div>");
        sb.append("<div style='height: 250px; display: flex; align-items: flex-end; justify-content: space-between; gap: 10px; padding-top: 20px; border-bottom: 1px solid rgba(128,128,128,0.2);'>");
        
        for (Integer height : barHeights) {
            sb.append("  <div style='width: 100%; background: var(--primary-color); height: ").append(height).append("%; border-radius: 4px 4px 0 0;'></div>");
        }
        
        sb.append("</div>");

        return Card.of(Paragraph.of(sb.toString())).render(theme);
    }
}
