package io.jettra.server.example.bean;

import io.jettra.server.example.model.User;
import io.jettra.server.example.service.SecurityService;
import io.jettra.server.jakarta.cdi.CdiAnnotations.Inject;
import io.jettra.server.jakarta.cdi.CdiAnnotations.Named;
import io.jettra.server.jakarta.cdi.CdiAnnotations.SessionScoped;
import io.jettra.server.jakarta.validation.ValidationEngine;
import io.jettra.server.jakarta.validation.ValidationEngine.NotNull;
import io.jettra.server.jakarta.validation.ValidationEngine.Size;
import java.io.Serializable;
import java.util.Optional;
import java.util.Set;

/**
 * Managed Bean para el control de autenticación y sesión en PrimeFaces.
 */
@Named("loginBean")
@SessionScoped
public class LoginBean implements Serializable {

    @NotNull(message = "El usuario no puede estar vacío")
    @Size(min = 3, max = 30, message = "El usuario debe tener al menos 3 caracteres")
    private String username;

    @NotNull(message = "La contraseña es requerida")
    @Size(min = 4, max = 64, message = "La contraseña debe tener al menos 4 caracteres")
    private String password;

    private String errorMessage;
    private User currentUser;
    private boolean loggedIn = false;

    @Inject
    private SecurityService securityService;

    public LoginBean() {}

    public LoginBean(SecurityService securityService) {
        this.securityService = securityService;
    }

    public boolean validateCredentials() {
        errorMessage = null;

        // Validación declarativa Jakarta Bean Validation
        Set<ValidationEngine.ConstraintViolation> violations = ValidationEngine.validate(this);
        if (!violations.isEmpty()) {
            errorMessage = violations.iterator().next().message();
            return false;
        }

        if (securityService == null) {
            errorMessage = "Servicio de seguridad no disponible";
            return false;
        }

        Optional<User> userOpt = securityService.authenticate(username, password);
        if (userOpt.isPresent()) {
            this.currentUser = userOpt.get();
            this.loggedIn = true;
            this.errorMessage = null;
            return true;
        } else {
            this.errorMessage = "Credenciales inválidas. Compruebe su usuario o contraseña.";
            this.loggedIn = false;
            return false;
        }
    }

    public String login() {
        if (validateCredentials()) {
            return "dashboard.xhtml?faces-redirect=true";
        }
        return null;
    }

    public String logout() {
        this.username = null;
        this.password = null;
        this.currentUser = null;
        this.loggedIn = false;
        this.errorMessage = null;
        return "login.xhtml?faces-redirect=true";
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public User getCurrentUser() { return currentUser; }
    public boolean isLoggedIn() { return loggedIn; }
}

