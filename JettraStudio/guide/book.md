# Guía Oficial de JettraStudio 🎨

Bienvenido a **JettraStudio**, el framework de componentes web basado en plantillas HTML y clases Java para el ecosistema **Jettra**, diseñado específicamente para aprovechar al máximo **Java 25+**.

JettraStudio implementa una arquitectura orientada a componentes donde la presentación en HTML5 estándar es completamente válida y se enlaza de forma bidireccional y limpia con la lógica en Java mediante identificadores:
- **Cero XML y cero configuración pesada**: Todo se resuelve por convención y tipado fuerte.
- **HTML5 estándar y previsualizable**: Las plantillas HTML pueden abrirse directamente en cualquier navegador o herramienta de diseño sin servidor.
- **Java 25 nativo**: Soporte fluido para *Records*, *Pattern Matching*, *Lambdas* y *Virtual Threads (Project Loom)*.
- **Compatibilidad total con temas JettraFlux**: Integra inmediatamente los temas visuales del ecosistema (**Games**, **Police**, **Futuristic**, **Espresso**, **Theme3D**, **Ocean**, **Matrix**, etc.).
- **Internacionalización (i18n) transparente**: Etiquetas de traducción `<jettras:message key="..."/>` y cambio de idioma en tiempo de ejecución con `LocalizationManager`.
- **Herencia visual con plantillas base**: Estructura de diseño maestro jerárquico con `<jettras:child/>` y páginas hijas `BasePage`.

---

## 1. Arquitectura y Filosofía

En JettraStudio, la interfaz de usuario se construye desacoplando limpiamente la estructura visual (HTML5) del comportamiento y estado de la aplicación (Java):

### Plantilla HTML (`HomePage.html`):
```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8" />
    <title>JettraStudio App</title>
</head>
<body>
    <div jettras:id="helloMessage">
        [El texto dinámico reemplazará esto]
    </div>
</body>
</html>
```

### Clase Java (`HomePage.java`):
```java
package com.myapp.pages;

import io.jettra.studio.core.WebPage;
import io.jettra.studio.components.Label;

public class HomePage extends WebPage {
    public HomePage() {
        add(new Label("helloMessage", "¡Hola desde JettraStudio!"));
    }
}
```

### Ventajas clave de JettraStudio:
1. **Los archivos HTML son 100% previsualizables en navegadores y editores**: Los diseñadores web pueden maquetar la página sin necesidad de aprender sintaxis de plantillas complejas ni mezclar lógica de script en el HTML. El atributo `jettras:id` es un atributo HTML válido que los navegadores ignoran durante la maquetación estática.
2. **Seguridad y Tipado Fuerte**: Cada componente es un objeto Java en memoria, protegiendo contra errores tipográficos y ataques de inyección (XSS) mediante escape automático de strings y control del ciclo de vida.
3. **Reutilización y Composición**: Los componentes pueden anidarse (`MarkupContainer`), agruparse en paneles modulares (`Panel`) o repetirse en bucles dinámicos (`ListView`).
4. **Alto Rendimiento con Virtual Threads**: Cada petición HTTP se procesa de forma concurrente y ultraligera utilizando Virtual Threads de Java 25.

---

## 2. El Sistema de Modelos (`IModel<T>`)

Los componentes en JettraStudio están desacoplados de los datos subyacentes gracias a la interfaz `IModel<T>`. Esto permite enlazar propiedades reactivas, objetos de base de datos o *Records* de Java:

### 2.1 `Model.of(value)`
Modelo simple que almacena directamente una referencia al objeto:
```java
IModel<String> statusModel = Model.of("Activo");
statusModel.setObject("Inactivo");
```

### 2.2 `PropertyModel.of(target, propertyName)`
Accede de manera reflexiva a getters/setters de Java Beans clásicos y campos de **Records de Java 25**:
```java
// Con Java 25 Records:
public record Jugador(String apodo, int nivel, double saldo) {}

Jugador player = new Jugador("PixelHunter", 42, 1550.50);

// Lee directamente player.apodo()
IModel<String> apodoModel = PropertyModel.of(player, "apodo");

// Lee directamente player.nivel()
IModel<Integer> nivelModel = PropertyModel.of(player, "nivel");
```

### 2.3 `LambdaModel.of(getter, setter)`
Permite enlazar cualquier variable o método con expresiones lambda:
```java
IModel<String> timeModel = LambdaModel.of(
    () -> LocalDateTime.now().format(DateTimeFormatter.ISO_TIME),
    null // de solo lectura
);
```

### 2.4 `ResourceModel.of("clave.i18n")`
Enlaza el texto a un archivo de recursos de internacionalización según el idioma activo del usuario.

---

## 3. Catálogo de Componentes de JettraStudio

JettraStudio cuenta con una amplia suite de componentes listos para utilizar:

