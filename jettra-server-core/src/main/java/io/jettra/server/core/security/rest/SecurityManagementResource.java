package io.jettra.server.core.security.rest;

import io.jettra.rest.annotations.DELETE;
import io.jettra.rest.annotations.GET;
import io.jettra.rest.annotations.POST;
import io.jettra.rest.annotations.Path;
import io.jettra.rest.annotations.accreditation.RolesAllowed;
import io.jettra.server.core.http.HttpRequest;
import io.jettra.server.core.http.HttpResponse;
import io.jettra.server.core.security.entity.JRole;
import io.jettra.server.core.security.entity.JUser;
import io.jettra.server.core.security.service.JettraSecurityService;
import io.jettra.server.openapi.annotations.ApiResponse;
import io.jettra.server.openapi.annotations.Operation;
import io.jettra.server.openapi.annotations.Tag;

import java.util.*;

/**
 * Recurso REST de gestión administrativa para JettraSecurityDB.
 * Permite consultar, registrar, modificar roles/contraseñas y eliminar usuarios de la base de datos de seguridad.
 */
@Path("/api/security")
@Tag(name = "JettraSecurityDB - Usuarios y Roles", description = "Operaciones de administración para usuarios, contraseñas, roles y permisos en la base de datos de seguridad embebida JettraSecurityDB.")
public class SecurityManagementResource {

    private final JettraSecurityService securityService;

    public SecurityManagementResource() {
        this(new JettraSecurityService());
    }

    public SecurityManagementResource(JettraSecurityService securityService) {
        this.securityService = securityService;
    }

    private Optional<JUser> checkAdminPrivileges(HttpRequest req, HttpResponse res) {
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            res.status(401).json(Map.of(
                    "error", "Acreditación requerida",
                    "message", "Debe proveer un token Bearer en el encabezado 'Authorization' de un usuario con rol ADMIN",
                    "status", 401
            ));
            return Optional.empty();
        }

        var userOpt = securityService.validateTokenAndGetUser(authHeader);
        if (userOpt.isEmpty()) {
            res.status(401).json(Map.of(
                    "error", "Token inválido o expirado",
                    "message", "El token JWT provisto no es válido o ha expirado.",
                    "status", 401
            ));
            return Optional.empty();
        }

        JUser user = userOpt.get();
        boolean isAdmin = (user.username() != null && "admin".equalsIgnoreCase(user.username()))
                || (user.firstName() != null && "admin".equalsIgnoreCase(user.firstName()))
                || (user.jRoles() != null && user.jRoles().stream()
                .anyMatch(r -> "ADMIN".equalsIgnoreCase(r.name()) || "SECURITY_ADMIN".equalsIgnoreCase(r.name())));

        if (!isAdmin) {
            res.status(403).json(Map.of(
                    "error", "Acceso denegado",
                    "message", "El usuario '" + user.username() + "' no posee el rol ADMIN necesario para administrar usuarios y roles en JettraSecurityDB.",
                    "status", 403
            ));
            return Optional.empty();
        }

