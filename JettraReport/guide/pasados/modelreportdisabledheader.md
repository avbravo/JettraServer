# @ModelReportDisabledHeader y @ModelReportHeader

Estas anotaciones a nivel de clase permiten tener un control completo sobre el diseño y la renderización de la sección del encabezado superior del reporte generado por el motor `JettraReport`.

---

## 1. @ModelReportDisabledHeader

La anotación `@ModelReportDisabledHeader` se aplica a nivel de clase (Modelos de datos o páginas controladoras `CrudView`) y su objetivo principal es **deshabilitar la inclusión automática del título de cabecera** predeterminado correspondiente al nombre de la clase (por ejemplo, `"LISTADO DE FACTURAS"` o `"REPORTE DE FACTURAMODEL"`). 

### Razón de ser
Por defecto, JettraStack genera un título automático en el encabezado. Si el desarrollador prefiere diseñar su propio encabezado utilizando etiquetas de campo mapeadas con `@ModelReportLabel` o definir un título de clase mucho más personalizado y estilizado mediante `@ModelReportHeader`, se debe usar `@ModelReportDisabledHeader` para evitar duplicidades en el documento final.

### Uso Básico

```java
import com.jettra.report.annotations.ModelReportDisabledHeader;
import io.jettra.wui.core.annotations.JettraViewModel;

@JettraViewModel
@ModelReportDisabledHeader
public class FacturaModel {
    // Los campos individuales con @ModelReportLabel construirán el encabezado en su lugar.
}
```

---

## 2. @ModelReportHeader

La anotación `@ModelReportHeader` se aplica a nivel de clase y sirve para **definir una cabecera totalmente personalizada a nivel de reporte**, soportando el ajuste de fuentes, colores, tamaños, alineación horizontal y decoraciones estilísticas. 

Funciona de manera análoga a `@ModelReportLabel`, pero su ámbito y aplicación son estrictamente globales para toda la clase del reporte, en lugar de aplicarse a campos individuales.

### Atributos de la Anotación

*   **`value`** (`String`, opcional): Especifica el texto literal que se mostrará como cabecera del reporte. Reemplaza el título por defecto.
*   **`label`** (`String`, opcional): Alternativa a `value` para especificar el texto de la cabecera.
*   **`orientation`** (`Orientation`, por defecto `Orientation.LEFT`): Controla la alineación horizontal de la cabecera dentro de la página. Valores soportados:
    *   `ModelReportHeader.Orientation.LEFT`: Alineación a la izquierda.
    *   `ModelReportHeader.Orientation.CENTER`: Alineación centrada.
    *   `ModelReportHeader.Orientation.RIGHT`: Alineación a la derecha.
*   **`font`** (`String`, por defecto `"Helvetica"`): Nombre de la familia tipográfica (ej. `Helvetica`, `Courier`, `Times-Roman`).
*   **`size`** (`int`, por defecto `14`): Tamaño de la fuente del título de cabecera.
*   **`textColor`** (`String`, por defecto `"#000000"`): Color del texto en formato hexadecimal (ej. `"#0055ff"`).
*   **`style`** (`Style[]`, por defecto vacío): Aplica múltiples efectos de texto del enum `ModelReportHeader.Style`. Valores soportados:
    *   `BOLD`: Negrita.
    *   `ITALIC`: Cursiva.
    *   `SUBLINE`: Subrayado.
    *   `STRIKETHROUGH`: Tachado.

### Uso Combinado (Ejemplo de cabecera customizada)

```java
import com.jettra.report.annotations.ModelReportDisabledHeader;
import com.jettra.report.annotations.ModelReportHeader;
import com.jettra.report.annotations.ModelReportHeader.Orientation;
import com.jettra.report.annotations.ModelReportHeader.Style;

@ModelReportDisabledHeader
@ModelReportHeader(
    value = "REGISTRO GENERAL DE FACTURACIÓN Y VENTAS",
    font = "Helvetica",
    size = 16,
    textColor = "#1f6feb",
    orientation = Orientation.CENTER,
    style = {Style.BOLD, Style.SUBLINE}
)
public class FacturaModel {
    // Atributos del modelo...
}
```

---

## 3. Ejemplo Completo en una Página CrudView

En `ViewDataTablePage.java` podemos configurar `@ModelReportDisabledHeader` para controlar de manera nativa la visualización de la cabecera cuando el usuario interactúe con los botones de exportación o abra la vista previa del modal:

```java
package com.jettra.example.pages.datatable.masterdetails;

import com.jettra.example.dashboard.DashboardBasePage;
import io.jettra.wui.complex.Center;
import io.jettra.wui.core.annotations.CrudView;
import com.jettra.report.annotations.ModelReportDisabledHeader;

@ModelReportDisabledHeader
@CrudView(
    model = com.jettra.example.model.FacturaModel.class, 
    repository = com.jettra.example.repository.FacturaRepository.class, 
    editable = true, 
    autoRender = false, 
    report = true, 
    reportShowViewer = true
)
public class ViewDataTablePage extends DashboardBasePage {

    public ViewDataTablePage() {
        super("Panel de Facturación");
    }

    @Override
    protected void initCenter(Center center, String username) {
        // Inicialización del componente CrudView y renderizado...
    }
}
```

## Consideraciones de Arquitectura

1.  **Carga Dinámica (Reflexión):** Dado que el módulo `JettraWUI` no tiene dependencia circular estática sobre el compilado de reportes, el framework lee ambas anotaciones en tiempo de ejecución utilizando reflexión y carga dinámica (`ClassLoader`). Esto garantiza un desacoplamiento impecable entre los módulos del sistema.
2.  **Soporte Multiformato:** Esta lógica de supresión de cabeceras automáticas y renderizado customizado se aplica de forma uniforme en todos los exportadores de JettraReport (PDF, Excel, Word y CSV).
