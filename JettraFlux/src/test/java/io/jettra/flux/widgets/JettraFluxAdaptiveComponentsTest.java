package io.jettra.flux.widgets;

import io.jettra.flux.theme.Themes;
import io.jettra.test.annotation.DisplayName;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.Test;

import static io.jettra.test.core.JettraAssert.*;

/**
 * Unit tests validating the 5 new reactive UI components in JettraFlux:
 * JettraFluxModal, JettraFluxButton, JettraFluxDynamicForm, JettraFluxJsonEditor, JettraFluxNotification.
 */
@NotRequiresRunningServer
public class JettraFluxAdaptiveComponentsTest {

    @Test
    @DisplayName("JettraFluxModal: verify fluent construction, lifecycle, and HTML structure")
    public void testJettraFluxModal() {
        JettraFluxModal modal = JettraFluxModal.of("testModal", "Test Modal Title")
                .subtitle("Modal Subtitle Explanation")
                .icon("fas fa-cogs")
                .badge("ACTIVE", "#10b981")
                .maxWidth("800px")
                .maxHeight("80vh")
                .open(true)
                .closeOnEsc(true)
                .closeOnClickOutside(true)
                .addBody(Span.of("Modal Body Content"))
                .addFooterAction(JettraFluxButton.of("Save", "fas fa-save").submit());

        assertNotNull(modal);
        assertEquals("testModal", modal.getId());

        String html = modal.render(Themes.FlatTheme());
        assertNotNull(html);
        assertTrue(html.contains("id=\"testModal\""));
        assertTrue(html.contains("Test Modal Title"));
        assertTrue(html.contains("Modal Subtitle Explanation"));
        assertTrue(html.contains("ACTIVE"));
        assertTrue(html.contains("Modal Body Content"));
        assertTrue(html.contains("window.JettraFluxModal"));
        assertTrue(html.contains("JettraFluxModal.close('testModal')"));
    }

    @Test
    @DisplayName("JettraFluxButton: verify variants, sizes, types, and click handlers")
    public void testJettraFluxButton() {
        JettraFluxButton primaryBtn = JettraFluxButton.of("Guardar", "fas fa-check")
                .id("btnSave")
                .variant(JettraFluxButton.Variant.PRIMARY)
                .size(JettraFluxButton.Size.MD)
                .submit()
                .badge("NEW")
                .onClickJs("console.log('Saved')");

        String primaryHtml = primaryBtn.render(Themes.FlatTheme());
        assertTrue(primaryHtml.contains("type=\"submit\""));
        assertTrue(primaryHtml.contains("Guardar"));
        assertTrue(primaryHtml.contains("fas fa-check"));
        assertTrue(primaryHtml.contains("NEW"));
        assertTrue(primaryHtml.contains("console.log('Saved')"));

        JettraFluxButton loadingBtn = JettraFluxButton.of("Procesando")
                .loading(true)
                .variant(JettraFluxButton.Variant.DANGER);

        String loadingHtml = loadingBtn.render(Themes.FlatTheme());
        assertTrue(loadingHtml.contains("fa-spinner fa-spin"));
    }

    @Test
    @DisplayName("JettraFluxDynamicForm: verify fields, polymorphic sections, and client script")
    public void testJettraFluxDynamicForm() {
        JettraFluxDynamicForm form = JettraFluxDynamicForm.of("engineForm", "/api/insert")
                .method("POST")
                .onSubmit("handleFormSubmit()")
                .onReset("handleFormReset()")
                .addField(TextField.of("common_name").value("Alpha"))
                .addSection("SECTION_A", Span.of("Content Section A"))
                .addSection("SECTION_B", Span.of("Content Section B"))
                .activeSection("SECTION_A")
                .addAction(JettraFluxButton.of("Submit").submit());

        assertNotNull(form);
        String html = form.render(Themes.FlatTheme());
        assertTrue(html.contains("id=\"engineForm\""));
        assertTrue(html.contains("action=\"/api/insert\""));
        assertTrue(html.contains("method=\"POST\""));
        assertTrue(html.contains("handleFormSubmit()"));
        assertTrue(html.contains("engineForm_section_SECTION_A"));
        assertTrue(html.contains("engineForm_section_SECTION_B"));
        assertTrue(html.contains("window.JettraFluxDynamicForm"));
        assertTrue(html.contains("switchSection: function"));
    }

