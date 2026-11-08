# JettraStudio 🎨

**JettraStudio** es el framework web moderno, tipado y basado en componentes Java 25+ y plantillas HTML5 puras del ecosistema **Jettra**.

Permite construir aplicaciones web ricas, seguras y altamente mantenibles manteniendo una separación estricta entre la presentación (HTML5 estándar) y la lógica de negocio (Java).

---

## Características Principales

- **Pureza HTML5**: Plantillas 100% previsualizables en navegadores estándar mediante el namespace `jettras:id`.
- **Desacoplamiento Total**: Las plantillas HTML no contienen código Java ni expresiones de script complejas.
- **Java 25 Nativo**: Soporte de primera clase para *Records*, *Pattern Matching*, *Lambdas* y *Virtual Threads (Loom)*.
- **Modelos Reactivos**: Abstracción `IModel<T>` con implementaciones directas (`Model`, `PropertyModel`, `LambdaModel`, `ResourceModel`).
- **Catálogo Completo de Componentes**:
  - Formularios y Controles: `TextField`, `TextArea`, `CheckBox`, `Select`, `Button`, `Form`.
  - Presentación y Tipografía: `Label`, `MultiLineLabel`, `Link`, `Image`.
  - Estructura y Contenedores: `Card`, `Modal`, `Alert`, `FeedbackPanel`, `Panel`.
  - Listas y Colecciones: `ListView`, `ListItem`, `Table`.
- **Temas JettraFlux**: Integración directa con los 14 temas visuales (`games`, `police`, `futuristic`, `espresso`, `theme3d`, `matrix`, `retro`, etc.) y cambio dinámico con `<jettras:theme-selector/>`.
- **Diseño Maestro (`BasePage`)**: Jerarquía de plantillas y layouts con punto de inserción `<jettras:child/>`.
- **Internacionalización**: Gestión multilingüe mediante `LocalizationManager` y `<jettras:message key="..."/>`.

---

## Inicio Rápido

### Dependencia Maven

```xml
<dependency>
    <groupId>io.jettra</groupId>
    <artifactId>JettraStudio</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 1. Plantilla HTML (`HomePage.html`)
```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8" />
    <title>JettraStudio App</title>
</head>
<body>
    <h1 jettras:id="titulo">Título por defecto</h1>
    <button jettras:id="btnAccion">Ejecutar</button>
</body>
</html>
```

### 2. Clase Java (`HomePage.java`)
```java
package com.myapp.pages;

import io.jettra.studio.core.WebPage;
import io.jettra.studio.components.Label;
import io.jettra.studio.components.Button;

public class HomePage extends WebPage {
    public HomePage() {
        add(new Label("titulo", "¡Bienvenido a JettraStudio!"));
        add(Button.of("btnAccion", "Hacer Clic", () -> {
            System.out.println("Acción ejecutada!");
        }).variant(Button.Variant.GOLD));
    }
}
```

### 3. Servidor HTTP
```java
import com.sun.net.httpserver.HttpServer;
import io.jettra.studio.server.StudioHandler;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new StudioHandler(HomePage.class));
        server.start();
        System.out.println("Servidor iniciado en http://localhost:8080");
    }
}
```

---

## Documentación Completa

Para la guía detallada de componentes, modelos y diseño maestro, consulta [guide/book.md](guide/book.md).
