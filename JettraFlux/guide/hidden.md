# JettraFlux - Hidden / InputHidden Component Guide

El componente `Hidden` (y su alias `InputHidden`) de **JettraFlux** permite representar y gestionar campos de formulario HTML `<input type="hidden">` de forma tipada, segura y fluida en Java puro, sin necesidad de concatenar cadenas HTML manuales ni usar `RawHtml`.

---

## 1. Importación

```java
import io.jettra.flux.widgets.Hidden;
// O alternativamente:
import io.jettra.flux.widgets.InputHidden;
```

---

## 2. Creación Básica

### Con Nombre y Valor Inicial:
```java
Hidden actionField = Hidden.of("action", "create_user");
Hidden dbField = Hidden.of("target_db", "customers_db");
```

### Con Nombre y Asignación Fluida:
```java
Hidden userId = Hidden.of("user_id").value("usr_10293");
```

### Estableciendo ID o Modificadores:
```java
Widget deleteUserHidden = Hidden.of("user_id")
    .id("delUserId");
```

---

## 3. Uso en Formularios (`Form`)

### Ejemplo de Creación de Entidad:
```java
Widget form = Form.of(
    Hidden.of("action", "insert_object"),
    Hidden.of("target_db", targetDb),
    Div.of(
        Label.of("Document ID"),
        TextField.of("target_id", "doc_101")
    ),
    Button.of(Icon.of("fas fa-save"), Text.of(" Guardar"))
        .attribute("type", "submit")
).action("/api/document").method("POST");
```

### Ejemplo de Formularios Ocultos para JavaScript / Modales:
```java
Widget deleteForm = Form.of(
    Hidden.of("action", "delete_user"),
    Hidden.of("user_id").id("delUserId")
).action(JettraServer.resolvePath("/users"))
 .method("POST")
 .id("deleteUserForm")
 .modifier(new Modifier().style("display:none;"));
```

---

## 4. Métodos Principales

| Método | Retorno | Descripción |
| :--- | :--- | :--- |
| `Hidden.of(String name, Object value)` | `Hidden` | Crea un campo oculto con nombre y valor especificado |
| `Hidden.of(String name)` | `Hidden` | Crea un campo oculto con nombre y valor vacío |
| `.value(Object value)` | `Hidden` | Asigna o actualiza el valor del campo |
| `.binding(String property)` | `Hidden` | Vincula el campo con propiedades del ViewModel/State |
| `.id(String id)` | `Widget` | Asigna el atributo `id` al elemento HTML |
| `.modifier(Modifier m)` | `Widget` | Aplica estilos CSS o atributos personalizados |

---

## 5. Salida HTML Generada

```html
<input type="hidden" name="action" value="create_user" class="espresso-hidden"/>
```
