package io.jettra.report;

import io.jettra.flux.core.Modifier;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import io.jettra.flux.widgets.Datatable;
import io.jettra.flux.widgets.Image;
import io.jettra.flux.widgets.Modal;
import io.jettra.flux.widgets.Button;
import io.jettra.flux.widgets.Div;
import io.jettra.flux.widgets.Paragraph;
import io.jettra.flux.widgets.Row;
import io.jettra.flux.widgets.SelectOne;
import io.jettra.flux.widgets.TD;

import java.util.ArrayList;
import java.util.List;

public class ReportViewer extends Widget {
    
    private Report report;
    private String uniqueId;
    private boolean isOpen = false;

    public ReportViewer(Report report, String uniqueId) {
        this.report = report;
        this.uniqueId = uniqueId;
        this.id = "reportModal_" + uniqueId;
    }
    
    public static ReportViewer of(Report report, String uniqueId) {
        return new ReportViewer(report, uniqueId);
    }
    
    public ReportViewer open(boolean open) {
        this.isOpen = open;
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        Widget content = buildUI();
        
        Widget modal = Modal.of(content)
            .open(isOpen)
            .modifier(new Modifier()
                .attribute("id", id)
                .style("max-width: 900px; padding: 0; background-color: #161b22; z-index: 9999; border: 1px solid #30363d; box-shadow: 0 0 50px rgba(0,0,0,0.5);"));
        
        return modal.render(theme);
    }
    
    private Widget buildUI() {
        // Toolbar
        Report.ViewerOptions options = report.getViewerOptions();
        List<Widget> toolbarItems = new ArrayList<>();
        
        if (options.isAllowPdf()) {
            toolbarItems.add(Button.of("📄\nPDF")
                .modifier(new Modifier().style("background-color: #da3633; height: 60px; width: 70px; display: flex; flex-direction: column; align-items: center; justify-content: center;"))
                .attribute("onclick", "location.href='?action=report&format=pdf';"));
        }
        if (options.isAllowExcel()) {
            toolbarItems.add(Button.of("📊\nExcel")
                .modifier(new Modifier().style("background-color: #238636; height: 60px; width: 70px; display: flex; flex-direction: column; align-items: center; justify-content: center;"))
                .attribute("onclick", "location.href='?action=report&format=excel';"));
        }
        if (options.isAllowCsv()) {
            toolbarItems.add(Button.of("📝\nCSV")
                .modifier(new Modifier().style("background-color: #8957e5; height: 60px; width: 70px; display: flex; flex-direction: column; align-items: center; justify-content: center;"))
                .attribute("onclick", "location.href='?action=report&format=csv';"));
        }
        if (options.isAllowWord()) {
            toolbarItems.add(Button.of("📘\nWord")
                .modifier(new Modifier().style("background-color: #0969da; height: 60px; width: 70px; display: flex; flex-direction: column; align-items: center; justify-content: center;"))
                .attribute("onclick", "location.href='?action=report&format=word';"));
        }
        if (options.isAllowPrint()) {
            toolbarItems.add(Button.of("🖨️\nImprimir")
                .modifier(new Modifier().style("background-color: #007bff; height: 60px; width: 100px; display: flex; flex-direction: column; align-items: center; justify-content: center;"))
                .attribute("onclick", "var content = document.getElementById('previewPanel_" + uniqueId + "').innerHTML; var win = window.open('', '', 'height=700,width=800'); win.document.write('<html><head><title>Reporte</title><style>body{font-family:sans-serif;padding:40px;} table{width:100%;border-collapse:collapse;margin-top:20px;} th,td{border:1px solid #eee;padding:12px;text-align:left;} th{color:#da3633;text-transform:uppercase;font-weight:bold;border-bottom:2px solid #da3633;}</style></head><body>'); win.document.write(content); win.document.write('</body></html>'); win.document.close(); win.print();"));
        }
        
        Widget sizeSelect = SelectOne.of(
                io.jettra.flux.widgets.RawHtml.of("<option value=\"100%\">Maximizar (100%)</option>"),
                io.jettra.flux.widgets.RawHtml.of("<option value=\"800px\">Original (800px)</option>"),
                io.jettra.flux.widgets.RawHtml.of("<option value=\"75%\">75%</option>"),
                io.jettra.flux.widgets.RawHtml.of("<option value=\"50%\">50%</option>"),
                io.jettra.flux.widgets.RawHtml.of("<option value=\"25%\">25%</option>")
            )
            .modifier(new Modifier()
                .attribute("id", "sizeSelect_" + uniqueId)
                .attribute("onchange", "var size = this.value; var modal = document.getElementById('reportModal_" + uniqueId + "'); if(modal) { if(size === '100%') { modal.style.width = '95vw'; modal.style.height = '95vh'; modal.style.maxWidth = '95vw'; modal.style.maxHeight = '95vh'; } else { modal.style.width = size; modal.style.maxWidth = size; modal.style.height = 'auto'; modal.style.maxHeight = '90vh'; } }")
                .style("height: 60px; padding: 0 15px; background-color: #21262d; color: white; border: 1px solid #30363d; border-radius: 6px;"));
            
        toolbarItems.add(sizeSelect);
        
        Widget closeBtn = Button.of("Cerrar")
            .modifier(new Modifier().style("background-color: #30363d; height: 60px; padding: 0 20px;"))
            .attribute("onclick", "document.getElementById('reportModal_" + uniqueId + "').style.display='none';");
        toolbarItems.add(closeBtn);
        
        Widget toolbarDiv = Div.of(toolbarItems.toArray(new Widget[0]))
            .modifier(new Modifier()
                .style("display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 15px; padding: 15px; background-color: #0d1117; border-bottom: 1px solid #30363d; border-radius: 8px 8px 0 0;"));
        
        // Report Preview Panel
        List<Widget> previewItems = new ArrayList<>();
        
        // Render Header
        for (Report.ReportElement el : report.getHeader().getElements()) {
            previewItems.add(renderElement(el, null));
        }
        
        // Render Detail
        boolean tableRendered = false;
        for (Report.ReportElement el : report.getDetail().getElements()) {
            if (el instanceof Report.Table) {
                previewItems.add(renderElement(el, null));
                tableRendered = true;
            }
        }
        
        if (!tableRendered && report.getData() != null) {
            for (Object row : report.getData()) {
                for (Report.ReportElement el : report.getDetail().getElements()) {
                    previewItems.add(renderElement(el, row));
                }
            }
        }
        
        // Render Summary
        for (Report.ReportElement el : report.getSummary().getElements()) {
            previewItems.add(renderElement(el, null));
        }
        
        // Render Footer
        for (Report.ReportElement el : report.getFooter().getElements()) {
            previewItems.add(renderElement(el, null));
        }
        
        Widget previewPanel = Div.of(previewItems.toArray(new Widget[0]))
            .modifier(new Modifier()
                .attribute("id", "previewPanel_" + uniqueId)
                .style("max-height: 70vh; overflow-y: auto; background-color: white; color: black; padding: 40px; margin: 0 20px 20px 20px; border-radius: 4px; border: 1px solid #ddd;"));
        
        return Div.of(toolbarDiv, previewPanel);
    }
    
