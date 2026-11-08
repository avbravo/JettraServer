package io.jettra.report.exporter;

import io.jettra.report.Report;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class JettraPdfExporter {

    private static class PdfObject {
        int id;
        long offset;
        String content;

        PdfObject(int id, String content) {
            this.id = id;
            this.content = content;
        }
    }

    private static class Cursor {
        float x = 0;
        float y = 0;

        void move(StringBuilder sb, float dx, float dy) {
            sb.append(dx).append(" ").append(dy).append(" Td\n");
            x += dx;
            y += dy;
        }

        void moveTo(StringBuilder sb, float targetX, float targetY) {
            move(sb, targetX - x, targetY - y);
        }
    }

    public static void export(Report report, String path) {
        try (FileOutputStream fos = new FileOutputStream(path)) {
            List<PdfObject> objects = new ArrayList<>();
            StringBuilder stream = new StringBuilder();
            Cursor cursor = new Cursor();

            Report.PageSettings settings = report.getPageSettings();
            boolean isLandscape = settings.getOrientation() == Report.PageSettings.Orientation.LANDSCAPE;
            int width = isLandscape ? 842 : 595;
            int height = isLandscape ? 595 : 842;

            stream.append("BT\n");
            
            // Header: Date and Time
            stream.append("/F1 10 Tf\n");
            stream.append("0 g\n"); // Reset color to black
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            cursor.moveTo(stream, width - 150, height - 30);
            stream.append("(").append(escapePdf("Fecha: " + now)).append(") Tj\n");

            // Footer: Page Number
            cursor.moveTo(stream, width / 2 - 20, 30);
            stream.append("(").append(escapePdf("Página 1")).append(") Tj\n");

            // Start Body
            cursor.moveTo(stream, settings.getMarginLeft(), height - settings.getMarginTop() - 20);

            // Header Elements
            for (Report.ReportElement el : report.getHeader().getElements()) {
                addPdfElement(stream, cursor, el, null, report.getData(), settings, width);
            }

            // Detail Elements
            boolean tableRendered = false;
            for (Report.ReportElement el : report.getDetail().getElements()) {
                if (el instanceof Report.Table) {
                    addPdfElement(stream, cursor, el, null, report.getData(), settings, width);
                    tableRendered = true;
                }
            }

            if (!tableRendered && report.getData() != null) {
                for (Object row : report.getData()) {
                    for (Report.ReportElement el : report.getDetail().getElements()) {
                        addPdfElement(stream, cursor, el, row, report.getData(), settings, width);
                    }
                }
            }

            // Summary Section (Charts)
            for (Report.Chart chart : report.getSummary().getCharts()) {
                addChartPlaceholder(stream, cursor, chart);
            }
            for (Report.ReportElement el : report.getSummary().getElements()) {
                addPdfElement(stream, cursor, el, null, report.getData(), settings, width);
            }

            // Footer Section
            for (Report.ReportElement el : report.getFooter().getElements()) {
                addPdfElement(stream, cursor, el, null, report.getData(), settings, width);
            }

            stream.append("ET\n");

            String streamContent = stream.toString();
            
            int nextId = 1;
            PdfObject catalog = new PdfObject(nextId++, "<< /Type /Catalog /Pages " + (nextId) + " 0 R >>");
            objects.add(catalog);
            
            PdfObject pages = new PdfObject(nextId++, "<< /Type /Pages /Kids [ " + (nextId) + " 0 R ] /Count 1 >>");
            objects.add(pages);
            
            PdfObject page = new PdfObject(nextId++, "<< /Type /Page /Parent 2 0 R /Resources << /Font << /F1 " + (nextId + 1) + " 0 R /F2 " + (nextId + 2) + " 0 R /F3 " + (nextId + 3) + " 0 R /F4 " + (nextId + 4) + " 0 R >> >> /Contents " + (nextId) + " 0 R /MediaBox [ 0 0 " + width + " " + height + " ] >>");
            objects.add(page);
            
            PdfObject contents = new PdfObject(nextId++, "<< /Length " + streamContent.length() + " >>\nstream\n" + streamContent + "endstream");
            objects.add(contents);
            
            PdfObject font1 = new PdfObject(nextId++, "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>");
            objects.add(font1);

            PdfObject font2 = new PdfObject(nextId++, "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>");
            objects.add(font2);

            PdfObject font3 = new PdfObject(nextId++, "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Oblique /Encoding /WinAnsiEncoding >>");
            objects.add(font3);

            PdfObject font4 = new PdfObject(nextId++, "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-BoldOblique /Encoding /WinAnsiEncoding >>");
            objects.add(font4);

            long currentOffset = 0;
            String pdfHeader = "%PDF-1.4\n";
            fos.write(pdfHeader.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));
            currentOffset += pdfHeader.length();

            for (PdfObject obj : objects) {
                obj.offset = currentOffset;
                String objHeader = obj.id + " 0 obj\n";
                String objFooter = "\nendobj\n";
                fos.write(objHeader.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));
                fos.write(obj.content.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));
                fos.write(objFooter.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));
                currentOffset += objHeader.length() + obj.content.length() + objFooter.length();
            }

            long xrefOffset = currentOffset;
            StringBuilder xref = new StringBuilder();
            xref.append("xref\n0 ").append(objects.size() + 1).append("\n0000000000 65535 f \n");
            for (PdfObject obj : objects) {
                xref.append(String.format("%010d 00000 n \n", obj.offset));
            }
            fos.write(xref.toString().getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));
            
            String trailer = "trailer\n<< /Size " + (objects.size() + 1) + " /Root 1 0 R >>\nstartxref\n" + xrefOffset + "\n%%EOF";
            fos.write(trailer.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));

        } catch (Exception e) {
            System.err.println("Error generating PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void applyStyle(StringBuilder stream, Report.ReportElement el) {
        String font = "F1";
        if (el.isBold() && el.isItalic()) font = "F4";
        else if (el.isBold()) font = "F2";
        else if (el.isItalic()) font = "F3";
        stream.append("/").append(font).append(" ").append(el.getFontSize()).append(" Tf\n");
        
        String hexColor = el.getFontColor();
        if (hexColor != null && hexColor.startsWith("#")) {
            try {
                int r = Integer.valueOf(hexColor.substring(1, 3), 16);
                int g = Integer.valueOf(hexColor.substring(3, 5), 16);
                int b = Integer.valueOf(hexColor.substring(5, 7), 16);
                stream.append(String.format("%.2f %.2f %.2f rg\n", r/255.0, g/255.0, b/255.0));
            } catch (Exception e) {
                stream.append("0 g\n");
            }
        } else {
            stream.append("0 g\n");
        }
    }

    private static void applyColumnStyle(StringBuilder stream, Report.Column col) {
        String font = "F1";
        if (col.isBold() && col.isItalic()) font = "F4";
        else if (col.isBold()) font = "F2";
        else if (col.isItalic()) font = "F3";
        stream.append("/").append(font).append(" ").append(col.getFontSize()).append(" Tf\n");
        String hexColor = col.getFontColor();
        if (hexColor != null && hexColor.startsWith("#")) {
            try {
                int r = Integer.valueOf(hexColor.substring(1, 3), 16);
                int g = Integer.valueOf(hexColor.substring(3, 5), 16);
                int b = Integer.valueOf(hexColor.substring(5, 7), 16);
                stream.append(String.format("%.2f %.2f %.2f rg\n", r/255.0, g/255.0, b/255.0));
            } catch (Exception e) {
                stream.append("0 g\n");
            }
        } else {
            stream.append("0 g\n");
        }
    }

    private static void addPdfElement(StringBuilder stream, Cursor cursor, Report.ReportElement el, Object row, List<?> data, Report.PageSettings settings, int width) {
        if (el instanceof Report.TextElement tel) {
            applyStyle(stream, tel);
            String alignment = el.getAlignment();
            float targetX = settings.getMarginLeft();
            float estimatedWidth = tel.getExpression().length() * (el.getFontSize() * 0.55f);
            if (alignment != null) {
                float usableWidth = width - settings.getMarginLeft() - settings.getMarginRight();
                if (alignment.equalsIgnoreCase("CENTER")) {
                    targetX = settings.getMarginLeft() + (usableWidth - estimatedWidth) / 2.0f;
                } else if (alignment.equalsIgnoreCase("RIGHT")) {
                    targetX = width - settings.getMarginRight() - estimatedWidth;
                }
            }
            cursor.moveTo(stream, targetX, cursor.y);
            String expr = resolveExpression(tel.getExpression(), row);
            stream.append("(").append(escapePdf(expr)).append(") Tj\n");
            
            if (el.isUnderline()) {
                stream.append(String.format("%.2f %.2f m %.2f %.2f l S\n", targetX, cursor.y - 1.5f, targetX + estimatedWidth, cursor.y - 1.5f));
            }
            if (el.isStrikethrough()) {
                stream.append(String.format("%.2f %.2f m %.2f %.2f l S\n", targetX, cursor.y + (el.getFontSize() * 0.3f), targetX + estimatedWidth, cursor.y + (el.getFontSize() * 0.3f)));
            }
            
            cursor.moveTo(stream, settings.getMarginLeft(), cursor.y - 15);
        } else if (el instanceof Report.Table table) {
            float startX = cursor.x;
            // Table Header
            stream.append("/F2 10 Tf\n"); // Bold headers
            stream.append("0 g\n");
            for (Report.Column col : table.getColumns()) {
                stream.append("(").append(escapePdf(col.getHeader())).append(") Tj\n");
                cursor.move(stream, col.getWidth(), 0);
            }
            cursor.moveTo(stream, startX, cursor.y - 15);
            
            // Table Data
            if (data != null) {
                for (Object item : data) {
                    for (Report.Column col : table.getColumns()) {
                        applyColumnStyle(stream, col);
                        Object val = getFieldValue(item, col.getDetailExpression());
                        stream.append("(").append(escapePdf(val != null ? val.toString() : "")).append(") Tj\n");
                        cursor.move(stream, col.getWidth(), 0);
                    }
                    cursor.moveTo(stream, startX, cursor.y - 12);
                    if (cursor.y < 50) break; 
                }
            }
            cursor.move(stream, 0, -10);
        }
    }

    private static void addChartPlaceholder(StringBuilder stream, Cursor cursor, Report.Chart chart) {
        stream.append("/F2 12 Tf\n0.2 0.4 0.6 rg\n"); // Blueish for chart title
        stream.append("(").append(escapePdf("[Grafico: " + chart.getType() + " - " + chart.getTitle() + "]")).append(") Tj\n");
        cursor.move(stream, 0, -80); // Reserve space for chart
        stream.append("0 g\n"); // Reset color
    }

    private static String escapePdf(String s) {
        if (s == null) return "";
        // Basic escaping for PDF strings and ensuring ISO-8859-1 compatibility
        return s.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
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
