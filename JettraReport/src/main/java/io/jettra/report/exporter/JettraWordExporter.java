package io.jettra.report.exporter;

import io.jettra.report.Report;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class JettraWordExporter {

    public static void export(Report report, String path) {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(path))) {
            String documentXml = generateDocumentXml(report);
            
            String contentTypesXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                    "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">\n" +
                    "    <Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>\n" +
                    "    <Default Extension=\"xml\" ContentType=\"application/xml\"/>\n" +
                    "    <Override PartName=\"/word/document.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml\"/>\n" +
                    "</Types>";

            String relsXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                    "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">\n" +
                    "    <Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"word/document.xml\"/>\n" +
                    "</Relationships>";

            addEntry(zos, "[Content_Types].xml", contentTypesXml);
            addEntry(zos, "_rels/.rels", relsXml);
            addEntry(zos, "word/document.xml", documentXml);

        } catch (Exception e) {
            System.err.println("Error generating Word: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void addEntry(ZipOutputStream zos, String name, String content) throws Exception {
        zos.putNextEntry(new ZipEntry(name));
        zos.write(content.getBytes("UTF-8"));
        zos.closeEntry();
    }

    private static String generateDocumentXml(Report report) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
        sb.append("<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">\n");
        sb.append("<w:body>\n");

        // Header Elements
        for (Report.ReportElement el : report.getHeader().getElements()) {
            addWordElement(sb, el, null, report.getData());
        }

        // Detail Elements
        boolean tableRendered = false;
        for (Report.ReportElement el : report.getDetail().getElements()) {
            if (el instanceof Report.Table) {
                addWordElement(sb, el, null, report.getData());
                tableRendered = true;
            }
        }
        
        if (!tableRendered && report.getData() != null) {
            for (Object row : report.getData()) {
                for (Report.ReportElement el : report.getDetail().getElements()) {
                    addWordElement(sb, el, row, report.getData());
                }
            }
        }

        // Footer Elements
        for (Report.ReportElement el : report.getFooter().getElements()) {
            addWordElement(sb, el, null, report.getData());
        }

        sb.append("</w:body></w:document>");
        return sb.toString();
    }

    private static void addWordElement(StringBuilder sb, Report.ReportElement el, Object row, List<?> data) {
        if (el instanceof Report.TextElement tel) {
            sb.append("<w:p>");
            if (tel.getAlignment() != null) {
                String al = tel.getAlignment().toLowerCase();
                sb.append("<w:pPr><w:jc w:val=\"").append(al).append("\"/></w:pPr>");
            }
            sb.append("<w:r>");
            sb.append("<w:rPr>");
            if (tel.isBold()) sb.append("<w:b/>");
            if (tel.isItalic()) sb.append("<w:i/>");
            if (tel.isUnderline()) sb.append("<w:u w:val=\"single\"/>");
            if (tel.isStrikethrough()) sb.append("<w:strike/>");
            
            // Size (in half-points)
            int sz = tel.getFontSize() * 2;
            sb.append("<w:sz w:val=\"").append(sz).append("\"/>");
            sb.append("<w:szCs w:val=\"").append(sz).append("\"/>");
            
            // Color
            if (tel.getFontColor() != null && tel.getFontColor().startsWith("#")) {
                String hex = tel.getFontColor().substring(1);
                sb.append("<w:color w:val=\"").append(hex).append("\"/>");
            }
            sb.append("</w:rPr>");
            
            sb.append("<w:t>").append(escapeXml(resolveExpression(tel.getExpression(), row))).append("</w:t></w:r></w:p>");
        } else if (el instanceof Report.Table table) {
            sb.append("<w:tbl>");
            sb.append("<w:tblPr><w:tblW w:w=\"0\" w:type=\"auto\"/><w:tblBorders><w:top w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\"/><w:left w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\"/><w:bottom w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\"/><w:right w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\"/><w:insideH w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\"/><w:insideV w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\"/></w:tblBorders></w:tblPr>");
            
            // Header Row
            sb.append("<w:tr>");
            for (Report.Column col : table.getColumns()) {
                sb.append("<w:tc><w:p><w:r><w:rPr><w:b/></w:rPr><w:t>").append(escapeXml(col.getHeader())).append("</w:t></w:r></w:p></w:tc>");
            }
            sb.append("</w:tr>");

            // Data Rows
            if (data != null) {
                for (Object item : data) {
                    sb.append("<w:tr>");
                    for (Report.Column col : table.getColumns()) {
                        Object val = getFieldValue(item, col.getDetailExpression());
                        sb.append("<w:tc><w:p><w:r><w:t>").append(escapeXml(val != null ? val.toString() : "")).append("</w:t></w:r></w:p></w:tc>");
                    }
                    sb.append("</w:tr>");
                }
            }
            sb.append("</w:tbl>");
        }
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

    private static String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private static Object getFieldValue(Object obj, String expression) {
        return io.jettra.report.ReportUtils.getFieldValue(obj, expression);
    }
}
