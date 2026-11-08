package io.jettra.server.flux.swagger;

import io.jettra.flux.core.FluxView;
import io.jettra.flux.core.Modifier;
import io.jettra.flux.core.Widget;
import io.jettra.flux.widgets.Badge;
import io.jettra.flux.widgets.Card;
import io.jettra.flux.widgets.Column;
import io.jettra.flux.widgets.Div;
import io.jettra.flux.widgets.Header;
import io.jettra.flux.widgets.Paragraph;
import io.jettra.flux.widgets.RawHtml;
import io.jettra.flux.widgets.Row;
import io.jettra.server.openapi.OpenApiGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Vista Swagger-UI interactiva para exploración y prueba de APIs OpenAPI 3.0.0
 * construida 100% en Java puro con componentes JettraFlux y soporte integral
 * de Acreditación de Seguridad mediante JWT Bearer y JettraSecurityDB.
 */
public class SwaggerFluxView extends FluxView {

    private final Map<String, Object> openApiModel;
    private final String title;
    private final String description;
    private final String version;

    public SwaggerFluxView() {
        this(Collections.emptyList());
    }

    public SwaggerFluxView(List<Class<?>> controllers) {
        this(OpenApiGenerator.generateModel(controllers));
    }

    @SuppressWarnings("unchecked")
    public SwaggerFluxView(Map<String, Object> openApiModel) {
        this.openApiModel = openApiModel != null ? openApiModel : Collections.emptyMap();

        Map<String, Object> info = (Map<String, Object>) this.openApiModel.getOrDefault("info", Collections.emptyMap());
        this.title = info.getOrDefault("title", "JettraServer API Explorer").toString();
        this.version = info.getOrDefault("version", "1.0.0").toString();
        this.description = info.getOrDefault("description", "Documentación OpenAPI interactiva con Acreditación de Usuario.").toString();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Widget build() {
        List<Widget> rootWidgets = new ArrayList<>();

        // 1. Cabecera Principal y Branding
        Widget headerBadge = Badge.of("OpenAPI 3.0.0").severity("primary")
                .modifier(new Modifier().style("font-weight: 700; margin-right: 0.5rem;"));
        Widget versionBadge = Badge.of("v" + version).severity("success")
                .modifier(new Modifier().style("font-weight: 700; margin-right: 0.5rem;"));
        Widget loomBadge = Badge.of("Java 25 Loom").severity("warning")
                .modifier(new Modifier().style("font-weight: 700; margin-right: 0.5rem;"));
        Widget secDbBadge = Badge.of("JettraSecurityDB Active").severity("danger")
                .modifier(new Modifier().style("font-weight: 700;"));

        Widget badgesRow = Row.of(headerBadge, versionBadge, loomBadge, secDbBadge)
                .modifier(new Modifier().style("display: flex; gap: 0.5rem; flex-wrap: wrap; margin-bottom: 0.6rem;"));

        Widget mainTitle = Header.of(1, "⚡ " + title)
                .modifier(new Modifier().style("margin: 0; font-size: 1.85rem; font-weight: 800; letter-spacing: -0.5px;"));

        Widget mainDesc = Paragraph.of(description)
                .modifier(new Modifier().style("color: #64748b; font-size: 1rem; margin: 0.5rem 0 1.5rem 0; max-width: 900px;"));

        rootWidgets.add(Column.of(badgesRow, mainTitle, mainDesc));

        // 2. Panel de Acreditación de Seguridad y Autenticación JWT (JettraSecurityDB)
        Widget accreditationCard = buildAccreditationPanel();
        rootWidgets.add(accreditationCard);

        // 3. Panel de Administración de Usuarios y Roles (JettraSecurityDB)
        Widget userRoleManagementCard = buildUserRoleManagementPanel();
        rootWidgets.add(userRoleManagementCard);

        // 4. Catálogo de Rutas y Endpoints
        Map<String, Object> paths = (Map<String, Object>) openApiModel.getOrDefault("paths", Collections.emptyMap());

        // Agrupar operaciones por tags
        Map<String, List<Map<String, Object>>> taggedOperations = new LinkedHashMap<>();

        int opCounter = 0;
        for (Map.Entry<String, Object> pathEntry : paths.entrySet()) {
            String path = pathEntry.getKey();
            Map<String, Object> methods = (Map<String, Object>) pathEntry.getValue();

            for (Map.Entry<String, Object> methodEntry : methods.entrySet()) {
                String httpMethod = methodEntry.getKey().toUpperCase();
                Map<String, Object> opData = new LinkedHashMap<>((Map<String, Object>) methodEntry.getValue());
                opData.put("path", path);
                opData.put("httpMethod", httpMethod);
                opData.put("opId", "op_" + (++opCounter));

                List<String> tags = (List<String>) opData.getOrDefault("tags", List.of("General"));
                String primaryTag = (tags != null && !tags.isEmpty()) ? tags.get(0) : "General";

                taggedOperations.computeIfAbsent(primaryTag, k -> new ArrayList<>()).add(opData);
            }
        }

        if (taggedOperations.isEmpty()) {
            rootWidgets.add(Card.of(
                    Paragraph.of("No se encontraron endpoints REST registrados con anotaciones OpenAPI o Jakarta REST.")
                            .modifier(new Modifier().style("color: #64748b; font-style: italic;"))
            ).modifier(new Modifier().style("background: #f8fafc; padding: 2rem; border-radius: 12px; margin-top: 1rem;")));
        } else {
            for (Map.Entry<String, List<Map<String, Object>>> group : taggedOperations.entrySet()) {
                String tag = group.getKey();
                List<Map<String, Object>> ops = group.getValue();

                List<Widget> opWidgets = new ArrayList<>();
                opWidgets.add(Header.of(2, "📁 " + tag)
                        .modifier(new Modifier().style("font-size: 1.35rem; font-weight: 700; margin: 1.5rem 0 0.8rem 0; border-bottom: 2px solid #e2e8f0; padding-bottom: 0.4rem; color: #1e293b;")));

                for (Map<String, Object> op : ops) {
                    opWidgets.add(buildOperationWidget(op));
                }

                rootWidgets.add(Column.of(opWidgets.toArray(new Widget[0])));
            }
        }

        // 4. Diálogo Modal Nativo JettraFlux (reemplaza alertas y confirmaciones nativas de navegador)
        rootWidgets.add(buildModalDialog());

        // 5. Script interactivo del cliente para Acreditación y Ejecución de Peticiones
        rootWidgets.add(buildClientScript());

        return Div.of(rootWidgets.toArray(new Widget[0]))
                .modifier(new Modifier().style("max-width: 1200px; margin: 0 auto; padding: 2rem 1.5rem; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; color: #0f172a;"));
    }

    private Widget buildAccreditationPanel() {
        Widget authTitle = Header.of(3, "🔐 Panel de Acreditación de Usuario (JWT Bearer Security)")
                .modifier(new Modifier().style("margin: 0 0 0.4rem 0; font-size: 1.15rem; font-weight: 700; color: #0f172a;"));

        Widget authDesc = Paragraph.of("Autentícate con credenciales registradas en JettraSecurityDB para obtener un token JWT Bearer. Todas las peticiones ejecutadas desde este Swagger-UI incluirán automáticamente el encabezado 'Authorization: Bearer <token>'.")
                .modifier(new Modifier().style("margin: 0 0 1rem 0; font-size: 0.88rem; color: #475569;"));

        Widget statusRow = RawHtml.of("""
            <div id="jettra-auth-status-container" style="display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 0.8rem; background: #f1f5f9; padding: 0.75rem 1rem; border-radius: 8px; margin-bottom: 1rem; border: 1px solid #cbd5e1;">
                <div style="display: flex; align-items: center; gap: 0.6rem;">
                    <span id="jettra-auth-indicator" style="width: 12px; height: 12px; border-radius: 50%; background: #94a3b8; display: inline-block;"></span>
                    <strong id="jettra-auth-status-text" style="font-size: 0.9rem; color: #334155;">Estado: No Acreditado (Peticiones Anónimas)</strong>
                </div>
                <div id="jettra-auth-roles-box" style="display: flex; gap: 0.4rem; font-size: 0.8rem;"></div>
            </div>
            """);

        Widget formsRow = RawHtml.of("""
            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 1rem;">
                <!-- Login con Usuario y Contraseña -->
                <div style="background: #ffffff; border: 1px solid #e2e8f0; border-radius: 8px; padding: 1rem; box-shadow: 0 1px 3px rgba(0,0,0,0.05);">
                    <div style="font-weight: 600; font-size: 0.85rem; margin-bottom: 0.6rem; color: #334155;">Acreditarse mediante Credenciales:</div>
                    <div style="display: flex; gap: 0.5rem; margin-bottom: 0.5rem;">
                        <input id="jettra-login-user" type="text" placeholder="Usuario (ej. admin)" value="admin" style="flex: 1; padding: 0.45rem 0.65rem; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 0.85rem;" />
                        <input id="jettra-login-pass" type="password" placeholder="Contraseña" value="admin" style="flex: 1; padding: 0.45rem 0.65rem; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 0.85rem;" />
                    </div>
                    <button id="jettra-btn-login" onclick="jettraLogin()" style="width: 100%; background: #2563eb; color: #ffffff; border: none; border-radius: 6px; padding: 0.5rem; font-weight: 600; font-size: 0.85rem; cursor: pointer; transition: background 0.2s;">
                        🔑 Iniciar Sesión y Acreditarse
                    </button>
                </div>

                <!-- Entrada Manual de Token Bearer JWT -->
                <div style="background: #ffffff; border: 1px solid #e2e8f0; border-radius: 8px; padding: 1rem; box-shadow: 0 1px 3px rgba(0,0,0,0.05);">
                    <div style="font-weight: 600; font-size: 0.85rem; margin-bottom: 0.6rem; color: #334155;">Ingresar Token JWT Existente:</div>
                    <div style="display: flex; gap: 0.5rem; margin-bottom: 0.5rem;">
                        <input id="jettra-manual-token" type="text" placeholder="Bearer eyJhbGci..." style="flex: 1; padding: 0.45rem 0.65rem; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 0.85rem; font-family: monospace;" />
                    </div>
                    <div style="display: flex; gap: 0.5rem;">
                        <button id="jettra-btn-apply-token" onclick="jettraApplyManualToken()" style="flex: 2; background: #059669; color: #ffffff; border: none; border-radius: 6px; padding: 0.5rem; font-weight: 600; font-size: 0.85rem; cursor: pointer; transition: background 0.2s;">
                            ✓ Aplicar Token
                        </button>
                        <button onclick="jettraLogout()" style="flex: 1; background: #ef4444; color: #ffffff; border: none; border-radius: 6px; padding: 0.5rem; font-weight: 600; font-size: 0.85rem; cursor: pointer; transition: background 0.2s;">
                            ✕ Limpiar
                        </button>
                    </div>
                </div>
            </div>
            """);

        return Card.of(Column.of(authTitle, authDesc, statusRow, formsRow))
                .modifier(new Modifier().style("background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 1.25rem; margin-bottom: 2rem; box-shadow: 0 2px 8px rgba(0,0,0,0.03);"));
    }

    private Widget buildUserRoleManagementPanel() {
        String html = """
            <div id="jettra-sec-admin-container" style="background: #ffffff; border: 1px solid #e2e8f0; border-radius: 12px; padding: 1.25rem; margin-bottom: 2rem; box-shadow: 0 2px 8px rgba(0,0,0,0.03);">
                <!-- Cabecera de la sección -->
                <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem; border-bottom: 2px solid #f1f5f9; padding-bottom: 0.8rem; margin-bottom: 1rem;">
                    <div>
                        <div style="display: flex; align-items: center; gap: 0.6rem; flex-wrap: wrap;">
                            <span style="font-size: 1.3rem;">👥</span>
                            <h3 style="margin: 0; font-size: 1.2rem; font-weight: 800; color: #0f172a;">Administración de Usuarios y Roles (JettraSecurityDB)</h3>
                            <span id="jettra-admin-lock-badge" style="background: #fee2e2; color: #dc2626; font-weight: 700; font-size: 0.75rem; padding: 0.2rem 0.6rem; border-radius: 9999px;">🔒 Requiere Rol ADMIN</span>
                            <span id="jettra-users-count-badge" style="display: none; background: #e0f2fe; color: #0369a1; font-weight: 700; font-size: 0.75rem; padding: 0.2rem 0.6rem; border-radius: 9999px;">0 Usuarios</span>
                        </div>
                        <p style="margin: 0.3rem 0 0 0; font-size: 0.85rem; color: #64748b;">
                            Gestión interactiva en tiempo real sobre las colecciones <code>juser</code>, <code>jrole</code> y <code>jcredential</code> almacenadas en la base de datos embebida de esta instancia.
                        </p>
                    </div>
                    <div id="jettra-admin-header-actions" style="display: none; gap: 0.5rem; align-items: center;">
                        <button onclick="jettraLoadUsers()" style="background: #f1f5f9; color: #334155; border: 1px solid #cbd5e1; border-radius: 6px; padding: 0.45rem 0.9rem; font-weight: 600; font-size: 0.82rem; cursor: pointer; display: flex; align-items: center; gap: 0.35rem; transition: all 0.2s;">
                            🔄 Refrescar
                        </button>
                        <button onclick="jettraToggleSection('jettra-sec-admin-content')" style="background: #f1f5f9; color: #334155; border: 1px solid #cbd5e1; border-radius: 6px; padding: 0.45rem 0.75rem; font-weight: 600; font-size: 0.82rem; cursor: pointer;">
                            <span id="jettra-sec-admin-toggle-icon">▼</span>
                        </button>
                    </div>
                </div>

                <!-- Mensaje de bloqueo cuando no está acreditado como ADMIN -->
                <div id="jettra-sec-admin-locked-msg" style="display: flex; align-items: center; gap: 0.8rem; background: #fffbeb; border: 1px solid #fef3c7; border-radius: 8px; padding: 1rem; color: #92400e; font-size: 0.88rem;">
                    <span style="font-size: 1.5rem;">🔒</span>
                    <div>
                        <strong id="jettra-sec-locked-title" style="display: block; font-size: 0.92rem; color: #78350f;">Sección protegida con rol ADMIN</strong>
                        <span id="jettra-sec-locked-desc">Para administrar usuarios y roles en JettraSecurityDB, primero debe iniciar sesión en el <b>Panel de Acreditación de Usuario</b> con una cuenta que posea el rol <b>ADMIN</b> (ej. <code>admin</code> / <code>admin</code>).</span>
                    </div>
                </div>

                <!-- Contenido administrativo activo (solo visible para rol ADMIN) -->
                <div id="jettra-sec-admin-wrapper" style="display: none;">
                    <div id="jettra-sec-admin-content" style="display: block;">
                        <!-- Notificación de estado -->
                        <div id="jettra-sec-notification" style="display: none; padding: 0.6rem 0.9rem; border-radius: 6px; font-size: 0.85rem; margin-bottom: 1rem;"></div>

                        <!-- Tabla de usuarios interactiva -->
                        <div style="overflow-x: auto; border: 1px solid #e2e8f0; border-radius: 8px; margin-bottom: 1.5rem; background: #fafafa;">
                            <table style="width: 100%; border-collapse: collapse; text-align: left; font-size: 0.85rem;">
                                <thead style="background: #f1f5f9; color: #475569; font-weight: 700;">
                                    <tr>
                                        <th style="padding: 0.65rem 0.9rem; border-bottom: 1px solid #e2e8f0;">Usuario</th>
                                        <th style="padding: 0.65rem 0.9rem; border-bottom: 1px solid #e2e8f0;">Email</th>
                                        <th style="padding: 0.65rem 0.9rem; border-bottom: 1px solid #e2e8f0;">Estado</th>
                                        <th style="padding: 0.65rem 0.9rem; border-bottom: 1px solid #e2e8f0;">Roles Asignados</th>
                                        <th style="padding: 0.65rem 0.9rem; border-bottom: 1px solid #e2e8f0; text-align: center;">Acciones de Gestión</th>
                                    </tr>
                                </thead>
                                <tbody id="jettra-users-tbody">
                                    <tr><td colspan="5" style="padding: 1.5rem; text-align: center; color: #94a3b8;">Cargando usuarios de JettraSecurityDB...</td></tr>
                                </tbody>
                            </table>
                        </div>

                        <!-- Formularios de Creación (Usuarios y Roles) en Grid -->
                        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(320px, 1fr)); gap: 1.25rem;">
                            <!-- Form Crear Usuario -->
                            <div style="background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 1rem;">
                                <div style="font-weight: 700; font-size: 0.9rem; color: #1e293b; margin-bottom: 0.6rem; display: flex; align-items: center; gap: 0.4rem;">
                                    <span>➕</span> Registrar Nuevo Usuario en JettraSecurityDB
                                </div>
                                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 0.5rem; margin-bottom: 0.5rem;">
                                    <input id="jettra-new-user" type="text" placeholder="Usuario (ej. operario)" style="padding: 0.45rem 0.6rem; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 0.82rem;" />
                                    <input id="jettra-new-pass" type="password" placeholder="Contraseña" style="padding: 0.45rem 0.6rem; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 0.82rem;" />
                                </div>
                                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 0.5rem; margin-bottom: 0.6rem;">
                                    <input id="jettra-new-email" type="email" placeholder="Email (ej. op@jettra.io)" style="padding: 0.45rem 0.6rem; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 0.82rem;" />
                                    <input id="jettra-new-roles" type="text" placeholder="Roles (ej. USER, MANAGER)" value="USER" style="padding: 0.45rem 0.6rem; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 0.82rem;" />
                                </div>
                                <button onclick="jettraCreateUser()" style="width: 100%; background: #2563eb; color: #ffffff; border: none; border-radius: 6px; padding: 0.5rem; font-weight: 700; font-size: 0.83rem; cursor: pointer; transition: background 0.2s;">
                                    💾 Guardar Usuario
                                </button>
                            </div>

                            <!-- Form Crear Roles y Catálogo -->
                            <div style="background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 1rem;">
                                <div style="font-weight: 700; font-size: 0.9rem; color: #1e293b; margin-bottom: 0.6rem; display: flex; align-items: center; gap: 0.4rem;">
                                    <span>🛡️</span> Roles del Sistema (JettraSecurityDB)
                                </div>
                                <div style="display: flex; gap: 0.5rem; margin-bottom: 0.75rem;">
                                    <input id="jettra-new-role-input" type="text" placeholder="Nuevo rol (ej. AUDITOR)" style="flex: 1; padding: 0.45rem 0.6rem; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 0.82rem; text-transform: uppercase;" />
                                    <button onclick="jettraCreateRole()" style="background: #0d9488; color: #ffffff; border: none; border-radius: 6px; padding: 0.45rem 0.8rem; font-weight: 700; font-size: 0.82rem; cursor: pointer;">
                                        ➕ Crear Rol
                                    </button>
                                </div>
                                <div style="font-size: 0.8rem; font-weight: 600; color: #475569; margin-bottom: 0.4rem;">Roles Registrados en la Instancia:</div>
                                <div id="jettra-roles-pills-container" style="display: flex; flex-wrap: wrap; gap: 0.4rem; min-height: 28px;">
                                    <span style="color: #94a3b8; font-size: 0.8rem;">Cargando roles...</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            """;
        return RawHtml.of(html);
    }

    @SuppressWarnings("unchecked")
    private Widget buildOperationWidget(Map<String, Object> op) {
        String method = op.get("httpMethod").toString();
        String path = op.get("path").toString();
        String summary = op.getOrDefault("summary", "").toString();
        String opId = op.get("opId").toString();
        List<String> rolesAllowed = (List<String>) op.get("x-roles-allowed");

        String methodColor;
        String methodBg;
        switch (method) {
            case "POST" -> { methodColor = "#16a34a"; methodBg = "#dcfce7"; }
            case "PUT" -> { methodColor = "#d97706"; methodBg = "#fef3c7"; }
            case "DELETE" -> { methodColor = "#dc2626"; methodBg = "#fee2e2"; }
            case "PATCH" -> { methodColor = "#9333ea"; methodBg = "#f3e8ff"; }
            default -> { methodColor = "#0284c7"; methodBg = "#e0f2fe"; }
        }

        StringBuilder rolesHtml = new StringBuilder();
        if (rolesAllowed != null && !rolesAllowed.isEmpty()) {
            rolesHtml.append("<span style=\"background: #fee2e2; color: #dc2626; border: 1px solid #fca5a5; font-size: 0.72rem; font-weight: 700; padding: 0.2rem 0.5rem; border-radius: 4px; margin-left: 0.5rem;\">🔒 Requiere: ").append(String.join(", ", rolesAllowed)).append("</span>");
        } else {
            rolesHtml.append("<span style=\"background: #f1f5f9; color: #64748b; font-size: 0.72rem; font-weight: 600; padding: 0.2rem 0.5rem; border-radius: 4px; margin-left: 0.5rem;\">🌐 Público</span>");
        }

        List<Map<String, Object>> parameters = (List<Map<String, Object>>) op.get("parameters");
        Map<String, Object> requestBody = (Map<String, Object>) op.get("requestBody");

        StringBuilder paramsHtml = new StringBuilder();
        if (parameters != null && !parameters.isEmpty()) {
            paramsHtml.append("<div style=\"margin-top: 1rem;\"><strong style=\"font-size: 0.85rem; color: #334155;\">Parámetros:</strong><table style=\"width: 100%; border-collapse: collapse; margin-top: 0.4rem; font-size: 0.85rem;\">");
            paramsHtml.append("<tr style=\"background: #f1f5f9; text-align: left;\"><th style=\"padding: 0.4rem; border: 1px solid #e2e8f0;\">Nombre</th><th style=\"padding: 0.4rem; border: 1px solid #e2e8f0;\">Tipo</th><th style=\"padding: 0.4rem; border: 1px solid #e2e8f0;\">Valor de Prueba</th></tr>");
            for (Map<String, Object> p : parameters) {
                String pName = p.get("name").toString();
                String pIn = p.get("in").toString();
                boolean req = Boolean.TRUE.equals(p.get("required"));
                paramsHtml.append("<tr>");
                paramsHtml.append("<td style=\"padding: 0.4rem; border: 1px solid #e2e8f0;\"><code>").append(pName).append("</code> ").append(req ? "<span style=\"color: red;\">*</span>" : "").append("<small style=\"color: #64748b;\"> (").append(pIn).append(")</small></td>");
                paramsHtml.append("<td style=\"padding: 0.4rem; border: 1px solid #e2e8f0;\">").append(pIn).append("</td>");
                paramsHtml.append("<td style=\"padding: 0.4rem; border: 1px solid #e2e8f0;\"><input type=\"text\" class=\"jettra-param-input\" data-op=\"").append(opId).append("\" data-name=\"").append(pName).append("\" data-in=\"").append(pIn).append("\" placeholder=\"valor...\" style=\"width: 95%; padding: 0.3rem 0.5rem; border: 1px solid #cbd5e1; border-radius: 4px; font-size: 0.8rem;\" /></td>");
                paramsHtml.append("</tr>");
            }
            paramsHtml.append("</table></div>");
        }

        StringBuilder bodyHtml = new StringBuilder();
        if (requestBody != null) {
            bodyHtml.append("<div style=\"margin-top: 1rem;\"><strong style=\"font-size: 0.85rem; color: #334155;\">Cuerpo de la Petición (JSON Request Body):</strong>");
            bodyHtml.append("<textarea id=\"jettra-body-").append(opId).append("\" rows=\"4\" style=\"width: 100%; margin-top: 0.4rem; padding: 0.5rem; border: 1px solid #cbd5e1; border-radius: 6px; font-family: monospace; font-size: 0.85rem; background: #ffffff;\">{\n  \"username\": \"testuser\",\n  \"password\": \"secret123\"\n}</textarea></div>");
        }

        String rawContent = String.format("""
            <div style="border: 1px solid %s; border-radius: 8px; margin-bottom: 0.85rem; background: #ffffff; overflow: hidden; transition: all 0.2s ease;">
                <div onclick="jettraToggleOp('%s')" style="display: flex; align-items: center; justify-content: space-between; padding: 0.75rem 1rem; cursor: pointer; background: %s; user-select: none;">
                    <div style="display: flex; align-items: center; gap: 0.75rem; flex-wrap: wrap;">
                        <span style="background: %s; color: #ffffff; font-weight: 800; font-size: 0.78rem; padding: 0.3rem 0.65rem; border-radius: 4px; letter-spacing: 0.5px;">%s</span>
                        <code style="font-size: 0.95rem; font-weight: 700; color: #0f172a;">%s</code>
                        <span style="color: #475569; font-size: 0.88rem;">%s</span>
                        %s
                    </div>
                    <span id="jettra-arrow-%s" style="color: %s; font-weight: bold; font-size: 1.1rem; transition: transform 0.2s;">▼</span>
                </div>
                <div id="%s" style="display: none; padding: 1.25rem; border-top: 1px solid %s; background: #fdfdfd;">
                    %s
                    %s
                    <div style="margin-top: 1.25rem; display: flex; align-items: center; gap: 0.8rem;">
                        <button onclick="jettraExecuteRequest('%s', '%s', '%s')" style="background: %s; color: #ffffff; border: none; border-radius: 6px; padding: 0.55rem 1.2rem; font-weight: 700; font-size: 0.88rem; cursor: pointer; display: flex; align-items: center; gap: 0.4rem;">
                            ▶ Ejecutar Petición (Execute)
                        </button>
                        <span id="jettra-load-%s" style="display: none; font-size: 0.85rem; color: #64748b;">⏳ Procesando petición en hilo virtual...</span>
                    </div>
                    <div id="jettra-res-box-%s" style="display: none; margin-top: 1.25rem; background: #0f172a; border-radius: 8px; padding: 1rem; color: #f8fafc; font-family: monospace; font-size: 0.85rem;">
                        <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #334155; padding-bottom: 0.5rem; margin-bottom: 0.6rem;">
                            <div>
                                <span id="jettra-res-status-%s" style="font-weight: 800; padding: 0.2rem 0.5rem; border-radius: 4px;">200 OK</span>
                                <span id="jettra-res-time-%s" style="color: #94a3b8; margin-left: 0.6rem;">0 ms</span>
                            </div>
                            <span style="color: #64748b; font-size: 0.75rem;">Respuesta HTTP/1.1</span>
                        </div>
                        <pre id="jettra-res-content-%s" style="margin: 0; overflow-x: auto; max-height: 350px; line-height: 1.4; color: #38bdf8;"></pre>
                    </div>
                </div>
            </div>
            """,
                methodColor + "40",
                opId,
                methodBg,
                methodColor,
                method,
                path,
                summary,
                rolesHtml,
                opId,
                methodColor,
                opId,
                methodColor + "30",
                paramsHtml,
                bodyHtml,
                opId,
                method,
                path,
                methodColor,
                opId,
                opId,
                opId,
                opId,
                opId
        );

        return RawHtml.of(rawContent);
    }

    private Widget buildModalDialog() {
        String html = """
            <!-- Diálogo Modal JettraFlux (Cero Popups Nativos JS) -->
            <div id="jettra-flux-dialog-backdrop" style="display: none; position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; background: rgba(15, 23, 42, 0.65); backdrop-filter: blur(4px); z-index: 999999; justify-content: center; align-items: center; padding: 1rem; box-sizing: border-box;">
                <div id="jettra-flux-dialog-card" style="background: #ffffff; border-radius: 12px; box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.25), 0 10px 10px -5px rgba(0, 0, 0, 0.1); width: 100%; max-width: 490px; overflow: hidden; border: 1px solid #e2e8f0; font-family: inherit;">
                    <!-- Cabecera del Diálogo JettraFlux -->
                    <div style="display: flex; align-items: center; justify-content: space-between; padding: 1rem 1.25rem; border-bottom: 1px solid #f1f5f9; background: #f8fafc;">
                        <div style="display: flex; align-items: center; gap: 0.6rem;">
                            <span id="jettra-dialog-icon" style="font-size: 1.3rem;">ℹ️</span>
                            <h4 id="jettra-dialog-title" style="margin: 0; font-size: 1.05rem; font-weight: 700; color: #0f172a;">Aviso del Sistema</h4>
                        </div>
                        <button type="button" onclick="jettraCloseDialog()" style="background: transparent; border: none; color: #94a3b8; font-size: 1.25rem; cursor: pointer; font-weight: bold; padding: 0.2rem 0.4rem; border-radius: 4px; line-height: 1;">✕</button>
                    </div>
                    <!-- Cuerpo del Diálogo JettraFlux -->
                    <div style="padding: 1.25rem;">
                        <div id="jettra-dialog-message" style="margin: 0; font-size: 0.92rem; color: #334155; line-height: 1.55;"></div>
                        <div id="jettra-dialog-input-container" style="display: none; margin-top: 1rem;">
                            <input type="text" id="jettra-dialog-input" style="width: 100%; box-sizing: border-box; padding: 0.55rem 0.75rem; border: 1.5px solid #cbd5e1; border-radius: 6px; font-size: 0.88rem; outline: none; transition: border-color 0.2s;" onfocus="this.style.borderColor='#2563eb'" onblur="this.style.borderColor='#cbd5e1'" />
                        </div>
                    </div>
                    <!-- Pie con Acciones -->
                    <div style="display: flex; justify-content: flex-end; gap: 0.6rem; padding: 0.85rem 1.25rem; background: #f8fafc; border-top: 1px solid #f1f5f9;">
                        <button id="jettra-dialog-cancel-btn" type="button" onclick="jettraDialogCancelAction()" style="display: none; background: #f1f5f9; color: #475569; border: 1px solid #cbd5e1; border-radius: 6px; padding: 0.48rem 1rem; font-weight: 600; font-size: 0.85rem; cursor: pointer; transition: background 0.2s;">Cancelar</button>
                        <button id="jettra-dialog-confirm-btn" type="button" onclick="jettraDialogConfirmAction()" style="background: #2563eb; color: #ffffff; border: none; border-radius: 6px; padding: 0.48rem 1.2rem; font-weight: 700; font-size: 0.85rem; cursor: pointer; transition: background 0.2s;">Aceptar</button>
                    </div>
                </div>
            </div>
            """;
        return RawHtml.of(html);
    }

    private Widget buildClientScript() {
        String script = """
            <script>
            (function() {
                // =========================================================================
                // Componente Dialog JettraFlux (Sustituto nativo de alert, confirm y prompt)
                // =========================================================================
                let jettraDialogCallback = null;
                let jettraDialogCancelCallback = null;

                window.jettraShowDialog = function(options) {
                    const backdrop = document.getElementById('jettra-flux-dialog-backdrop');
                    const titleEl = document.getElementById('jettra-dialog-title');
                    const iconEl = document.getElementById('jettra-dialog-icon');
                    const msgEl = document.getElementById('jettra-dialog-message');
                    const inputContainer = document.getElementById('jettra-dialog-input-container');
                    const inputEl = document.getElementById('jettra-dialog-input');
                    const cancelBtn = document.getElementById('jettra-dialog-cancel-btn');
                    const confirmBtn = document.getElementById('jettra-dialog-confirm-btn');

                    if (!backdrop) return;

                    titleEl.innerText = options.title || 'Aviso del Sistema';
                    iconEl.innerText = options.icon || (options.type === 'error' ? '❌' : (options.type === 'success' ? '✅' : (options.type === 'confirm' ? '❓' : (options.type === 'prompt' ? '✏️' : 'ℹ️'))));
                    msgEl.innerHTML = options.message || '';

                    jettraDialogCallback = options.onConfirm || null;
                    jettraDialogCancelCallback = options.onCancel || null;

                    confirmBtn.innerText = options.confirmText || 'Aceptar';
                    confirmBtn.style.background = options.confirmColor || '#2563eb';

                    if (options.type === 'confirm' || options.type === 'prompt') {
                        cancelBtn.style.display = 'inline-block';
                        cancelBtn.innerText = options.cancelText || 'Cancelar';
                    } else {
                        cancelBtn.style.display = 'none';
                    }

                    if (options.type === 'prompt') {
                        inputContainer.style.display = 'block';
                        inputEl.placeholder = options.placeholder || '';
                        inputEl.value = options.inputValue || '';
                        setTimeout(() => { inputEl.focus(); inputEl.select(); }, 60);
                    } else {
                        inputContainer.style.display = 'none';
                    }

                    backdrop.style.display = 'flex';
                };

                window.jettraCloseDialog = function() {
                    const backdrop = document.getElementById('jettra-flux-dialog-backdrop');
                    if (backdrop) backdrop.style.display = 'none';
                    jettraDialogCallback = null;
                    jettraDialogCancelCallback = null;
                };

                window.jettraDialogConfirmAction = function() {
                    const inputEl = document.getElementById('jettra-dialog-input');
                    const val = inputEl ? inputEl.value : '';
                    const cb = jettraDialogCallback;
                    jettraCloseDialog();
                    if (cb) cb(val);
                };

                window.jettraDialogCancelAction = function() {
                    const cb = jettraDialogCancelCallback;
                    jettraCloseDialog();
                    if (cb) cb();
                };

                document.addEventListener('keydown', function(e) {
                    const backdrop = document.getElementById('jettra-flux-dialog-backdrop');
                    if (!backdrop || backdrop.style.display !== 'flex') return;
                    if (e.key === 'Escape') {
                        jettraDialogCancelAction();
                    } else if (e.key === 'Enter') {
                        const inputContainer = document.getElementById('jettra-dialog-input-container');
                        if (inputContainer && inputContainer.style.display !== 'none') {
                            e.preventDefault();
                            jettraDialogConfirmAction();
                        }
                    }
                });

                // =========================================================================
                // Gestión de Estado de Seguridad y Acreditación JWT (JettraSecurityDB)
                // =========================================================================
                function parseJwtPayload(token) {
                    if (!token) return null;
                    try {
                        const parts = token.split('.');
                        if (parts.length !== 3) return null;
                        let b64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
                        while (b64.length % 4) { b64 += '='; }
                        try {
                            return JSON.parse(decodeURIComponent(escape(atob(b64))));
                        } catch(e) {
                            return JSON.parse(atob(b64));
                        }
                    } catch(err) {
                        return null;
                    }
                }

                function extractRoles(payload) {
                    if (!payload) return [];
                    const raw = payload.roles !== undefined ? payload.roles : (payload.groups !== undefined ? payload.groups : payload.role);
                    if (!raw) return [];
                    let res = [];
                    if (Array.isArray(raw)) {
                        raw.forEach(item => {
                            if (typeof item === 'string') {
                                res.push(item.trim());
                            } else if (item && typeof item === 'object' && item.name) {
                                res.push(String(item.name).trim());
                            } else if (item !== null && item !== undefined) {
                                res.push(String(item).trim());
                            }
                        });
                    } else if (typeof raw === 'string') {
                        let clean = raw.trim();
                        if (clean.startsWith('[') && clean.endsWith(']')) {
                            clean = clean.substring(1, clean.length - 1);
                        }
                        clean.split(',').forEach(s => {
                            let part = s.trim().replace(/^["']|["']$/g, '');
                            if (part) res.push(part);
                        });
                    }
                    return res;
                }

                function refreshAuthStatus() {
                    const token = localStorage.getItem('jettra_jwt_token');
                    const statusText = document.getElementById('jettra-auth-status-text');
                    const indicator = document.getElementById('jettra-auth-indicator');
                    const rolesBox = document.getElementById('jettra-auth-roles-box');
                    const manualInput = document.getElementById('jettra-manual-token');
                    const adminBadge = document.getElementById('jettra-admin-lock-badge');
                    const adminLockedMsg = document.getElementById('jettra-sec-admin-locked-msg');
                    const adminWrapper = document.getElementById('jettra-sec-admin-wrapper');
                    const adminActions = document.getElementById('jettra-admin-header-actions');
                    const countBadge = document.getElementById('jettra-users-count-badge');
                    const lockedTitle = document.getElementById('jettra-sec-locked-title');
                    const lockedDesc = document.getElementById('jettra-sec-locked-desc');

                    if (!token) {
                        if (indicator) indicator.style.background = '#94a3b8';
                        if (statusText) {
                            statusText.innerText = 'Estado: No Acreditado (Peticiones Bloqueadas)';
                            statusText.style.color = '#334155';
                        }
                        if (rolesBox) rolesBox.innerHTML = '';
                        if (manualInput) {
                            manualInput.style.borderColor = '#cbd5e1';
                            manualInput.style.background = '#ffffff';
                        }

                        // Bloquear sección administrativa
                        if (adminWrapper) adminWrapper.style.display = 'none';
                        if (adminLockedMsg) adminLockedMsg.style.display = 'flex';
                        if (adminActions) adminActions.style.display = 'none';
                        if (countBadge) countBadge.style.display = 'none';
                        if (adminBadge) {
                            adminBadge.innerText = '🔒 Requiere Rol ADMIN';
                            adminBadge.style.background = '#fee2e2';
                            adminBadge.style.color = '#dc2626';
                        }
                        if (lockedTitle) lockedTitle.innerText = 'Sección Protegida con Rol ADMIN';
                        if (lockedDesc) lockedDesc.innerHTML = 'Para administrar usuarios y roles en JettraSecurityDB, primero debe iniciar sesión en el <b>Panel de Acreditación de Usuario</b> con una cuenta que posea el rol <b>ADMIN</b> (ej. <code>admin</code> / <code>admin</code>).';
                        return;
                    }

                    if (manualInput) {
                        manualInput.value = token;
                        manualInput.style.borderColor = '#059669';
                        manualInput.style.background = '#f0fdf4';
                    }
                    if (indicator) indicator.style.background = '#22c55e';

                    // Parsear payload JWT para extraer subject, roles y expiración
                    const payload = parseJwtPayload(token);
                    if (!payload) {
                        localStorage.removeItem('jettra_jwt_token');
                        refreshAuthStatus();
                        return;
                    }

                    if (payload.exp && (payload.exp * 1000 < Date.now())) {
                        localStorage.removeItem('jettra_jwt_token');
                        refreshAuthStatus();
                        jettraShowDialog({
                            title: 'Sesión Expirada',
                            message: 'Su token JWT ha expirado. Por favor acredítese nuevamente para continuar utilizando las APIs.',
                            icon: '⏳',
                            confirmColor: '#dc2626'
                        });
                        return;
                    }

                    const user = payload.sub || 'Usuario';
                    const roles = extractRoles(payload);
                    const isAdmin = (user.toLowerCase() === 'admin') || roles.some(r => typeof r === 'string' && (r.toUpperCase() === 'ADMIN' || r.toUpperCase() === 'SECURITY_ADMIN'));

                    if (statusText) {
                        statusText.innerText = '🔓 Acreditado como: ' + user;
                        statusText.style.color = '#15803d';
                    }

                    if (rolesBox) {
                        let rolesHtml = '';
                        roles.forEach(r => {
                            rolesHtml += '<span style="background: #dcfce7; color: #16a34a; border: 1px solid #86efac; padding: 0.15rem 0.5rem; border-radius: 4px; font-weight: 700;">' + r + '</span>';
                        });
                        rolesBox.innerHTML = rolesHtml;
                    }

                    // Validar si el usuario posee rol ADMIN para activar la sección JettraSecurityDB
                    if (isAdmin) {
                        if (adminWrapper) {
                            adminWrapper.style.display = 'block';
                            const adminContent = document.getElementById('jettra-sec-admin-content');
                            if (adminContent) adminContent.style.display = 'block';
                        }
                        if (adminLockedMsg) adminLockedMsg.style.display = 'none';
                        if (adminActions) adminActions.style.display = 'flex';
                        if (countBadge) countBadge.style.display = 'inline-block';
                        if (adminBadge) {
                            adminBadge.innerText = '🔓 Acceso ADMIN Concedido';
                            adminBadge.style.background = '#dcfce7';
                            adminBadge.style.color = '#15803d';
                        }
                        jettraLoadUsers();
                    } else {
                        if (adminWrapper) adminWrapper.style.display = 'none';
                        if (adminLockedMsg) adminLockedMsg.style.display = 'flex';
                        if (adminActions) adminActions.style.display = 'none';
                        if (countBadge) countBadge.style.display = 'none';
                        if (adminBadge) {
                            adminBadge.innerText = '🔒 Acceso Denegado (No es ADMIN)';
                            adminBadge.style.background = '#fee2e2';
                            adminBadge.style.color = '#dc2626';
                        }
                        if (lockedTitle) lockedTitle.innerText = 'Acceso Denegado: Rol ADMIN Requerido';
                        if (lockedDesc) lockedDesc.innerHTML = 'El usuario autenticado (<b>' + user + '</b>) no posee el rol <b>ADMIN</b> requerido para administrar usuarios y roles en JettraSecurityDB. Por favor inicie sesión con una cuenta administradora.';
                    }
                }

                // =========================================================================
                // Acciones de Acreditación (Login, Aplicar Token, Cerrar Sesión)
                // =========================================================================
                window.jettraLogin = async function() {
                    const user = document.getElementById('jettra-login-user').value.trim();
                    const pass = document.getElementById('jettra-login-pass').value.trim();
                    const btn = document.getElementById('jettra-btn-login');
                    const origText = btn ? btn.innerText : '🔑 Iniciar Sesión y Acreditarse';

                    if (!user || !pass) {
                        jettraShowDialog({
                            title: 'Campos Requeridos',
                            message: 'Por favor introduzca tanto el <b>Usuario</b> como la <b>Contraseña</b> para obtener el token JWT de acreditación.',
                            icon: '⚠️',
                            confirmColor: '#d97706'
                        });
                        return;
                    }

                    if (btn) {
                        btn.disabled = true;
                        btn.innerText = '⏳ Verificando credenciales...';
                    }

                    try {
                        const res = await fetch('/api/auth/login?username=' + encodeURIComponent(user) + '&password=' + encodeURIComponent(pass), {
                            method: 'POST',
                            headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
                            body: JSON.stringify({ username: user, password: pass })
                        });
                        let data = {};
                        try { data = await res.json(); } catch(e) {}

                        if (btn) {
                            btn.disabled = false;
                            btn.innerText = origText;
                        }

                        if (res.ok && data.rawToken) {
                            // Mostrar AUTOMÁTICAMENTE el token en la sección "Ingresar Token JWT Existente"
                            const manualInput = document.getElementById('jettra-manual-token');
                            if (manualInput) {
                                manualInput.value = data.rawToken;
                                manualInput.style.borderColor = '#059669';
                                manualInput.style.background = '#f0fdf4';
                            }

                            jettraShowDialog({
                                title: 'Credenciales Validadas',
                                message: 'Las credenciales de <b>' + user + '</b> fueron validadas correctamente por el servidor y se ha emitido un nuevo JWT Token.<br><br>'
                                    + 'El token se ha cargado <b>automáticamente</b> en la casilla <b>"Ingresar Token JWT Existente"</b>.<br><br>'
                                    + '👉 Haga clic en el botón verde <b>"✓ Aplicar Token"</b> para validar el token y habilitar las funciones de <b>Administración de Usuarios y Roles (JettraSecurityDB)</b> y el <b>Microservicio de Clientes</b>.',
                                icon: '🔑',
                                confirmText: 'Entendido',
                                confirmColor: '#059669',
                                onConfirm: function() {
                                    const applyBtn = document.getElementById('jettra-btn-apply-token');
                                    if (applyBtn) {
                                        applyBtn.focus();
                                        applyBtn.scrollIntoView({ behavior: 'smooth', block: 'center' });
                                    }
                                }
                            });
                        } else {
                            jettraShowDialog({
                                title: 'Fallo de Autenticación',
                                message: data.error || 'Credenciales incorrectas o usuario inactivo en JettraSecurityDB.',
                                icon: '❌',
                                confirmColor: '#dc2626'
                            });
                        }
                    } catch (err) {
                        if (btn) {
                            btn.disabled = false;
                            btn.innerText = origText;
                        }
                        jettraShowDialog({
                            title: 'Error de Red / Conexión',
                            message: 'No fue posible comunicar con el servicio de autenticación (<code>/api/auth/login</code>): ' + err.message,
                            icon: '⚠️',
                            confirmColor: '#dc2626'
                        });
                    }
                };

                window.jettraApplyManualToken = function() {
                    const manualInput = document.getElementById('jettra-manual-token');
                    const t = manualInput ? manualInput.value.trim() : '';

                    if (!t) {
                        jettraShowDialog({
                            title: 'Token Requerido',
                            message: 'La casilla de token está vacía. Inicie sesión mediante credenciales o pegue un Token JWT Bearer existente para aplicar.',
                            icon: '⚠️',
                            confirmColor: '#d97706'
                        });
                        return;
                    }

                    const raw = t.startsWith('Bearer ') ? t.substring(7).trim() : t;
                    const parts = raw.split('.');

                    if (parts.length !== 3) {
                        jettraShowDialog({
                            title: 'Token JWT Inválido',
                            message: 'El valor provisto no cumple con el formato estándar de un JWT Token (debe contener 3 segmentos Base64 separados por puntos).',
                            icon: '❌',
                            confirmColor: '#dc2626'
                        });
                        return;
                    }

                    const payload = parseJwtPayload(raw);
                    if (!payload) {
                        jettraShowDialog({
                            title: 'Error de Decodificación',
                            message: 'No se pudo decodificar el contenido del token JWT.',
                            icon: '❌',
                            confirmColor: '#dc2626'
                        });
                        return;
                    }

                    if (payload.exp && (payload.exp * 1000 < Date.now())) {
                        jettraShowDialog({
                            title: 'Token JWT Expirado',
                            message: 'El token ingresado expiró el <b>' + new Date(payload.exp * 1000).toLocaleString() + '</b>. Por favor genere uno nuevo autenticándose.',
                            icon: '⏳',
                            confirmColor: '#dc2626'
                        });
                        return;
                    }

                    // Guardar y refrescar toda la interfaz interactiva
                    localStorage.setItem('jettra_jwt_token', raw);
                    refreshAuthStatus();

                    const user = payload.sub || 'Usuario';
                    const roles = extractRoles(payload);
                    const isAdmin = (user.toLowerCase() === 'admin') || roles.some(r => typeof r === 'string' && (r.toUpperCase() === 'ADMIN' || r.toUpperCase() === 'SECURITY_ADMIN'));

                    let extraAdmin = isAdmin
                        ? '<br><br>🛡️ <b>Acceso Administrador:</b> La sección <b>Administración de Usuarios y Roles (JettraSecurityDB)</b> ha sido habilitada.'
                        : '<br><br>ℹ️ <i>Nota: Su cuenta no posee rol ADMIN. La administración de usuarios permanecerá bloqueada.</i>';

                    jettraShowDialog({
                        title: 'Token JWT Aplicado y Validado',
                        message: 'Acreditación completada para <b>' + user + '</b>.<br>'
                            + '<b>Roles activos:</b> ' + (roles.length > 0 ? roles.join(', ') : 'Ninguno')
                            + extraAdmin
                            + '<br><br>⚡ Las peticiones del <b>Microservicio de Clientes</b> y demás rutas OpenAPI han sido habilitadas para prueba interactiva.',
                        icon: '✅',
                        confirmText: 'Continuar',
                        confirmColor: '#16a34a'
                    });
                };

                window.jettraLogout = function() {
                    localStorage.removeItem('jettra_jwt_token');
                    const manualInput = document.getElementById('jettra-manual-token');
                    if (manualInput) {
                        manualInput.value = '';
                        manualInput.style.borderColor = '#cbd5e1';
                        manualInput.style.background = '#ffffff';
                    }
                    refreshAuthStatus();
                    jettraShowDialog({
                        title: 'Sesión Finalizada',
                        message: 'Se ha eliminado el Token JWT. Las operaciones de Swagger-UI vuelven a requerir acreditación previa.',
                        icon: 'ℹ️',
                        confirmColor: '#2563eb'
                    });
                };

                window.jettraToggleSection = function(id) {
                    const el = document.getElementById(id);
                    const icon = document.getElementById('jettra-sec-admin-toggle-icon');
                    if (!el) return;
                    if (el.style.display === 'none') {
                        el.style.display = 'block';
                        if (icon) icon.innerText = '▼';
                    } else {
                        el.style.display = 'none';
                        if (icon) icon.innerText = '▶';
                    }
                };

                function showSecNotification(msg, isError) {
                    const notif = document.getElementById('jettra-sec-notification');
                    if (!notif) return;
                    notif.style.display = 'block';
                    notif.style.background = isError ? '#fee2e2' : '#dcfce7';
                    notif.style.color = isError ? '#991b1b' : '#166534';
                    notif.style.border = '1px solid ' + (isError ? '#f87171' : '#86efac');
                    notif.innerText = msg;
                    setTimeout(() => { notif.style.display = 'none'; }, 4500);
                }

                function getAuthHeaders() {
                    const headers = { 'Content-Type': 'application/json', 'Accept': 'application/json' };
                    const token = localStorage.getItem('jettra_jwt_token');
                    if (token) headers['Authorization'] = 'Bearer ' + token;
                    return headers;
                }

                window.jettraLoadUsers = async function() {
                    const tbody = document.getElementById('jettra-users-tbody');
                    const countBadge = document.getElementById('jettra-users-count-badge');
                    const rolesPills = document.getElementById('jettra-roles-pills-container');
                    if (!tbody) return;

                    try {
                        const [usersRes, rolesRes] = await Promise.all([
                            fetch('/api/security/users', { headers: getAuthHeaders() }),
                            fetch('/api/security/roles', { headers: getAuthHeaders() })
                        ]);

                        const users = await usersRes.json();
                        let roles = [];
                        try { roles = await rolesRes.json(); } catch(e) {}

                        if (rolesPills && Array.isArray(roles)) {
                            if (roles.length === 0) {
                                rolesPills.innerHTML = '<span style="color: #94a3b8; font-size: 0.8rem;">Sin roles registrados</span>';
                            } else {
                                let rHtml = '';
                                roles.forEach(r => {
                                    const rName = (typeof r === 'object' && r.name) ? r.name : r;
                                    rHtml += '<span style="background: #e2e8f0; color: #334155; font-size: 0.75rem; font-weight: 700; padding: 0.2rem 0.5rem; border-radius: 4px; border: 1px solid #cbd5e1;">🛡️ ' + rName + '</span>';
                                });
                                rolesPills.innerHTML = rHtml;
                            }
                        }

                        if (!Array.isArray(users) || users.length === 0) {
                            tbody.innerHTML = '<tr><td colspan="5" style="padding: 1.5rem; text-align: center; color: #64748b;">No hay usuarios registrados en JettraSecurityDB.</td></tr>';
                            if (countBadge) countBadge.innerText = '0 Usuarios';
                            return;
                        }

                        if (countBadge) countBadge.innerText = users.length + (users.length === 1 ? ' Usuario' : ' Usuarios');

                        let rowsHtml = '';
                        users.forEach(u => {
                            const isActive = u.active !== false;
                            const statusBadge = isActive
                                ? '<span style="background: #dcfce7; color: #15803d; font-weight: 700; font-size: 0.75rem; padding: 0.2rem 0.5rem; border-radius: 4px; border: 1px solid #86efac;">● Activo</span>'
                                : '<span style="background: #fee2e2; color: #b91c1c; font-weight: 700; font-size: 0.75rem; padding: 0.2rem 0.5rem; border-radius: 4px; border: 1px solid #fca5a5;">○ Inactivo</span>';

                            let rolesBadges = '';
                            const uRoles = u.roles || [];
                            if (uRoles.length === 0) {
                                rolesBadges = '<span style="color: #94a3b8; font-size: 0.75rem; font-style: italic;">Sin roles</span>';
                            } else {
                                uRoles.forEach(r => {
                                    rolesBadges += '<span style="background: #e0f2fe; color: #0369a1; border: 1px solid #bae6fd; font-size: 0.75rem; font-weight: 700; padding: 0.15rem 0.45rem; border-radius: 4px; margin-right: 0.3rem; display: inline-flex; align-items: center; gap: 0.3rem;">'
                                        + r
                                        + '<span onclick="jettraRemoveRole(\\'' + u.username + '\\', \\'' + r + '\\')" title="Revocar rol" style="cursor: pointer; color: #ef4444; font-weight: 900; font-size: 0.85rem;">×</span>'
                                        + '</span>';
                                });
                            }

                            rowsHtml += '<tr style="border-bottom: 1px solid #e2e8f0; background: #ffffff;">'
                                + '<td style="padding: 0.65rem 0.9rem; font-weight: 700; color: #0f172a;">👤 <code>' + u.username + '</code></td>'
                                + '<td style="padding: 0.65rem 0.9rem; color: #475569; font-size: 0.82rem;">' + (u.email || '<span style="color: #94a3b8;">-</span>') + '</td>'
                                + '<td style="padding: 0.65rem 0.9rem;">' + statusBadge + '</td>'
                                + '<td style="padding: 0.65rem 0.9rem;">' + rolesBadges + '</td>'
                                + '<td style="padding: 0.65rem 0.9rem; text-align: center;">'
                                + '<div style="display: inline-flex; gap: 0.35rem; flex-wrap: wrap; justify-content: center;">'
                                + '<button onclick="jettraPromptAddRole(\\'' + u.username + '\\')" title="Asignar rol" style="background: #f1f5f9; color: #2563eb; border: 1px solid #cbd5e1; border-radius: 4px; padding: 0.25rem 0.5rem; font-size: 0.75rem; font-weight: 600; cursor: pointer;">➕ Rol</button>'
                                + '<button onclick="jettraPromptPassword(\\'' + u.username + '\\')" title="Cambiar clave" style="background: #f1f5f9; color: #d97706; border: 1px solid #cbd5e1; border-radius: 4px; padding: 0.25rem 0.5rem; font-size: 0.75rem; font-weight: 600; cursor: pointer;">🔑 Clave</button>'
                                + '<button onclick="jettraToggleStatus(\\'' + u.username + '\\', ' + isActive + ')" title="Alternar activo/inactivo" style="background: #f1f5f9; color: #475569; border: 1px solid #cbd5e1; border-radius: 4px; padding: 0.25rem 0.5rem; font-size: 0.75rem; font-weight: 600; cursor: pointer;">⚡ ' + (isActive ? 'Desactivar' : 'Activar') + '</button>'
                                + '<button onclick="jettraDeleteUser(\\'' + u.username + '\\')" title="Eliminar usuario" style="background: #fee2e2; color: #dc2626; border: 1px solid #fca5a5; border-radius: 4px; padding: 0.25rem 0.5rem; font-size: 0.75rem; font-weight: 600; cursor: pointer;">🗑️</button>'
                                + '</div></td></tr>';
                        });
                        tbody.innerHTML = rowsHtml;
                    } catch (err) {
                        tbody.innerHTML = '<tr><td colspan="5" style="padding: 1.5rem; text-align: center; color: #ef4444;">Error conectando con la API de seguridad: ' + err.message + '</td></tr>';
                    }
                };

                window.jettraCreateUser = async function() {
                    const u = document.getElementById('jettra-new-user').value.trim();
                    const p = document.getElementById('jettra-new-pass').value.trim();
                    const e = document.getElementById('jettra-new-email').value.trim();
                    const r = document.getElementById('jettra-new-roles').value.trim();

                    if (!u || !p) {
                        jettraShowDialog({
                            title: 'Datos Requeridos',
                            message: 'El <b>Nombre de usuario</b> y la <b>Contraseña</b> son obligatorios para registrar un nuevo usuario.',
                            icon: '⚠️',
                            confirmColor: '#d97706'
                        });
                        return;
                    }

                    try {
                        const res = await fetch('/api/security/users', {
                            method: 'POST',
                            headers: getAuthHeaders(),
                            body: JSON.stringify({ username: u, password: p, email: e, roles: r })
                        });
                        const data = await res.json();
                        if (res.ok) {
                            showSecNotification('Usuario ' + u + ' creado exitosamente en JettraSecurityDB', false);
                            document.getElementById('jettra-new-user').value = '';
                            document.getElementById('jettra-new-pass').value = '';
                            document.getElementById('jettra-new-email').value = '';
                            jettraLoadUsers();
                            jettraShowDialog({
                                title: 'Usuario Creado',
                                message: 'El usuario <code>' + u + '</code> ha sido registrado exitosamente en JettraSecurityDB.',
                                icon: '✅',
                                confirmColor: '#16a34a'
                            });
                        } else {
                            showSecNotification(data.error || 'Error al crear usuario', true);
                            jettraShowDialog({
                                title: 'Error Creando Usuario',
                                message: data.error || 'No se pudo crear el usuario en JettraSecurityDB.',
                                icon: '❌',
                                confirmColor: '#dc2626'
                            });
                        }
                    } catch(err) {
                        showSecNotification('Error: ' + err.message, true);
                    }
                };

                window.jettraDeleteUser = function(username) {
                    jettraShowDialog({
                        title: 'Eliminar Usuario',
                        message: '¿Confirma que desea eliminar permanentemente al usuario <code>' + username + '</code> de JettraSecurityDB?',
                        type: 'confirm',
                        icon: '🗑️',
                        confirmText: 'Eliminar',
                        confirmColor: '#dc2626',
                        onConfirm: async function() {
                            try {
                                const res = await fetch('/api/security/users?username=' + encodeURIComponent(username), {
                                    method: 'DELETE',
                                    headers: getAuthHeaders()
                                });
                                const data = await res.json();
                                if (res.ok) {
                                    showSecNotification('Usuario ' + username + ' eliminado exitosamente', false);
                                    jettraLoadUsers();
                                } else {
                                    showSecNotification(data.error || 'Error al eliminar usuario', true);
                                }
                            } catch(err) {
                                showSecNotification('Error: ' + err.message, true);
                            }
                        }
                    });
                };

                window.jettraPromptAddRole = function(username) {
                    jettraShowDialog({
                        title: 'Asignar Rol al Usuario',
                        message: 'Ingrese el nombre del rol a asignar al usuario <code>' + username + '</code> (ej. ADMIN, MANAGER, AUDITOR):',
                        type: 'prompt',
                        icon: '➕',
                        placeholder: 'Nombre del rol (ej. ADMIN)...',
                        confirmText: 'Asignar Rol',
                        confirmColor: '#2563eb',
                        onConfirm: async function(role) {
                            if (!role || !role.trim()) return;

                            try {
                                const res = await fetch('/api/security/users/role', {
                                    method: 'POST',
                                    headers: getAuthHeaders(),
                                    body: JSON.stringify({ username: username, role: role.trim().toUpperCase(), action: 'add' })
                                });
                                const data = await res.json();
                                if (res.ok) {
                                    showSecNotification('Rol ' + role.toUpperCase() + ' asignado a ' + username, false);
                                    jettraLoadUsers();
                                } else {
                                    showSecNotification(data.error || 'Error asignando rol', true);
                                }
                            } catch(err) {
                                showSecNotification('Error: ' + err.message, true);
                            }
                        }
                    });
                };

                window.jettraRemoveRole = function(username, role) {
                    jettraShowDialog({
                        title: 'Revocar Rol',
                        message: '¿Confirma que desea revocar el rol <b>' + role + '</b> del usuario <code>' + username + '</code>?',
                        type: 'confirm',
                        icon: '⚠️',
                        confirmText: 'Revocar',
                        confirmColor: '#dc2626',
                        onConfirm: async function() {
                            try {
                                const res = await fetch('/api/security/users/role', {
                                    method: 'POST',
                                    headers: getAuthHeaders(),
                                    body: JSON.stringify({ username: username, role: role, action: 'remove' })
                                });
                                const data = await res.json();
                                if (res.ok) {
                                    showSecNotification('Rol ' + role + ' revocado de ' + username, false);
                                    jettraLoadUsers();
                                } else {
                                    showSecNotification(data.error || 'Error revocando rol', true);
                                }
                            } catch(err) {
                                showSecNotification('Error: ' + err.message, true);
                            }
                        }
                    });
                };

                window.jettraPromptPassword = function(username) {
                    jettraShowDialog({
                        title: 'Cambiar Contraseña',
                        message: 'Ingrese la nueva contraseña para el usuario <code>' + username + '</code>:',
                        type: 'prompt',
                        icon: '🔑',
                        placeholder: 'Nueva contraseña...',
                        confirmText: 'Guardar Contraseña',
                        confirmColor: '#d97706',
                        onConfirm: async function(newPass) {
                            if (!newPass || !newPass.trim()) return;

                            try {
                                const res = await fetch('/api/security/users/password', {
                                    method: 'POST',
                                    headers: getAuthHeaders(),
                                    body: JSON.stringify({ username: username, password: newPass.trim() })
                                });
                                const data = await res.json();
                                if (res.ok) {
                                    showSecNotification('Contraseña actualizada para ' + username, false);
                                } else {
                                    showSecNotification(data.error || 'Error actualizando contraseña', true);
                                }
                            } catch(err) {
                                showSecNotification('Error: ' + err.message, true);
                            }
                        }
                    });
                };

                window.jettraToggleStatus = async function(username, currentStatus) {
                    try {
                        const res = await fetch('/api/security/users/status', {
                            method: 'POST',
                            headers: getAuthHeaders(),
                            body: JSON.stringify({ username: username, active: !currentStatus })
                        });
                        const data = await res.json();
                        if (res.ok) {
                            showSecNotification('Estado actualizado para ' + username, false);
                            jettraLoadUsers();
                        } else {
                            showSecNotification(data.error || 'Error actualizando estado', true);
                        }
                    } catch(err) {
                        showSecNotification('Error: ' + err.message, true);
                    }
                };

                window.jettraCreateRole = async function() {
                    const input = document.getElementById('jettra-new-role-input');
                    const role = input ? input.value.trim().toUpperCase() : '';
                    if (!role) {
                        jettraShowDialog({
                            title: 'Rol Requerido',
                            message: 'Por favor ingrese el nombre del nuevo rol a registrar.',
                            icon: '⚠️',
                            confirmColor: '#d97706'
                        });
                        return;
                    }

                    try {
                        const res = await fetch('/api/security/roles', {
                            method: 'POST',
                            headers: getAuthHeaders(),
                            body: JSON.stringify({ name: role })
                        });
                        const data = await res.json();
                        if (res.ok) {
                            showSecNotification('Rol ' + role + ' registrado en JettraSecurityDB', false);
                            if (input) input.value = '';
                            jettraLoadUsers();
                        } else {
                            showSecNotification(data.error || 'Error registrando rol', true);
                        }
                    } catch(err) {
                        showSecNotification('Error: ' + err.message, true);
                    }
                };

                window.jettraToggleOp = function(opId) {
                    const el = document.getElementById(opId);
                    const arrow = document.getElementById('jettra-arrow-' + opId);
                    if (!el) return;
                    if (el.style.display === 'none' || el.style.display === '') {
                        el.style.display = 'block';
                        if (arrow) arrow.style.transform = 'rotate(180deg)';
                    } else {
                        el.style.display = 'none';
                        if (arrow) arrow.style.transform = 'rotate(0deg)';
                    }
                };

                // =========================================================================
                // Ejecución Protegida de Endpoints (Requiere Acreditación Previa)
                // =========================================================================
                window.jettraExecuteRequest = async function(opId, method, path) {
                    const loader = document.getElementById('jettra-load-' + opId);
                    const resBox = document.getElementById('jettra-res-box-' + opId);
                    const statusEl = document.getElementById('jettra-res-status-' + opId);
                    const timeEl = document.getElementById('jettra-res-time-' + opId);
                    const contentEl = document.getElementById('jettra-res-content-' + opId);

                    const token = localStorage.getItem('jettra_jwt_token');

                    // RECUERDA: TODAS LAS OPERACIONES CON swagger-ui, para ejecutarse primero el usuario debe haberse logeado y el server devuelto el JWT Token valido
                    if (!token) {
                        if (loader) loader.style.display = 'none';
                        if (resBox) resBox.style.display = 'block';
                        if (statusEl) {
                            statusEl.innerText = '401 No Acreditado';
                            statusEl.style.background = '#dc2626';
                            statusEl.style.color = '#ffffff';
                        }
                        if (timeEl) timeEl.innerText = '0 ms';
                        if (contentEl) {
                            contentEl.innerText = JSON.stringify({
                                error: "Acreditación Requerida",
                                message: "Operación bloqueada en Swagger-UI: Todas las peticiones requieren que el usuario se haya acreditado previamente en el 'Panel de Acreditación de Usuario' y obtenido un JWT Token válido del servidor.",
                                status: 401
                            }, null, 2);
                        }
                        jettraShowDialog({
                            title: 'Acreditación Requerida',
                            message: '<b>Operación Bloqueada:</b> Para ejecutar peticiones en Swagger-UI (incluyendo el <i>Microservicio de Clientes</i>), primero debe autenticarse en el <b>Panel de Acreditación de Usuario</b>, obtener el token y pulsar el botón <b>✓ Aplicar Token</b>.',
                            icon: '🔒',
                            confirmText: 'Ir a Acreditación',
                            confirmColor: '#2563eb',
                            onConfirm: function() {
                                const loginUserInput = document.getElementById('jettra-login-user');
                                if (loginUserInput) {
                                    loginUserInput.scrollIntoView({ behavior: 'smooth', block: 'center' });
                                    loginUserInput.focus();
                                }
                            }
                        });
                        return;
                    }

                    // Validar si el token expiró antes de lanzar la petición
                    try {
                        const parts = token.split('.');
                        if (parts.length === 3) {
                            let b64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
                            while (b64.length % 4) { b64 += '='; }
                            const payload = JSON.parse(decodeURIComponent(escape(atob(b64))));
                            if (payload.exp && (payload.exp * 1000 < Date.now())) {
                                localStorage.removeItem('jettra_jwt_token');
                                refreshAuthStatus();
                                jettraShowDialog({
                                    title: 'Token JWT Expirado',
                                    message: 'Su token JWT ha expirado. Por favor acredítese nuevamente en el Panel de Acreditación.',
                                    icon: '⏳',
                                    confirmColor: '#dc2626'
                                });
                                return;
                            }
                        }
                    } catch(e) {}

                    if (loader) loader.style.display = 'inline-block';
                    if (resBox) resBox.style.display = 'none';

                    let finalPath = path;
                    const queryParams = new URLSearchParams();

                    // Procesar parámetros de ruta y query
                    const inputs = document.querySelectorAll('.jettra-param-input[data-op="' + opId + '"]');
                    inputs.forEach(input => {
                        const pName = input.getAttribute('data-name');
                        const pIn = input.getAttribute('data-in');
                        const val = input.value.trim();
                        if (val) {
                            if (pIn === 'path') {
                                finalPath = finalPath.replace('{' + pName + '}', encodeURIComponent(val));
                            } else if (pIn === 'query') {
                                queryParams.append(pName, val);
                            }
                        }
                    });

                    const qs = queryParams.toString();
                    if (qs) {
                        finalPath += (finalPath.includes('?') ? '&' : '?') + qs;
                    }

                    const headers = { 'Accept': 'application/json' };
                    headers['Authorization'] = 'Bearer ' + token;

                    let body = null;
                    if (method === 'POST' || method === 'PUT' || method === 'PATCH') {
                        headers['Content-Type'] = 'application/json';
                        const bodyArea = document.getElementById('jettra-body-' + opId);
                        if (bodyArea) {
                            body = bodyArea.value;
                        }
                    }

                    const t0 = performance.now();
                    try {
                        const res = await fetch(finalPath, {
                            method: method,
                            headers: headers,
                            body: body
                        });
                        const t1 = performance.now();
                        const latency = Math.round(t1 - t0);

                        const text = await res.text();
                        let formattedText = text;
                        try {
                            const json = JSON.parse(text);
                            formattedText = JSON.stringify(json, null, 2);
                        } catch (e) {}

                        if (loader) loader.style.display = 'none';
                        if (resBox) resBox.style.display = 'block';

                        if (statusEl) {
                            statusEl.innerText = res.status + ' ' + (res.statusText || '');
                            if (res.status >= 200 && res.status < 300) {
                                statusEl.style.background = '#15803d';
                                statusEl.style.color = '#ffffff';
                            } else if (res.status === 401 || res.status === 403) {
                                statusEl.style.background = '#dc2626';
                                statusEl.style.color = '#ffffff';
                            } else {
                                statusEl.style.background = '#d97706';
                                statusEl.style.color = '#ffffff';
                            }
                        }

                        if (timeEl) timeEl.innerText = latency + ' ms';
                        if (contentEl) contentEl.innerText = formattedText || '(Cuerpo de respuesta vacío)';
                    } catch (err) {
                        if (loader) loader.style.display = 'none';
                        if (resBox) resBox.style.display = 'block';
                        if (statusEl) {
                            statusEl.innerText = 'ERROR';
                            statusEl.style.background = '#dc2626';
                            statusEl.style.color = '#ffffff';
                        }
                        if (timeEl) timeEl.innerText = '-';
                        if (contentEl) contentEl.innerText = 'Error al ejecutar petición: ' + err.message;
                    }
                };

                // Iniciar estado al cargar la página
                window.addEventListener('DOMContentLoaded', function() {
                    refreshAuthStatus();
                });
                setTimeout(function() {
                    refreshAuthStatus();
                }, 100);
            })();
            </script>
            """;

        return RawHtml.of(script);
    }
}
