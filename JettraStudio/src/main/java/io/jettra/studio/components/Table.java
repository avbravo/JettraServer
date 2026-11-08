package io.jettra.studio.components;

import io.jettra.studio.core.Component;
import io.jettra.studio.markup.MarkupTag;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Data table component for rendering structured grids with column headers and rows.
 *
 * @param <T> Row data object type
 */
public class Table<T> extends Component {

    private List<String> headers = new ArrayList<>();
    private List<T> rows = new ArrayList<>();
    private List<Function<T, Object>> columnExtractors = new ArrayList<>();

    public Table(String id) {
        super(id);
    }

    public Table(String id, List<String> headers, List<T> rows) {
        super(id);
        if (headers != null) this.headers = headers;
        if (rows != null) this.rows = rows;
    }

    public static <T> Table<T> of(String id) {
        return new Table<>(id);
    }

    public static <T> Table<T> of(String id, List<String> headers, List<T> rows) {
        return new Table<>(id, headers, rows);
    }

    public Table<T> addColumn(String header, Function<T, Object> extractor) {
        headers.add(header);
        columnExtractors.add(extractor);
        return this;
    }

    public Table<T> headers(List<String> headers) {
        if (headers != null) this.headers = headers;
        return this;
    }

    public Table<T> rows(List<T> rows) {
        if (rows != null) this.rows = rows;
        return this;
    }

    @Override
    public void onComponentTag(MarkupTag tag) {
        super.onComponentTag(tag);
        addCssClass("espresso-datatable");
    }

    @Override
    protected void onComponentTagBody(MarkupTag tag, StringBuilder buffer) {
        // Table Head
        if (!headers.isEmpty()) {
            buffer.append("<thead>\n  <tr>\n");
            for (String h : headers) {
                buffer.append("    <th>").append(escapeHtml(h)).append("</th>\n");
            }
            buffer.append("  </tr>\n</thead>\n");
        }

        // Table Body
        buffer.append("<tbody>\n");
        if (rows.isEmpty()) {
            buffer.append("  <tr><td colspan=\"").append(Math.max(1, headers.size()))
                  .append("\" style=\"text-align:center; padding:16px; color:var(--jf-text-secondary, #888);\">No records found</td></tr>\n");
        } else {
            for (T row : rows) {
                buffer.append("  <tr>\n");
                if (!columnExtractors.isEmpty()) {
                    for (Function<T, Object> extractor : columnExtractors) {
                        Object cellVal = extractor.apply(row);
                        buffer.append("    <td>").append(escapeHtml(cellVal != null ? cellVal.toString() : "")).append("</td>\n");
                    }
                } else if (row instanceof List<?> cellList) {
                    for (Object cell : cellList) {
                        buffer.append("    <td>").append(escapeHtml(cell != null ? cell.toString() : "")).append("</td>\n");
                    }
                } else {
                    buffer.append("    <td>").append(escapeHtml(row.toString())).append("</td>\n");
                }
                buffer.append("  </tr>\n");
            }
        }
        buffer.append("</tbody>\n");
    }
}
