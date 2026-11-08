# @RenderedCrud en JettraFlux

La anotación `@RenderedCrud` permite la generación de código en tiempo de compilación para interfaces de usuario completas de mantenimiento de datos (CRUD) integradas de forma nativa con **JettraFlux**. Ha reemplazado por completo el uso de la antigua `@CrudView` de `JettraWUI`.

## Requisitos

1. Asegúrate de que tu modelo esté correctamente mapeado (por ejemplo, con `@FluxModelToRecordConversor`).
2. El repositorio debe contener métodos como `findAll`, `save`, `delete`, etc.
3. El proyecto debe depender de `JettraFlux` y `JettraReport` si se van a generar visores de impresión.

## Uso Básico

Aplica la anotación sobre una interfaz que extienda tu página base (como `TemplatePage` en el caso de `JettraFluxExample`).

```java
import io.jettra.flux.annotations.crud.RenderedCrud;
import com.flux.example.pages.template.TemplatePage;

@RenderedCrud(
    model = PlanetaModel.class, 
    repository = PlanetaRepository.class,
    title = "Gestión de Planetas",
    editable = true,
    report = true,
    reportTitle = "Reporte de Planetas"
)
public interface PlanetaPageDef extends TemplatePage {
    // Si necesitas lógica adicional después de que se arme el CRUD
    default void afterBuildCenter(HttpExchange exchange, Map<String, String> params, String currentTheme) {
        // Lógica personalizada aquí
    }
}
```

## Características Generadas

El procesador `RenderedCrudProcessor` analizará la interfaz en tiempo de compilación y generará una implementación (`PlanetaPageDefImpl`) que hereda automáticamente de la clase base (`TemplatePage`) y sobrescribe su método `buildCenter`.

Esta implementación inyectará de forma nativa los siguientes componentes de **JettraFlux**:

- **Barra de Herramientas**: Botones para crear un *Nuevo Registro* o *Imprimir*.
- **Datatable (Tabla de Datos)**: Generada mediante `Datatable.ofWidgets(...)`. Contará con una columna inicial de "Selección" (Checkbox), columnas dinámicas extraídas del Modelo, y una columna final de "Acciones" (con botones *Editar* y *Eliminar*).
- **Modales de Edición y Creación**: Generados con `Modal.of(...)`.
- **Visor de Reportes**: Integración transparente con `ReportViewer` de `JettraReport`.

## Atributos Principales

- `model`: Clase del modelo que contiene los datos a reflejar en las columnas de la tabla.
- `repository`: Repositorio o fuente de datos a inyectar en el *Handler* asociado (`*RenderedCrudHandler`).
- `editable`: Si es `true`, generará botones de edición y eliminación.
- `report`: Si es `true`, generará un botón que lanza la previsualización del visor integrado (`ReportViewer`).
- `title`: Título de la vista en el dashboard.

## Ventajas sobre JettraWUI
Al usar JettraFlux, todo el renderizado evita el uso de Reflection costoso en tiempo de ejecución. Cada widget y tabla se genera como código Java tipado y directo, lo cual brinda alta performance y mayor facilidad para depuración o personalización inyectando lógica en el `afterBuildCenter`.
