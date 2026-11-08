# @ModelReportHeader

La anotación `@ModelReportHeader` se aplica a nivel de clase para definir **cabeceras personalizadas y altamente estilizadas en los reportes**. A diferencia de `@ModelReportLabel`, que actúa sobre campos específicos, `@ModelReportHeader` permite establecer títulos y banners generales de reporte a nivel global de la clase.

---

## 1. Características Principales

*   **Anotación Repetible:** Permite declarar múltiples cabeceras en el mismo modelo/clase para estructurar títulos principales, subtítulos o avisos adicionales secuenciales.
*   **Filtrado por Tipo de Reporte (`ReportType`):** Soporta la propiedad `type` para indicar si la cabecera se aplica en reportes estándar (`NORMAL`), o en secciones específicas de reportes Master-Detail (`MASTER` o `DETAILS`).

---

## 2. Atributos de la Anotación

*   **`value`** (`String`, opcional): Texto literal a mostrar como cabecera.
*   **`label`** (`String`, opcional): Alternativa a `value` para especificar el texto.
*   **`type`** (`ReportType`, por defecto `ReportType.NORMAL`): Define a qué tipo de reporte o sección se asocia la cabecera:
    *   `ReportType.NORMAL`: Se renderiza en reportes estándar de cuadrícula/listado.
    *   `ReportType.MASTER`: Se renderiza en la sección principal/maestra de un reporte Master-Detail (el que contiene `@ViewDataTable`).
    *   `ReportType.DETAILS`: Se renderiza al inicio de la sección de detalle en un reporte Master-Detail.
*   **`orientation`** (`Orientation`, por defecto `Orientation.LEFT`): Alineación horizontal del texto (`LEFT`, `CENTER`, `RIGHT`).
*   **`font`** (`String`, por defecto `"Helvetica"`): Nombre de la familia tipográfica (ej. `Helvetica`, `Times-Roman`, `Courier`).
*   **`size`** (`int`, por defecto `14`): Tamaño de la fuente.
*   **`textColor`** (`String`, por defecto `"#000000"`): Color hexadecimal del texto (ej. `"#1f6feb"`).
*   **`style`** (`Style[]`, por defecto vacío): Aplica decoraciones del enum `Style` (`BOLD`, `ITALIC`, `SUBLINE`, `STRIKETHROUGH`).

---

## 3. Ejemplo de Uso Básico (Múltiples Cabeceras)

Este ejemplo muestra cómo definir un título principal de reporte en negrita y un subtítulo informativo inmediatamente inferior:

```java
import com.jettra.report.annotations.ModelReportHeader;
import com.jettra.report.annotations.ModelReportHeader.Orientation;
import com.jettra.report.annotations.ModelReportHeader.Style;

@ModelReportHeader(
    value = "CORPORACIÓN ADUANERA S.A.", 
    orientation = Orientation.CENTER, 
    size = 18, 
    textColor = "#0b5ed7", 
    style = {Style.BOLD}
)
@ModelReportHeader(
    value = "Listado Consolidado de Transacciones de Importación", 
    orientation = Orientation.CENTER, 
    size = 12, 
    textColor = "#555555", 
    style = {Style.ITALIC}
)
public class TransaccionModel {
    // Atributos...
}
```

---

## 4. Ejemplo en Reportes Master-Detail (`MASTER` | `DETAILS`)

Cuando trabajamos con relaciones maestro-detalle (por ejemplo, facturas que contienen una lista de líneas de factura a través de la anotación `@ViewDataTable`), podemos clasificar las cabeceras para que aparezcan en su sección correspondiente:

```java
import com.jettra.report.annotations.ModelReportDisabledHeader;
import com.jettra.report.annotations.ModelReportHeader;

@ModelReportDisabledHeader
// Este encabezado irá en la parte superior del reporte principal de la factura
@ModelReportHeader(
    value = "REPORTE DE FACTURAS DE VENTAS (MAESTRO)", 
    type = ModelReportHeader.ReportType.MASTER, 
    orientation = ModelReportHeader.Orientation.CENTER, 
    size = 16, 
    textColor = "#1f6feb", 
    style = {ModelReportHeader.Style.BOLD}
)
// Este encabezado servirá para rotular la tabla de los detalles
@ModelReportHeader(
    value = "DETALLE DE ARTÍCULOS Y SERVICIOS COMPRADOS", 
    type = ModelReportHeader.ReportType.DETAILS, 
    orientation = ModelReportHeader.Orientation.LEFT, 
    size = 12, 
    textColor = "#ff5500", 
    style = {ModelReportHeader.Style.ITALIC}
)
public class FacturaModel {

    @NotNull
    private Long idFactura;

    @ViewDataTable(...)
    private List<LineaFacturaModel> lineaFacturaModel;
}
```

---

## 5. Consideraciones Técnicas

1.  **Compatibilidad Reflexiva:** El framework de Jettra Stack (`JettraWUI`) evalúa estas anotaciones dinámicamente en tiempo de ejecución a través de la API `Class.getAnnotationsByType()`, asegurando el soporte nativo tanto para anotaciones únicas como para agrupaciones repetibles (`@ModelReportHeaders`).
2.  **Soporte Multiformato:** Todas las cabeceras procesadas y estilizadas se exportan con alta fidelidad a archivos **PDF, Excel, Word y CSV**, así como en la vista previa del dashboard (`CrudView` in-page preview).
