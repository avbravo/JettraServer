# FluxBinding en JettraFlux

El `FluxBinding` es una herramienta de JettraFlux para implementar **enlace de datos bidireccional** entre la vista y el modelo. Automatiza la captura de parámetros HTTP hacia objetos de dominio y ejecuta automáticamente el motor de validación de `JettraRules` y el de cómputo (`@Compute`).

## 1. Anotación del Modelo

Debes anotar tu variable de modelo con `@FluxBinding`.

```java
import io.jettra.flux.annotations.binding.FluxBinding;

public class FormsPage extends TemplatePage {

    @FluxBinding(model = PersonModel.class)
    PersonModel personModel = new PersonModel();
    
    // ...
}
```

## 2. Marcado de los Componentes (Widgets)

Para que el modelo se enchufe adecuadamente a la vista, los componentes del formulario como `TextField` deben identificar su propiedad correspondiente del modelo usando los métodos encadenables `.id()` y `.binding()`.

```java
TextField.of(msg.getProperty("person.name"), "Enter your name")
    .id("name")
    .binding("name")
    .modifier(new Modifier().style("margin-bottom: 15px; width: 100%;"));
```
Internamente `.binding()` asignará el atributo `data-bind="name"` al HTML generado.

## 3. Vinculación en el Evento de Acción (Action)

En tu método de acción asociado, puedes instanciar el `FluxBinder`, el cual tomará el modelo y los parámetros, inyectará los valores directamente en los campos del modelo, ejecutará `@Compute` y validará según las `@Rules`.

```java
@ActionWidgetAllow(role={"ADMIN","MANAGER"})
private void saveForm(HttpExchange exchange, Map<String, String> params) {
    io.jettra.rules.core.RuleResult result = new io.jettra.flux.binding.FluxBinder(personModel)
        .bind(params)
        .compute()
        .validate();
        
    if (!result.isValid()) {
        // Enviar notificación de error
        IO.println("Error de validación: " + result.getMessage());
    } else {
        // Proceder con el guardado
        io.jettra.flux.sync.JettraSyncManager.notifyChange("FormsModel", io.jettra.flux.sync.SyncType.UPDATE, getLoggedUser(exchange));
    }
}
```

## Beneficios
- **Código Limpio**: Ya no es necesario acceder a `params.get("name")` y parsear cadenas de texto a enteros, dobles, etc., manualmente.
- **Validación Automática**: Si `PersonModel` tiene anotaciones de JettraRules (ej. `@NotNull`, `@Size`), se evaluarán sin código extra.
- **Cálculo Automático**: Evaluará métodos `@Compute` si el modelo lo requiere (ej. Subtotales).
