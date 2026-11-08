package io.jettra.report.exporter;

import io.jettra.report.Report;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class JettraExcelExporter {

    public static void export(Report report, String path) {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(path))) {
            Report.Table table = findTable(report);

            List<String> sharedStrings = new ArrayList<>();
            String sheetXml = generateSheetXml(report, table, sharedStrings);
            String workbookXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                    "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">\n" +
                    "    <sheets><sheet name=\"Report\" sheetId=\"1\" r:id=\"rId1\"/></sheets>\n" +
                    "</workbook>";
            
            String contentTypesXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                    "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">\n" +
                    "    <Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>\n" +
                    "    <Default Extension=\"xml\" ContentType=\"application/xml\"/>\n" +
                    "    <Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>\n" +
                    "    <Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>\n" +
                    "    <Override PartName=\"/xl/sharedStrings.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml\"/>\n" +
                    "</Types>";

            String relsXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                    "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">\n" +
                    "    <Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>\n" +
                    "</Relationships>";

            String workbookRelsXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                    "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">\n" +
                    "    <Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>\n" +
                    "    <Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/sharedStrings\" Target=\"sharedStrings.xml\"/>\n" +
                    "</Relationships>";

            addEntry(zos, "[Content_Types].xml", contentTypesXml);
            addEntry(zos, "_rels/.rels", relsXml);
            addEntry(zos, "xl/workbook.xml", workbookXml);
            addEntry(zos, "xl/_rels/workbook.xml.rels", workbookRelsXml);
            addEntry(zos, "xl/worksheets/sheet1.xml", sheetXml);
            addEntry(zos, "xl/sharedStrings.xml", generateSharedStringsXml(sharedStrings));

        } catch (Exception e) {
            System.err.println("Error generating Excel: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void addEntry(ZipOutputStream zos, String name, String content) throws Exception {
        zos.putNextEntry(new ZipEntry(name));
        zos.write(content.getBytes("UTF-8"));
        zos.closeEntry();
    }

    private static String generateSheetXml(Report report, Report.Table table, List<String> sharedStrings) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
        sb.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">\n");
        sb.append("<sheetData>\n");

        int rowIndex = 1;

        // Report Headers
        for (Report.ReportElement el : report.getHeader().getElements()) {
            if (el instanceof Report.TextElement tel) {
                sb.append("<row r=\"").append(rowIndex).append("\">");
                sb.append("<c r=\"A").append(rowIndex).append("\" t=\"s\">");
                sb.append("<v>").append(getSharedStringIndex(sharedStrings, tel.getExpression())).append("</v>");
                sb.append("</c>");
                sb.append("</row>\n");
                rowIndex++;
            }
        }
        rowIndex++; // Empty row

        // Detail Elements (Table)
        boolean tableRendered = (table != null);

        if (tableRendered) {
            // Table Headers
            sb.append("<row r=\"").append(rowIndex).append("\">");
            List<Report.Column> columns = table.getColumns();
            for (int i = 0; i < columns.size(); i++) {
                String val = columns.get(i).getHeader();
                sb.append("<c r=\"").append(getColumnLetter(i)).append(rowIndex).append("\" t=\"s\">");
                sb.append("<v>").append(getSharedStringIndex(sharedStrings, val)).append("</v>");
                sb.append("</c>");
            }
            sb.append("</row>\n");
            rowIndex++;

            // Data
            List<?> data = report.getData();
            if (data != null) {
                for (Object item : data) {
                    sb.append("<row r=\"").append(rowIndex).append("\">");
                    for (int i = 0; i < columns.size(); i++) {
                        Object val = getFieldValue(item, columns.get(i).getDetailExpression());
                        String sVal = val != null ? val.toString() : "";
                        sb.append("<c r=\"").append(getColumnLetter(i)).append(rowIndex).append("\" t=\"s\">");
                        sb.append("<v>").append(getSharedStringIndex(sharedStrings, sVal)).append("</v>");
                        sb.append("</c>");
                    }
                    sb.append("</row>\n");
                    rowIndex++;
                }
            }
        } else if (report.getData() != null) {
            // Non-table details
            for (Object row : report.getData()) {
                for (Report.ReportElement el : report.getDetail().getElements()) {
                    if (el instanceof Report.TextElement tel) {
                        String val = resolveExpression(tel.getExpression(), row);
                        sb.append("<row r=\"").append(rowIndex).append("\">");
                        sb.append("<c r=\"A").append(rowIndex).append("\" t=\"s\">");
                        sb.append("<v>").append(getSharedStringIndex(sharedStrings, val)).append("</v>");
                        sb.append("</c>");
                        sb.append("</row>\n");
                        rowIndex++;
                    }
                }
            }
        }

        rowIndex++; // Empty row
        // Report Footers
        for (Report.ReportElement el : report.getFooter().getElements()) {
            if (el instanceof Report.TextElement tel) {
                sb.append("<row r=\"").append(rowIndex).append("\">");
                sb.append("<c r=\"A").append(rowIndex).append("\" t=\"s\">");
                sb.append("<v>").append(getSharedStringIndex(sharedStrings, tel.getExpression())).append("</v>");
                sb.append("</c>");
                sb.append("</row>\n");
                rowIndex++;
            }
        }

        sb.append("</sheetData></worksheet>");
        return sb.toString();
    }

    private static String generateSharedStringsXml(List<String> sharedStrings) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
        sb.append("<sst xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" count=\"")
                .append(sharedStrings.size()).append("\" uniqueCount=\"").append(sharedStrings.size()).append("\">\n");
        for (String s : sharedStrings) {
            sb.append("<si><t>").append(escapeXml(s)).append("</t></si>\n");
        }
        sb.append("</sst>");
        return sb.toString();
    }

    private static int getSharedStringIndex(List<String> sharedStrings, String s) {
        int idx = sharedStrings.indexOf(s);
        if (idx == -1) {
            idx = sharedStrings.size();
            sharedStrings.add(s);
        }
        return idx;
    }

    private static String getColumnLetter(int col) {
        StringBuilder sb = new StringBuilder();
        while (col >= 0) {
            sb.insert(0, (char) ('A' + (col % 26)));
            col = (col / 26) - 1;
        }
        return sb.toString();
    }

    private static String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
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
