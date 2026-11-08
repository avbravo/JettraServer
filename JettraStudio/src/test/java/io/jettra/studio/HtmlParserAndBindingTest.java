package io.jettra.studio;

import io.jettra.studio.components.Button;
import io.jettra.studio.components.Label;
import io.jettra.studio.core.WebPage;
import io.jettra.studio.markup.HtmlParser;
import io.jettra.studio.markup.MarkupTag;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.Test;

import java.util.List;

import static io.jettra.test.core.JettraAssert.*;

@NotRequiresRunningServer
public class HtmlParserAndBindingTest {

    public static class SampleHomePage extends WebPage {
        public SampleHomePage() {
            add(new Label("helloMessage", "Hello JettraStudio!"));
            add(new Button("actionBtn", "Ejecutar Proceso"));
        }
    }

    @Test
    public void testHtmlParserExtractsComponents() {
        String html = "<!DOCTYPE html>\n"
            + "<html>\n"
            + "<head><title>Test</title></head>\n"
            + "<body>\n"
            + "  <div class=\"container\">\n"
            + "    <h1 jettrat:id=\"helloMessage\">[Placeholder Header]</h1>\n"
            + "    <p jettra:id=\"infoText\">Static Description</p>\n"
            + "    <input type=\"text\" jettrat:id=\"inputField\" class=\"form-ctrl\" />\n"
            + "    <button jettrat:id=\"actionBtn\">Click</button>\n"
            + "  </div>\n"
            + "</body>\n"
            + "</html>";

        HtmlParser parser = new HtmlParser();
        List<MarkupTag> tags = parser.parseTags(html);

        assertEquals(4, tags.size(), "Parser should find 4 component tags");
        assertEquals("h1", tags.get(0).getTagName());
        assertEquals("helloMessage", tags.get(0).getId());
        assertEquals("[Placeholder Header]", tags.get(0).getBodyContent());

        assertEquals("p", tags.get(1).getTagName());
        assertEquals("infoText", tags.get(1).getId());

        assertEquals("input", tags.get(2).getTagName());
        assertEquals("inputField", tags.get(2).getId());
        assertTrue(tags.get(2).isSelfClosing());

        assertEquals("button", tags.get(3).getTagName());
        assertEquals("actionBtn", tags.get(3).getId());
    }

    @Test
    public void testWebPageBindingAndRendering() {
        SampleHomePage page = new SampleHomePage();
        page.setCustomMarkup("<!DOCTYPE html>\n"
            + "<html>\n"
            + "<head><title>Demo</title></head>\n"
            + "<body>\n"
            + "  <div jettrat:id=\"helloMessage\">[Label's message goes here]</div>\n"
            + "  <button jettrat:id=\"actionBtn\">Default Button</button>\n"
            + "</body>\n"
            + "</html>");

        String rendered = page.renderPage();
        assertNotNull(rendered);
        assertTrue(rendered.contains("Hello JettraStudio!"));
        assertTrue(rendered.contains("Ejecutar Proceso"));
        assertFalse(rendered.contains("[Label's message goes here]"));
        assertFalse(rendered.contains("jettrat:id"));
    }
}
