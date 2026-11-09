package io.jettra.aura.core;

import io.jettra.aura.ajax.AuraAjaxRequestTarget;
import io.jettra.aura.annotation.AuraAction;
import io.jettra.aura.annotation.AuraHeader;
import io.jettra.aura.model.IModel;
import io.jettra.aura.template.AuraMarkupDocument;
import io.jettra.aura.template.AuraTemplateLocator;
import io.jettra.aura.template.AuraTemplateParser;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Representa la página web raíz en JettraAura.
 * Cada página Java está asociada por convención a una plantilla HTML homónima (ej. Index.java ↔ Index.html).
 * Gestiona el ciclo de vida de la petición HTTP, eventos, parámetros, cabeceras y renderizado.
 */
public class AuraPage extends AuraContainer {

    private String pageTitle;
    private final Map<String, String> pageParameters = new LinkedHashMap<>();
    private final List<String> headerStylesheets = new ArrayList<>();
    private final List<String> headerScripts = new ArrayList<>();

    private transient AuraRequest request;
    private transient AuraResponse response;
    private transient AuraSession session;

    public AuraPage() {
        super("page");
        applyHeaderAnnotations();
    }

    public AuraPage(IModel<?> model) {
        super("page", model);
        applyHeaderAnnotations();
    }

    public AuraPage(Map<String, String> parameters) {
        super("page");
        if (parameters != null) this.pageParameters.putAll(parameters);
        applyHeaderAnnotations();
    }

    private void applyHeaderAnnotations() {
        AuraHeader header = getClass().getAnnotation(AuraHeader.class);
        if (header != null) {
            if (!header.title().isBlank()) this.pageTitle = header.title();
            Collections.addAll(this.headerStylesheets, header.stylesheets());
            Collections.addAll(this.headerScripts, header.scripts());
        }
    }

    public String getPageTitle() {
        return pageTitle != null ? pageTitle : getClass().getSimpleName();
    }

