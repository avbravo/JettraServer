package io.jettra.studio;

import io.jettra.studio.components.*;
import io.jettra.studio.core.Component;
import io.jettra.studio.core.MarkupContainer;
import io.jettra.studio.markup.MarkupTag;
import io.jettra.studio.model.IModel;
import io.jettra.studio.model.Model;
import io.jettra.studio.model.PropertyModel;
import io.jettra.studio.model.LambdaModel;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.jettra.test.core.JettraAssert.*;

@NotRequiresRunningServer
public class ComponentAndModelTest {

    public record Person(String name, String role, int age) {}

    public static class PersonBean {
        private String name;
        private String email;

        public PersonBean(String name, String email) {
            this.name = name;
            this.email = email;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    @Test
    public void testModelImplementations() {
        // Simple Model
        Model<String> m1 = Model.of("Initial");
        assertEquals("Initial", m1.getObject());
        m1.setObject("Updated");
        assertEquals("Updated", m1.getObject());

        // Lambda Model
        StringBuilder buffer = new StringBuilder("LambdaInit");
        IModel<String> lm = LambdaModel.of(buffer::toString, s -> {
            buffer.setLength(0);
            buffer.append(s);
        });
        assertEquals("LambdaInit", lm.getObject());
        lm.setObject("LambdaNew");
        assertEquals("LambdaNew", lm.getObject());

        // PropertyModel with Java 25 Record
        Person p = new Person("Alice", "Admin", 30);
        PropertyModel<String> nameModel = PropertyModel.of(p, "name");
        PropertyModel<String> roleModel = PropertyModel.of(p, "role");
        PropertyModel<Integer> ageModel = PropertyModel.of(p, "age");
        assertEquals("Alice", nameModel.getObject());
        assertEquals("Admin", roleModel.getObject());
        assertEquals(30, ageModel.getObject());

        // PropertyModel with Java Bean read/write
        PersonBean bean = new PersonBean("Bob", "bob@example.com");
        PropertyModel<String> beanEmailModel = PropertyModel.of(bean, "email");
        assertEquals("bob@example.com", beanEmailModel.getObject());
        beanEmailModel.setObject("newbob@example.com");
        assertEquals("newbob@example.com", bean.getEmail());
    }

    @Test
    public void testLabelComponentRendering() {
        Label lbl = new Label("helloMessage", "Hello JettraStudio!");
        MarkupTag tag = new MarkupTag("div", "helloMessage");
        tag.setBodyContent("[Old placeholder]");
        StringBuilder sb = new StringBuilder();
        lbl.render(tag, sb);

        String html = sb.toString();
        assertTrue(html.contains("<div"));
        assertTrue(html.contains("Hello JettraStudio!"));
        assertFalse(html.contains("[Old placeholder]"));
        assertFalse(html.contains("jettrat:id"));
    }

    @Test
    public void testMultiLineLabel() {
        MultiLineLabel lbl = new MultiLineLabel("multi", "Line1\nLine2\r\nLine3");
        MarkupTag tag = new MarkupTag("p", "multi");
        StringBuilder sb = new StringBuilder();
        lbl.render(tag, sb);

        String html = sb.toString();
        assertTrue(html.contains("Line1<br/>Line2<br/>Line3"));
    }

    @Test
    public void testButtonAndClickAction() {
        AtomicBoolean clicked = new AtomicBoolean(false);
        Button btn = Button.of("saveBtn", "Guardar Datos", () -> clicked.set(true))
            .variant(Button.Variant.GOLD);

        MarkupTag tag = new MarkupTag("button", "saveBtn");
        StringBuilder sb = new StringBuilder();
        btn.render(tag, sb);

        String html = sb.toString();
        assertTrue(html.contains("games-btn-gold"));
        assertTrue(html.contains("Guardar Datos"));

        // Trigger action
        btn.onClick();
        assertTrue(clicked.get(), "Button click action should be executed");
    }

    @Test
    public void testTextFieldAndTextArea() {
        TextField<String> tf = new TextField<>("username", Model.of("admin")).placeholder("Ingrese usuario");
        MarkupTag tfTag = new MarkupTag("input", "username");
        tfTag.setSelfClosing(true);
        StringBuilder sbTf = new StringBuilder();
        tf.render(tfTag, sbTf);

        String tfHtml = sbTf.toString();
        assertTrue(tfHtml.contains("value=\"admin\""));
        assertTrue(tfHtml.contains("placeholder=\"Ingrese usuario\""));
        assertTrue(tfHtml.contains("espresso-textfield"));

        TextArea<String> ta = new TextArea<>("notes", Model.of("Notas del sistema")).rows(5);
        MarkupTag taTag = new MarkupTag("textarea", "notes");
        StringBuilder sbTa = new StringBuilder();
        ta.render(taTag, sbTa);

        String taHtml = sbTa.toString();
        assertTrue(taHtml.contains("rows=\"5\""));
        assertTrue(taHtml.contains("Notas del sistema"));
    }

    @Test
    public void testCheckBoxAndSelect() {
        CheckBox cb = new CheckBox("agree", Model.of(true));
        MarkupTag cbTag = new MarkupTag("input", "agree");
        cbTag.setSelfClosing(true);
        StringBuilder sbCb = new StringBuilder();
        cb.render(cbTag, sbCb);
        assertTrue(sbCb.toString().contains("checked=\"checked\""));

        Select<String> sel = new Select<>("roles", Model.of("ADMIN"), List.of("USER", "ADMIN", "GUEST"));
        MarkupTag selTag = new MarkupTag("select", "roles");
        StringBuilder sbSel = new StringBuilder();
        sel.render(selTag, sbSel);

        String selHtml = sbSel.toString();
        assertTrue(selHtml.contains("<option value=\"ADMIN\" selected=\"selected\">ADMIN</option>"));
        assertTrue(selHtml.contains("<option value=\"USER\">USER</option>"));
    }

    @Test
    public void testCardAlertAndModal() {
        Card card = Card.of("dashCard", "Métricas del Servidor").subtitle("Clúster Principal");
        MarkupTag cardTag = new MarkupTag("div", "dashCard");
        cardTag.setBodyContent("<span>CPU: 12%</span>");
        StringBuilder sbCard = new StringBuilder();
        card.render(cardTag, sbCard);

        String cardHtml = sbCard.toString();
        assertTrue(cardHtml.contains("Métricas del Servidor"));
        assertTrue(cardHtml.contains("Clúster Principal"));
        assertTrue(cardHtml.contains("CPU: 12%"));

        Alert alert = Alert.success("alertBox", "Operación exitosa");
        MarkupTag alertTag = new MarkupTag("div", "alertBox");
        StringBuilder sbAlert = new StringBuilder();
        alert.render(alertTag, sbAlert);
        assertTrue(sbAlert.toString().contains("espresso-alert-success"));
        assertTrue(sbAlert.toString().contains("Operación exitosa"));

        Modal modal = Modal.of("userModal", "Editar Usuario").open();
        MarkupTag modalTag = new MarkupTag("div", "userModal");
        modalTag.setBodyContent("<p>Formulario de usuario</p>");
        StringBuilder sbModal = new StringBuilder();
        modal.render(modalTag, sbModal);
        assertTrue(sbModal.toString().contains("Editar Usuario"));
        assertTrue(sbModal.toString().contains("games-modal"));
    }
}
