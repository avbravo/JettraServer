# @ModelReportLabel

La anotación `@ModelReportLabel` se utiliza para personalizar cómo se muestran los campos individuales de un modelo (como pares etiqueta-valor) cuando se genera un reporte en formato PDF o Word para un solo registro, especialmente en contextos de tipo Master-Detail. 

Permite definir un texto de etiqueta personalizado, así como configurar la sección exacta del reporte y la alineación horizontal donde se debe renderizar la información.

## Uso Básico

```java
import com.jettra.report.annotations.ModelReportLabel;

public class FacturaModel {
    
    @ModelReportLabel(label = "Gran Total Factura", section = ModelReportLabel.Section.LASTPAGE, orientation = ModelReportLabel.Orientation.RIGHT)
    private Double total;
    
}
```

## Atributos de la Anotación

La anotación acepta los siguientes parámetros:

*   **`label`** (`String`, opcional): Especifica el texto de la etiqueta que precederá al valor del campo en el reporte. Si no se especifica, se utilizará el valor de la anotación `@PropertiesLabel` (si existe) o el nombre de la variable.
*   **`section`** (`Section`, por defecto `Section.HEADER`): Indica en qué sección del reporte se agregará la etiqueta. Los valores posibles son:
    *   `ModelReportLabel.Section.HEADER`: Coloca la información en el encabezado superior del reporte (antes de las tablas de detalles).
    *   `ModelReportLabel.Section.FOOTER`: Coloca la información en el pie de página de cada página.
    *   `ModelReportLabel.Section.LASTPAGE`: Coloca la información al final del reporte (en la sección de resumen), ideal para totales y firmas.
*   **`orientation`** (`Orientation`, por defecto `Orientation.LEFT`): Controla la alineación horizontal del texto en la sección asignada. Los valores posibles son:
    *   `ModelReportLabel.Orientation.LEFT`: Alineación a la izquierda.
    *   `ModelReportLabel.Orientation.CENTER`: Alineación centrada.
    *   `ModelReportLabel.Orientation.RIGHT`: Alineación a la derecha.
*   **`font`** (`String`, por defecto `"Helvetica"`): Nombre de la fuente a utilizar.
*   **`size`** (`int`, por defecto `10`): Tamaño de la fuente.
*   **`textColor`** (`String`, por defecto `"#000000"`): Color del texto en formato Hexadecimal (ej: `"#FF0000"`).
*   **`style`** (`Style[]`, por defecto vacío): Permite aplicar múltiples estilos al texto usando el enum `ModelReportLabel.Style`. Valores soportados:
    *   `BOLD`: Negrita.
    *   `ITALIC`: Cursiva.
    *   `SUBLINE`: Subrayado.
    *   `STRIKETHROUGH`: Tachado.

## Ejemplo Completo

En este ejemplo de `FacturaModel`, utilizamos `@ModelReportLabel` para distribuir la información maestra: el ID y el Cliente en la cabecera, mientras que el Descuento y el Total General se envían al final del documento alineados según convenga.

```java
import io.jettra.wui.core.annotations.JettraViewModel;
import com.jettra.report.annotations.ModelReportLabel;
import io.jettra.wui.core.annotations.PropertiesLabel;
import io.jettra.wui.core.annotations.ViewDataTable;
import io.jettra.wui.core.annotations.ViewSelectOne;
import io.jettra.wui.validations.NotNull;
import java.util.List;

@JettraViewModel
public class FacturaModel {

    @NotNull
    @PropertiesLabel(value = "factura.id", label = "ID Factura")
    // Se mostrará en el HEADER alineado a la Izquierda
    @ModelReportLabel(label = "Factura ID", section = ModelReportLabel.Section.HEADER, orientation = ModelReportLabel.Orientation.LEFT)
    private Long idFactura;

    @NotNull
    @ViewSelectOne(label = "nombre", fieldOnlyMasterTable = "nombre", source = "ClienteRepository", method = "findAll")
    @PropertiesLabel(value = "factura.cliente", label = "Cliente")
    // Se mostrará en el HEADER alineado a la Izquierda
    @ModelReportLabel(label = "Cliente Beneficiario", section = ModelReportLabel.Section.HEADER, orientation = ModelReportLabel.Orientation.LEFT)
    private ClienteModel clienteModel;

    @ViewDataTable(editableRowMaster = false, showRowInMasterTable = false, row="productoId, precio, cantidad, total", editablerow="productoId, cantidad", source="FacturaRepository", method="getLineas")
    @PropertiesLabel(value = "factura.lineas", label = "Detalle de Líneas")
    private List<LineaFacturaModel> lineaFacturaModel;

    @PropertiesLabel(value = "factura.descuento", label = "Descuento")
    // Se mostrará al final del reporte (LASTPAGE) centrado
    @ModelReportLabel(label = "Descuento Aplicado", section = ModelReportLabel.Section.LASTPAGE, orientation = ModelReportLabel.Orientation.CENTER)
    private Double descuento;

    @PropertiesLabel(value = "factura.total", label = "Total")
    // Se mostrará al final del reporte (LASTPAGE) alineado a la derecha
    @ModelReportLabel(label = "Gran Total Factura", section = ModelReportLabel.Section.LASTPAGE, orientation = ModelReportLabel.Orientation.RIGHT)
    private Double total;

}
```

## Consideraciones

1.  Esta anotación es evaluada por el framework `JettraMVC` cuando se solicita la impresión de un registro individual (`id` proporcionado en la URL) y se utiliza el motor de reportes integrado de Jettra.
2.  Si un campo no tiene la anotación `@ModelReportLabel`, el framework usará el comportamiento predeterminado que es agregar el campo como una etiqueta en el `HEADER` alineado a la izquierda (`LEFT`).
3.  Los campos marcados con `@Hidden` u otros campos internos (como `serialVersionUID` o la propia lista de detalles) son excluidos automáticamente del reporte y no procesarán la anotación `@ModelReportLabel`.
