package io.jettra.server.example.model;

import io.jettra.server.jakarta.persistence.PersistenceAnnotations.Entity;
import io.jettra.server.jakarta.persistence.PersistenceAnnotations.Id;
import io.jettra.server.jakarta.persistence.PersistenceAnnotations.Table;
import io.jettra.server.jakarta.validation.ValidationEngine.NotNull;
import io.jettra.server.jakarta.validation.ValidationEngine.Size;
import java.io.Serializable;

/**
 * Entidad de Usuario para autenticación y control de acceso.
 */
@Entity
@Table(name = "usuarios")
public class User implements Serializable {

    @Id
    @NotNull(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 30, message = "El usuario debe tener entre 3 y 30 caracteres")
    private String username;

    @NotNull(message = "La contraseña es obligatoria")
    @Size(min = 4, max = 64, message = "La contraseña debe tener al menos 4 caracteres")
    private String password;

    private String fullName;
    private String role;
    private String email;

    public User() {}

    public User(String username, String password, String fullName, String role, String email) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.email = email;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
