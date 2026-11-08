package io.jettra.flux.transport;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JettraFluxTransport - Fluent, type-safe reactive client HTTP transport component for JettraFlux.
 * Encapsulates AJAX/Fetch operations, strict header contracts (Accept: application/json),
 * resilient 4xx/5xx error interceptors preventing JSON syntax errors on HTML responses,
 * form serialization, loading indicators, and notification dispatching.
 */
public class JettraFluxTransport extends Widget {

    public enum ContentType {
        APPLICATION_JSON("application/json; charset=UTF-8"),
        APPLICATION_FORM_URLENCODED("application/x-www-form-urlencoded; charset=UTF-8");

        private final String headerValue;

        ContentType(String headerValue) {
            this.headerValue = headerValue;
        }

        public String getHeaderValue() {
            return headerValue;
        }
    }

    private final String method;
    private final String url;
    private String formId;
    private String activeSectionAttr;
    private ContentType contentType = ContentType.APPLICATION_JSON;
    private String acceptHeader = "application/json";
    private final Map<String, String> customHeaders = new LinkedHashMap<>();
    private String payloadJson;
    private String loadingButtonId;
    private String loadingButtonText;
    private String originalButtonHtml;
    private String modalIdToClose;
    private String notificationId;
    private String onSuccessJs;
    private String onErrorJs;
    private String redirectUrl;
    private int redirectDelayMs = 800;
    private String functionName = "submitJettraFluxTransport";

    private JettraFluxTransport(String method, String url) {
        this.method = (method != null && !method.isBlank()) ? method.toUpperCase() : "POST";
        this.url = (url != null && !url.isBlank()) ? url : "";
    }

    public static JettraFluxTransport post(String url) {
        return new JettraFluxTransport("POST", url);
    }

    public static JettraFluxTransport get(String url) {
        return new JettraFluxTransport("GET", url);
    }

    public static JettraFluxTransport of(String method, String url) {
        return new JettraFluxTransport(method, url);
    }

    public JettraFluxTransport form(String formId) {
        this.formId = formId;
        return this;
    }

    public JettraFluxTransport activeSection(String sectionKey) {
        this.activeSectionAttr = sectionKey;
        return this;
    }

    public JettraFluxTransport contentType(ContentType contentType) {
        this.contentType = contentType != null ? contentType : ContentType.APPLICATION_JSON;
        return this;
    }

    public JettraFluxTransport accept(String acceptHeader) {
        this.acceptHeader = acceptHeader;
        return this;
    }

    public JettraFluxTransport header(String name, String value) {
        if (name != null && !name.isBlank()) {
            this.customHeaders.put(name, value != null ? value : "");
        }
        return this;
    }

    public JettraFluxTransport payload(String json) {
        this.payloadJson = json;
        return this;
    }

    public JettraFluxTransport loadingButton(String buttonId, String loadingText) {
        this.loadingButtonId = buttonId;
        this.loadingButtonText = loadingText;
        return this;
    }

    public JettraFluxTransport originalButtonHtml(String html) {
        this.originalButtonHtml = html;
        return this;
    }

    public JettraFluxTransport modalToClose(String modalId) {
        this.modalIdToClose = modalId;
        return this;
    }

    public JettraFluxTransport notificationTarget(String notificationId) {
        this.notificationId = notificationId;
        return this;
    }

    public JettraFluxTransport onSuccess(String jsCode) {
        this.onSuccessJs = jsCode;
        return this;
    }

    public JettraFluxTransport onError(String jsCode) {
        this.onErrorJs = jsCode;
        return this;
    }

    public JettraFluxTransport redirectOnSuccess(String redirectUrl) {
        this.redirectUrl = redirectUrl;
        return this;
    }

    public JettraFluxTransport redirectDelay(int delayMs) {
        this.redirectDelayMs = delayMs;
        return this;
    }

