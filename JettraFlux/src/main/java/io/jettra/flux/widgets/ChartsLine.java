package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.Arrays;
import java.util.List;

public class ChartsLine extends Widget {
    private final List<Widget> children;

    private ChartsLine(List<Widget> children) {
        this.children = children;
    }

    public static ChartsLine of(Widget... children) {
        return new ChartsLine(Arrays.asList(children));
    }

    public static ChartsLine of(String data) {
        return new ChartsLine(Arrays.asList(Text.of(data)));
    }

    @Override
    public String render(ThemeData theme) {
        String id = "chart_" + java.util.UUID.randomUUID().toString().replace("-", "");
        StringBuilder sb = new StringBuilder();
        sb.append("<div ").append(renderCommonAttributes(theme, "espresso-chartsline")).append(" style=\"width: 100%; height: 300px; position: relative;\">\n");
        sb.append("<canvas id=\"").append(id).append("\"></canvas>\n");
        sb.append("<img src=\"data:image/gif;base64,R0lGODlhAQABAIAAAP///wAAACH5BAEAAAAALAAAAAABAAEAAAICRAEAOw==\" onload=\"");
        sb.append("var img = this; ");
        sb.append("function initChart() { ");
        sb.append("  var canvas = document.getElementById('").append(id).append("'); ");
        sb.append("  if(!canvas) return; ");
        sb.append("  var ctx = canvas.getContext('2d'); ");
        sb.append("  new Chart(ctx, { ");
        sb.append("    type: 'line', ");
        sb.append("    data: { ");
        sb.append("      labels: ['January', 'February', 'March', 'April', 'May', 'June', 'July'], ");
        sb.append("      datasets: [{ label: 'First Dataset', data: [65, 59, 80, 81, 56, 55, 40], fill: false, borderColor: '#3b82f6', tension: 0.4 }, ");
        sb.append("                 { label: 'Second Dataset', data: [28, 48, 40, 19, 86, 27, 90], fill: false, borderColor: '#f59e0b', tension: 0.4 }] ");
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
