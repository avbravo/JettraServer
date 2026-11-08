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

        // 4. Script interactivo del cliente para Acreditación y Ejecución de Peticiones
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
                        <button onclick="jettraApplyManualToken()" style="flex: 2; background: #059669; color: #ffffff; border: none; border-radius: 6px; padding: 0.5rem; font-weight: 600; font-size: 0.85rem; cursor: pointer;">
                            ✓ Aplicar Token
                        </button>
                        <button onclick="jettraLogout()" style="flex: 1; background: #ef4444; color: #ffffff; border: none; border-radius: 6px; padding: 0.5rem; font-weight: 600; font-size: 0.85rem; cursor: pointer;">
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
            <div style="background: #ffffff; border: 1px solid #e2e8f0; border-radius: 12px; padding: 1.25rem; margin-bottom: 2rem; box-shadow: 0 2px 8px rgba(0,0,0,0.03);">
                <!-- Cabecera de la sección -->
                <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem; border-bottom: 2px solid #f1f5f9; padding-bottom: 0.8rem; margin-bottom: 1rem;">
                    <div>
                        <div style="display: flex; align-items: center; gap: 0.6rem;">
                            <span style="font-size: 1.3rem;">👥</span>
                            <h3 style="margin: 0; font-size: 1.2rem; font-weight: 800; color: #0f172a;">Administración de Usuarios y Roles (JettraSecurityDB)</h3>
                            <span id="jettra-users-count-badge" style="background: #e0f2fe; color: #0369a1; font-weight: 700; font-size: 0.75rem; padding: 0.2rem 0.6rem; border-radius: 9999px;">0 Usuarios</span>
                        </div>
                        <p style="margin: 0.3rem 0 0 0; font-size: 0.85rem; color: #64748b;">
                            Gestión interactiva en tiempo real sobre las colecciones <code>juser</code>, <code>jrole</code> y <code>jcredential</code> almacenadas en la base de datos embebida de esta instancia.
                        </p>
                    </div>
                    <div style="display: flex; gap: 0.5rem; align-items: center;">
                        <button onclick="jettraLoadUsers()" style="background: #f1f5f9; color: #334155; border: 1px solid #cbd5e1; border-radius: 6px; padding: 0.45rem 0.9rem; font-weight: 600; font-size: 0.82rem; cursor: pointer; display: flex; align-items: center; gap: 0.35rem; transition: all 0.2s;">
                            🔄 Refrescar
                        </button>
                        <button onclick="jettraToggleSection('jettra-sec-admin-content')" style="background: #f1f5f9; color: #334155; border: 1px solid #cbd5e1; border-radius: 6px; padding: 0.45rem 0.75rem; font-weight: 600; font-size: 0.82rem; cursor: pointer;">
                            <span id="jettra-sec-admin-toggle-icon">▼</span>
                        </button>
                    </div>
                </div>

                <!-- Contenido colapsable -->
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

    private Widget buildClientScript() {
        String script = """
            <script>
            (function() {
                function refreshAuthStatus() {
                    const token = localStorage.getItem('jettra_jwt_token');
                    const statusText = document.getElementById('jettra-auth-status-text');
                    const indicator = document.getElementById('jettra-auth-indicator');
                    const rolesBox = document.getElementById('jettra-auth-roles-box');
                    const manualInput = document.getElementById('jettra-manual-token');

                    if (!token) {
                        indicator.style.background = '#94a3b8';
                        statusText.innerText = 'Estado: No Acreditado (Peticiones Anónimas)';
                        statusText.style.color = '#334155';
                        rolesBox.innerHTML = '';
                        if (manualInput) manualInput.value = '';
                        return;
                    }

                    if (manualInput) manualInput.value = token;
                    indicator.style.background = '#22c55e';

                    // Parsear payload JWT para extraer subject y roles
                    try {
                        const parts = token.split('.');
                        if (parts.length === 3) {
                            const payload = JSON.parse(atob(parts[1]));
                            const user = payload.sub || 'Usuario';
                            const roles = payload.roles || [];
                            statusText.innerText = '🔓 Acreditado como: ' + user;
                            statusText.style.color = '#15803d';

                            let rolesHtml = '';
                            roles.forEach(r => {
                                rolesHtml += '<span style="background: #dcfce7; color: #16a34a; border: 1px solid #86efac; padding: 0.15rem 0.5rem; border-radius: 4px; font-weight: 700;">' + r + '</span>';
                            });
                            rolesBox.innerHTML = rolesHtml;
                            return;
                        }
                    } catch (e) {}

                    statusText.innerText = '🔓 Token Activo Guardado';
                    statusText.style.color = '#15803d';
                }

                window.jettraLogin = async function() {
                    const user = document.getElementById('jettra-login-user').value;
                    const pass = document.getElementById('jettra-login-pass').value;
                    if (!user || !pass) {
                        alert('Por favor introduce usuario y contraseña');
                        return;
                    }

                    try {
                        const res = await fetch('/api/auth/login', {
                            method: 'POST',
                            headers: { 'Content-Type': 'application/json' },
                            body: JSON.stringify({ username: user, password: pass })
                        });
                        const data = await res.json();
                        if (res.ok && data.rawToken) {
                            localStorage.setItem('jettra_jwt_token', data.rawToken);
                            refreshAuthStatus();
                            jettraLoadUsers();
                            alert('Acreditación exitosa para usuario: ' + user);
                        } else {
                            alert('Fallo de autenticación: ' + (data.error || 'Credenciales inválidas'));
                        }
                    } catch (err) {
                        alert('Error conectando con el servicio de autenticación: ' + err.message);
                    }
                };

                window.jettraApplyManualToken = function() {
                    const t = document.getElementById('jettra-manual-token').value.trim();
                    if (!t) return;
                    const raw = t.startsWith('Bearer ') ? t.substring(7).trim() : t;
                    localStorage.setItem('jettra_jwt_token', raw);
                    refreshAuthStatus();
                    jettraLoadUsers();
                };

                window.jettraLogout = function() {
                    localStorage.removeItem('jettra_jwt_token');
                    refreshAuthStatus();
                    jettraLoadUsers();
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
                        showSecNotification('Nombre de usuario y contraseña son requeridos', true);
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
                        } else {
                            showSecNotification(data.error || 'Error al crear usuario', true);
                        }
                    } catch(err) {
                        showSecNotification('Error: ' + err.message, true);
                    }
                };

                window.jettraDeleteUser = async function(username) {
                    if (!confirm('¿Confirma que desea eliminar el usuario \\'' + username + '\\' de JettraSecurityDB?')) return;

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
                };

                window.jettraPromptAddRole = async function(username) {
                    const role = prompt('Ingrese el nombre del rol a asignar a \\'' + username + '\\' (ej. ADMIN, MANAGER, AUDITOR):');
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
                };

                window.jettraRemoveRole = async function(username, role) {
                    if (!confirm('¿Revocar el rol \\'' + role + '\\' del usuario \\'' + username + '\\'?')) return;

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
                };

                window.jettraPromptPassword = async function(username) {
                    const newPass = prompt('Ingrese la nueva contraseña para el usuario \\'' + username + '\\':');
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
                        showSecNotification('Ingrese el nombre del nuevo rol', true);
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

                window.jettraExecuteRequest = async function(opId, method, path) {
                    const loader = document.getElementById('jettra-load-' + opId);
                    const resBox = document.getElementById('jettra-res-box-' + opId);
                    const statusEl = document.getElementById('jettra-res-status-' + opId);
                    const timeEl = document.getElementById('jettra-res-time-' + opId);
                    const contentEl = document.getElementById('jettra-res-content-' + opId);

                    loader.style.display = 'inline-block';
                    resBox.style.display = 'none';

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
                    const token = localStorage.getItem('jettra_jwt_token');
                    if (token) {
                        headers['Authorization'] = 'Bearer ' + token;
                    }

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

                        loader.style.display = 'none';
                        resBox.style.display = 'block';

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

                        timeEl.innerText = latency + ' ms';
                        contentEl.innerText = formattedText || '(Cuerpo de respuesta vacío)';
                    } catch (err) {
                        loader.style.display = 'none';
                        resBox.style.display = 'block';
                        statusEl.innerText = 'ERROR';
                        statusEl.style.background = '#dc2626';
                        statusEl.style.color = '#ffffff';
                        timeEl.innerText = '-';
                        contentEl.innerText = 'Error al ejecutar petición: ' + err.message;
                    }
                };

                // Iniciar estado al cargar la página
                window.addEventListener('DOMContentLoaded', function() {
                    refreshAuthStatus();
                    jettraLoadUsers();
                });
                setTimeout(function() {
                    refreshAuthStatus();
                    jettraLoadUsers();
                }, 100);
            })();
            </script>
            """;

        return RawHtml.of(script);
    }
}
