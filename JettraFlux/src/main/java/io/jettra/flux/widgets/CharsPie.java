package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.Arrays;
import java.util.List;

public class CharsPie extends Widget {
    private final List<Widget> children;

    private CharsPie(List<Widget> children) {
        this.children = children;
    }

    public static CharsPie of(Widget... children) {
        return new CharsPie(Arrays.asList(children));
    }

    public static CharsPie of(String data) {
        return new CharsPie(Arrays.asList(Text.of(data)));
    }

    @Override
    public String render(ThemeData theme) {
        String id = "chart_" + java.util.UUID.randomUUID().toString().replace("-", "");
        StringBuilder sb = new StringBuilder();
        sb.append("<div ").append(renderCommonAttributes(theme, "espresso-charspie")).append(" style=\"width: 100%; height: 300px; position: relative;\">\n");
        sb.append("<canvas id=\"").append(id).append("\"></canvas>\n");
        sb.append("<img src=\"data:image/gif;base64,R0lGODlhAQABAIAAAP///wAAACH5BAEAAAAALAAAAAABAAEAAAICRAEAOw==\" onload=\"");
        sb.append("var img = this; ");
        sb.append("function initChart() { ");
        sb.append("  var canvas = document.getElementById('").append(id).append("'); ");
        sb.append("  if(!canvas) return; ");
        sb.append("  var ctx = canvas.getContext('2d'); ");
        sb.append("  new Chart(ctx, { ");
        sb.append("    type: 'pie', ");
        sb.append("    data: { ");
        sb.append("      labels: ['A', 'B', 'C'], ");
        sb.append("      datasets: [{ ");
        sb.append("          data: [300, 50, 100], ");
        sb.append("          backgroundColor: ['#42A5F5', '#66BB6A', '#FFA726'], ");
        sb.append("          hoverBackgroundColor: ['#64B5F6', '#81C784', '#FFB74D'] ");
        sb.append("      }] ");
        sb.append("    }, ");
        sb.append("    options: { responsive: true, maintainAspectRatio: false } ");
        sb.append("  }); ");
        sb.append("  if(img && img.parentNode) img.parentNode.removeChild(img); ");
        sb.append("} ");
        sb.append("if(typeof Chart === 'undefined') { ");
        sb.append("  var s = document.createElement('script'); ");
        sb.append("  s.src = 'https://cdn.jsdelivr.net/npm/chart.js'; ");
        sb.append("  s.onload = initChart; ");
        sb.append("  document.head.appendChild(s); ");
        sb.append("} else { initChart(); }\" style=\"display:none;\" />\n");
        sb.append("</div>\n");
        return sb.toString();
    }
}
