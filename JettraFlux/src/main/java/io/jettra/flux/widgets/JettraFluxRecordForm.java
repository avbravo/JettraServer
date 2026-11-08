package io.jettra.flux.widgets;

import io.jettra.flux.core.Modifier;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

import java.util.ArrayList;
import java.util.List;

/**
 * JettraFluxRecordForm - Dynamic, reactive Record component builder for JettraFlux.
 * Built under the Composite/Component pattern to manage typed schemas (_schema)
 * and component value maps (components) for Java Records and immutable structures.
 *
 * Supports comprehensive data types:
 * - Temporal types: Date, LocalDate, LocalTime, LocalDateTime, Instant, ZonedDateTime, OffsetDateTime
 * - Primitives and wrappers: byte, short, int, long, float, double, boolean, char
 * - Collections: List<>, Array<>, Set<>, Collection<>
 * - Enums: Enum
 * - Objects & Nested Record References: e.g. Pais, Persona, Object, Record
 * - Bidirectional synchronization between visual form and canonical JSON
 */
public class JettraFluxRecordForm extends Widget {

    public record RecordFieldEntry(String name, String type, String value) {}

    public static final String[] SUPPORTED_PRIMITIVES = {
        "String", "Integer", "int", "Long", "long", "Double", "double", "Float", "float",
        "Boolean", "boolean", "Short", "short", "Byte", "byte", "Character", "char"
    };

    public static final String[] SUPPORTED_TEMPORALS = {
        "LocalDate", "LocalTime", "LocalDateTime", "Instant", "ZonedDateTime", "OffsetDateTime", "Date"
    };

    public static final String[] SUPPORTED_COLLECTIONS = {
        "List<String>", "List<Integer>", "List<Double>", "List<Object>", "Set<String>", "Collection<Object>", "Array<String>", "String[]", "Object[]"
    };

    public static final String[] SUPPORTED_OBJECTS = {
        "Enum", "Object", "Record", "Pais"
    };

    private String formId;
    private String recordClass = "com.jettra.model.EmployeeRecord";
    private String tableName = "employees";
    private String height = "240px";
    private final List<RecordFieldEntry> fields = new ArrayList<>();

    private JettraFluxRecordForm(String formId) {
        this.formId = formId;
        this.id = formId;
    }

    public static JettraFluxRecordForm of(String formId) {
        return new JettraFluxRecordForm(formId);
    }

    public static JettraFluxRecordForm of(String formId, String recordClass, String tableName) {
        JettraFluxRecordForm form = new JettraFluxRecordForm(formId);
        if (recordClass != null) form.recordClass = recordClass;
        if (tableName != null) form.tableName = tableName;
        return form;
    }

    public JettraFluxRecordForm recordClass(String recordClass) {
        if (recordClass != null) this.recordClass = recordClass;
        return this;
    }

    public JettraFluxRecordForm tableName(String tableName) {
        if (tableName != null) this.tableName = tableName;
        return this;
    }

    public JettraFluxRecordForm height(String height) {
        if (height != null) this.height = height;
        return this;
    }

    public JettraFluxRecordForm addField(String name, String type, String value) {
        if (name != null) {
            this.fields.add(new RecordFieldEntry(name, type != null ? type : "String", value != null ? value : ""));
        }
        return this;
    }

    public JettraFluxRecordForm sampleEmployeeRecord() {
        this.recordClass = "com.jettra.model.EmployeeRecord";
        this.fields.clear();
        this.fields.add(new RecordFieldEntry("first_name", "String", "John"));
        this.fields.add(new RecordFieldEntry("last_name", "String", "Doe"));
        this.fields.add(new RecordFieldEntry("email", "String", "john.doe@company.org"));
        this.fields.add(new RecordFieldEntry("age", "Integer", "34"));
        this.fields.add(new RecordFieldEntry("salary", "Double", "85000.0"));
        this.fields.add(new RecordFieldEntry("department", "String", "ENGINEERING"));
        this.fields.add(new RecordFieldEntry("created_at", "String", "2026-09-07T10:00:00Z"));
        return this;
    }

    public JettraFluxRecordForm samplePersonaRecord() {
        this.recordClass = "com.jettra.model.Persona";
        this.tableName = "personas";
        this.fields.clear();
        this.fields.add(new RecordFieldEntry("id", "String", "PER-001"));
        this.fields.add(new RecordFieldEntry("nombre", "String", "Carlos Mendez"));
        this.fields.add(new RecordFieldEntry("edad", "int", "32"));
        this.fields.add(new RecordFieldEntry("activo", "boolean", "true"));
        this.fields.add(new RecordFieldEntry("fechaNacimiento", "LocalDate", "1994-05-12"));
        this.fields.add(new RecordFieldEntry("horaEntrada", "LocalTime", "08:30:00"));
        this.fields.add(new RecordFieldEntry("registroAuditoria", "Instant", "2026-09-08T08:00:00Z"));
        this.fields.add(new RecordFieldEntry("zonaHoraria", "ZonedDateTime", "2026-09-08T08:00:00-05:00[America/Panama]"));
        this.fields.add(new RecordFieldEntry("habilidades", "List<String>", "[\"Java 25\", \"Distributed Systems\", \"JettraDB\"]"));
        this.fields.add(new RecordFieldEntry("rol", "Enum", "SENIOR_ARCHITECT"));
        this.fields.add(new RecordFieldEntry("pais", "Pais", "{\"codigo\": \"PA\", \"nombre\": \"Panamá\", \"continente\": \"América\"}"));
        return this;
    }

