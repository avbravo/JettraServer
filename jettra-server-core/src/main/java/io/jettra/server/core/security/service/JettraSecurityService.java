package io.jettra.server.core.security.service;

import io.jettra.jwt.JettraJWT;
import io.jettra.server.core.security.db.JettraSecurityDB;
import io.jettra.server.core.security.entity.JCredential;
import io.jettra.server.core.security.entity.JRole;
import io.jettra.server.core.security.entity.JUser;
import io.jettra.server.core.security.repository.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

/**
 * Servicio central de seguridad en JettraServer respaldado por JettraSecurityDB.
 * Gestiona autenticación, emisión y validación de tokens JWT nativos, usuarios, credenciales y roles.
 */
public class JettraSecurityService {

    private JUserRepository userRepository;
    private JCredentialRepository credentialRepository;
    private JRoleRepository roleRepository;
    private JAccreditationRepository accreditationRepository;
    private JettraSecurityDB securityDB;

    private String jwtSecret = "jettra_server_super_secure_jwt_key_2026";
    private long jwtExpirationMs = 86400000L; // 24 horas

    public JettraSecurityService() {
        this(new JettraSecurityDB());
    }

    public JettraSecurityService(JettraSecurityDB db) {
        this.securityDB = db != null ? db : new JettraSecurityDB();
        this.userRepository = new JUserRepositoryImpl(this.securityDB);
        this.credentialRepository = new JCredentialRepositoryImpl(this.securityDB);
        this.roleRepository = new JRoleRepositoryImpl(this.securityDB);
        this.accreditationRepository = new JAccreditationRepositoryImpl(this.securityDB);
    }

    public JettraSecurityService(JUserRepository userRepository, JCredentialRepository credentialRepository, JRoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.credentialRepository = credentialRepository;
        this.roleRepository = roleRepository;
    }

    public JettraSecurityService configureJwt(String secret, long expirationMs) {
        if (secret != null && !secret.isBlank()) {
            this.jwtSecret = secret;
        }
        if (expirationMs > 0) {
            this.jwtExpirationMs = expirationMs;
        }
        return this;
    }

    private void initReposIfNull() {
        if (securityDB == null) {
            securityDB = new JettraSecurityDB();
        }
        if (userRepository == null) userRepository = new JUserRepositoryImpl(securityDB);
        if (credentialRepository == null) credentialRepository = new JCredentialRepositoryImpl(securityDB);
        if (roleRepository == null) roleRepository = new JRoleRepositoryImpl(securityDB);
        if (accreditationRepository == null) accreditationRepository = new JAccreditationRepositoryImpl(securityDB);
    }

    /**
     * Autentica credenciales contra JettraSecurityDB y emite un token JWT.
     */
    public Optional<String> authenticate(String username, String plainPassword) {
        initReposIfNull();
        if (username == null || plainPassword == null || username.isBlank() || plainPassword.isBlank()) {
            return Optional.empty();
        }

        Optional<JCredential> credOpt = credentialRepository.findByUsernamePassword(username, plainPassword);
        if (credOpt.isEmpty()) {
            return Optional.empty();
        }

        JCredential cred = credOpt.get();
        if (cred.active() != null && !cred.active()) {
            return Optional.empty();
        }

        // Actualizar último login
        JCredential updatedCred = new JCredential(
                cred.id(),
                cred.jUser(),
                cred.username(),
                cred.passwordHash(),
                cred.active(),
                Instant.now()
        );
        credentialRepository.save(updatedCred);

        // Obtener usuario y roles
        JUser user = cred.jUser();
        if (user == null || user.id() == null) {
            user = userRepository.findByUsername(username).orElse(null);
        }

        List<String> roleNames = new ArrayList<>();
        if (user != null && user.jRoles() != null) {
            for (JRole r : user.jRoles()) {
                if (r != null && r.name() != null) {
                    roleNames.add(r.name());
                }
            }
        }
        if (roleNames.isEmpty() && "admin".equalsIgnoreCase(username)) {
            roleNames.add("ADMIN");
        }

        JettraJWT jwt = new JettraJWT(jwtSecret, jwtExpirationMs);
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roleNames);
        claims.put("groups", roleNames);
        if (user != null && user.email() != null) {
            claims.put("email", user.email());
        }