| Componente | HTML Recomendado | Propósito y Capacidades |
|---|---|---|
| `Label` | `<span>`, `<div>`, `<h1>`... | Renderiza texto escapado de forma segura dentro del elemento HTML. |
| `MultiLineLabel` | `<div>`, `<p>` | Convierte saltos de línea `\n` en `<br/>` automáticamente. |
| `Button` | `<button>`, `<input>`, `<a>` | Botón interactivo con variantes de tema (`GOLD`, `BLUE`, `LIME`, `RED`, `PURPLE`, `DARK`, `PRIMARY`). |
| `Link` | `<a>` | Enlace hipertexto con destino `href` configurable y soporte de clics. |
| `TextField<T>` | `<input type="text">` | Campo de entrada tipado con `placeholder`, `required` y validación. |
| `TextArea<T>` | `<textarea>` | Área de texto multilinea con control de filas (`rows`) y columnas (`cols`). |
| `CheckBox` | `<input type="checkbox">` | Selector booleano que gestiona automáticamente el atributo `checked`. |
| `Select<T>` | `<select>` | Lista desplegable con opciones tipadas (`<option>`). |
| `Form<T>` | `<form>` | Contenedor de formularios con método de envío (`POST`/`GET`) y procesamiento de submit. |
| `Card` | `<div>` | Tarjeta estilizada con cabecera, título, subtítulo e insignia (`badge`). |
| `Alert` | `<div>` | Alerta contextual con estilos automáticos (`SUCCESS`, `DANGER`, `WARNING`, `INFO`). |
| `Modal` | `<div>` | Ventana emergente con botón de cierre integrado y estilo centrado. |
| `Table<T>` | `<table>` | Tabla de datos paginable que renderiza filas y columnas a partir de colecciones. |
| `ListView<T>` | `<tbody>`, `<div>`, `<ul>` | Repetidor dinámico para iteración y renderizado de colecciones. |
| `FeedbackPanel` | `<div>`, `<ul>` | Panel para mostrar mensajes de validación y notificaciones al usuario. |
| `Image` | `<img>` | Control de imagen con soporte de `src`, `alt`, dimensiones y estilos. |
| `Panel` | `<div>` | Componente modular reutilizable con su propia plantilla HTML embebible. |

---

## 4. Ejemplos de Uso de Componentes

### 4.1 Botones con Temas Visuales
```java
Button btnGuardar = Button.of("btnGuardar", "Guardar Partida", () -> {
    System.out.println("Guardando partida del jugador...");
}).variant(Button.Variant.GOLD);

Button btnCancelar = Button.of("btnCancelar", "Cancelar")
    .variant(Button.Variant.RED);
```
HTML asociado:
```html
<button jettras:id="btnGuardar">Guardar</button>
<button jettras:id="btnCancelar">Cancelar</button>
```

### 4.2 Repetidor Dinámico `ListView`
Para renderizar listas y colecciones de elementos:
```java
List<Jugador> jugadores = List.of(
    new Jugador("Ares", 80, 4200.0),
    new Jugador("Valkiria", 75, 3100.0),
    new Jugador("Fenrir", 90, 9999.0)
);

add(new ListView<Jugador>("listaJugadores", Model.ofList(jugadores)) {
    @Override
    protected void populateItem(ListItem<Jugador> item) {
        Jugador j = item.getModelObject();
        item.add(new Label("apodo", j.apodo()));
        item.add(new Label("nivel", "Nivel: " + j.nivel()));
        item.add(new Label("saldo", String.format("$%.2f", j.saldo())));
    }
});
```
HTML asociado:
```html
<div class="ranking-container">
    <div jettras:id="listaJugadores" class="jugador-card">
        <h3 jettras:id="apodo">Nombre Jugador</h3>
        <span jettras:id="nivel">Nivel 1</span>
        <strong jettras:id="saldo">$0.00</strong>
    </div>
</div>
```

---

## 5. Plantillas Maestras y Herencia Visual (`BasePage`)

JettraStudio incluye un sistema de diseño maestro jerárquico. Una página base define la cabecera, el menú de navegación, el selector de temas y el pie de página común, declarando el punto de inserción con `<jettras:child/>`:

### Plantilla Base (`BasePage.html`):
```html
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>JettraStudio Dashboard</title>
</head>
<body>
    <header class="jettra-base-header">
        <div class="logo">⚡ JettraStudio Dashboard</div>
        <div class="actions">
            <!-- Selector visual de temas autoinyectado -->
            <jettras:theme-selector/>
        </div>
    </header>

    <main class="jettra-base-main">
        <!-- AQUÍ SE INYECTA EL CONTENIDO DE LA PÁGINA HIJA -->
        <jettras:child/>
    </main>

    <footer class="jettra-base-footer">
        JettraStudio &copy; 2026 Ecosistema Jettra
    </footer>
</body>
</html>
```