    public AuraPage setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
        return this;
    }

    public Map<String, String> getPageParameters() {
        return Collections.unmodifiableMap(pageParameters);
    }

    public String getParameter(String name) {
        return pageParameters.get(name);
    }

    public AuraPage setParameter(String name, String value) {
        if (value != null) {
            pageParameters.put(name, value);
        } else {
            pageParameters.remove(name);
        }
        return this;
    }

    public AuraRequest getRequest() {
        return request;
    }

    public void setRequest(AuraRequest request) {
        this.request = request;
        if (request != null && !request.getParameters().isEmpty()) {
            this.pageParameters.putAll(request.getParameters());
        }
    }

    public AuraResponse getResponse() {
        return response;
    }

    public void setResponse(AuraResponse response) {
        this.response = response;
    }

    public void setResponsePage(Class<? extends AuraPage> pageClass) {
        if (pageClass != null) {
            String path = io.jettra.aura.server.AuraApplication.get() != null
                ? io.jettra.aura.server.AuraApplication.get().getPathForPage(pageClass)
                : null;
            if (path != null && response != null) {
                response.redirect(path);
            }
        }
    }

    public void setResponsePage(AuraPage page) {
        if (page != null) {
            setResponsePage(page.getClass());
        }
    }

    public AuraSession getSession() {
        if (session == null) {
            session = AuraSession.get();
        }
        return session;
    }

    public void setSession(AuraSession session) {
        this.session = session;
    }

    public void addStylesheet(String href) {
        if (href != null && !headerStylesheets.contains(href)) {
            headerStylesheets.add(href);
        }
    }

    public void addScript(String src) {
        if (src != null && !headerScripts.contains(src)) {
            headerScripts.add(src);
        }
    }

    // Feedback Flash Messages
    public void info(String message) {
        getSession().info(message);
    }

    public void success(String message) {
        getSession().success(message);
    }

    public void warn(String message) {
        getSession().warn(message);
    }

    public void error(String message) {
        getSession().error(message);
    }

    /**
     * Renderiza la página completa combinando la plantilla HTML asociada y los componentes Java.
     */
    public String renderPage() {
        autoBindAnnotatedComponents();
        onInitialize();
        onConfigure();
        onBeforeRender();

        // 1. Cargar y parsear la plantilla HTML asociada (ej. Index.html para Index.java)
        String templateHtml = AuraTemplateLocator.loadTemplate(getClass());
        AuraMarkupDocument document = AuraTemplateParser.parse(templateHtml);

        // 2. Renderizar el árbol de nodos combinando marcado y componentes
        String renderedHtml = document.render(this);

        // 3. Inyectar contribuciones de cabecera si existe la etiqueta <head>
        renderedHtml = injectHeaderContributions(renderedHtml);

        // 4. Desacoplar modelos para liberar memoria
        detach();

        return renderedHtml;
    }

    /**
     * Renderiza únicamente el fragmento de contenido de esta página junto con el runtime JS de JettraAura,
     * ideal para incrustarse dentro de páginas Jakarta Faces (.xhtml) o contenedores JettraFlux.
     */
    public String renderSection() {
        autoBindAnnotatedComponents();
        onInitialize();
        onConfigure();
        onBeforeRender();

        String sectionHtml = AuraTemplateLocator.loadSectionTemplate(getClass());
        AuraMarkupDocument document = AuraTemplateParser.parse(sectionHtml);
        String renderedSection = document.render(this);
        detach();

        if (!renderedSection.contains("id=\"jettra-aura-runtime\"")) {
            return renderedSection + "\n" + getAuraClientScript();
        }
        return renderedSection;
    }

    /**
     * Renderiza un único componente dentro del contexto de la plantilla HTML/XHTML de esta página.
     * Utilizado por {@link AuraAjaxRequestTarget} para refrescar contenedores, tablas o diálogos.
     */
    public String renderComponent(AuraComponent component) {
        if (component == null) return "";
        autoBindAnnotatedComponents();
        String templateHtml = AuraTemplateLocator.loadTemplate(getClass());
        AuraMarkupDocument document = AuraTemplateParser.parse(templateHtml);
        StringBuilder sb = new StringBuilder();
        if (document.renderComponent(this, component, sb)) {
            return sb.toString();
        }
        return "";
    }

    /**
     * Invoca métodos anotados con {@link AuraAction} cuyo valor coincida con el targetId o id del componente.
     */
    public boolean invokeAuraAction(String targetId, AuraRequest req, AuraResponse res, AuraAjaxRequestTarget ajaxTarget) {
        if (targetId == null || targetId.isBlank()) return false;
        String simpleId = targetId.contains(":") ? targetId.substring(targetId.lastIndexOf(':') + 1) : targetId;
        boolean invoked = false;

        Class<?> clazz = getClass();
        while (clazz != null && clazz != AuraPage.class && clazz != Object.class) {
            for (Method method : clazz.getDeclaredMethods()) {
                AuraAction action = method.getAnnotation(AuraAction.class);
                if (action != null) {
                    String actionId = action.value().isBlank() ? method.getName() : action.value();
                    if (actionId.equals(targetId) || actionId.equals(simpleId)) {
                        try {
                            method.setAccessible(true);
                            Class<?>[] paramTypes = method.getParameterTypes();
                            Object[] args = new Object[paramTypes.length];
                            for (int i = 0; i < paramTypes.length; i++) {
                                if (AuraAjaxRequestTarget.class.isAssignableFrom(paramTypes[i])) {
                                    args[i] = ajaxTarget;
                                } else if (AuraRequest.class.isAssignableFrom(paramTypes[i])) {
                                    args[i] = req;
                                } else if (AuraResponse.class.isAssignableFrom(paramTypes[i])) {
                                    args[i] = res;
                                } else if (AuraEvent.class.isAssignableFrom(paramTypes[i])) {
                                    AuraComponent src = get(targetId);
                                    args[i] = new AuraEvent(src != null ? src : this, req != null ? req.getParameter("aura:event") : "action", req, res, ajaxTarget);
                                }
                            }
                            method.invoke(this, args);
                            invoked = true;
                        } catch (Exception e) {
                            throw new RuntimeException("Error invocando @AuraAction en " + method.getName(), e);
                        }
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
        return invoked;
    }

    private String injectHeaderContributions(String html) {
        if (!html.contains("</head>")) {
            return html;
        }

        StringBuilder headerInjection = new StringBuilder();

        // Reemplazar o inyectar <title>
        if (pageTitle != null && !pageTitle.isBlank()) {
            if (html.contains("<title>") && html.contains("</title>")) {
                html = html.replaceFirst("<title>.*?</title>", "<title>" + escapeHtml(pageTitle) + "</title>");
            } else {
                headerInjection.append("  <title>").append(escapeHtml(pageTitle)).append("</title>\n");
            }
        }

        // Inyectar hojas de estilo adicionales
        for (String css : headerStylesheets) {
            headerInjection.append("  <link rel=\"stylesheet\" href=\"").append(css).append("\" />\n");
        }

        // Inyectar scripts adicionales
        for (String js : headerScripts) {
            headerInjection.append("  <script src=\"").append(js).append("\"></script>\n");
        }

        // Micro-librería JavaScript de cliente para Ajax en JettraAura
        headerInjection.append(getAuraClientScript());

        return html.replace("</head>", headerInjection + "</head>");
    }

    public static String getAuraClientScript() {
        return """
          <script id="jettra-aura-runtime">
            function auraAjax(componentId, eventType, extraParams, triggerEl) {
              const url = window.location.pathname;
              const formData = new URLSearchParams(extraParams || {});

              let el = triggerEl
                || document.querySelector('[data-aura-path="' + componentId + '"]')
                || document.getElementById('aura-' + componentId.replace(/:/g, '-'));
              let form = el ? el.closest('form') : null;
              if (form) {
                const fd = new FormData(form);
                for (const [k, v] of fd.entries()) {
                  if (!formData.has(k)) {
                    formData.append(k, v);
                  }
                }
              }

              formData.append('aura:ajax', 'true');
              formData.append('aura:target', componentId);
              formData.append('aura:event', eventType || 'click');

              fetch(url, {
                method: 'POST',
                headers: { 'X-Requested-With': 'XMLHttpRequest', 'Content-Type': 'application/x-www-form-urlencoded' },
                body: formData.toString()
              })
              .then(r => r.json())
              .then(data => {
                if (typeof data === 'string') {
                  try { data = JSON.parse(data); } catch (e) {}
                }
                if (!data) return;
                if (data.redirect) {
                  window.location.href = data.redirect;
                  return;
                }
                if (data.updates) {
                  for (const [id, html] of Object.entries(data.updates)) {
                    const targetEl = document.getElementById(id)
                      || document.querySelector('[aura\\\\:id="' + id + '"]')
                      || document.querySelector('[flux\\\\:id="' + id + '"]')
                      || document.querySelector('[data-aura-id="' + id + '"]')
                      || document.querySelector('[data-flux-id="' + id + '"]');
                    if (targetEl) {
                      const temp = document.createElement('div');
                      temp.innerHTML = html.trim();
                      if (temp.firstElementChild) {
                        targetEl.replaceWith(temp.firstElementChild);
                      } else {
                        targetEl.innerHTML = html;
                      }
                    }
                  }
                }
                if (data.scripts) {
                  for (const script of data.scripts) {
                    try { eval(script); } catch (e) { console.error('Aura Script Error:', e); }
                  }
                }
              })
              .catch(err => console.error('Aura AJAX Error:', err));
            }

            const fluxAjax = auraAjax;

            function auraCloseModal(markupId, componentPath) {
              const el = document.getElementById(markupId);
              if (el) el.style.display = 'none';
              if (componentPath) {
                auraAjax(componentPath, 'close');
              }
            }

            const fluxCloseModal = auraCloseModal;

            document.addEventListener('keydown', function(evt) {
              if (evt.key === 'Escape') {
                const openModals = document.querySelectorAll('.aura-modal-backdrop');
                openModals.forEach(function(m) {
                  if (m.style.display !== 'none') {
                    const path = m.getAttribute('data-aura-path');
                    auraCloseModal(m.id, path);
                  }
                });
              }
            });
          </script>
        """;
    }
}

