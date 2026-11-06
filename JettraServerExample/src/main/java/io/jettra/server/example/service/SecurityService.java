package io.jettra.server.example.service;

import io.jettra.server.example.model.User;
import io.jettra.server.jakarta.cdi.CdiAnnotations.ApplicationScoped;
import io.jettra.server.jakarta.cdi.CdiAnnotations.Inject;
import io.jettra.server.jakarta.persistence.EntityManager;
import io.jettra.server.jakarta.persistence.EntityManagerFactory;
import io.jettra.server.jakarta.transaction.TransactionAnnotations.Transactional;
import java.util.Optional;

/**
 * Servicio de seguridad y autenticación compatible con Jakarta CDI y JPA.
 */
@ApplicationScoped
public class SecurityService {

    @Inject
    private EntityManagerFactory emf;

    public SecurityService() {}

    public SecurityService(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public void initDefaultUsers() {
        EntityManager em = getEntityManager();
        if (em.find(User.class, "admin") == null) {
            em.persist(new User("admin", "admin123", "Administrador Principal", "ADMIN", "admin@jettra.io"));
        }
        if (em.find(User.class, "operador") == null) {
            em.persist(new User("operador", "op1234", "Operador de Planta", "OPERATOR", "operador@jettra.io"));
        }
    }

    @Transactional
    public Optional<User> authenticate(String username, String password) {
        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            return Optional.empty();
        }

        EntityManager em = getEntityManager();
        User user = em.find(User.class, username.trim());
        if (user != null && password.equals(user.getPassword())) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    private EntityManager getEntityManager() {
        if (emf != null) {
            return emf.createEntityManager();
        }
        throw new IllegalStateException("EntityManagerFactory no inicializado");
    }
}

