package io.jettra.report;

import io.jettra.report.exporter.JettraCsvExporter;
import io.jettra.report.exporter.JettraExcelExporter;
import io.jettra.report.exporter.JettraWordExporter;
import io.jettra.report.exporter.JettraPdfExporter;
import java.util.ArrayList;
import java.util.List;

/**
 * Main class for JettraReport library.
 * Supports multiple sections, charts, and subreports.
 */
public class Report {
    public static final String PAGINATION_VAR = "$V{PAGE_NUMBER}";
    
    private String title;
    private List<?> data = new ArrayList<>();
    private PageSettings pageSettings = new PageSettings();
    private Header header = new Header();
    private List<Group> groups = new ArrayList<>();
    private Detail detail = new Detail();
    private Summary summary = new Summary();
    private Footer footer = new Footer();
    private ViewerOptions viewerOptions = new ViewerOptions();

    public Report(String title) {
        this.title = title;
    }

    public List<?> getData() { return data; }
    public void setData(List<?> data) { this.data = data; }

    public PageSettings getPageSettings() { return pageSettings; }
    public Header getHeader() { return header; }
    public List<Group> getGroups() { return groups; }
    public Detail getDetail() { return detail; }
    public Summary getSummary() { return summary; }
    public Footer getFooter() { return footer; }
    public ViewerOptions getViewerOptions() { return viewerOptions; }

    public ReportViewer createViewer(String uniqueId) {
        return new ReportViewer(this, uniqueId);
    }

    // Nested classes for report sections
    
    public static class ViewerOptions {
        private boolean showViewer = true;
        private boolean allowPrint = true;
        private boolean allowPdf = true;
        private boolean allowExcel = true;
        private boolean allowCsv = true;
        private boolean allowWord = true;

        public ViewerOptions setShowViewer(boolean showViewer) { this.showViewer = showViewer; return this; }
        public ViewerOptions setAllowPrint(boolean allowPrint) { this.allowPrint = allowPrint; return this; }
        public ViewerOptions setAllowPdf(boolean allowPdf) { this.allowPdf = allowPdf; return this; }
        public ViewerOptions setAllowExcel(boolean allowExcel) { this.allowExcel = allowExcel; return this; }
        public ViewerOptions setAllowCsv(boolean allowCsv) { this.allowCsv = allowCsv; return this; }
        public ViewerOptions setAllowWord(boolean allowWord) { this.allowWord = allowWord; return this; }

        public boolean isShowViewer() { return showViewer; }
        public boolean isAllowPrint() { return allowPrint; }
        public boolean isAllowPdf() { return allowPdf; }
        public boolean isAllowExcel() { return allowExcel; }
        public boolean isAllowCsv() { return allowCsv; }
        public boolean isAllowWord() { return allowWord; }
    }

    public static class PageSettings {
        public enum Orientation { PORTRAIT, LANDSCAPE }
        public enum PageSize { A4, LETTER, LEGAL }
        
        private Orientation orientation = Orientation.PORTRAIT;
        private PageSize pageSize = PageSize.A4;
        private int marginLeft = 30, marginRight = 30, marginTop = 30, marginBottom = 30;

        public PageSettings setOrientation(Orientation orientation) { this.orientation = orientation; return this; }
        public PageSettings setPageSize(PageSize pageSize) { this.pageSize = pageSize; return this; }
        public PageSettings setMargins(int left, int right, int top, int bottom) {
            this.marginLeft = left; this.marginRight = right;
            this.marginTop = top; this.marginBottom = bottom;
            return this;
        }
        
        public Orientation getOrientation() { return orientation; }
        public PageSize getPageSize() { return pageSize; }
        public int getMarginLeft() { return marginLeft; }
        public int getMarginRight() { return marginRight; }
        public int getMarginTop() { return marginTop; }
        public int getMarginBottom() { return marginBottom; }
    }

    public static class Header {
        private List<ReportElement> elements = new ArrayList<>();
        public void addElement(ReportElement element) { elements.add(element); }
        public List<ReportElement> getElements() { return elements; }
    }

    public static class Detail {
        private List<ReportElement> elements = new ArrayList<>();
        private List<Report> subreports = new ArrayList<>();
        public void addElement(ReportElement element) { elements.add(element); }
        public void addSubreport(Report subreport) { subreports.add(subreport); }
        public List<ReportElement> getElements() { return elements; }
        public List<Report> getSubreports() { return subreports; }
    }

