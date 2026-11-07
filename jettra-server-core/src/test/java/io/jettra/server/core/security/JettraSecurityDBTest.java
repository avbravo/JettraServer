package io.jettra.server.core.security;

import io.jettra.server.core.JettraServer;
import io.jettra.server.core.config.ServerConfig;
import io.jettra.server.core.security.db.JettraSecurityDB;
import io.jettra.server.core.security.entity.JCredential;
import io.jettra.server.core.security.entity.JRole;
import io.jettra.server.core.security.entity.JUser;
import io.jettra.server.core.security.repository.*;
import io.jettra.server.core.security.service.JettraSecurityService;
import io.jettra.test.annotation.DisplayName;
import io.jettra.test.annotation.Test;
import io.jettra.test.core.JettraAssert;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

/**
 * Pruebas unitarias y de integración para JettraSecurityDB en JettraServer.
 * Utiliza 100% JettraTest.
 */
public class JettraSecurityDBTest {

    @Test
    @DisplayName("Inicialización automática de usuario admin, roles y credenciales en JettraSecurityDB")
    public void testMasterRecordsInitialization() throws Exception {
        Path tempDir = Files.createTempDirectory("jettra-sec-test-init");
        try {
            JettraSecurityDB db = new JettraSecurityDB(tempDir.toString());
            JettraSecurityDBInitializer.initializeIfEmpty(db);

            JUserRepository userRepo = new JUserRepositoryImpl(db);
            JCredentialRepository credRepo = new JCredentialRepositoryImpl(db);
            JRoleRepository roleRepo = new JRoleRepositoryImpl(db);

            // Verificar roles
            Optional<JRole> adminRole = roleRepo.findByName("ADMIN");
            JettraAssert.assertTrue(adminRole.isPresent(), "El rol ADMIN debe existir");
            JettraAssert.assertTrue(roleRepo.findByName("USER").isPresent(), "El rol USER debe existir");

            // Verificar usuario admin
            Optional<JUser> adminOpt = userRepo.findByUsername("admin");
            JettraAssert.assertTrue(adminOpt.isPresent(), "El usuario 'admin' debe existir");
            JUser admin = adminOpt.get();
            JettraAssert.assertEquals("admin", admin.username(), "Username debe ser admin");
            JettraAssert.assertTrue(admin.active(), "El usuario admin debe estar activo");
            JettraAssert.assertTrue(admin.jRoles().stream().anyMatch(r -> "ADMIN".equalsIgnoreCase(r.name())), "Debe tener rol ADMIN");

            // Verificar credencial admin
            Optional<JCredential> credOpt = credRepo.findByUsername("admin");
            JettraAssert.assertTrue(credOpt.isPresent(), "La credencial de admin debe existir");
            String expectedHash = JettraSecurityDBInitializer.hashPassword("admin");
            JettraAssert.assertEquals(expectedHash, credOpt.get().passwordHash(), "La contraseña debe estar hasheada con SHA-256");
        } finally {
            deleteDirectory(tempDir.toFile());
        }
    }

    @Test
    @DisplayName("Autenticación y generación de JWT con JettraSecurityService")
    public void testAuthenticationAndJwtGeneration() throws Exception {
        Path tempDir = Files.createTempDirectory("jettra-sec-test-auth");
        try {
            JettraSecurityDB db = new JettraSecurityDB(tempDir.toString());
            JettraSecurityDBInitializer.initializeIfEmpty(db);
            JettraSecurityService service = new JettraSecurityService(db);

            // Autenticación correcta
            Optional<String> tokenOpt = service.authenticate("admin", "admin");
            JettraAssert.assertTrue(tokenOpt.isPresent(), "La autenticación debe ser exitosa");
            String token = tokenOpt.get();
            JettraAssert.assertNotNull(token, "El token no debe ser nulo");
            JettraAssert.assertFalse(token.isBlank(), "El token no debe estar vacío");

            // Validación de token
            Optional<JUser> userOpt = service.validateTokenAndGetUser(token);
            JettraAssert.assertTrue(userOpt.isPresent(), "El token debe resolver al usuario admin");
            JettraAssert.assertEquals("admin", userOpt.get().username(), "El usuario validado debe ser admin");

            // Autenticación fallida con password incorrecto
            Optional<String> badPass = service.authenticate("admin", "bad_password");
            JettraAssert.assertTrue(badPass.isEmpty(), "La autenticación debe fallar con clave errónea");

            // Autenticación con usuario inexistente
            Optional<String> nonExistent = service.authenticate("nonexistent", "secret");
            JettraAssert.assertTrue(nonExistent.isEmpty(), "La autenticación debe fallar para usuario inexistente");
        } finally {
            deleteDirectory(tempDir.toFile());
        }
    }

    @Test
    @DisplayName("Gestión de usuarios, cambio de contraseña y asignación de roles")
    public void testUserManagementAndRoleAssignment() throws Exception {
        Path tempDir = Files.createTempDirectory("jettra-sec-test-mgmt");
        try {
            JettraSecurityDB db = new JettraSecurityDB(tempDir.toString());
            JettraSecurityDBInitializer.initializeIfEmpty(db);
            JettraSecurityService service = new JettraSecurityService(db);

            // Registrar nuevo usuario
            JUser newUser = service.registerUser("developer", "dev123", "dev@jettra.io", "555-1234", Set.of("USER"));
            JettraAssert.assertNotNull(newUser, "El usuario debe haberse registrado");
            JettraAssert.assertEquals("developer", newUser.username(), "Username coincide");

            // Autenticar nuevo usuario
            Optional<String> devToken = service.authenticate("developer", "dev123");
            JettraAssert.assertTrue(devToken.isPresent(), "El nuevo usuario debe autenticarse exitosamente");

            // Agregar rol MANAGER
            service.addRoleToUser("developer", "MANAGER");
            JUser updatedUser = service.findUser("developer").orElseThrow();
            JettraAssert.assertTrue(updatedUser.jRoles().stream().anyMatch(r -> "MANAGER".equalsIgnoreCase(r.name())), "Debe poseer rol MANAGER");

            // Cambiar contraseña
            service.changePassword("developer", "newSecret2026");
            Optional<String> oldLogin = service.authenticate("developer", "dev123");
            JettraAssert.assertTrue(oldLogin.isEmpty(), "La contraseña antigua ya no debe ser válida");

            Optional<String> newLogin = service.authenticate("developer", "newSecret2026");
            JettraAssert.assertTrue(newLogin.isPresent(), "La nueva contraseña debe funcionar");
        } finally {
            deleteDirectory(tempDir.toFile());
        }
    }

    @Test
    @DisplayName("Integración nativa de JettraSecurityDB en instancia JettraServer")
    public void testJettraServerSecurityIntegration() throws Exception {
        Path tempDir = Files.createTempDirectory("jettra-server-sec-test");
        try {
            ServerConfig config = new ServerConfig()
                    .setPort(9199)
                    .setDataDir(tempDir.toString());

            JettraServer server = new JettraServer(config);
            JettraAssert.assertNotNull(server.getSecurityDB(), "JettraServer debe exponer SecurityDB");
            JettraAssert.assertNotNull(server.getSecurityService(), "JettraServer debe exponer SecurityService");

            Optional<JUser> adminOpt = server.getSecurityService().findUser("admin");
            JettraAssert.assertTrue(adminOpt.isPresent(), "JettraServer debe tener inicializado el usuario admin por defecto");
        } finally {
            deleteDirectory(tempDir.toFile());
        }
    }

    private void deleteDirectory(File dir) {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) deleteDirectory(f);
                    else f.delete();
                }
            }
            dir.delete();
        }
    }
}