    public JettraFluxTransport functionName(String functionName) {
        if (functionName != null && !functionName.isBlank()) {
            this.functionName = functionName;
        }
        return this;
    }

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public String getFormId() {
        return formId;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public String getAcceptHeader() {
        return acceptHeader;
    }

    public Map<String, String> getCustomHeaders() {
        return Collections.unmodifiableMap(customHeaders);
    }

    public String getFunctionName() {
        return functionName;
    }

    /**
     * Generates the client JavaScript code defining the transport function.
     */
    public String renderClientFunction() {
        StringBuilder js = new StringBuilder();
        js.append("function ").append(functionName).append("(customPayload) {\n");

        // 1. Resolve target button and update loading state
        if (loadingButtonId != null && !loadingButtonId.isBlank()) {
            js.append("  var __btn = document.getElementById('").append(loadingButtonId).append("');\n");
            js.append("  var __origHtml = __btn ? __btn.innerHTML : '';\n");
            js.append("  if (__btn) {\n");
            js.append("    __btn.disabled = true;\n");
            String loadTxt = (loadingButtonText != null && !loadingButtonText.isBlank()) ? loadingButtonText : "Procesando...";
            js.append("    __btn.innerHTML = '<i class=\"fas fa-spinner fa-spin\"></i> ").append(loadTxt).append("';\n");
            js.append("  }\n");
        }

        // Helper to restore button
        js.append("  function __restoreBtn() {\n");
        if (loadingButtonId != null && !loadingButtonId.isBlank()) {
            js.append("    if (__btn) {\n");
            js.append("      __btn.disabled = false;\n");
            if (originalButtonHtml != null && !originalButtonHtml.isBlank()) {
                js.append("      __btn.innerHTML = '").append(originalButtonHtml.replace("'", "\\'")).append("';\n");
            } else {
                js.append("      __btn.innerHTML = __origHtml;\n");
            }
            js.append("    }\n");
        }
        js.append("  }\n");

        // Helper to show notification
        js.append("  function __notify(title, msg, type) {\n");
        if (notificationId != null && !notificationId.isBlank()) {
            js.append("    if (window.JettraFluxNotification) {\n");
            js.append("      JettraFluxNotification.show('").append(notificationId).append("', title, msg, type);\n");
            js.append("    } else {\n");
            js.append("      console.log('[' + type + '] ' + title + ': ' + msg);\n");
            js.append("    }\n");
        } else {
            js.append("    console.log('[' + type + '] ' + title + ': ' + msg);\n");
        }
        js.append("  }\n");

        // 2. Prepare payload & URL
        js.append("  var __url = '").append(url).append("';\n");
        if (formId != null && !formId.isBlank()) {
            js.append("  var __form = document.getElementById('").append(formId).append("');\n");
            js.append("  if (__form && __form.action && !__url) { __url = __form.action; }\n");
            js.append("  if (!__url) { __url = window.location.href; }\n");

            if (contentType == ContentType.APPLICATION_JSON) {
                // Serialize active form section to JSON object
                js.append("  var __payloadObj = {};\n");
                js.append("  if (__form) {\n");
                js.append("    var __formData = new FormData(__form);\n");
                js.append("    __formData.forEach(function(v, k) { __payloadObj[k] = v; });\n");
                js.append("  }\n");
                js.append("  if (customPayload && typeof customPayload === 'object') {\n");
                js.append("    for (var __k in customPayload) { if (customPayload.hasOwnProperty(__k)) { __payloadObj[__k] = customPayload[__k]; } }\n");
                js.append("  }\n");
                js.append("  var __body = JSON.stringify(__payloadObj);\n");
            } else {
                // Form URL-encoded
                js.append("  var __params = new URLSearchParams();\n");
                js.append("  if (__form) {\n");
                js.append("    var __formData = new FormData(__form);\n");
                js.append("    __formData.forEach(function(v, k) { __params.append(k, v); });\n");
                js.append("  }\n");
                js.append("  if (customPayload && typeof customPayload === 'object') {\n");
                js.append("    for (var __k in customPayload) { if (customPayload.hasOwnProperty(__k)) { __params.append(__k, customPayload[__k]); } }\n");
                js.append("  }\n");
                js.append("  var __body = __params.toString();\n");
            }
        } else if (payloadJson != null && !payloadJson.isBlank()) {
            js.append("  var __body = JSON.stringify(").append(payloadJson).append(");\n");
        } else {
            js.append("  var __body = (typeof customPayload === 'object') ? JSON.stringify(customPayload) : (customPayload || null);\n");
        }

        // 3. Dispatch Fetch Request with strict headers
        js.append("  var __headers = {\n");
        js.append("    'Accept': '").append(acceptHeader != null ? acceptHeader : "application/json").append("',\n");
        js.append("    'Content-Type': '").append(contentType.getHeaderValue()).append("',\n");
        js.append("    'X-Requested-With': 'XMLHttpRequest'\n");
        for (Map.Entry<String, String> header : customHeaders.entrySet()) {
            js.append("    ,'").append(header.getKey()).append("': '").append(header.getValue().replace("'", "\\'")).append("'\n");
        }
        js.append("  };\n");

        js.append("  fetch(__url, {\n");
        js.append("    method: '").append(method).append("',\n");
        js.append("    headers: __headers,\n");
        if (!"GET".equalsIgnoreCase(method) && !"HEAD".equalsIgnoreCase(method)) {
            js.append("    body: __body\n");
        }
        js.append("  })\n");

        // 4. Resilient Response Processing (Safe JSON & HTML Error Interceptor)
        js.append("  .then(function(response) {\n");
        js.append("    var contentType = response.headers.get('content-type') || '';\n");
        js.append("    var isJson = contentType.indexOf('application/json') !== -1;\n");
        js.append("    if (response.ok) {\n");
        js.append("      if (isJson) { return response.json(); }\n");
        js.append("      return response.text().then(function(t) { return { status: 'SUCCESS', message: t }; });\n");
        js.append("    } else {\n");
        // Non-200 Error Handler: Intercept 4xx/5xx
        js.append("      if (isJson) {\n");
        js.append("        return response.json().then(function(errObj) {\n");
        js.append("          errObj.__httpStatus = response.status;\n");
        js.append("          throw errObj;\n");
        js.append("        });\n");
        js.append("      } else {\n");
        // Response is HTML or text (e.g. <h1>400 Bad Request</h1>)
        js.append("        return response.text().then(function(htmlText) {\n");
        js.append("          var cleanMsg = htmlText.replace(/<[^>]*>/g, ' ').replace(/\\s+/g, ' ').trim();\n");
        js.append("          throw {\n");
        js.append("            status: 'ERROR',\n");
        js.append("            __httpStatus: response.status,\n");
        js.append("            error: response.statusText || 'Bad Request',\n");
        js.append("            message: cleanMsg || ('Error HTTP ' + response.status + ': ' + response.statusText)\n");
        js.append("          };\n");
        js.append("        });\n");
        js.append("      }\n");
        js.append("    }\n");
        js.append("  })\n");

        // 5. Success Handler
        js.append("  .then(function(data) {\n");
        js.append("    __restoreBtn();\n");
        js.append("    if (data.status === 'ERROR') {\n");
        js.append("      var errMsg = data.message || 'Error en la operación.';\n");
        js.append("      if (data.errors && data.errors.length) { errMsg += ' (' + data.errors.join(', ') + ')'; }\n");
        js.append("      __notify('Error al Procesar', errMsg, 'ERROR');\n");
        if (onErrorJs != null && !onErrorJs.isBlank()) {
            js.append("      (").append(onErrorJs).append(")(data);\n");
        }
        js.append("      return;\n");
        js.append("    }\n");

        // Operational Success
        js.append("    var succMsg = data.message || 'Operación completada exitosamente.';\n");
        js.append("    __notify('¡Operación Exitosa!', succMsg, 'SUCCESS');\n");

        if (modalIdToClose != null && !modalIdToClose.isBlank()) {
            js.append("    setTimeout(function() {\n");
            js.append("      if (window.JettraFluxModal) { JettraFluxModal.close('").append(modalIdToClose).append("'); }\n");
            if (redirectUrl != null && !redirectUrl.isBlank()) {
                js.append("      var __dest = '").append(redirectUrl.replace("'", "\\'")).append("';\n");
                js.append("      window.location.href = __dest;\n");
            }
            js.append("    }, ").append(redirectDelayMs).append(");\n");
        } else if (redirectUrl != null && !redirectUrl.isBlank()) {
            js.append("    setTimeout(function() {\n");
            js.append("      window.location.href = '").append(redirectUrl.replace("'", "\\'")).append("';\n");
            js.append("    }, ").append(redirectDelayMs).append(");\n");
        }

        if (onSuccessJs != null && !onSuccessJs.isBlank()) {
            js.append("    (").append(onSuccessJs).append(")(data);\n");
        }
        js.append("  })\n");

        // 6. Resilient Catch Handler
        js.append("  .catch(function(err) {\n");
        js.append("    __restoreBtn();\n");
        js.append("    var errTitle = err.__httpStatus ? ('Error Servidor (' + err.__httpStatus + ')') : 'Fallo de Red / Servidor';\n");
        js.append("    var errMsg = err.message || (typeof err === 'string' ? err : 'Error desconocido al comunicar con el servidor.');\n");
        js.append("    if (err.errors && Array.isArray(err.errors) && err.errors.length) {\n");
        js.append("      errMsg += ' - ' + err.errors.join(' | ');\n");
        js.append("    }\n");
        js.append("    __notify(errTitle, errMsg, 'ERROR');\n");
        if (onErrorJs != null && !onErrorJs.isBlank()) {
            js.append("    try { (").append(onErrorJs).append(")(err); } catch(e) {}\n");
        }
        js.append("  });\n");

        js.append("}\n");
        return js.toString();
    }

    @Override
    public String render(ThemeData theme) {
        return "<script>\n" + renderClientFunction() + "</script>\n";
    }
}