        String rawToken = jwt.generateToken(claims, username);
        return Optional.of(rawToken);
    }

    /**
     * Valida un token JWT y retorna el usuario autenticado.
     */
    public Optional<JUser> validateTokenAndGetUser(String rawToken) {
        initReposIfNull();
        if (rawToken == null || rawToken.isBlank()) {
            return Optional.empty();
        }
        if (rawToken.startsWith("Bearer ")) {
            rawToken = rawToken.substring(7);
        }

        try {
            JettraJWT jwt = new JettraJWT(jwtSecret, jwtExpirationMs);
            if (!jwt.validateToken(rawToken)) {
                return Optional.empty();
            }
            String username = jwt.extractUsername(rawToken);
            if (username != null && !username.isBlank()) {
                return userRepository.findByUsername(username);
            }
        } catch (Exception ignored) {}

        return Optional.empty();
    }

    /**
     * Crea un nuevo usuario y su credencial asociada.
     */
    public JUser registerUser(String username, String plainPassword, String email, String phone, Set<String> roleNames) {
        initReposIfNull();
        if (username == null || username.isBlank() || plainPassword == null || plainPassword.isBlank()) {
            throw new IllegalArgumentException("Nombre de usuario y contraseña son requeridos.");
        }

        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalStateException("El usuario '" + username + "' ya se encuentra registrado.");
        }

        Set<JRole> roles = new HashSet<>();
        if (roleNames != null && !roleNames.isEmpty()) {
            for (String rn : roleNames) {
                JRole role = roleRepository.findByName(rn).orElseGet(() -> {
                    JRole nr = new JRole(UUID.nameUUIDFromBytes(rn.getBytes(StandardCharsets.UTF_8)), rn, true);
                    roleRepository.save(nr);
                    return nr;
                });
                roles.add(role);
            }
        } else {
            roleRepository.findByName("USER").ifPresent(roles::add);
        }

        UUID userId = UUID.randomUUID();
        JUser user = new JUser(
                userId,
                username,
                "*",
                email != null ? email : username + "@jettra.local",
                phone != null ? phone : "",
                true,
                roles,
                Set.of("*")
        );
        userRepository.save(user);

        String hash = JettraSecurityDBInitializer.hashPassword(plainPassword);
        JCredential credential = new JCredential(
                UUID.randomUUID(),
                user,
                username,
                hash,
                true,
                Instant.now()
        );
        credentialRepository.save(credential);

        return user;
    }

    public List<JUser> listUsers() {
        initReposIfNull();
        return userRepository.findAll();
    }

    public Optional<JUser> findUser(String username) {
        initReposIfNull();
        return userRepository.findByUsername(username);
    }

    public void deleteUser(String username) {
        initReposIfNull();
        Optional<JUser> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            userRepository.delete(userOpt.get().id());
            Optional<JCredential> credOpt = credentialRepository.findByUsername(username);
            credOpt.ifPresent(jCredential -> credentialRepository.delete(jCredential.id()));
        }
    }

    public void changePassword(String username, String newPlainPassword) {
        initReposIfNull();
        if (username == null || newPlainPassword == null || newPlainPassword.isBlank()) {
            throw new IllegalArgumentException("Usuario y nueva contraseña no pueden estar vacíos.");
        }
        Optional<JCredential> credOpt = credentialRepository.findByUsername(username);
        if (credOpt.isEmpty()) {
            throw new NoSuchElementException("Credencial para usuario '" + username + "' no encontrada.");
        }
        JCredential cred = credOpt.get();
        String hash = JettraSecurityDBInitializer.hashPassword(newPlainPassword);
        JCredential updated = new JCredential(
                cred.id(),
                cred.jUser(),
                cred.username(),
                hash,
                cred.active(),
                cred.lastLogin()
        );
        credentialRepository.save(updated);
    }

    public void setUserStatus(String username, boolean active) {
        initReposIfNull();
        Optional<JUser> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new NoSuchElementException("Usuario '" + username + "' no encontrado.");
        }
        userRepository.updateUser(username, new UserUpdateCommand(null, null, active, null, null));
        Optional<JCredential> credOpt = credentialRepository.findByUsername(username);
        if (credOpt.isPresent()) {
            JCredential c = credOpt.get();
            credentialRepository.save(new JCredential(c.id(), c.jUser(), c.username(), c.passwordHash(), active, c.lastLogin()));
        }
    }

    public void addRoleToUser(String username, String roleName) {
        initReposIfNull();
        Optional<JUser> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new NoSuchElementException("Usuario '" + username + "' no encontrado.");
        }
        String cleanRole = roleName.toUpperCase().trim();
        JRole role = roleRepository.findByName(cleanRole).orElseGet(() -> {
            JRole nr = new JRole(UUID.nameUUIDFromBytes(cleanRole.getBytes(StandardCharsets.UTF_8)), cleanRole, true);
            roleRepository.save(nr);
            return nr;
        });
        Set<JRole> roles = new HashSet<>(userOpt.get().jRoles() != null ? userOpt.get().jRoles() : Set.of());
        roles.add(role);
        userRepository.updateUser(username, new UserUpdateCommand(null, null, null, roles, null));
    }

    public void removeRoleFromUser(String username, String roleName) {
        initReposIfNull();
        Optional<JUser> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new NoSuchElementException("Usuario '" + username + "' no encontrado.");
        }
        Set<JRole> roles = new HashSet<>();
        if (userOpt.get().jRoles() != null) {
            for (JRole r : userOpt.get().jRoles()) {
                if (r != null && !roleName.equalsIgnoreCase(r.name())) {
                    roles.add(r);
                }
            }
        }
        userRepository.updateUser(username, new UserUpdateCommand(null, null, null, roles, null));
    }

    public void assignDatabase(String username, String database) {
        initReposIfNull();
        Optional<JUser> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new NoSuchElementException("Usuario '" + username + "' no encontrado.");
        }
        Set<String> dbs = new HashSet<>(userOpt.get().assignedDatabases() != null ? userOpt.get().assignedDatabases() : Set.of());
        dbs.add(database.trim());
        userRepository.updateUser(username, new UserUpdateCommand(null, null, null, null, dbs));
    }

    public void revokeDatabase(String username, String database) {
        initReposIfNull();
        Optional<JUser> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new NoSuchElementException("Usuario '" + username + "' no encontrado.");
        }
        Set<String> dbs = new HashSet<>();
        if (userOpt.get().assignedDatabases() != null) {
            for (String db : userOpt.get().assignedDatabases()) {
                if (!database.equalsIgnoreCase(db)) {
                    dbs.add(db);
                }
            }
        }
        userRepository.updateUser(username, new UserUpdateCommand(null, null, null, null, dbs));
    }

    public List<JRole> listRoles() {
        initReposIfNull();
        return roleRepository.findAll();
    }

    public JRole createRole(String roleName) {
        initReposIfNull();
        String name = roleName.toUpperCase().trim();
        Optional<JRole> existing = roleRepository.findByName(name);
        if (existing.isPresent()) return existing.get();
        JRole newRole = new JRole(UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8)), name, true);
        roleRepository.save(newRole);
        return newRole;
    }

    public JCredentialRepository getCredentialRepository() {
        initReposIfNull();
        return credentialRepository;
    }

    public JUserRepository getUserRepository() {
        initReposIfNull();
        return userRepository;
    }

    public JRoleRepository getRoleRepository() {
        initReposIfNull();
        return roleRepository;
    }

    public JAccreditationRepository getAccreditationRepository() {
        initReposIfNull();
        return accreditationRepository;
    }

    public JettraSecurityDB getSecurityDB() {
        initReposIfNull();
        return securityDB;
    }

    public String getJwtSecret() {
        return jwtSecret;
    }

    public long getJwtExpirationMs() {
        return jwtExpirationMs;
    }
}

