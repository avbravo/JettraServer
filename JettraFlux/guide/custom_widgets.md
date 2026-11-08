# Componentes de JettraFlux (Widgets Personalizados)

A continuación se documentan los nuevos componentes creados en el paquete `io.jettra.flux.widgets` para simplificar la creación de plantillas como `TemplatePage` y evitar el uso de HTML puro.

## 1. SidebarLogo

Este widget encapsula el logo de la barra lateral (típicamente usado en plantillas tipo Atlantis). Muestra un ícono y un texto dentro de un contenedor `div`.

- **Clase CSS:** `sidebar-logo`
- **Uso:** `io.jettra.flux.widgets.SidebarLogo.of("fas fa-layer-group", "Atlantis")`

## 2. SidebarCategory

Se utiliza para definir el texto separador o encabezado de categoría en la barra lateral.

- **Clase CSS:** `sidebar-category`
- **Uso:** `io.jettra.flux.widgets.SidebarCategory.of("Dashboards")`

## 3. ActionIcon

Proporciona un ícono simple de FontAwesome (u otra biblioteca) que ejecuta directamente una función de JavaScript (`onclick="..."`) en el navegador.

- **Uso:** `io.jettra.flux.widgets.ActionIcon.of("fas fa-bars top-bars-icon", "toggleSidebar()")`

## 4. TopAvatar

Representa un avatar circular (generalmente para el usuario logueado) en la barra superior.

- **Clase CSS:** `top-avatar`
- **Uso:** `io.jettra.flux.widgets.TopAvatar.of("U")`

## 5. ActionButton

Un botón con un ícono y un texto, diseñado principalmente para las acciones rápidas de la barra superior, como el botón de "Today".

- **Clase CSS:** `top-btn-today`
- **Uso:** `io.jettra.flux.widgets.ActionButton.of("fas fa-calendar", "Today")`

## 6. StatCard

Tarjeta diseñada específicamente para los cuadros de estadísticas principales en el Dashboard (ej. "Conversion Rate", "Avg. Order Value"). Incluye título, porcentaje de cambio (positivo o negativo), y el valor de la métrica.

- **Clases CSS:** `stat-card`, `stat-header`, `stat-badge up/down`, `stat-value`
- **Uso:** `io.jettra.flux.widgets.StatCard.of("Conversion Rate", "0.8%", "0.81%", false)` (El último parámetro indica si la flecha apunta hacia arriba y si es verde).

## E-Commerce & Forms Extensions

### Dropdown
- **Description:** A stylized select element for single-item selection from a list of strings.
- **Props:** `options` (varargs `String`), `placeholder` (String).
- **Events:** Controlled externally via standard form submission or JS.
- **Usage:**
  ```java
  Widget myDropdown = Dropdown.of("Option 1", "Option 2").placeholder("Select one...");
  ```

### InputNumber
- **Description:** A numeric input field styled with increment and decrement buttons, hiding native spin buttons.
- **Props:** `value` (int) initial value.
- **Usage:**
  ```java
  Widget quantity = InputNumber.of(1);
  ```

### Rating
- **Description:** A visual star rating component.
- **Props:** `value` (int) 1 to 5 stars.
- **Usage:**
  ```java
  Widget myRating = Rating.of(4);
  ```

### Editor
- **Description:** A text area with a visual (mocked) rich-text toolbar to match the Atlantis design.
- **Props:** `content` (String) initial content.
- **Usage:**
  ```java
  Widget editor = Editor.of("Initial text...");
  ```
