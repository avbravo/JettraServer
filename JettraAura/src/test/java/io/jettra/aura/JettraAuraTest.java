package io.jettra.aura;

import io.jettra.aura.ajax.AuraAjaxButton;
import io.jettra.aura.ajax.AuraAjaxRequestTarget;
import io.jettra.aura.components.AuraLabel;
import io.jettra.aura.core.AuraRequest;
import io.jettra.aura.core.AuraResponse;
import io.jettra.aura.model.CompoundPropertyModel;
import io.jettra.aura.model.IModel;
import io.jettra.aura.model.LambdaModel;
import io.jettra.aura.model.LoadableDetachableModel;
import io.jettra.aura.model.Model;
import io.jettra.aura.model.PropertyModel;
import io.jettra.aura.server.AuraApplication;
import io.jettra.aura.server.AuraModule;
import io.jettra.aura.template.AuraMarkupDocument;
import io.jettra.aura.template.AuraTemplateParser;
import io.jettra.server.core.JettraServer;
import io.jettra.server.core.config.ServerConfig;
import io.jettra.test.annotation.DisplayName;
import io.jettra.test.annotation.Test;
import io.jettra.test.core.JettraAssert;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Suite de pruebas unitarias e integradas para el framework JettraAura.
 * 100% validado con JettraTest y libre de dependencias JUnit.
 */
public class JettraAuraTest {

    public static class Person {
        private String name;
        private int age;

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
    }

    @Test
    @DisplayName("1. Verificar funcionamiento de Modelos (PropertyModel y CompoundPropertyModel)")
    public void testModelBinding() {
        Person p = new Person("Carlos", 35);

        // PropertyModel
        PropertyModel<String> nameModel = PropertyModel.of(p, "name");
        JettraAssert.assertEquals("Carlos", nameModel.getObject(), "Debe leer getter");
        nameModel.setObject("Maria");
        JettraAssert.assertEquals("Maria", p.getName(), "Debe actualizar setter");

        PropertyModel<Integer> ageModel = PropertyModel.of(p, "age");
        JettraAssert.assertEquals(35, ageModel.getObject(), "Debe leer edad");
        ageModel.setObject(40);
        JettraAssert.assertEquals(40, p.getAge(), "Debe actualizar edad");

        // CompoundPropertyModel
        CompoundPropertyModel<Person> cpm = CompoundPropertyModel.of(p);
        IModel<String> boundName = cpm.bind("name");
        JettraAssert.assertEquals("Maria", boundName.getObject(), "Compound debe vincular name");
        boundName.setObject("Sofia");
        JettraAssert.assertEquals("Sofia", p.getName(), "Compound debe actualizar target");
    }

    @Test
    @DisplayName("2. Verificar LambdaModel y LoadableDetachableModel")
    public void testLambdaAndDetachableModels() {
        int[] counter = new int[]{10};
        LambdaModel<Integer> lm = LambdaModel.of(() -> counter[0], v -> counter[0] = v);
        JettraAssert.assertEquals(10, lm.getObject(), "Lambda debe leer valor");
        lm.setObject(25);
        JettraAssert.assertEquals(25, counter[0], "Lambda debe mutar valor");

        LoadableDetachableModel<String> ldm = new LoadableDetachableModel<>() {
            @Override
            protected String load() {
                return "Cargado Perezosamente";
            }
        };

        JettraAssert.assertFalse(ldm.isAttached(), "Antes de getObject no debe estar attached");
        JettraAssert.assertEquals("Cargado Perezosamente", ldm.getObject(), "Debe cargar bajo demanda");
        JettraAssert.assertTrue(ldm.isAttached(), "Debe estar attached");
        ldm.detach();
        JettraAssert.assertFalse(ldm.isAttached(), "Después de detach debe quedar liberado");
    }

