package io.jettra.flux.transport;

import io.jettra.flux.theme.Themes;
import io.jettra.test.annotation.DisplayName;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.Test;

import java.util.Map;

import static io.jettra.test.core.JettraAssert.*;

@NotRequiresRunningServer
public class JettraFluxTransportTest {

    @Test
    @DisplayName("Verify JettraFluxTransport builds client script with strict Accept and JSON headers")
    public void testTransportHeadersAndRendering() {
        JettraFluxTransport transport = JettraFluxTransport.post("/engines?engine=DOCUMENT")
                .contentType(JettraFluxTransport.ContentType.APPLICATION_JSON)
                .accept("application/json")
                .header("X-Custom-Client", "JettraFlux-2026")
                .form("adaptiveRecordInsertForm")
                .loadingButton("btnAdaptiveSubmitInsert", "Guardando...")
                .modalToClose("adaptiveRecordInsertModal")
                .notificationTarget("adaptiveRecordInsertNotification")
                .functionName("submitAdaptiveRecordInsert");

        String script = transport.render(Themes.AstTheme());
        assertNotNull(script);
        assertTrue(script.contains("<script>"), "Must be wrapped in script tag");
        assertTrue(script.contains("function submitAdaptiveRecordInsert("), "Must define target function");
        assertTrue(script.contains("'Accept': 'application/json'"), "Must enforce Accept: application/json");
        assertTrue(script.contains("'Content-Type': 'application/json; charset=UTF-8'"), "Must enforce application/json Content-Type");
        assertTrue(script.contains("'X-Custom-Client': 'JettraFlux-2026'"), "Must include custom headers");
        assertTrue(script.contains("'X-Requested-With': 'XMLHttpRequest'"), "Must include XMLHttpRequest header");
    }

    @Test
    @DisplayName("Verify JettraFluxTransport includes safe non-JSON / HTML error interceptor")
    public void testSafeHtmlErrorInterceptor() {
        JettraFluxTransport transport = JettraFluxTransport.post("/api/insert")
                .functionName("doSubmit");

        String fnCode = transport.renderClientFunction();
        // Verifies the HTML tag stripping interceptor so HTML 400 doesn't crash JSON.parse
        assertTrue(fnCode.contains("htmlText.replace(/<[^>]*>/g, ' ')"), "Must safely strip HTML tags from non-JSON errors");
        assertTrue(fnCode.contains("isJson = contentType.indexOf('application/json') !== -1"), "Must check Content-Type header");
        assertTrue(fnCode.contains("response.json()"), "Must parse JSON when response is JSON");
    }

    @Test
    @DisplayName("Verify JettraFluxTransport URL-encoded configuration")
    public void testUrlEncodedContentType() {
        JettraFluxTransport transport = JettraFluxTransport.post("/form/submit")
                .contentType(JettraFluxTransport.ContentType.APPLICATION_FORM_URLENCODED)
                .form("myForm")
                .functionName("sendForm");

        assertEquals(JettraFluxTransport.ContentType.APPLICATION_FORM_URLENCODED, transport.getContentType());
        String code = transport.renderClientFunction();
        assertTrue(code.contains("new URLSearchParams()"), "Must use URLSearchParams for form-urlencoded");
        assertTrue(code.contains("application/x-www-form-urlencoded"), "Must set form-urlencoded Content-Type");
    }
}