    public List<RecordFieldEntry> getFields() {
        return List.copyOf(fields);
    }

    public String getRecordClass() {
        return recordClass;
    }

    public String getTableName() {
        return tableName;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();

        String containerId = formId + "_record_editor_container";
        String fieldsTableId = formId + "_record_fields_tbody";
        String jsonContainerId = formId + "_record_json_container";
        String visualContainerId = formId + "_record_visual_container";
        String jsonTextareaId = formId + "_payload";
        String schemaHiddenId = formId + "_schema_hidden";
        String compHiddenId = formId + "_components_hidden";
        String classInputId = formId + "_class";
        String tableInputId = formId + "_table";

        if (fields.isEmpty()) {
            sampleEmployeeRecord();
        }

        sb.append("<div id=\"").append(containerId).append("\" class=\"jettra-flux-record-form ")
          .append(modifier != null ? modifier.getClasses() : "").append("\" ")
          .append("style=\"display:flex; flex-direction:column; gap:12px; background:var(--j-bg-body, #0a0f1d); ")
          .append("border:1px solid var(--j-border, rgba(255,255,255,0.1)); border-radius:8px; padding:14px; ")
          .append(modifier != null ? modifier.getStyles() : "").append("\">\n");

        // Hidden inputs for form transport serialization
        sb.append("  <input type=\"hidden\" id=\"").append(schemaHiddenId).append("\" name=\"rec_schema\" value=\"\" />\n");
        sb.append("  <input type=\"hidden\" id=\"").append(compHiddenId).append("\" name=\"rec_components\" value=\"\" />\n");

        // Top Row: Table Name & Canonical Record Class FQCN
        sb.append("  <div style=\"display:flex; gap:12px; flex-wrap:wrap;\">\n");
        sb.append("    <div style=\"flex:1; min-width:200px;\">\n");
        sb.append("      <label style=\"font-size:11.5px; font-weight:700; color:var(--j-text-secondary, #94a3b8); margin-bottom:4px; display:flex; align-items:center; gap:6px;\">\n");
        sb.append("        <i class=\"fas fa-table\" style=\"color:#10b981; font-size:12px;\"></i> Nombre de Tabla (Target Table):\n");
        sb.append("      </label>\n");
        sb.append("      <input type=\"text\" id=\"").append(tableInputId).append("\" name=\"target_coll\" value=\"").append(tableName).append("\" ")
          .append("oninput=\"JettraFluxRecordForm.updatePayload('").append(formId).append("')\" ")
          .append("placeholder=\"employees\" ")
          .append("style=\"width:100%; padding:8px 12px; background:var(--j-bg-subsurface, #1e293b); border:1px solid var(--j-border, rgba(255,255,255,0.1)); ")
          .append("border-radius:6px; color:var(--j-text-primary, #f8fafc); font-size:12.5px; font-weight:600; box-sizing:border-box;\" />\n");
        sb.append("    </div>\n");

        sb.append("    <div style=\"flex:1.5; min-width:260px;\">\n");
        sb.append("      <label style=\"font-size:11.5px; font-weight:700; color:var(--j-text-secondary, #94a3b8); margin-bottom:4px; display:flex; align-items:center; gap:6px;\">\n");
        sb.append("        <i class=\"fas fa-microchip\" style=\"color:#10b981; font-size:12px;\"></i> Java 25 Canonical Record Class (_recordClass):\n");
        sb.append("        <span style=\"font-size:9.5px; background:rgba(16,185,129,0.15); color:#10b981; padding:1px 6px; border-radius:10px; font-weight:800;\">CANONICAL RECORD</span>\n");
        sb.append("      </label>\n");
        sb.append("      <input type=\"text\" id=\"").append(classInputId).append("\" name=\"rec_class\" value=\"").append(recordClass).append("\" ")
          .append("oninput=\"JettraFluxRecordForm.updatePayload('").append(formId).append("')\" ")
          .append("placeholder=\"com.jettra.model.EmployeeRecord\" ")
          .append("style=\"width:100%; padding:8px 12px; background:var(--j-bg-subsurface, #1e293b); border:1px solid var(--j-border, rgba(255,255,255,0.1)); ")
          .append("border-radius:6px; color:#38bdf8; font-family:monospace; font-size:12.5px; font-weight:600; box-sizing:border-box;\" />\n");
        sb.append("    </div>\n");
        sb.append("  </div>\n");

        // Action Toolbar
        sb.append("  <div style=\"display:flex; justify-content:space-between; align-items:center; margin-top:4px; border-top:1px solid var(--j-border, rgba(255,255,255,0.08)); padding-top:8px; flex-wrap:wrap; gap:8px;\">\n");
        sb.append("    <div style=\"display:flex; gap:6px; align-items:center; flex-wrap:wrap;\">\n");
        sb.append("      <button type=\"button\" onclick=\"JettraFluxRecordForm.addField('").append(formId).append("')\" ")
          .append("style=\"background:#10b981; color:#0f172a; border:none; padding:4px 10px; border-radius:5px; font-size:11px; font-weight:700; cursor:pointer; display:inline-flex; align-items:center; gap:4px;\">\n")
          .append("        <i class=\"fas fa-plus\"></i> Agregar Campo\n")
          .append("      </button>\n");
        sb.append("      <button type=\"button\" onclick=\"JettraFluxRecordForm.inferTypes('").append(formId).append("')\" ")
          .append("style=\"background:rgba(255,255,255,0.08); color:var(--j-text-primary, #f8fafc); border:1px solid var(--j-border, rgba(255,255,255,0.15)); padding:4px 10px; border-radius:5px; font-size:11px; font-weight:600; cursor:pointer; display:inline-flex; align-items:center; gap:4px;\">\n")
          .append("        <i class=\"fas fa-wand-magic-sparkles\" style=\"color:#38bdf8;\"></i> Inferir Esquema\n")
          .append("      </button>\n");
        sb.append("      <button type=\"button\" onclick=\"JettraFluxRecordForm.loadEmployeeSample('").append(formId).append("')\" ")
          .append("style=\"background:rgba(255,255,255,0.08); color:var(--j-text-primary, #f8fafc); border:1px solid var(--j-border, rgba(255,255,255,0.15)); padding:4px 10px; border-radius:5px; font-size:11px; font-weight:600; cursor:pointer; display:inline-flex; align-items:center; gap:4px;\">\n")
          .append("        <i class=\"fas fa-rotate\" style=\"color:#f59e0b;\"></i> Plantilla Employee\n")
          .append("      </button>\n");
        sb.append("      <button type=\"button\" onclick=\"JettraFluxRecordForm.loadPersonaSample('").append(formId).append("')\" ")
          .append("style=\"background:rgba(168,85,247,0.15); color:#a855f7; border:1px solid rgba(168,85,247,0.3); padding:4px 10px; border-radius:5px; font-size:11px; font-weight:700; cursor:pointer; display:inline-flex; align-items:center; gap:4px;\">\n")
          .append("        <i class=\"fas fa-user-tag\"></i> Plantilla Persona (Nested Record & Tipos)\n")
          .append("      </button>\n");
        sb.append("    </div>\n");

        sb.append("    <div style=\"display:flex; gap:4px; align-items:center; background:var(--j-bg-subsurface, #1e293b); padding:2px; border-radius:6px; border:1px solid var(--j-border, rgba(255,255,255,0.1));\">\n");
        sb.append("      <button type=\"button\" id=\"").append(formId).append("_btn_mode_visual\" onclick=\"JettraFluxRecordForm.toggleViewMode('").append(formId).append("','visual')\" ")
          .append("style=\"background:rgba(16,185,129,0.2); color:#10b981; border:none; padding:3px 8px; border-radius:4px; font-size:10.5px; font-weight:700; cursor:pointer;\">")
          .append("<i class=\"fas fa-list-check\"></i> Editor de Esquema</button>\n");
        sb.append("      <button type=\"button\" id=\"").append(formId).append("_btn_mode_json\" onclick=\"JettraFluxRecordForm.toggleViewMode('").append(formId).append("','json')\" ")
          .append("style=\"background:none; color:var(--j-text-muted, #94a3b8); border:none; padding:3px 8px; border-radius:4px; font-size:10.5px; font-weight:600; cursor:pointer;\">")
          .append("<i class=\"fas fa-code\"></i> JSON Canónico</button>\n");
        sb.append("    </div>\n");
        sb.append("  </div>\n");

        // Visual Field / Schema Rows Container
        sb.append("  <div id=\"").append(visualContainerId).append("\" style=\"display:block;\">\n");
        sb.append("    <table style=\"width:100%; border-collapse:collapse; font-size:11.5px;\">\n");
        sb.append("      <thead>\n");
        sb.append("        <tr style=\"background:var(--j-bg-subsurface, #1e293b); border-bottom:1px solid var(--j-border, rgba(255,255,255,0.1)); text-align:left; color:var(--j-text-secondary, #94a3b8);\">\n");
        sb.append("          <th style=\"padding:6px 8px; width:28%; font-weight:700; text-transform:uppercase; letter-spacing:0.5px;\">Nombre de Campo (_schema)</th>\n");
        sb.append("          <th style=\"padding:6px 8px; width:27%; font-weight:700; text-transform:uppercase; letter-spacing:0.5px;\">Tipo Tipado (Temporal, Primitivo, Colección, Objeto)</th>\n");
        sb.append("          <th style=\"padding:6px 8px; width:40%; font-weight:700; text-transform:uppercase; letter-spacing:0.5px;\">Valor Componente (components)</th>\n");
        sb.append("          <th style=\"padding:6px 8px; width:5%; text-align:center;\"></th>\n");
        sb.append("        </tr>\n");
        sb.append("      </thead>\n");
        sb.append("      <tbody id=\"").append(fieldsTableId).append("\">\n");

        for (RecordFieldEntry entry : fields) {
            renderFieldRowHtml(sb, formId, entry.name(), entry.type(), entry.value());
        }

        sb.append("      </tbody>\n");
        sb.append("    </table>\n");
        sb.append("  </div>\n");

        // Canonical JSON Editor Container (Dual view)
        sb.append("  <div id=\"").append(jsonContainerId).append("\" style=\"display:none; flex-direction:column; gap:6px;\">\n");
        sb.append("    <div style=\"display:flex; justify-content:space-between; align-items:center; background:var(--j-bg-subsurface, #1e293b); padding:5px 10px; border-radius:6px 6px 0 0; border:1px solid var(--j-border, rgba(255,255,255,0.1)); border-bottom:none;\">\n");
        sb.append("      <div style=\"display:flex; align-items:center; gap:6px;\">\n");
        sb.append("        <i class=\"fas fa-file-invoice\" style=\"color:#10b981; font-size:12px;\"></i>\n");
        sb.append("        <span style=\"font-size:11px; font-weight:700; color:var(--j-text-secondary, #94a3b8); text-transform:uppercase;\">Canonical Record Payload Serialization</span>\n");
        sb.append("        <span id=\"").append(formId).append("_json_status\" style=\"font-size:9.5px; font-weight:700; padding:1px 6px; border-radius:10px; background:rgba(16,185,129,0.15); color:#10b981; border:1px solid rgba(16,185,129,0.3);\">SYNCED</span>\n");
        sb.append("      </div>\n");
        sb.append("      <div style=\"display:flex; gap:6px;\">\n");
        sb.append("        <button type=\"button\" onclick=\"JettraFluxRecordForm.syncFromPayload('").append(formId).append("')\" ")
          .append("style=\"background:none; border:none; color:#38bdf8; font-size:10.5px; font-weight:600; cursor:pointer; padding:2px 6px;\">")
          .append("<i class=\"fas fa-sync-alt\"></i> Aplicar JSON a Campos</button>\n");
        sb.append("      </div>\n");
        sb.append("    </div>\n");

        sb.append("    <textarea id=\"").append(jsonTextareaId).append("\" name=\"rec_payload\" ")
          .append("oninput=\"JettraFluxRecordForm.onJsonInput('").append(formId).append("')\" ")
          .append("style=\"width:100%; height:").append(height).append("; padding:10px 12px; font-family:'Fira Code', Consolas, Monaco, monospace; ")
          .append("font-size:12px; line-height:1.5; color:#10b981; background:var(--j-bg-subsurface, #0f172a); border:1px solid var(--j-border, rgba(255,255,255,0.1)); ")
          .append("border-radius:0 0 6px 6px; outline:none; resize:vertical; box-sizing:border-box;\"></textarea>\n");
        sb.append("  </div>\n");

        sb.append("</div>\n");

        // Reusable client controller script
        sb.append("<script>\n")
          .append("if (!window.JettraFluxRecordForm) {\n")
          .append("  window.JettraFluxRecordForm = {\n")
          .append("    types: [\n")
          .append("      'String', 'Integer', 'int', 'Long', 'long', 'Double', 'double', 'Float', 'float',\n")
          .append("      'Boolean', 'boolean', 'Short', 'short', 'Byte', 'byte', 'Character', 'char',\n")
          .append("      'LocalDate', 'LocalTime', 'LocalDateTime', 'Instant', 'ZonedDateTime', 'OffsetDateTime', 'Date',\n")
          .append("      'List<String>', 'List<Integer>', 'List<Double>', 'List<Object>', 'Set<String>', 'Collection<Object>', 'Array<String>',\n")
          .append("      'Enum', 'Object', 'Record', 'Pais'\n")
          .append("    ],\n")
          .append("    addField: function(formId, name, type, val) {\n")
          .append("      var tbody = document.getElementById(formId + '_record_fields_tbody');\n")
          .append("      if (!tbody) return;\n")
          .append("      var fName = name || 'field_' + (tbody.children.length + 1);\n")
          .append("      var fType = type || 'String';\n")
          .append("      var fVal = val !== undefined ? (typeof val === 'object' ? JSON.stringify(val) : val) : '';\n")
          .append("      var tr = document.createElement('tr');\n")
          .append("      tr.className = 'record-field-row';\n")
          .append("      tr.style.borderBottom = '1px solid var(--j-border, rgba(255,255,255,0.06))';\n")
          .append("      var html = '<td style=\"padding:4px 6px;\"><input type=\"text\" class=\"rec-field-name\" value=\"' + fName + '\" oninput=\"JettraFluxRecordForm.updatePayload(\\\\'' + formId + '\\\\')\" style=\"width:100%; padding:4px 8px; background:var(--j-bg-body, #0a0f1d); border:1px solid var(--j-border, rgba(255,255,255,0.1)); border-radius:4px; color:var(--j-text-primary, #f8fafc); font-size:12px; font-weight:600; box-sizing:border-box;\" /></td>';\n")
          .append("      html += '<td style=\"padding:4px 6px;\"><input type=\"text\" list=\"' + formId + '_type_list\" class=\"rec-field-type\" value=\"' + fType + '\" onchange=\"JettraFluxRecordForm.updatePayload(\\\\'' + formId + '\\\\')\" oninput=\"JettraFluxRecordForm.updatePayload(\\\\'' + formId + '\\\\')\" style=\"width:100%; padding:4px 6px; background:var(--j-bg-body, #0a0f1d); border:1px solid var(--j-border, rgba(255,255,255,0.1)); border-radius:4px; color:#38bdf8; font-size:12px; font-weight:600; box-sizing:border-box;\" /><datalist id=\"' + formId + '_type_list\">';\n")
          .append("      this.types.forEach(function(t) { html += '<option value=\"' + t + '\">'; });\n")
          .append("      html += '</datalist></td>';\n")
          .append("      html += '<td style=\"padding:4px 6px;\"><input type=\"text\" class=\"rec-field-value\" value=\"' + (fVal + '').replace(/\"/g, '&quot;') + '\" oninput=\"JettraFluxRecordForm.updatePayload(\\\\'' + formId + '\\\\')\" style=\"width:100%; padding:4px 8px; background:var(--j-bg-body, #0a0f1d); border:1px solid var(--j-border, rgba(255,255,255,0.1)); border-radius:4px; color:#10b981; font-size:12px; font-weight:600; box-sizing:border-box;\" /></td>';\n")
          .append("      html += '<td style=\"padding:4px 6px; text-align:center;\"><button type=\"button\" onclick=\"JettraFluxRecordForm.removeField(this, \\\\'' + formId + '\\\\')\" style=\"background:none; border:none; color:#ef4444; font-size:12px; cursor:pointer; padding:3px 6px;\" title=\"Eliminar campo\"><i class=\"fas fa-trash-alt\"></i></button></td>';\n")
          .append("      tr.innerHTML = html;\n")
          .append("      tbody.appendChild(tr);\n")
          .append("      this.updatePayload(formId);\n")
          .append("    },\n")
          .append("    removeField: function(btn, formId) {\n")
          .append("      var tr = btn.closest('tr');\n")
          .append("      if (tr) tr.remove();\n")
          .append("      this.updatePayload(formId);\n")
          .append("    },\n")
          .append("    updatePayload: function(formId) {\n")
          .append("      var tbody = document.getElementById(formId + '_record_fields_tbody');\n")
          .append("      var ta = document.getElementById(formId + '_payload') || document.getElementById(formId + '_payload_textarea');\n")
          .append("      var schemaHidden = document.getElementById(formId + '_schema_hidden');\n")
          .append("      var compHidden = document.getElementById(formId + '_components_hidden');\n")
          .append("      var classInput = document.getElementById(formId + '_class') || document.getElementById(formId + '_class_input');\n")
          .append("      var tableInput = document.getElementById(formId + '_table') || document.getElementById(formId + '_table_input');\n")
          .append("      if (!tbody || !ta) return;\n")
          .append("      var recordClass = classInput ? classInput.value.trim() : 'com.jettra.model.EmployeeRecord';\n")
          .append("      var tableName = tableInput ? tableInput.value.trim() : 'employees';\n")
          .append("      var schema = {};\n")
          .append("      var components = {};\n")
          .append("      var rows = tbody.querySelectorAll('.record-field-row');\n")
          .append("      rows.forEach(function(row) {\n")
          .append("        var nameInp = row.querySelector('.rec-field-name');\n")
          .append("        var typeSel = row.querySelector('.rec-field-type');\n")
          .append("        var valInp = row.querySelector('.rec-field-value');\n")
          .append("        if (nameInp && typeSel && valInp) {\n")
          .append("          var key = nameInp.value.trim();\n")
          .append("          if (key) {\n")
          .append("            var type = typeSel.value.trim();\n")
          .append("            var raw = valInp.value.trim();\n")
          .append("            schema[key] = type;\n")
          .append("            var lowType = type.toLowerCase();\n")
          .append("            if (lowType === 'integer' || lowType === 'int' || lowType === 'short' || lowType === 'byte') {\n")
          .append("              var num = parseInt(raw, 10);\n")
          .append("              components[key] = isNaN(num) ? 0 : num;\n")
          .append("            } else if (lowType === 'long') {\n")
          .append("              var lnum = parseInt(raw, 10);\n")
          .append("              components[key] = isNaN(lnum) ? 0 : lnum;\n")
          .append("            } else if (lowType === 'double' || lowType === 'float') {\n")
          .append("              var dbl = parseFloat(raw);\n")
          .append("              components[key] = isNaN(dbl) ? 0.0 : dbl;\n")
          .append("            } else if (lowType === 'boolean') {\n")
          .append("              components[key] = (raw === 'true' || raw === '1');\n")
          .append("            } else if (lowType.startsWith('list') || lowType.startsWith('array') || lowType.startsWith('set') || lowType.startsWith('collection') || lowType.endsWith('[]')) {\n")
          .append("              try {\n")
          .append("                components[key] = JSON.parse(raw);\n")
          .append("              } catch(e) {\n")
          .append("                components[key] = raw ? raw.split(',').map(function(s){ return s.trim(); }) : [];\n")
          .append("              }\n")
          .append("            } else if (lowType === 'object' || lowType === 'record' || lowType === 'pais' || /^[A-Z][a-zA-Z0-9_]*$/.test(type)) {\n")
          .append("              try {\n")
          .append("                components[key] = JSON.parse(raw);\n")
          .append("              } catch(e) {\n")
          .append("                components[key] = raw;\n")
          .append("              }\n")
          .append("            } else {\n")
          .append("              components[key] = raw;\n")
          .append("            }\n")
          .append("          }\n")
          .append("        }\n")
          .append("      });\n")
          .append("      if (!schema['_table']) schema['_table'] = 'String';\n")
          .append("      if (!components['_table']) components['_table'] = tableName || 'default';\n")
          .append("      var canonical = {\n")
          .append("        '_recordClass': recordClass,\n")
          .append("        '_timestamp': Date.now(),\n")
          .append("        '_version': 1,\n")
          .append("        '_schema': schema,\n")
          .append("        'components': components\n")
          .append("      };\n")
          .append("      var jsonStr = JSON.stringify(canonical, null, 2);\n")
          .append("      ta.value = jsonStr;\n")
          .append("      if (schemaHidden) schemaHidden.value = JSON.stringify(schema);\n")
          .append("      if (compHidden) compHidden.value = JSON.stringify(components);\n")
          .append("      var st = document.getElementById(formId + '_json_status');\n")
          .append("      if (st) { st.textContent = 'SYNCED'; st.style.color = '#10b981'; st.style.borderColor = 'rgba(16,185,129,0.3)'; }\n")
          .append("    },\n")
          .append("    onJsonInput: function(formId) {\n")
          .append("      var ta = document.getElementById(formId + '_payload') || document.getElementById(formId + '_payload_textarea');\n")
          .append("      var st = document.getElementById(formId + '_json_status');\n")
          .append("      if (!ta || !st) return;\n")
          .append("      try {\n")
          .append("        JSON.parse(ta.value);\n")
          .append("        st.textContent = 'VALID JSON';\n")
          .append("        st.style.color = '#10b981';\n")
          .append("      } catch (e) {\n")
          .append("        st.textContent = 'SYNTAX ERROR';\n")
          .append("        st.style.color = '#ef4444';\n")
          .append("      }\n")
          .append("    },\n")
          .append("    syncFromPayload: function(formId) {\n")
          .append("      var ta = document.getElementById(formId + '_payload') || document.getElementById(formId + '_payload_textarea');\n")
          .append("      var tbody = document.getElementById(formId + '_record_fields_tbody');\n")
          .append("      var classInput = document.getElementById(formId + '_class') || document.getElementById(formId + '_class_input');\n")
          .append("      var tableInput = document.getElementById(formId + '_table') || document.getElementById(formId + '_table_input');\n")
          .append("      if (!ta || !tbody) return;\n")
          .append("      try {\n")
          .append("        var obj = JSON.parse(ta.value);\n")
          .append("        if (obj._recordClass && classInput) classInput.value = obj._recordClass;\n")
          .append("        var schema = obj._schema || {};\n")
          .append("        var comps = obj.components || obj;\n")
          .append("        if (comps._table && tableInput) tableInput.value = comps._table;\n")
          .append("        tbody.innerHTML = '';\n")
          .append("        var keys = Object.keys(comps);\n")
          .append("        for (var i = 0; i < keys.length; i++) {\n")
          .append("          var k = keys[i];\n")
          .append("          if (k === '_table' || k.startsWith('_record') || k === '_timestamp' || k === '_version' || k === '_schema') continue;\n")
          .append("          var val = comps[k];\n")
          .append("          var t = schema[k] || (typeof val === 'number' ? (Number.isInteger(val) ? 'Integer' : 'Double') : (typeof val === 'boolean' ? 'Boolean' : 'String'));\n")
          .append("          this.addField(formId, k, t, val !== undefined ? (typeof val === 'object' ? JSON.stringify(val) : val) : '');\n")
          .append("        }\n")
          .append("        this.updatePayload(formId);\n")
          .append("        this.toggleViewMode(formId, 'visual');\n")
          .append("      } catch (e) {\n")
          .append("        alert('Error al parsear el payload JSON: ' + e.message);\n")
          .append("      }\n")
          .append("    },\n")
          .append("    inferTypes: function(formId) {\n")
          .append("      var tbody = document.getElementById(formId + '_record_fields_tbody');\n")
          .append("      if (!tbody) return;\n")
          .append("      var rows = tbody.querySelectorAll('.record-field-row');\n")
          .append("      rows.forEach(function(row) {\n")
          .append("        var nameInp = row.querySelector('.rec-field-name');\n")
          .append("        var typeSel = row.querySelector('.rec-field-type');\n")
          .append("        var valInp = row.querySelector('.rec-field-value');\n")
          .append("        if (typeSel && valInp) {\n")
          .append("          var val = valInp.value.trim();\n")
          .append("          var keyName = nameInp ? nameInp.value.trim().toLowerCase() : '';\n")
          .append("          if (val === 'true' || val === 'false') {\n")
          .append("            typeSel.value = 'Boolean';\n")
          .append("          } else if (/^\\d{4}-\\d{2}-\\d{2}$/.test(val)) {\n")
          .append("            typeSel.value = 'LocalDate';\n")
          .append("          } else if (/^\\d{2}:\\d{2}(:\\d{2}(\\.\\d+)?)?$/.test(val)) {\n")
          .append("            typeSel.value = 'LocalTime';\n")
          .append("          } else if (/^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}(:\\d{2}(\\.\\d+)?)?Z$/.test(val)) {\n")
          .append("            typeSel.value = 'Instant';\n")
          .append("          } else if (/^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}(:\\d{2}(\\.\\d+)?)?[+-]\\d{2}:\\d{2}\\[.*\\]$/.test(val)) {\n")
          .append("            typeSel.value = 'ZonedDateTime';\n")
          .append("          } else if (/^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}(:\\d{2}(\\.\\d+)?)?[+-]\\d{2}:\\d{2}$/.test(val)) {\n")
          .append("            typeSel.value = 'OffsetDateTime';\n")
          .append("          } else if (/^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}/.test(val)) {\n")
          .append("            typeSel.value = 'LocalDateTime';\n")
          .append("          } else if (/^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$/.test(val)) {\n")
          .append("            typeSel.value = 'Date';\n")
          .append("          } else if (val.startsWith('[') && val.endsWith(']')) {\n")
          .append("            typeSel.value = 'List<String>';\n")
          .append("          } else if (val.startsWith('{') && val.endsWith('}')) {\n")
          .append("            typeSel.value = (keyName === 'pais') ? 'Pais' : 'Object';\n")
          .append("          } else if (/^-?\\d+$/.test(val)) {\n")
          .append("            typeSel.value = (parseInt(val, 10) > 2147483647 || parseInt(val, 10) < -2147483648) ? 'Long' : 'Integer';\n")
          .append("          } else if (/^-?\\d*\\.\\d+$/.test(val)) {\n")
          .append("            typeSel.value = 'Double';\n")
          .append("          } else if (/^[A-Z][A-Z0-9_]{2,}$/.test(val)) {\n")
          .append("            typeSel.value = 'Enum';\n")
          .append("          } else {\n")
          .append("            typeSel.value = 'String';\n")
          .append("          }\n")
          .append("        }\n")
          .append("      });\n")
          .append("      this.updatePayload(formId);\n")
          .append("    },\n")
          .append("    loadEmployeeSample: function(formId) {\n")
          .append("      var classInput = document.getElementById(formId + '_class') || document.getElementById(formId + '_class_input');\n")
          .append("      var tableInput = document.getElementById(formId + '_table') || document.getElementById(formId + '_table_input');\n")
          .append("      if (classInput) classInput.value = 'com.jettra.model.EmployeeRecord';\n")
          .append("      if (tableInput) tableInput.value = 'employees';\n")
          .append("      var tbody = document.getElementById(formId + '_record_fields_tbody');\n")
          .append("      if (tbody) tbody.innerHTML = '';\n")
          .append("      this.addField(formId, 'first_name', 'String', 'John');\n")
          .append("      this.addField(formId, 'last_name', 'String', 'Doe');\n")
          .append("      this.addField(formId, 'email', 'String', 'john.doe@company.org');\n")
          .append("      this.addField(formId, 'age', 'Integer', '34');\n")
          .append("      this.addField(formId, 'salary', 'Double', '85000.0');\n")
          .append("      this.addField(formId, 'department', 'String', 'ENGINEERING');\n")
          .append("      this.addField(formId, 'created_at', 'String', '2026-09-07T10:00:00Z');\n")
          .append("      this.updatePayload(formId);\n")
          .append("    },\n")
          .append("    loadPersonaSample: function(formId) {\n")
          .append("      var classInput = document.getElementById(formId + '_class') || document.getElementById(formId + '_class_input');\n")
          .append("      var tableInput = document.getElementById(formId + '_table') || document.getElementById(formId + '_table_input');\n")
          .append("      if (classInput) classInput.value = 'com.jettra.model.Persona';\n")
          .append("      if (tableInput) tableInput.value = 'personas';\n")
          .append("      var tbody = document.getElementById(formId + '_record_fields_tbody');\n")
          .append("      if (tbody) tbody.innerHTML = '';\n")
          .append("      this.addField(formId, 'id', 'String', 'PER-001');\n")
          .append("      this.addField(formId, 'nombre', 'String', 'Carlos Mendez');\n")
          .append("      this.addField(formId, 'edad', 'int', '32');\n")
          .append("      this.addField(formId, 'activo', 'boolean', 'true');\n")
          .append("      this.addField(formId, 'fechaNacimiento', 'LocalDate', '1994-05-12');\n")
          .append("      this.addField(formId, 'horaEntrada', 'LocalTime', '08:30:00');\n")
          .append("      this.addField(formId, 'registroAuditoria', 'Instant', '2026-09-08T08:00:00Z');\n")
          .append("      this.addField(formId, 'zonaHoraria', 'ZonedDateTime', '2026-09-08T08:00:00-05:00[America/Panama]');\n")
          .append("      this.addField(formId, 'habilidades', 'List<String>', '[\"Java 25\", \"Distributed Systems\", \"JettraDB\"]');\n")
          .append("      this.addField(formId, 'rol', 'Enum', 'SENIOR_ARCHITECT');\n")
          .append("      this.addField(formId, 'pais', 'Pais', '{\"codigo\":\"PA\",\"nombre\":\"Panamá\",\"continente\":\"América\"}');\n")
          .append("      this.updatePayload(formId);\n")
          .append("    },\n")
          .append("    toggleViewMode: function(formId, mode) {\n")
          .append("      var visual = document.getElementById(formId + '_record_visual_container');\n")
          .append("      var json = document.getElementById(formId + '_record_json_container');\n")
          .append("      var btnVisual = document.getElementById(formId + '_btn_mode_visual');\n")
          .append("      var btnJson = document.getElementById(formId + '_btn_mode_json');\n")
          .append("      if (mode === 'json') {\n")
          .append("        this.updatePayload(formId);\n")
          .append("        if (visual) visual.style.display = 'none';\n")
          .append("        if (json) json.style.display = 'flex';\n")
          .append("        if (btnVisual) { btnVisual.style.background = 'none'; btnVisual.style.color = 'var(--j-text-muted, #94a3b8)'; }\n")
          .append("        if (btnJson) { btnJson.style.background = 'rgba(16,185,129,0.2)'; btnJson.style.color = '#10b981'; }\n")
          .append("      } else {\n")
          .append("        if (visual) visual.style.display = 'block';\n")
          .append("        if (json) json.style.display = 'none';\n")
          .append("        if (btnVisual) { btnVisual.style.background = 'rgba(16,185,129,0.2)'; btnVisual.style.color = '#10b981'; }\n")
          .append("        if (btnJson) { btnJson.style.background = 'none'; btnJson.style.color = 'var(--j-text-muted, #94a3b8)'; }\n")
          .append("      }\n")
          .append("    }\n")
          .append("  };\n")
          .append("}\n")
          .append("setTimeout(function() { if (window.JettraFluxRecordForm) window.JettraFluxRecordForm.updatePayload('").append(formId).append("'); }, 50);\n")
          .append("</script>\n");

        return sb.toString();
    }