### Página Hija (`DashboardPage.java`):
```java
package com.myapp.pages;

import io.jettra.studio.core.BasePage;
import io.jettra.studio.components.*;

public class DashboardPage extends BasePage {
    public DashboardPage() {
        setPageTitle("Panel de Control - JettraStudio");

        add(new Label("statusServer", "En línea - 99.98%"));
        add(Button.of("btnReboot", "Reiniciar Nodo").variant(Button.Variant.RED));
        add(Alert.success("alertBox", "Todos los servicios operan con normalidad"));
    }
}
```

### Plantilla Hija (`DashboardPage.html`):
```html
<div class="dashboard-grid">
    <div jettras:id="alertBox">Alerta del sistema</div>
    
    <div class="metric-card">
        <h4>Estado del Clúster:</h4>
        <div jettras:id="statusServer">Cargando...</div>
        <button jettras:id="btnReboot">Reiniciar</button>
    </div>
</div>
```

Al renderizar `DashboardPage`, JettraStudio combinará automáticamente la estructura del layout padre con el cuerpo de la página hija.

---

## 6. Soporte de Idiomas e Internacionalización (i18n)

JettraStudio soporta traducción dinámica mediante el gestor `LocalizationManager` y la enumeración `LanguageStudio`:

### 6.1 Idiomas Soportados
- `ES` (Español) - Idioma por defecto
- `EN` (Inglés)
- `PT` (Portugués)
- `FR` (Francés)
- `DE` (Alemán)
- `IT` (Italiano)

### 6.2 Archivos de Recursos
En el directorio de recursos (`src/main/resources`):
- `messages_es.properties`:
  ```properties
  app.welcome=¡Bienvenido a JettraStudio!
  btn.save=Guardar Cambios
  status.online=Servidor Activo
  ```
- `messages_en.properties`:
  ```properties
  app.welcome=Welcome to JettraStudio!
  btn.save=Save Changes
  status.online=Server Active
  ```

### 6.3 Uso en HTML
Utiliza la etiqueta de traducción directa:
```html
<h1><jettras:message key="app.welcome"/></h1>
<button jettras:id="btnSave"><jettras:message key="btn.save"/></button>
```

### 6.4 Cambio de Idioma por Código
```java
LocalizationManager.getInstance().setCurrentLanguage(LanguageStudio.EN);
```

---

## 7. Integración de Temas de JettraFlux

JettraStudio aprovecha la rica paleta estética de **JettraFlux**, inyectando dinámicamente las variables CSS y tokens de diseño:

### Temas Disponibles:
1. **Games Theme** (`"games"`): El espectacular tema HUD 3D de videojuegos (oro, azul cobalto, marcos angulados, neón cibernético).
2. **Police Theme** (`"police"`): Tema táctico institucional y contrastado.
3. **Core Theme** (`"core"`): Tema sobrio corporativo y funcional.
4. **SL Theme** (`"sl"`): Tema ultramoderno minimalista.
5. **Heroes Theme** (`"heroes"`): Gradientes enérgicos y dinámicos.
6. **Matrix Theme** (`"matrix"`): Estilo ciberespacio fosforescente en verde terminal.
7. **Retro Theme** (`"retro"`): Paleta cálida nostálgica vintage.
8. **Flat Theme** (`"flat"`): Minimalismo moderno plano.
9. **Theme 3D** (`"theme3d"`): Neumorfismo con sombras suaves y profundidad.
10. **Futuristic Theme** (`"futuristic"`): Neones púrpuras y cianes sobre fondo oscuro.
11. **Ast Theme** (`"ast"`): Tonos astronómicos profundos.
12. **Atlantis Theme** (`"atlantis"`): Tonos acuáticos profundos y cian.
13. **Ocean Theme** (`"ocean"`): Tonos marinos refrescantes.
14. **Dark Theme** (`"dark"`): Modo oscuro de alto contraste.

### 7.1 Selector de Tema en HTML
Simplemente agrega la etiqueta en cualquier parte de tu plantilla:
```html
<jettras:theme-selector/>
```
JettraStudio generará un menú desplegable interactivo que permite al usuario alternar entre temas al instante, actualizando las variables CSS en el elemento `<head>`.

### 7.2 Configuración de Tema en la Página
```java
public class MyPage extends WebPage {
    public MyPage() {
        setTheme("games"); // Aplica el tema Games por defecto
    }
}
```

---

## 8. Integración con el Servidor (`JettraServer` / HTTP Server)

JettraStudio se integra de forma directa con los servidores HTTP del ecosistema Jettra (`JettraServer` o `com.sun.net.httpserver.HttpServer`):

