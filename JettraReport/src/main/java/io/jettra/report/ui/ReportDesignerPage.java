package io.jettra.report.ui;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.jettra.flux.core.Modifier;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import io.jettra.flux.theme.Themes;
import io.jettra.flux.widgets.Button;
import io.jettra.flux.widgets.Card;
import io.jettra.flux.widgets.Div;
import io.jettra.flux.widgets.Header;
import io.jettra.flux.widgets.Scaffold;
import io.jettra.flux.widgets.Span;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Visual designer for JettraReport.
 * Integrated with JettraWebDesigner using JettraFlux.
 */
public class ReportDesignerPage implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if ("POST".equalsIgnoreCase(method)) {
            // handle post logic
        }
        
        ThemeData theme = Themes.AstTheme();
        
        // Sidebar - Tools
        Widget toolbar = Div.of(
            Header.of(4, "Report Elements"),
            createToolButton("Text Field", "🔤"),
            createToolButton("Date Field", "📅"),
            createToolButton("Numeric Field", "🔢"),
            createToolButton("Table", "▦"),
            createToolButton("Image", "🖼️"),
            createToolButton("Chart", "📊"),
            createToolButton("Subreport", "📋"),
            createToolButton("Subtotal", "Σ")
        ).modifier(new Modifier()
            .style("width: 250px; background: var(--surface-color); padding: 15px;"));

        // Canvas - Report Structure
        Widget sections = Div.of(
            createSectionBox("Page Header", "#f0f0f0"),
            createSectionBox("Column Header", "#e0e0e0"),
            createSectionBox("Detail", "#ffffff"),
            createSectionBox("Column Footer", "#e0e0e0"),
            createSectionBox("Page Footer", "#f0f0f0"),
            createSectionBox("Summary", "#d0d0d0")
        ).modifier(new Modifier().style("padding: 10px;"));
        
        // Use Div.of instead of Card.title since title is a modifier attribute in some cases, or maybe we can just use Card.of and no title if Card has no title method.
        Widget canvas = Card.of(sections)
            .modifier(new Modifier().attribute("title", "Report Canvas").style("width: 100%;"));
        
        Widget layout = Div.of(toolbar, canvas)
            .modifier(new Modifier().style("display: flex; gap: 20px; height: 100%;"));
            
        Widget centerContent = Div.of(layout)
            .modifier(new Modifier().style("padding: 20px; height: calc(100vh - 100px);"));
            
        Scaffold scaffold = Scaffold.of().body(centerContent);
        
        String html = "<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n" +
                "<meta charset=\"utf-8\" />\n" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />\n" +
                "<title>Report Designer - JettraReport</title>\n" +
                theme.generateGlobalCss() + "\n" +
                "</head>\n<body style=\"margin: 0; padding: 0; box-sizing: border-box; background-color: var(--background-color);\">\n" +
                scaffold.render(theme) + "\n" +
                "</body>\n</html>";

        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.getResponseBody().close();
    }

    private Widget createToolButton(String label, String icon) {
        return Button.of(icon + " " + label)
            .modifier(new Modifier()
                .cssClass("j-btn")
                .style("width: 100%; margin-bottom: 10px; justify-content: flex-start;"));
    }

    private Widget createSectionBox(String title, String color) {
        return Div.of(
            Span.of(title).modifier(new Modifier().style("font-weight: bold; color: #333;"))
        ).modifier(new Modifier()
            .style("border: 1px dashed #ccc; padding: 20px; margin-bottom: 5px; background-color: " + color + ";"));
    }
}
