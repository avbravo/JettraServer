package io.jettra.server.core.security.repository;

import io.jettra.server.core.security.db.JettraSecurityDB;
import io.jettra.server.core.security.entity.JAccreditation;
import io.jettra.server.core.security.entity.JCredential;
import io.jettra.server.core.security.entity.JRole;
import io.jettra.server.core.security.entity.JUser;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

/**
 * Inicializador y sincronizador del esquema de seguridad para JettraSecurityDB en JettraServer.
 * Verifica y crea los roles por defecto (ADMIN, MANAGER, USER, DEMO),
 * el usuario inicial 'admin' con rol ADMIN y sus credenciales con contraseña encriptada 'admin'.
 */
public class JettraSecurityDBInitializer {

    public static synchronized void initializeIfEmpty() {
        initializeIfEmpty(new JettraSecurityDB());
    }

    public static synchronized void initializeIfEmpty(String baseDir) {
        initializeIfEmpty(new JettraSecurityDB(baseDir));
    }

    public static synchronized void initializeIfEmpty(JettraSecurityDB db) {
        JRoleRepository roleRepo = new JRoleRepositoryImpl(db);
        JUserRepository userRepo = new JUserRepositoryImpl(db);
        JCredentialRepository credRepo = new JCredentialRepositoryImpl(db);
        JAccreditationRepository accRepo = new JAccreditationRepositoryImpl(db);

        System.out.println("[JettraSecurityDB] Verificando registros maestros de seguridad (JUsers, JRole, JCredential)...");

        // 1. Crear Roles si faltan (ADMIN, MANAGER, USER, DEMO)
        Map<String, JRole> defaultRolesMap = new HashMap<>();
        String[] defaultRoleNames = {"ADMIN", "MANAGER", "USER", "DEMO"};
        List<JRole> existingRoles = roleRepo.findAll();

        for (String roleName : defaultRoleNames) {
            Optional<JRole> foundRole = existingRoles.stream()
                    .filter(r -> r.name() != null && r.name().equalsIgnoreCase(roleName))
                    .findFirst();

            if (foundRole.isPresent()) {
                defaultRolesMap.put(roleName, foundRole.get());
            } else {
                JRole newRole = new JRole(UUID.nameUUIDFromBytes(roleName.getBytes(StandardCharsets.UTF_8)), roleName, true);
                roleRepo.save(newRole);
                defaultRolesMap.put(roleName, newRole);
                System.out.println("[JettraSecurityDB] Creado JRole por defecto: " + roleName);
            }
        }

        // 2. Crear Usuario admin si no existe
        List<JUser> users = userRepo.findAll();
        JUser adminUser = users.stream()
                .filter(u -> "admin".equalsIgnoreCase(u.firstName()) || (u.email() != null && u.email().startsWith("admin")))
                .findFirst()
                .orElse(null);

        if (adminUser == null) {
            Set<JRole> adminRoles = new HashSet<>();
            adminRoles.add(defaultRolesMap.get("ADMIN"));

            adminUser = new JUser(
                    UUID.nameUUIDFromBytes("admin".getBytes(StandardCharsets.UTF_8)),
                    "admin",
                    "admin",
                    "admin@jettra.io",
                    "1234567890",
                    true,
                    adminRoles,
                    Set.of("*")
            );
            userRepo.save(adminUser);
            System.out.println("[JettraSecurityDB] Creado JUser maestro: admin");
        }

        // 3. Crear Credencial para usuario admin (password: 'admin' encriptada SHA-256)
        List<JCredential> credentials = credRepo.findAll();
        boolean adminCredExists = credentials.stream()
                .anyMatch(c -> "admin".equalsIgnoreCase(c.username()));

        if (!adminCredExists) {
            String encryptedPassword = hashPassword("admin");
            JCredential adminCredential = new JCredential(
                    UUID.nameUUIDFromBytes("admin-cred".getBytes(StandardCharsets.UTF_8)),
                    adminUser,
                    "admin",
                    encryptedPassword,
                    true,
                    Instant.now()
            );
            credRepo.save(adminCredential);
            System.out.println("[JettraSecurityDB] Creado JCredential para usuario 'admin' (password: admin)");
        }

        // 4. Crear Acreditaciones por defecto
        List<JAccreditation> existingAccs = accRepo.findAll();
        for (String roleName : defaultRoleNames) {
            JRole roleObj = defaultRolesMap.get(roleName);
            if (roleObj != null) {
                boolean accExists = existingAccs.stream()
                        .anyMatch(a -> a.jRole() != null && roleObj.id().equals(a.jRole().id()));

                if (!accExists) {
                    JAccreditation acc = new JAccreditation(
                            UUID.nameUUIDFromBytes(("acc-" + roleName).getBytes(StandardCharsets.UTF_8)),
                            roleObj,
                            roleName + "_FEATURE",
                            true
                    );
                    accRepo.save(acc);
                }
            }
        }

        System.out.println("[JettraSecurityDB] Seguridad inicializada y sincronizada exitosamente en: " + db.getBaseDir());
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Error hashing password", ex);
        }
    }
}