    @Test
    @DisplayName("JettraFluxJsonEditor: verify validation status badge, formatting tools, and textarea")
    public void testJettraFluxJsonEditor() {
        String json = "{\n  \"status\": \"OK\",\n  \"count\": 10\n}";
        JettraFluxJsonEditor editor = JettraFluxJsonEditor.of("jsonEditor", "Config JSON", json)
                .name("config_payload")
                .height("180px")
                .showToolbar(true);

        assertNotNull(editor);
        String html = editor.render(Themes.FlatTheme());
        assertTrue(html.contains("id=\"jsonEditor_container\""));
        assertTrue(html.contains("Config JSON"));
        assertTrue(html.contains("VALID JSON"));
        assertTrue(html.contains("Prettify"));
        assertTrue(html.contains("Minify"));
        assertTrue(html.contains("window.JettraFluxJsonEditor"));
        assertTrue(html.contains("validate: function"));
        assertTrue(html.contains("format: function"));
    }

    @Test
    @DisplayName("JettraFluxNotification: verify alert types, dismiss button, and client controls")
    public void testJettraFluxNotification() {
        JettraFluxNotification notif = JettraFluxNotification.of("testNotif", "Operation completed successfully.", JettraFluxNotification.Type.SUCCESS)
                .title("Operación Exitosa")
                .dismissible(true)
                .visible(true);

        assertNotNull(notif);
        String html = notif.render(Themes.FlatTheme());
        assertTrue(html.contains("id=\"testNotif\""));
        assertTrue(html.contains("Operación Exitosa"));
        assertTrue(html.contains("Operation completed successfully."));
        assertTrue(html.contains("window.JettraFluxNotification"));
        assertTrue(html.contains("show: function"));
        assertTrue(html.contains("hide: function"));
    }

    @Test
    @DisplayName("JettraFluxSelect: verify fluent options, binding, onChange, and rendering")
    public void testJettraFluxSelect() {
        JettraFluxSelect select = JettraFluxSelect.of("id_mode_select", "id_gen_mode")
                .binding("id_mode")
                .onChange("handleModeChange(this)")
                .addOption("UUID", "UUID v4 (Automático)", true)
                .addOption("SNOWFLAKE", "Snowflake (Distribuido)")
                .addOption("MANUAL", "Manual");

        assertNotNull(select);
        assertEquals(3, select.options().size());
        String html = select.render(Themes.FlatTheme());
        assertTrue(html.contains("id=\"id_mode_select\""));
        assertTrue(html.contains("name=\"id_gen_mode\""));
        assertTrue(html.contains("data-binding=\"id_mode\""));
        assertTrue(html.contains("onchange=\"handleModeChange(this)\""));
        assertTrue(html.contains("value=\"UUID\" selected"));
        assertTrue(html.contains("value=\"MANUAL\""));
    }

    @Test
    @DisplayName("JettraFluxButton: verify form attribute binding and submit action")
    public void testJettraFluxButtonFormBinding() {
        JettraFluxButton btn = JettraFluxButton.of("Insertar Registro", "fas fa-plus-circle")
                .id("btnAdaptiveSubmitInsert")
                .form("adaptiveRecordInsertForm")
                .variant(JettraFluxButton.Variant.PRIMARY)
                .onClickJs("submitAdaptiveRecordInsert()")
                .submit();

        assertNotNull(btn);
        assertEquals("adaptiveRecordInsertForm", btn.form());
        String html = btn.render(Themes.FlatTheme());
        assertTrue(html.contains("id=\"btnAdaptiveSubmitInsert\""));
        assertTrue(html.contains("form=\"adaptiveRecordInsertForm\""));
        assertTrue(html.contains("type=\"submit\""));
        assertTrue(html.contains("submitAdaptiveRecordInsert()"));
        assertTrue(html.contains("fas fa-plus-circle"));
    }

    @Test
    @DisplayName("RadioButton: verify onChange event handler rendering")
    public void testRadioButtonOnChange() {
        RadioButton rb = RadioButton.of("rb_node", "Node (Vertex)")
                .name("graph_mode")
                .value("node")
                .checked(true)
                .onChange("handleGraphModeChange('node')");

        assertNotNull(rb);
        String html = rb.render(Themes.FlatTheme());
        assertTrue(html.contains("name=\"graph_mode\""));
        assertTrue(html.contains("value=\"node\""));
        assertTrue(html.contains("checked=\"checked\""));
        assertTrue(html.contains("onchange=\"handleGraphModeChange('node')\""));
    }
}
