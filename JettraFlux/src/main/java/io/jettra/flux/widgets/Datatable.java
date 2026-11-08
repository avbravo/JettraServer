package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.List;

public class Datatable extends Widget {
    private final List<String> headers;
    private final List<List<String>> rows;
    
    private final List<Widget> widgetHeaders;
    private final List<List<Widget>> widgetRows;

    private Datatable(List<String> headers, List<List<String>> rows) {
        this.headers = headers;
        this.rows = rows;
        this.widgetHeaders = null;
        this.widgetRows = null;
    }
    
    private Datatable(List<Widget> widgetHeaders, List<List<Widget>> widgetRows, boolean isWidget) {
        this.headers = null;
        this.rows = null;
        this.widgetHeaders = widgetHeaders;
        this.widgetRows = widgetRows;
    }

    public static Datatable of(List<String> headers, List<List<String>> rows) {
        return new Datatable(headers, rows);
    }
    
    public static Datatable ofWidgets(List<Widget> headers, List<List<Widget>> rows) {
        return new Datatable(headers, rows, true);
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table ").append(renderCommonAttributes(theme, "espresso-datatable")).append(">\n");
        
        if (headers != null && !headers.isEmpty()) {
            sb.append("  <thead>\n    <tr>\n");
            for (String header : headers) {
                sb.append("      <th>").append(header).append("</th>\n");
            }
            sb.append("    </tr>\n  </thead>\n");
        } else if (widgetHeaders != null && !widgetHeaders.isEmpty()) {
            sb.append("  <thead>\n    <tr>\n");
            for (Widget header : widgetHeaders) {
                sb.append("      <th>").append(header.render(theme)).append("</th>\n");
            }
            sb.append("    </tr>\n  </thead>\n");
        }
        
        sb.append("  <tbody>\n");
        if (rows != null) {
            for (List<String> row : rows) {
                sb.append("    <tr>\n");
                for (String cell : row) {
                    sb.append("      <td>").append(cell).append("</td>\n");
                }
                sb.append("    </tr>\n");
            }
        } else if (widgetRows != null) {
            for (List<Widget> row : widgetRows) {
                sb.append("    <tr>\n");
                for (Widget cell : row) {
                    sb.append("      <td>").append(cell.render(theme)).append("</td>\n");
                }
                sb.append("    </tr>\n");
            }
        }
        sb.append("  </tbody>\n");
        sb.append("</table>\n");
        return sb.toString();
    }
}