    @Test
    @DisplayName("3. Verificar Analizador Sintáctico de Plantillas (AuraTemplateParser)")
    public void testTemplateParser() {
        String html = """
            <div class="container">
                <h1 aura:id="title">Texto original</h1>
                <p j:id="desc">Descripción</p>
                <input aura:id="age" type="number" />
            </div>
            """;

        AuraMarkupDocument doc = AuraTemplateParser.parse(html);
        JettraAssert.assertTrue(!doc.getRootNodes().isEmpty(), "El documento debe contener nodos parseados");
    }

    @Test
    @DisplayName("4. Verificar Asociación 1:1 entre TestIndex.java y TestIndex.html")
    public void testPageRenderingAndBinding() {
        TestIndex page = new TestIndex();
        String html = page.renderPage();

        // 1. Verificar título inyectado en <head>
        JettraAssert.assertTrue(html.contains("<title>Inicio - JettraAura Test</title>"), "Debe tener título");

        // 2. Verificar que se reemplazó la maqueta por el valor del AuraLabel
        JettraAssert.assertTrue(html.contains("¡Bienvenido a JettraAura!"), "Debe contener el texto del label");
        JettraAssert.assertFalse(html.contains("Texto de Maqueta Reemplazable"), "No debe contener la maqueta");

        // 3. Verificar que se renderizaron las filas del ListView
        JettraAssert.assertTrue(html.contains("MicroProfile"), "Debe listar MicroProfile");
        JettraAssert.assertTrue(html.contains("JakartaEE"), "Debe listar JakartaEE");
        JettraAssert.assertTrue(html.contains("Project Loom"), "Debe listar Project Loom");

        // 4. Verificar input del formulario vinculado con CompoundPropertyModel
        JettraAssert.assertTrue(html.contains("value=\"defaultUser\""), "Debe tener el valor por defecto del modelo");
    }

    @Test
    @DisplayName("5. Verificar procesamiento de envío de formulario POST")
    public void testFormSubmission() {
        TestIndex page = new TestIndex();

        AuraRequest req = new AuraRequest("POST", "/test-index");
        req.setParameter("username", "Roberto");

        AuraResponse res = new AuraResponse();
        page.setRequest(req);
        page.setResponse(res);

        // Procesar submit
        io.jettra.aura.components.AuraForm<?> form = (io.jettra.aura.components.AuraForm<?>) page.get("userForm");
        form.processSubmit(req);

        JettraAssert.assertTrue(page.isFormSubmitted(), "El formulario debió ejecutar onSubmit");
        JettraAssert.assertEquals("Roberto", page.getUser().getUsername(), "El modelo debió actualizarse a Roberto");

        // Renderizar página tras submit y verificar mensaje de éxito en FeedbackPanel
        String html = page.renderPage();
        JettraAssert.assertTrue(html.contains("Usuario Roberto guardado con éxito"), "FeedbackPanel debe mostrar mensaje");
    }

    @Test
    @DisplayName("6. Verificar procesamiento reactivo de peticiones Ajax")
    public void testAjaxHandling() {
        TestIndex page = new TestIndex();
        AuraLabel label = (AuraLabel) page.get("welcomeMsg");
        label.setOutputMarkupId(true);

        AuraAjaxRequestTarget target = new AuraAjaxRequestTarget(page);
        label.setModelObject("¡Actualizado vía AJAX!");
        target.add(label);
        target.appendJavaScript("console.log('Ajax completado');");

        String json = target.toJson();
        JettraAssert.assertTrue(json.contains("updates"), "JSON debe contener updates");
        JettraAssert.assertTrue(json.contains("¡Actualizado vía AJAX!"), "Debe contener el nuevo contenido HTML");
        JettraAssert.assertTrue(json.contains("scripts"), "JSON debe contener scripts");
        JettraAssert.assertTrue(json.contains("console.log"), "Debe contener el script");
    }