```java
package com.myapp;

import com.sun.net.httpserver.HttpServer;
import io.jettra.studio.server.StudioHandler;
import io.jettra.studio.server.StudioServerHelper;
import com.myapp.pages.DashboardPage;
import com.myapp.pages.HomePage;

import java.net.InetSocketAddress;

public class App {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Registro de rutas y páginas JettraStudio
        server.createContext("/", new StudioHandler(HomePage.class));
        server.createContext("/dashboard", new StudioHandler(DashboardPage.class));

        server.setExecutor(null);
        server.start();

        System.out.println("🚀 Servidor JettraStudio iniciado en http://localhost:8080");
    }
}
```

También es posible renderizar páginas directamente a un String para testing o envío personalizado:
```java
HomePage page = new HomePage();
String html = page.renderPage();
```

---

## 9. Ejemplo Completo: Módulo de Gestión de Inventario

### Clase Java: `InventoryPage.java`
```java
package com.myapp.pages;

import io.jettra.studio.core.BasePage;
import io.jettra.studio.components.*;
import io.jettra.studio.model.Model;
import java.util.ArrayList;
import java.util.List;

public class InventoryPage extends BasePage {

    public record ItemInventario(String codigo, String nombre, int stock, double precio) {}

    private final List<ItemInventario> items = new ArrayList<>(List.of(
        new ItemInventario("ITM-001", "Poción de Maná", 45, 12.50),
        new ItemInventario("ITM-002", "Espada Láser", 3, 499.00),
        new ItemInventario("ITM-003", "Escudo de Titanio", 8, 250.00)
    ));

    public InventoryPage() {
        setPageTitle("Inventario Central - JettraStudio");
        setTheme("games");

        // Alerta de estado
        add(Alert.info("alertaStock", "3 ítems disponibles en la armería"));

        // Tabla de ítems con ListView
        add(new ListView<ItemInventario>("filasItems", Model.ofList(items)) {
            @Override
            protected void populateItem(ListItem<ItemInventario> item) {
                ItemInventario itm = item.getModelObject();
                item.add(new Label("codigo", itm.codigo()));
                item.add(new Label("nombre", itm.nombre()));
                item.add(new Label("stock", String.valueOf(itm.stock())));
                item.add(new Label("precio", String.format("$%.2f", itm.precio())));
                item.add(Button.of("btnComprar", "Adquirir", () -> {
                    System.out.println("Comprando item: " + itm.nombre());
                }).variant(Button.Variant.GOLD));
            }
        });

        // Modal para nuevo ítem
        Modal modal = Modal.of("modalNuevo", "Agregar Nuevo Artefacto");
        add(modal);

        add(Button.of("btnAbrirModal", "+ Nuevo Ítem", modal::open)
            .variant(Button.Variant.LIME));
    }
}
```

### Plantilla HTML: `InventoryPage.html`
```html
<div class="inventory-container" style="max-width: 1000px; margin: 0 auto;">
    <div jettras:id="alertaStock">Avisos del inventario</div>

    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:16px;">
        <h2>Inventario de Artículos</h2>
        <button jettras:id="btnAbrirModal">+ Nuevo Ítem</button>
    </div>

    <table class="jettra-table" style="width:100%; border-collapse:collapse;">
        <thead>
            <tr>
                <th>Código</th>
                <th>Descripción</th>
                <th>Cantidad</th>
                <th>Precio</th>
                <th>Acciones</th>
            </tr>
        </thead>
        <tbody>
            <tr jettras:id="filasItems">
                <td jettras:id="codigo">ITM-000</td>
                <td jettras:id="nombre">Nombre</td>
                <td jettras:id="stock">0</td>
                <td jettras:id="precio">$0.00</td>
                <td><button jettras:id="btnComprar">Comprar</button></td>
            </tr>
        </tbody>
    </table>

    <div jettras:id="modalNuevo">
        <form style="display:flex; flex-direction:column; gap:12px;">
            <label>Nombre del Ítem:</label>
            <input type="text" class="espresso-textfield" placeholder="Ej: Arco de plasma" />
            <button type="submit" class="games-btn-gold">Guardar</button>
        </form>
    </div>
</div>
```

---

## 10. Resumen de Buenas Prácticas

1. **Mantén los archivos HTML limpios**: No escribas scripts complejos dentro del HTML; usa `jettras:id` para delegar el control a la clase Java.
2. **Usa `BasePage` para layouts consistentes**: Define el encabezado, selector de temas y pie de página en una sola plantilla maestra y reutilízala en todas las pantallas.
3. **Aprovecha los Records de Java 25**: Combina `PropertyModel.of(recordInstance, "campo")` para obtener enlace automático de propiedades sin código repetitivo.
4. **Utiliza variantes de botones y temas visuales**: Personaliza la experiencia con `Button.Variant.GOLD`, `Button.Variant.BLUE`, etc., asegurando coherencia visual con el resto del ecosistema Jettra.