    private void renderFieldRowHtml(StringBuilder sb, String formId, String name, String type, String value) {
        sb.append("        <tr class=\"record-field-row\" style=\"border-bottom:1px solid var(--j-border, rgba(255,255,255,0.06));\">\n");
        sb.append("          <td style=\"padding:4px 6px;\">\n");
        sb.append("            <input type=\"text\" class=\"rec-field-name\" value=\"").append(escapeAttr(name)).append("\" ")
          .append("oninput=\"JettraFluxRecordForm.updatePayload('").append(formId).append("')\" ")
          .append("style=\"width:100%; padding:4px 8px; background:var(--j-bg-body, #0a0f1d); border:1px solid var(--j-border, rgba(255,255,255,0.1)); ")
          .append("border-radius:4px; color:var(--j-text-primary, #f8fafc); font-size:12px; font-weight:600; box-sizing:border-box;\" />\n");
        sb.append("          </td>\n");

        sb.append("          <td style=\"padding:4px 6px;\">\n");
        sb.append("            <input type=\"text\" list=\"").append(formId).append("_type_list\" class=\"rec-field-type\" value=\"").append(escapeAttr(type)).append("\" ")
          .append("onchange=\"JettraFluxRecordForm.updatePayload('").append(formId).append("')\" ")
          .append("oninput=\"JettraFluxRecordForm.updatePayload('").append(formId).append("')\" ")
          .append("style=\"width:100%; padding:4px 6px; background:var(--j-bg-body, #0a0f1d); border:1px solid var(--j-border, rgba(255,255,255,0.1)); ")
          .append("border-radius:4px; color:#38bdf8; font-size:12px; font-weight:600; box-sizing:border-box;\" />\n");
        sb.append("            <datalist id=\"").append(formId).append("_type_list\">\n");

        for (String t : SUPPORTED_PRIMITIVES) {
            sb.append("              <option value=\"").append(t).append("\">\n");
        }
        for (String t : SUPPORTED_TEMPORALS) {
            sb.append("              <option value=\"").append(t).append("\">\n");
        }
        for (String t : SUPPORTED_COLLECTIONS) {
            sb.append("              <option value=\"").append(t).append("\">\n");
        }
        for (String t : SUPPORTED_OBJECTS) {
            sb.append("              <option value=\"").append(t).append("\">\n");
        }

        sb.append("            </datalist>\n");
        sb.append("          </td>\n");

        sb.append("          <td style=\"padding:4px 6px;\">\n");
        sb.append("            <input type=\"text\" class=\"rec-field-value\" value=\"").append(escapeAttr(value)).append("\" ")
          .append("oninput=\"JettraFluxRecordForm.updatePayload('").append(formId).append("')\" ")
          .append("style=\"width:100%; padding:4px 8px; background:var(--j-bg-body, #0a0f1d); border:1px solid var(--j-border, rgba(255,255,255,0.1)); ")
          .append("border-radius:4px; color:#10b981; font-size:12px; font-weight:600; box-sizing:border-box;\" />\n");
        sb.append("          </td>\n");

        sb.append("          <td style=\"padding:4px 6px; text-align:center;\">\n");
        sb.append("            <button type=\"button\" onclick=\"JettraFluxRecordForm.removeField(this, '").append(formId).append("')\" ")
          .append("style=\"background:none; border:none; color:#ef4444; font-size:12px; cursor:pointer; padding:3px 6px;\" title=\"Eliminar campo\">\n")
          .append("              <i class=\"fas fa-trash-alt\"></i>\n")
          .append("            </button>\n");
        sb.append("          </td>\n");
        sb.append("        </tr>\n");
    }

    private static String escapeAttr(String val) {
        if (val == null) return "";
        return val.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