    private Widget renderElement(Report.ReportElement el, Object row) {
        if (el instanceof Report.TextElement tel) {
            String text = tel.getExpression();
            if (row != null) {
                text = resolveExpression(text, row);
            }
            Widget p = Paragraph.of(text);
            Modifier mod = new Modifier();
            if (tel.isBold()) mod.style("font-weight: bold;");
            if (tel.getFontSize() > 0) mod.style("font-size: " + tel.getFontSize() + "px;");
            if (tel.getFontColor() != null) mod.style("color: " + tel.getFontColor() + ";");
            ((Paragraph)p).modifier(mod);
            return p;
        } else if (el instanceof Report.DateElement del) {
            String text = resolveExpression(del.getExpression(), row);
            Widget p = Paragraph.of(text);
            Modifier mod = new Modifier();
            if (del.isBold()) mod.style("font-weight: bold;");
            if (del.getFontColor() != null) mod.style("color: " + del.getFontColor() + ";");
            ((Paragraph)p).modifier(mod);
            return p;
        } else if (el instanceof Report.NumericElement nel) {
            String text = resolveExpression(nel.getExpression(), row);
            Widget p = Paragraph.of(text);
            Modifier mod = new Modifier().style("text-align: right;");
            if (nel.isBold()) mod.style("font-weight: bold;");
            if (nel.getFontColor() != null) mod.style("color: " + nel.getFontColor() + ";");
            ((Paragraph)p).modifier(mod);
            return p;
        } else if (el instanceof Report.Table table) {
            List<Widget> widgetHeaders = new ArrayList<>();
            for (Report.Column col : table.getColumns()) {
                String widthStyle = col.getWidth() > 0 ? "width: " + col.getWidth() + "px;" : "";
                Widget td = TD.of(io.jettra.flux.widgets.RawHtml.of(col.getHeader().toUpperCase()))
                        .modifier(new Modifier()
                            .style("font-weight: bold; color: #da3633; font-size: 1.2rem; padding: 15px; border-bottom: 1px solid #eee; text-align: left; " + widthStyle));
                widgetHeaders.add(td);
            }
            
            List<List<Widget>> widgetRows = new ArrayList<>();
            if (report.getData() != null) {
                for (Object item : report.getData()) {
                    List<Widget> dataRow = new ArrayList<>();
                    for (Report.Column col : table.getColumns()) {
                        Object val = getFieldValue(item, col.getDetailExpression());
                        String textVal = val != null ? val.toString() : "";
                        Modifier mod = new Modifier().style("border-bottom: 1px solid #eee;");
                        if (col.isBold()) mod.style("font-weight: bold;");
                        if (col.getFontColor() != null) mod.style("color: " + col.getFontColor() + ";");
                        if (col.getFontSize() > 0) mod.style("font-size: " + col.getFontSize() + "px;");
                        
                        Widget td = TD.of(io.jettra.flux.widgets.RawHtml.of(textVal)).modifier(mod);
                        dataRow.add(td);
                    }
                    widgetRows.add(dataRow);
                }
            }
            
            return Datatable.ofWidgets(widgetHeaders, widgetRows).modifier(new Modifier().style("width: 100%;"));
        } else if (el instanceof Report.ImageElement img) {
            return Image.of(img.getPath());
        }
        return Div.of();
    }

    private String resolveExpression(String expression, Object row) {
        if (expression == null) return "";
        if (row == null) return expression;
        
        // Basic resolution for $F{field}
        if (expression.contains("$F{")) {
            String fieldName = expression.substring(expression.indexOf("$F{") + 3, expression.indexOf("}"));
            Object val = getFieldValue(row, fieldName);
            return expression.replace("$F{" + fieldName + "}", val != null ? val.toString() : "");
        }
        
        // If it's just a field name and we are in detail
        Object val = getFieldValue(row, expression);
        return val != null ? val.toString() : expression;
    }
    
    private Object getFieldValue(Object obj, String expression) {
        return ReportUtils.getFieldValue(obj, expression);
    }
}