        return Optional.of(user);
    }

    @GET
    @Path("/users")
    @RolesAllowed({"ADMIN", "SECURITY_ADMIN"})
    @Operation(summary = "Listar todos los usuarios en JettraSecurityDB", description = "Retorna la lista completa de usuarios con sus roles y estados registrados.")
    @ApiResponse(responseCode = "200", description = "Lista de usuarios recuperada exitosamente")
    public void listUsers(HttpRequest req, HttpResponse res) {
        if (checkAdminPrivileges(req, res).isEmpty()) return;
        var users = securityService.listUsers();
        var result = users.stream().map(u -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", u.id().toString());
            map.put("username", u.username());
            map.put("email", u.email() != null ? u.email() : "");
            map.put("phone", u.phone() != null ? u.phone() : "");
            map.put("active", u.active() != null ? u.active() : true);
            map.put("roles", u.jRoles() != null ? u.jRoles().stream().map(JRole::name).toList() : List.of());
            return map;
        }).toList();
        res.json(result);
    }

    @POST
    @Path("/users")
    @RolesAllowed({"ADMIN", "SECURITY_ADMIN"})
    @Operation(summary = "Registrar nuevo usuario en JettraSecurityDB", description = "Crea un usuario y sus credenciales de acceso con sus roles asignados.")
    @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente")
    @ApiResponse(responseCode = "400", description = "Parámetros inválidos o usuario ya existente")
    public void createUser(HttpRequest req, HttpResponse res) {
        if (checkAdminPrivileges(req, res).isEmpty()) return;
        Map<String, Object> body = parseBody(req);
        String username = getString(body, "username", req.getQueryParam("username"));
        String password = getString(body, "password", req.getQueryParam("password"));
        String email = getString(body, "email", req.getQueryParam("email"));
        String phone = getString(body, "phone", req.getQueryParam("phone"));
        Object rolesObj = body.get("roles");

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            res.status(400).json(Map.of("error", "Los campos 'username' y 'password' son obligatorios"));
            return;
        }

        Set<String> roleSet = new HashSet<>();
        if (rolesObj instanceof List<?> list) {
            for (Object o : list) {
                if (o != null && !o.toString().isBlank()) roleSet.add(o.toString().trim().toUpperCase());
            }
        } else if (rolesObj instanceof String str && !str.isBlank()) {
            for (String r : str.split(",")) {
                if (!r.trim().isEmpty()) roleSet.add(r.trim().toUpperCase());
            }
        } else {
            String qRoles = req.getQueryParam("roles");
            if (qRoles != null && !qRoles.isBlank()) {
                for (String r : qRoles.split(",")) {
                    if (!r.trim().isEmpty()) roleSet.add(r.trim().toUpperCase());
                }
            }
        }

        if (roleSet.isEmpty()) {
            roleSet.add("USER");
        }

        try {
            JUser created = securityService.registerUser(username, password, email, phone, roleSet);
            res.status(201).json(Map.of(
                    "status", "ok",
                    "message", "Usuario '" + username + "' creado exitosamente en JettraSecurityDB",
                    "id", created.id().toString(),
                    "username", created.username(),
                    "roles", roleSet
            ));
        } catch (Exception e) {
            res.status(400).json(Map.of("error", e.getMessage()));
        }
    }

    @DELETE
    @Path("/users")
    @RolesAllowed({"ADMIN", "SECURITY_ADMIN"})
    @Operation(summary = "Eliminar usuario de JettraSecurityDB", description = "Elimina de forma permanente un usuario y sus credenciales.")
    @ApiResponse(responseCode = "200", description = "Usuario eliminado exitosamente")
    @ApiResponse(responseCode = "400", description = "Nombre de usuario requerido")
    public void deleteUser(HttpRequest req, HttpResponse res) {
        if (checkAdminPrivileges(req, res).isEmpty()) return;
        Map<String, Object> body = parseBody(req);
        String username = getString(body, "username", req.getQueryParam("username"));

        deleteUserByUsername(username, req, res);
    }

    public void deleteUserByUsername(String username, HttpRequest req, HttpResponse res) {
        if (checkAdminPrivileges(req, res).isEmpty()) return;
        if (username == null || username.isBlank()) {
            res.status(400).json(Map.of("error", "El parámetro 'username' es requerido"));
            return;
        }

        if ("admin".equalsIgnoreCase(username)) {
            res.status(400).json(Map.of("error", "No se permite eliminar la cuenta de administrador principal 'admin'"));
            return;
        }

        try {
            securityService.deleteUser(username);
            res.json(Map.of("status", "ok", "message", "Usuario '" + username + "' eliminado de JettraSecurityDB"));
        } catch (Exception e) {
            res.status(400).json(Map.of("error", e.getMessage()));
        }
    }

    @POST
    @Path("/users/role")
    @RolesAllowed({"ADMIN", "SECURITY_ADMIN"})
    @Operation(summary = "Asignar o revocar rol a un usuario", description = "Añade o remueve un rol específico asignado al usuario en JettraSecurityDB.")
    @ApiResponse(responseCode = "200", description = "Rol actualizado exitosamente")
    public void updateRole(HttpRequest req, HttpResponse res) {
        if (checkAdminPrivileges(req, res).isEmpty()) return;
        Map<String, Object> body = parseBody(req);
        String username = getString(body, "username", req.getQueryParam("username"));
        String role = getString(body, "role", req.getQueryParam("role"));
        String action = getString(body, "action", req.getQueryParam("action"));

        if (username == null || role == null || username.isBlank() || role.isBlank()) {
            res.status(400).json(Map.of("error", "Los campos 'username' y 'role' son requeridos"));
            return;
        }

        try {
            if ("remove".equalsIgnoreCase(action) || "delete".equalsIgnoreCase(action)) {
                securityService.removeRoleFromUser(username, role);
                res.json(Map.of("status", "ok", "message", "Rol '" + role + "' removido del usuario '" + username + "'"));
            } else {
                securityService.addRoleToUser(username, role);
                res.json(Map.of("status", "ok", "message", "Rol '" + role + "' asignado al usuario '" + username + "'"));
            }
        } catch (Exception e) {
            res.status(400).json(Map.of("error", e.getMessage()));
        }
    }

    @POST
    @Path("/users/status")
    @RolesAllowed({"ADMIN", "SECURITY_ADMIN"})
    @Operation(summary = "Modificar estado de activación del usuario", description = "Habilita o deshabilita la cuenta de un usuario.")
    @ApiResponse(responseCode = "200", description = "Estado modificado exitosamente")
    public void updateStatus(HttpRequest req, HttpResponse res) {
        if (checkAdminPrivileges(req, res).isEmpty()) return;
        Map<String, Object> body = parseBody(req);
        String username = getString(body, "username", req.getQueryParam("username"));
        Object activeObj = body.get("active");
        boolean active = true;

        if (activeObj instanceof Boolean b) {
            active = b;
        } else if (activeObj != null) {
            active = Boolean.parseBoolean(activeObj.toString());
        } else if (req.getQueryParam("active") != null) {
            active = Boolean.parseBoolean(req.getQueryParam("active"));
        }

        if (username == null || username.isBlank()) {
            res.status(400).json(Map.of("error", "El campo 'username' es requerido"));
            return;
        }

        try {
            securityService.setUserStatus(username, active);
            res.json(Map.of("status", "ok", "message", "Estado del usuario '" + username + "' actualizado a " + (active ? "Activo" : "Inactivo")));
        } catch (Exception e) {
            res.status(400).json(Map.of("error", e.getMessage()));
        }
    }

    @POST
    @Path("/users/password")
    @RolesAllowed({"ADMIN", "SECURITY_ADMIN"})
    @Operation(summary = "Actualizar contraseña de usuario", description = "Modifica la contraseña del usuario calculando su nuevo hash en JettraSecurityDB.")
    @ApiResponse(responseCode = "200", description = "Contraseña actualizada exitosamente")
    public void updatePassword(HttpRequest req, HttpResponse res) {
        if (checkAdminPrivileges(req, res).isEmpty()) return;
        Map<String, Object> body = parseBody(req);
        String username = getString(body, "username", req.getQueryParam("username"));
        String newPassword = getString(body, "password", getString(body, "newPassword", req.getQueryParam("password")));

        if (username == null || newPassword == null || username.isBlank() || newPassword.isBlank()) {
            res.status(400).json(Map.of("error", "Los campos 'username' y 'password' son requeridos"));
            return;
        }

        try {
            securityService.changePassword(username, newPassword);
            res.json(Map.of("status", "ok", "message", "Contraseña del usuario '" + username + "' actualizada exitosamente"));
        } catch (Exception e) {
            res.status(400).json(Map.of("error", e.getMessage()));
        }
    }

    @GET
    @Path("/roles")
    @RolesAllowed({"ADMIN", "SECURITY_ADMIN", "MANAGER", "USER"})
    @Operation(summary = "Listar roles disponibles", description = "Retorna todos los roles definidos en la instancia de JettraSecurityDB.")
    @ApiResponse(responseCode = "200", description = "Lista de roles obtenida exitosamente")
    public void listRoles(HttpRequest req, HttpResponse res) {
        if (checkAdminPrivileges(req, res).isEmpty()) return;
        var roles = securityService.listRoles().stream().map(r -> Map.of(
                "id", r.id().toString(),
                "name", r.name(),
                "active", r.active() != null ? r.active() : true
        )).toList();
        res.json(roles);
    }

    @POST
    @Path("/roles")
    @RolesAllowed({"ADMIN", "SECURITY_ADMIN"})
    @Operation(summary = "Registrar nuevo rol", description = "Crea un nuevo rol en JettraSecurityDB.")
    @ApiResponse(responseCode = "201", description = "Rol creado exitosamente")
    public void createRole(HttpRequest req, HttpResponse res) {
        if (checkAdminPrivileges(req, res).isEmpty()) return;
        Map<String, Object> body = parseBody(req);
        String name = getString(body, "name", getString(body, "role", req.getQueryParam("name")));

        if (name == null || name.isBlank()) {
            res.status(400).json(Map.of("error", "El nombre del rol es requerido"));
            return;
        }

        try {
            JRole created = securityService.createRole(name.trim().toUpperCase());
            res.status(201).json(Map.of("status", "ok", "message", "Rol '" + created.name() + "' registrado exitosamente", "role", created.name()));
        } catch (Exception e) {
            res.status(400).json(Map.of("error", e.getMessage()));
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseBody(HttpRequest req) {
        if (req.getBody() != null && req.getBody().length > 0) {
            try {
                Map<?, ?> map = req.getBodyAsJson(Map.class);
                if (map != null) {
                    Map<String, Object> typed = new LinkedHashMap<>();
                    for (Map.Entry<?, ?> entry : map.entrySet()) {
                        if (entry.getKey() != null) {
                            typed.put(entry.getKey().toString(), entry.getValue());
                        }
                    }
                    return typed;
                }
            } catch (Exception ignored) {}
        }
        return Collections.emptyMap();
    }

    private String getString(Map<String, Object> map, String key, String defaultValue) {
        if (map != null && map.containsKey(key) && map.get(key) != null) {
            String val = map.get(key).toString().trim();
            if (!val.isEmpty()) return val;
        }
        return defaultValue != null ? defaultValue.trim() : null;
    }
}