    public static class Footer {
        private List<ReportElement> elements = new ArrayList<>();
        public void addElement(ReportElement element) { elements.add(element); }
        public List<ReportElement> getElements() { return elements; }
    }

    public static class Summary {
        private List<ReportElement> elements = new ArrayList<>();
        private List<Chart> charts = new ArrayList<>();
        public void addElement(ReportElement element) { elements.add(element); }
        public void addChart(Chart chart) { charts.add(chart); }
        public List<ReportElement> getElements() { return elements; }
        public List<Chart> getCharts() { return charts; }
    }

    public static class Group {
        private String groupByExpression;
        private Header groupHeader = new Header();
        private Footer groupFooter = new Footer();
        private List<Subtotal> subtotals = new ArrayList<>();
        
        public String getGroupByExpression() { return groupByExpression; }
        public void setGroupByExpression(String groupByExpression) { this.groupByExpression = groupByExpression; }
        public Header getGroupHeader() { return groupHeader; }
        public Footer getGroupFooter() { return groupFooter; }
        public List<Subtotal> getSubtotals() { return subtotals; }
    }

    // Elements

    public interface ReportElement {
        String getFontName();
        int getFontSize();
        boolean isBold();
        String getFontColor();
        void setFontName(String fontName);
        void setFontSize(int fontSize);
        void setBold(boolean bold);
        void setFontColor(String fontColor);
        String getAlignment();
        void setAlignment(String alignment);
        boolean isItalic();
        void setItalic(boolean italic);
        boolean isUnderline();
        void setUnderline(boolean underline);
        boolean isStrikethrough();
        void setStrikethrough(boolean strikethrough);
    }

    public static abstract class AbstractReportElement implements ReportElement {
        protected String fontName = "Helvetica";
        protected int fontSize = 10;
        protected boolean bold = false;
        protected boolean italic = false;
        protected boolean underline = false;
        protected boolean strikethrough = false;
        protected String fontColor = "#000000";
        protected String alignment = "LEFT";

        @Override public String getFontName() { return fontName; }
        @Override public int getFontSize() { return fontSize; }
        @Override public boolean isBold() { return bold; }
        @Override public String getFontColor() { return fontColor; }
        @Override public void setFontName(String fontName) { this.fontName = fontName; }
        @Override public void setFontSize(int fontSize) { this.fontSize = fontSize; }
        @Override public void setBold(boolean bold) { this.bold = bold; }
        @Override public void setFontColor(String fontColor) { this.fontColor = fontColor; }
        @Override public String getAlignment() { return alignment; }
        @Override public void setAlignment(String alignment) { this.alignment = alignment; }
        @Override public boolean isItalic() { return italic; }
        @Override public void setItalic(boolean italic) { this.italic = italic; }
        @Override public boolean isUnderline() { return underline; }
        @Override public void setUnderline(boolean underline) { this.underline = underline; }
        @Override public boolean isStrikethrough() { return strikethrough; }
        @Override public void setStrikethrough(boolean strikethrough) { this.strikethrough = strikethrough; }
    }

    public static class TextElement extends AbstractReportElement {
        private String expression;
        
        public TextElement(String expression) { this.expression = expression; }
        public String getExpression() { return expression; }
    }

    public static class DateElement extends AbstractReportElement {
        private String expression;
        private String pattern = "dd/MM/yyyy";
        
        public DateElement(String expression) { this.expression = expression; }
        public DateElement(String expression, String pattern) { 
            this.expression = expression; 
            this.pattern = pattern;
        }
        public String getExpression() { return expression; }
        public String getPattern() { return pattern; }
    }

    public static class NumericElement extends AbstractReportElement {
        private String expression;
        private String format = "#,##0.00";
        
        public NumericElement(String expression) { this.expression = expression; }
        public NumericElement(String expression, String format) { 
            this.expression = expression; 
            this.format = format;
        }
        public String getExpression() { return expression; }
        public String getFormat() { return format; }
    }

    public static class Table implements ReportElement {
        private List<Column> columns = new ArrayList<>();
        private String datasourceExpression;
        private String alignment = "LEFT";
        
