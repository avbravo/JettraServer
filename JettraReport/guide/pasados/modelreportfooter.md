# @ModelReportFooter

La anotación `@ModelReportFooter` se utiliza a nivel de clase para definir **pies de página personalizados y altamente estilizados en los reportes**. Permite incrustar firmas de confidencialidad, mensajes corporativos, fechas automatizadas, o avisos de cumplimiento normativo al final de las páginas generadas por el motor `JettraReport`.

---

## 1. Características Principales

*   **Anotación Repetible:** Permite declarar múltiples pies de página en el mismo modelo/clase para estructurar avisos en diferentes ubicaciones o estilos.
*   **Filtrado por Tipo de Reporte (`ReportType`):** Soporta la propiedad `type` para indicar si el pie de página se aplica en reportes estándar (`NORMAL`), o en secciones específicas de reportes Master-Detail (`MASTER` o `DETAILS`).

---

## 2. Atributos de la Anotación

*   **`value`** (`String`, opcional): Texto literal a mostrar en el pie de página.
*   **`label`** (`String`, opcional): Alternativa a `value`.
*   **`type`** (`ReportType`, por defecto `ReportType.NORMAL`): Asocia el pie de página con un tipo de reporte o sección específica:
    *   `ReportType.NORMAL`: Se renderiza en reportes estándar de cuadrícula/listado.
    *   `ReportType.MASTER`: Se renderiza en la sección principal/maestra de un reporte Master-Detail.
    *   `ReportType.DETAILS`: Se renderiza en la sección de detalles/tabla de un reporte Master-Detail.
*   **`orientation`** (`Orientation`, por defecto `Orientation.LEFT`): Alineación horizontal del texto (`LEFT`, `CENTER`, `RIGHT`).
*   **`font`** (`String`, por defecto `"Helvetica"`): Nombre de la familia tipográfica (ej. `Helvetica`, `Courier`, `Times-Roman`).
*   **`size`** (`int`, por defecto `10`): Tamaño de la fuente del pie de página.
*   **`textColor`** (`String`, por defecto `"#000000"`): Color hexadecimal del texto (ej. `"#555555"`).
*   **`style`** (`Style[]`, por defecto vacío): Aplica decoraciones del enum `Style` (`BOLD`, `ITALIC`, `SUBLINE`, `STRIKETHROUGH`).

---

## 3. Ejemplo de Uso Básico (Múltiples Pies de Página)

Este ejemplo muestra cómo declarar dos pies de página: un descargo de responsabilidad alineado a la izquierda y un aviso de confidencialidad en negrita a la derecha.

```java
import com.jettra.report.annotations.ModelReportFooter;
import com.jettra.report.annotations.ModelReportFooter.Orientation;
import com.jettra.report.annotations.ModelReportFooter.Style;

@ModelReportFooter(
    value = "Generado automáticamente por la suite corporativa JettraStack.", 
    orientation = Orientation.LEFT, 
    size = 8, 
    textColor = "#777777", 
    style = {Style.ITALIC}
)
@ModelReportFooter(
    value = "CONFIDENCIAL - PROPIEDAD DE CORPORACIÓN ADUANERA", 
    orientation = Orientation.RIGHT, 
    size = 8, 
    textColor = "#ff0000", 
    style = {Style.BOLD}
)
public class TransaccionModel {
    // Atributos...
}
```

---

## 4. Ejemplo en Reportes Master-Detail (`MASTER` | `DETAILS`)

Cuando trabajamos con relaciones maestro-detalle (por ejemplo, facturas que contienen una lista de líneas de factura a través de la anotación `@ViewDataTable`), podemos clasificar los pies de página para que se asocien y rendericen condicionalmente según convenga:

```java
import com.jettra.report.annotations.ModelReportDisabledHeader;
import com.jettra.report.annotations.ModelReportFooter;

@ModelReportDisabledHeader
@ModelReportFooter(
    value = "Página generada por el motor de reportes nativo JettraReport", 
    type = ModelReportFooter.ReportType.MASTER, 
    orientation = ModelReportFooter.Orientation.CENTER, 
    size = 8, 
    textColor = "#555555"
)
@ModelReportFooter(
    value = "Confidencial - Solo para uso interno de auditoría de facturación", 
    type = ModelReportFooter.ReportType.DETAILS, 
    orientation = ModelReportFooter.Orientation.RIGHT, 
    size = 8, 
    textColor = "#ff0000", 
    style = {ModelReportFooter.Style.BOLD}
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

1.  **Compatibilidad Reflexiva:** El framework de Jettra Stack (`JettraWUI`) evalúa estas anotaciones dinámicamente en tiempo de ejecución a través de la API `Class.getAnnotationsByType()`, asegurando el soporte nativo tanto para anotaciones de pie de página únicas como para agrupaciones repetibles (`@ModelReportFooters`).
2.  **Soporte Multiformato:** Todos los pies de página procesados se exportan de manera nativa y uniforme en formatos **PDF, Excel, Word y CSV**, y en la vista previa del dashboard (`CrudView` preview).
