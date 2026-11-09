package io.jettra.aura;

import io.jettra.aura.annotation.AuraPath;
import io.jettra.aura.components.AuraButton;
import io.jettra.aura.components.AuraFeedbackPanel;
import io.jettra.aura.components.AuraForm;
import io.jettra.aura.components.AuraLabel;
import io.jettra.aura.components.AuraListView;
import io.jettra.aura.components.AuraTextField;
import io.jettra.aura.core.AuraWebPage;
import io.jettra.aura.model.CompoundPropertyModel;
import io.jettra.aura.model.Model;
import java.io.Serializable;
import java.util.List;

@AuraPath("/test-index")
public class TestIndex extends AuraWebPage {

    public static class UserModel implements Serializable {
        private String username = "defaultUser";
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }

    private final UserModel user = new UserModel();
    private boolean formSubmitted = false;

    public TestIndex() {
        setPageTitle("Inicio - JettraAura Test");

        // 1. Label
        add(new AuraLabel("welcomeMsg", "¡Bienvenido a JettraAura!"));

        // 2. Feedback Panel
        add(new AuraFeedbackPanel("feedback"));

        // 3. ListView
        List<String> items = List.of("MicroProfile", "JakartaEE", "Project Loom");
        add(new AuraListView<String>("itemList", items, item -> {
            item.add(new AuraLabel("itemName", item.getModelData()));
        }));

        // 4. Form con CompoundPropertyModel
        AuraForm<UserModel> form = new AuraForm<>("userForm", new CompoundPropertyModel<>(user), () -> {
            formSubmitted = true;
            success("Usuario " + user.getUsername() + " guardado con éxito.");
        });

        form.add(new AuraTextField<>("username"));
        form.add(new AuraButton("btnSave"));
        add(form);
    }

    public UserModel getUser() {
        return user;
    }

    public boolean isFormSubmitted() {
        return formSubmitted;
    }
}