        @Override public String getFontName() { return "Helvetica"; }
        @Override public int getFontSize() { return 10; }
        @Override public boolean isBold() { return false; }
        @Override public String getFontColor() { return "#000000"; }
        @Override public void setFontName(String fontName) {}
        @Override public void setFontSize(int fontSize) {}
        @Override public void setBold(boolean bold) {}
        @Override public void setFontColor(String fontColor) {}
        @Override public String getAlignment() { return alignment; }
        @Override public void setAlignment(String alignment) { this.alignment = alignment; }
        @Override public boolean isItalic() { return false; }
        @Override public void setItalic(boolean italic) {}
        @Override public boolean isUnderline() { return false; }
        @Override public void setUnderline(boolean underline) {}
        @Override public boolean isStrikethrough() { return false; }
        @Override public void setStrikethrough(boolean strikethrough) {}

        public void addColumn(Column column) { columns.add(column); }
        public List<Column> getColumns() { return columns; }
        
        public String getDatasourceExpression() { return datasourceExpression; }
        public void setDatasourceExpression(String datasourceExpression) { this.datasourceExpression = datasourceExpression; }
    }

    public static class Column {
        private String header;
        private String detailExpression;
        private int width;
        private String fontName = "Helvetica";
        private int fontSize = 10;
        private boolean bold = false;
        private boolean italic = false;
        private boolean underline = false;
        private boolean strikethrough = false;
        private String fontColor = "#000000";
        
        public Column(String header, String detailExpression, int width) {
            this.header = header;
            this.detailExpression = detailExpression;
            this.width = width;
        }
        public String getHeader() { return header; }
        public String getDetailExpression() { return detailExpression; }
        public int getWidth() { return width; }

        public String getFontName() { return fontName; }
        public Column setFontName(String fontName) { this.fontName = fontName; return this; }
        public int getFontSize() { return fontSize; }
        public Column setFontSize(int fontSize) { this.fontSize = fontSize; return this; }
        public boolean isBold() { return bold; }
        public Column setBold(boolean bold) { this.bold = bold; return this; }
        public String getFontColor() { return fontColor; }
        public Column setFontColor(String fontColor) { this.fontColor = fontColor; return this; }
        public boolean isItalic() { return italic; }
        public Column setItalic(boolean italic) { this.italic = italic; return this; }
        public boolean isUnderline() { return underline; }
        public Column setUnderline(boolean underline) { this.underline = underline; return this; }
        public boolean isStrikethrough() { return strikethrough; }
        public Column setStrikethrough(boolean strikethrough) { this.strikethrough = strikethrough; return this; }
    }

    public static class ImageElement extends AbstractReportElement {
        private String path;
        public ImageElement(String path) { this.path = path; }
        public String getPath() { return path; }
    }

    public static class Chart {
        public enum Type { BAR, PIE, LINE, RADAR, DOUGHNUT }
        private Type type;
        private String title;
        private String[] labels;
        private List<Dataset> datasets = new ArrayList<>();

        public static class Dataset {
            public String label;
            public Number[] data;
            public String[] backgroundColor;
            public Dataset(String label, Number[] data) { this.label = label; this.data = data; }
        }

        public Chart(Type type) { this.type = type; }
        public Chart setTitle(String title) { this.title = title; return this; }
        public Chart setLabels(String... labels) { this.labels = labels; return this; }
        public Chart addDataset(String label, Number[] data) { datasets.add(new Dataset(label, data)); return this; }
        
        public Type getType() { return type; }
        public String getTitle() { return title; }
        public String[] getLabels() { return labels; }
        public List<Dataset> getDatasets() { return datasets; }
    }

    public static class Subtotal {
        public enum Function { SUM, AVG, COUNT, MIN, MAX }
        private String fieldExpression;
        private Function function;
    }

    // Export methods

    public void exportToPdf(String path) {
        JettraPdfExporter.export(this, path);
    }

    public void exportToWord(String path) {
        JettraWordExporter.export(this, path);
    }

    public void exportToExcel(String path) {
        JettraExcelExporter.export(this, path);
    }

    public void exportToCsv(String path) {
        JettraCsvExporter.export(this, path);
    }

    public void exportToTxt(String path) { /* Implementation */ }
}