    @Test
    @DisplayName("7. Verificar Integración de JettraAura con JettraServer en puerto HTTP")
    public void testServerIntegration() throws Exception {
        int testPort = 9876;
        ServerConfig config = ServerConfig.load().setPort(testPort);
        JettraServer server = JettraServer.create(config);

        // Acoplar JettraAura al servidor
        AuraModule aura = AuraModule.attach(server);
        aura.page("/test-index", TestIndex.class);
        aura.homePage(TestIndex.class);

        server.start();

        try {
            HttpClient client = HttpClient.newHttpClient();

            // Petición GET a la página montada
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + testPort + "/test-index"))
                .GET()
                .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            JettraAssert.assertEquals(200, res.statusCode(), "Debe responder HTTP 200");
            JettraAssert.assertTrue(res.body().contains("¡Bienvenido a JettraAura!"), "Debe servir la página renderizada");
            JettraAssert.assertTrue(res.body().contains("Project Loom"), "Debe renderizar la lista");

            // Petición a la ruta raíz (homePage)
            HttpRequest reqRoot = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + testPort + "/"))
                .GET()
                .build();
            HttpResponse<String> resRoot = client.send(reqRoot, HttpResponse.BodyHandlers.ofString());
            JettraAssert.assertEquals(200, resRoot.statusCode(), "Root debe responder HTTP 200");
            JettraAssert.assertTrue(resRoot.body().contains("¡Bienvenido a JettraAura!"), "Root debe servir TestIndex");

        } finally {
            server.stop();
        }
    }

    @Test
    @DisplayName("8. Verificar eventos AuraEvent, resolución en AuraListView y AuraModal")
    public void testEventsListViewAndModal() {
        TestIndex page = new TestIndex();

        // 1. Verificar ID determinista de componente
        AuraLabel label = (AuraLabel) page.get("welcomeMsg");
        JettraAssert.assertEquals("aura-welcomeMsg", label.getMarkupId(), "MarkupId debe ser determinista");

        // 2. Verificar registro y disparo de AuraEventListener
        boolean[] eventTriggered = new boolean[]{false};
        label.onEvent("custom", event -> {
            eventTriggered[0] = true;
            JettraAssert.assertEquals("custom", event.getEventType(), "Debe coincidir el tipo de evento");
        });
        label.fireEvent(new io.jettra.aura.core.AuraEvent(label, "custom", null, null, null));
        JettraAssert.assertTrue(eventTriggered[0], "El listener de evento debió ejecutarse");

        // 3. Verificar resolución de componentes hijos dentro de AuraListView sin render previo
        io.jettra.aura.core.AuraComponent rowItemLabel = page.get("itemList:1:itemName");
        JettraAssert.assertNotNull(rowItemLabel, "AuraListView debe poblar y resolver rutas de filas (itemList:1:itemName)");
        JettraAssert.assertEquals("JakartaEE", rowItemLabel.getModelObject(), "El segundo elemento debe ser JakartaEE");

        // 4. Verificar AuraModal renderizando componentes hijos
        io.jettra.aura.components.AuraModal modal = new io.jettra.aura.components.AuraModal("testModal", "Confirmar Acción");
        modal.add(new AuraLabel("modalMsg", "¿Desea continuar?"));
        modal.show();
        JettraAssert.assertTrue(modal.isOpen(), "El modal debe estar abierto");
        String modalHtml = AuraTemplateParser.parse("<div aura:id=\"testModal\"><span aura:id=\"modalMsg\"></span></div>").render(new io.jettra.aura.core.AuraContainer("root") {{
            add(modal);
        }});
        JettraAssert.assertTrue(modalHtml.contains("Confirmar Acción"), "El modal debe renderizar el título");
        JettraAssert.assertTrue(modalHtml.contains("¿Desea continuar?"), "El modal debe renderizar sus componentes hijos");
        JettraAssert.assertTrue(modalHtml.contains("display: flex;"), "El modal abierto debe tener display: flex");
    }
}

