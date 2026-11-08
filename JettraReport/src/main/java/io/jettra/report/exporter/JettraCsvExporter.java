package io.jettra.report.exporter;

import io.jettra.report.Report;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.List;

public class JettraCsvExporter {
    
    public static void export(Report report, String path) {
        try (PrintWriter writer = new PrintWriter(new FileOutputStream(path))) {
            Report.Table table = findTable(report);

            if (table != null) {
                // Report Header
                for (Report.ReportElement el : report.getHeader().getElements()) {
                    if (el instanceof Report.TextElement tel) {
                        writer.println("\"" + tel.getExpression().replace("\"", "\"\"") + "\"");
                    }
                }
                writer.println();

                // Table Headers
                StringBuilder sb = new StringBuilder();
                List<Report.Column> columns = table.getColumns();
                for (int i = 0; i < columns.size(); i++) {
                    sb.append("\"").append(columns.get(i).getHeader().replace("\"", "\"\"")).append("\"");
                    if (i < columns.size() - 1) sb.append(",");
                }
                writer.println(sb.toString());

                // Data
                List<?> data = report.getData();
                if (data != null) {
                    for (Object item : data) {
                        sb = new StringBuilder();
                        for (int i = 0; i < columns.size(); i++) {
                            Object val = getFieldValue(item, columns.get(i).getDetailExpression());
                            sb.append("\"").append(val != null ? val.toString().replace("\"", "\"\"") : "").append("\"");
                            if (i < columns.size() - 1) sb.append(",");
                        }
                        writer.println(sb.toString());
                    }
                }
            } else if (report.getData() != null) {
                for (Object row : report.getData()) {
                    for (Report.ReportElement el : report.getDetail().getElements()) {
                        if (el instanceof Report.TextElement tel) {
                            String expr = resolveExpression(tel.getExpression(), row);
                            writer.println("\"" + expr.replace("\"", "\"\"") + "\"");
                        }
                    }
                }
            }
            writer.println();
            // Report Footer
            for (Report.ReportElement el : report.getFooter().getElements()) {
                if (el instanceof Report.TextElement tel) {
                    writer.println("\"" + tel.getExpression().replace("\"", "\"\"") + "\"");
                }
            }
        } catch (Exception e) {
            System.err.println("Error generating CSV: " + e.getMessage());
        }
    }

    private static Report.Table findTable(Report report) {
        for (Report.ReportElement el : report.getDetail().getElements()) {
            if (el instanceof Report.Table) {
                return (Report.Table) el;
            }
        }
        return null;
    }

    private static String resolveExpression(String expression, Object row) {
        if (expression == null) return "";
        if (row == null) return expression;
        
        if (expression.contains("$F{")) {
            String fieldName = expression.substring(expression.indexOf("$F{") + 3, expression.indexOf("}"));
            Object val = getFieldValue(row, fieldName);
            return expression.replace("$F{" + fieldName + "}", val != null ? val.toString() : "");
        }
        
        Object val = getFieldValue(row, expression);
        return val != null ? val.toString() : expression;
    }

    private static Object getFieldValue(Object obj, String expression) {
        return io.jettra.report.ReportUtils.getFieldValue(obj, expression);
    }
}
