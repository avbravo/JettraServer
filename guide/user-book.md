# Guía Oficial de Usuario: JettraServer 1.0

Servidor de Aplicaciones Empresariales y Microservicios de Ultra-Alto Rendimiento optimizado para Java 25 y Google Antigravity IDE.

---

## Contenido

1. [Introducción y Filosofía](#1-introducción-y-filosofía)
2. [Requisitos y Configuración del Entorno](#2-requisitos-y-configuración-del-entorno)
3. [Arranque Rápido (Quickstart)](#3-arranque-rápido-quickstart)
4. [Selector de los Tres Modos de Almacenamiento](#4-selector-de-los-tres-modos-de-almacenamiento)
   - [4.1 Modo JVM_RAM](#41-modo-jvm_ram)
   - [4.2 Modo DISK_MEMORY](#42-modo-disk_memory)
   - [4.3 Modo OFF_HEAP](#43-modo-off_heap)
   - [4.4 Configuración Dinámica y Vía Entorno](#44-configuración-dinámica-y-vía-entorno)
5. [Desarrollo de Servicios Web con Jakarta REST](#5-desarrollo-de-servicios-web-con-jakarta-rest)
6. [Inyección de Dependencias con Jakarta CDI](#6-inyección-de-dependencias-con-jakarta-cdi)
7. [Persistencia y Acceso a Datos con Jakarta Persistence (JPA)](#7-persistencia-y-acceso-a-datos-con-jakarta-persistence-jpa)
8. [Transacciones con Jakarta Transactions (JTA)](#8-transacciones-con-jakarta-transactions-jta)
9. [WebSockets y Comunicación Bidireccional](#9-websockets-y-comunicación-bidireccional)
10. [Seguridad y Control de Acceso con JettraJWT](#10-seguridad-y-control-de-acceso-con-jettrajwt)
11. [Concurrencia con Hilos Virtuales (Project Loom)](#11-concurrencia-con-hilos-virtuales-project-loom)
12. [Mensajería Asíncrona (Jakarta JMS / JmsBroker)](#12-mensajería-asíncrona-jakarta-jms--jmsbroker)
13. [Validación de Datos (Jakarta Bean Validation)](#13-validación-de-datos-jakarta-bean-validation)
14. [Procesamiento JSON (JSON-P y JSON-B)](#14-procesamiento-json-json-p-y-json-b)
15. [Desarrollo Frontend Reactivo Tipado con JettraFlux](#15-desarrollo-frontend-reactivo-tipado-con-jettraflux)
16. [Pruebas Automatizadas con JettraTest](#16-pruebas-automatizadas-con-jettratest)
17. [Despliegue en Docker y Docker Compose con ZGC](#17-despliegue-en-docker-y-docker-compose-con-zgc)

---

## 1. Introducción y Filosofía

**JettraServer** es un servidor de aplicaciones modular, moderno y de huella ultraligera concebido desde cero para sacar el máximo provecho de **Java 25**, **Project Loom (Virtual Threads)** y la memoria moderna (*Foreign Function & Memory API*).

A diferencia de los servidores de aplicaciones Java tradicionales que requieren cientos de megabytes de RAM y varios segundos (o minutos) para arrancar, **JettraServer**:
- Arranca en frío (*cold start*) en menos de **2 milisegundos**.
- Soporta de forma nativa e integrada la suite completa de especificaciones **Jakarta EE** sin dependencias infladas.
- Ofrece un subsistema de almacenamiento de **triple modo intercambiable** (`JVM_RAM`, `DISK_MEMORY` y `OFF_HEAP`).
- Se integra de forma reactiva con el motor de interfaz de usuario en Java puro **`JettraFlux`**.
- Está 100% libre de JUnit, validado integralmente con el framework moderno **`JettraTest`**.

---

## 2. Requisitos y Configuración del Entorno

### Requisitos Mínimos
- **JDK:** BellSoft Liberica JDK 25 o compatible (con soporte `--enable-preview`).
- **Build Tool:** Apache Maven 3.9+ o Gradle 8.10+.
- **Sistema Operativo:** Linux (x86_64 o aarch64), macOS o Windows.

### Dependencias Maven
Para incluir `JettraServer` en tu proyecto, añade el BOM o los módulos específicos a tu `pom.xml`:

```xml
<dependencies>
    <!-- Servidor Núcleo y Motor NIO -->
    <dependency>
        <groupId>io.jettra</groupId>
        <artifactId>jettra-server-core</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>

    <!-- Especificaciones Jakarta EE Ligeras -->
    <dependency>
        <groupId>io.jettra</groupId>
        <artifactId>jettra-server-jakarta</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>

    <!-- Renderizado UI Tipado con JettraFlux -->
    <dependency>
        <groupId>io.jettra</groupId>
        <artifactId>jettra-server-flux</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>

    <!-- Framework de Pruebas JettraTest -->
    <dependency>
        <groupId>io.jettra</groupId>
        <artifactId>JettraTest</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## 3. Arranque Rápido (Quickstart)

Crear y arrancar un microservicio con `JettraServer` solo requiere unas pocas líneas de código en Java:

```java
package com.miempresa.app;

import io.jettra.server.core.JettraServer;
import io.jettra.server.core.config.ServerConfig;
import io.jettra.server.core.storage.StorageMode;

public class MainApp {
    public static void main(String[] args) {
        ServerConfig config = new ServerConfig()
            .setPort(8080)
            .setStorageMode(StorageMode.JVM_RAM)
            .setWorkers(Runtime.getRuntime().availableProcessors());

        JettraServer server = JettraServer.create(config);

        // Rutas básicas
        server.get("/hola", (req, res) -> res.body("¡Hola desde JettraServer en Java 25!"));
        
        server.get("/api/info", (req, res) -> res.json(java.util.Map.of(
            "app", "Mi Microservicio",
            "version", "1.0.0",
            "status", "UP"
        )));

        // Iniciar el servidor
        server.start();
    }
}
```

---

## 4. Selector de los Tres Modos de Almacenamiento

`JettraServer` incorpora una arquitectura única de persistencia con **tres motores especializados e intercambiables**:

```
                       ┌─────────────────────────────┐
                       │     JettraStorageEngine     │
                       │             SPI             │
                       └──────────────┬──────────────┘
                                      │
         ┌────────────────────────────┼───────────────────────────┐
         ▼                            ▼                           ▼
┌───────────────────┐      ┌─────────────────────┐     ┌─────────────────────┐
│      JVM_RAM      │      │     DISK_MEMORY     │     │      OFF_HEAP       │
├───────────────────┤      ├─────────────────────┤     ├─────────────────────┤
│ • Heap / Stack    │      │ • JettraMemory      │     │ • Arena Off-Heap    │
│ • UnifiedMap      │      │ • MemTable + Disk   │     │ • Páginas ranuradas │
│ • Cero I/O disco  │      │ • Persistencia LSM  │     │ • Cero presión GC   │
│ • Máx throughput  │      │ • E/S sincr/asincr  │     │ • Segmentos nativos │
└───────────────────┘      └─────────────────────┘     └─────────────────────┘
```

### 4.1 Modo JVM_RAM
- **Uso ideal:** Microservicios efímeros, capas de caché en memoria de ultra-alta velocidad, pruebas unitarias y mocks de base de datos.
- **Ventajas:** Latencia sub-microsegundo, cero overhead de serialización en disco.
- **Activación programática:**
```java
ServerConfig config = new ServerConfig().setStorageMode(StorageMode.JVM_RAM);
```

### 4.2 Modo DISK_MEMORY
- **Uso ideal:** Aplicaciones empresariales que requieren persistencia transaccional duradera en disco combinada con una estructura LSM MemTable en memoria intermedia (`JettraMemory`).
- **Ventajas:** Los datos sobreviven reinicios del servidor, registros compactados automáticamente en archivos de datos (.jdb).
- **Activación programática:**
```java
ServerConfig config = new ServerConfig()
    .setStorageMode(StorageMode.DISK_MEMORY)
    .setDataDirectory("/var/data/jettra");
```

### 4.3 Modo OFF_HEAP
- **Uso ideal:** Grandes volúmenes de datos en memoria (decenas o cientos de gigabytes) eliminando por completo las pausas de recolección de basura (*Garbage Collection*). Utiliza la *Foreign Function & Memory API* de Java 25 (`Arena.ofShared()`).
- **Ventajas:** Cero overhead de Garbage Collector, arquitectura de páginas ranuradas (*Slotted-Page*), máxima predictibilidad de latencia.
- **Activación programática:**
```java
ServerConfig config = new ServerConfig().setStorageMode(StorageMode.OFF_HEAP);
```

### 4.4 Configuración Dinámica y Vía Entorno
Puedes configurar el modo de almacenamiento sin modificar el código fuente:

1. **Vía Propiedad del Sistema Java (`-D`):**
   ```bash
   java -Djettra.storage.mode=OFF_HEAP -jar miapp.jar
   ```

2. **Vía Variable de Entorno:**
   ```bash
   export JETTRA_STORAGE_MODE=DISK_MEMORY
   export JETTRA_DATA_DIR=/datos/miapp
   java -jar miapp.jar
   ```

3. **Vía Archivo `jettra-server.properties` en el Classpath:**
   ```properties
   jettra.server.port=8080
   jettra.storage.mode=OFF_HEAP
   jettra.storage.dir=./data
   jettra.security.jwt.secret=mi_clave_secreta_super_segura_2026
   ```

---

## 5. Desarrollo de Servicios Web con Jakarta REST

El módulo `jettra-server-jakarta` provee soporte completo para anotaciones estándar de **Jakarta RESTful Web Services 3.1**:

```java
package com.miempresa.app.rest;

import io.jettra.server.jakarta.rest.RestAnnotations.*;
import java.util.Map;

@Path("/api/clientes")
@Produces("application/json")
@Consumes("application/json")
public class ClienteResource {

    @GET
    @Path("/{id}")
    public Map<String, Object> obtenerPorId(@PathParam("id") String id) {
        return Map.of("id", id, "nombre", "Corporación Alfa", "estado", "ACTIVO");
    }

    @POST
    public Map<String, Object> crear(Map<String, Object> payload) {
        return Map.of("mensaje", "Cliente registrado exitosamente", "datos", payload);
    }
}
```

Para registrar el recurso en el contenedor:
```java
JakartaContainer container = JakartaContainer.attach(server);
container.registerRestResource(new ClienteResource());
```

---

## 6. Inyección de Dependencias con Jakarta CDI

`JettraServer` incluye un contenedor de inyección de dependencias ligero compatible con **Jakarta CDI 4.1**:

```java
package com.miempresa.app.service;

import io.jettra.server.jakarta.cdi.CdiAnnotations.ApplicationScoped;
import io.jettra.server.jakarta.cdi.CdiAnnotations.Inject;

@ApplicationScoped
public class NotificacionService {
    public void notificar(String mensaje) {
        System.out.println("[NOTIFICACIÓN]: " + mensaje);
    }
}

@ApplicationScoped
public class PedidoService {
    @Inject
    private NotificacionService notificador;

    public void procesarPedido(String pedidoId) {
        notificador.notificar("Pedido " + pedidoId + " procesado.");
    }
}
```

---

## 7. Persistencia y Acceso a Datos con Jakarta Persistence (JPA)

El `EntityManager` de `JettraServer` se comunica de forma transparente con cualquiera de los tres modos de almacenamiento configurados (`JVM_RAM`, `DISK_MEMORY` u `OFF_HEAP`):

```java
package com.miempresa.app.entity;

import io.jettra.server.jakarta.persistence.JpaAnnotations.*;

@Entity
@Table(name = "productos")
public class Producto {
    @Id
    private String id;
    private String descripcion;
    private double precio;

    public Producto() {}
    public Producto(String id, String descripcion, double precio) {
        this.id = id;
        this.descripcion = descripcion;
        this.precio = precio;
    }

    // Getters y Setters
    public String getId() { return id; }
    public String getDescripcion() { return descripcion; }
    public double getPrecio() { return precio; }
}
```

Operaciones de persistencia:
```java
EntityManager em = container.getEntityManager();

// Inserción
Producto p = new Producto("PROD-001", "Servidor Rack 1U", 1250.00);
em.persist(p);

// Consulta por ID
Producto encontrado = em.find(Producto.class, "PROD-001");

// Actualización y Eliminación
em.remove(encontrado);
```

---

## 8. Transacciones con Jakarta Transactions (JTA)

Control transaccional declarativo y programático:

```java
UserTransaction utx = container.getUserTransaction();
try {
    utx.begin();
    em.persist(new Factura("FAC-001", 350.0));
    em.persist(new FacturaDetalle("FAC-001", "ITEM-A", 1, 350.0));
    utx.commit();
} catch (Exception e) {
    utx.rollback();
    throw e;
}
```

---

## 9. WebSockets y Comunicación Bidireccional

Soporte nativo para sockets bidireccionales con **Jakarta WebSocket 2.1**:

```java
package com.miempresa.app.ws;

import io.jettra.server.jakarta.websocket.WebSocketAnnotations.*;
import io.jettra.server.jakarta.websocket.WebSocketSession;

@ServerEndpoint("/ws/telemetria")
public class TelemetriaSocket {

    @OnOpen
    public void onOpen(WebSocketSession session) {
        System.out.println("Nueva conexión: " + session.getId());
    }

    @OnMessage
    public void onMessage(String message, WebSocketSession session) throws Exception {
        session.sendText("ACK: " + message);
    }

    @OnClose
    public void onClose(WebSocketSession session) {
        System.out.println("Conexión cerrada: " + session.getId());
    }
}
```

---

## 10. Seguridad y Control de Acceso con JettraJWT

Autenticación sin estado y autorización declarativa mediante tokens firmados con HMAC-SHA256:

```java
@Path("/api/admin")
@RolesAllowed({"ADMIN", "SUPERVISOR"})
public class AdminResource {
    @GET
    @Path("/metricas")
    public String verMetricas() {
        return "Acceso concedido a zona administrativa";
    }
}
```

Generación de tokens:
```java
JettraJWT jwt = new JettraJWT("mi_clave_secreta", 3600000L);
String token = jwt.generateToken("admin_user", List.of("ADMIN"));
```

---

## 11. Concurrencia con Hilos Virtuales (Project Loom)

`JettraServer` no requiere pools de miles de hilos del sistema operativo. Utiliza el `ManagedExecutorService` de Jakarta Concurrency respaldado por hilos virtuales:

```java
ManagedExecutorService executor = container.getExecutorService();
executor.submit(() -> {
    // Tarea ejecutada en un Virtual Thread ultraligero
    procesarReporteMasivo();
});
```

---

## 12. Mensajería Asíncrona (Jakarta JMS / JmsBroker)

Cola de mensajes en memoria de alta velocidad compatible con **Jakarta Messaging**:

```java
JmsBroker broker = container.getJmsBroker();
Queue colaPedidos = broker.createQueue("pedidos.pendientes");

// Envío
broker.send(colaPedidos, new JmsTextMessage("Pedido #994", Map.of("origen", "WEB")));

// Recepción sincrónica con timeout
JmsTextMessage mensaje = broker.receive(colaPedidos, 2000);
```

---

## 13. Validación de Datos (Jakarta Bean Validation)

Validación de restricciones tipadas:

```java
public class RegistroUsuario {
    @NotNull
    @Size(min = 3, max = 50)
    private String usuario;

    @NotNull
    @Email
    private String email;

    @Min(18)
    private int edad;
}

Set<ConstraintViolation> errores = ValidationEngine.validate(registro);
if (!errores.isEmpty()) {
    // Procesar violaciones de integridad
}
```

---

## 14. Procesamiento JSON (JSON-P y JSON-B)

Serialización y deserialización de alto rendimiento sin reflexión costosa:

```java
Jsonb jsonb = Jsonb.create();
String jsonString = jsonb.toJson(miObjeto);
MiObjeto obj = jsonb.fromJson(jsonString, MiObjeto.class);
```

---

## 15. Desarrollo Frontend Reactivo Tipado con JettraFlux

Con el módulo `jettra-server-flux`, puedes construir aplicaciones web completas en **Java puro**, sin tocar código HTML, plantillas ni JavaScript:

```java
package com.miempresa.app.views;

import io.jettra.flux.core.FluxView;
import io.jettra.flux.core.Widget;
import io.jettra.flux.widgets.Button;
import io.jettra.flux.widgets.Div;
import io.jettra.flux.widgets.Text;

public class DashboardView extends FluxView {
    @Override
    public Widget build() {
        return Div.of(
            Text.of("Panel de Operaciones JettraServer"),
            Button.of("Actualizar Datos")
        );
    }
}
```

Registro en el servidor:
```java
FluxModule flux = FluxModule.attach(server);
flux.page("/app/dashboard", "Dashboard de Control", DashboardView.class);
```

---

## 16. Pruebas Automatizadas con JettraTest

`JettraServer` promueve una arquitectura **100% libre de JUnit**, utilizando el framework moderno `JettraTest`:

```java
package com.miempresa.app.test;

import io.jettra.test.annotation.DisplayName;
import io.jettra.test.annotation.Test;
import io.jettra.test.core.JettraAssert;
import io.jettra.server.core.JettraServer;
import io.jettra.server.core.config.ServerConfig;
import io.jettra.server.core.storage.StorageMode;

public class MicroservicioTest {

    @Test
    @DisplayName("Verificar cold start y respuesta HTTP de microservicio")
    public void testColdStart() throws Exception {
        ServerConfig config = new ServerConfig().setPort(9090).setStorageMode(StorageMode.JVM_RAM);
        JettraServer server = JettraServer.create(config);
        server.get("/ping", (req, res) -> res.body("pong"));
        server.start();

        JettraAssert.assertTrue(server.isRunning(), "El servidor debe estar en ejecución");
        server.stop();
    }
}
```

Ejecución desde la consola:
```bash
mvn test
```

---

## 17. Despliegue en Docker y Docker Compose con ZGC

### Dockerfile Optimizado (Multi-Stage con ZGC)
```dockerfile
FROM bellsoft/liberica-openjdk-debian:25-cds AS builder
WORKDIR /app
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY jettra-server-core jettra-server-core
COPY jettra-server-jakarta jettra-server-jakarta
COPY jettra-server-flux jettra-server-flux
RUN mvn clean package -DskipTests

FROM bellsoft/liberica-openjdk-debian:25-slim
WORKDIR /app
COPY --from=builder /app/target/app-shaded.jar /app/app.jar

# Variables de entorno JVM y Jettra
ENV JAVA_OPTS="-XX:+UseZGC -XX:+ZGenerational -Xms256m -Xmx1024m --enable-preview"
ENV JETTRA_STORAGE_MODE="OFF_HEAP"

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
```

### Docker Compose
```yaml
version: '3.8'

services:
  jettra-app:
    image: miempresa/jettra-app:1.0.0
    container_name: jettra_server_node
    ports:
      - "8080:8080"
    environment:
      - JETTRA_STORAGE_MODE=DISK_MEMORY
      - JETTRA_DATA_DIR=/var/data/jettra
    volumes:
      - jettra_data:/var/data/jettra
    deploy:
      resources:
        limits:
          memory: 2G
        reservations:
          memory: 512M

volumes:
  jettra_data:
```

---

## 18. Aplicación de Ejemplo: JettraServerExample

El proyecto [`JettraServerExample`](file:///home/avbravo/NetBeansProjects/jettrastack_local/jettraframeworkscontainers/JettraServer/JettraServerExample) ilustra una aplicación empresarial web completa que integra:
1. **Jakarta Faces & PrimeFaces 14:**
   - **Formulario de Login:** Valida credenciales declarativamente contra el servicio de seguridad y entidades JPA.
   - **Plantilla Dashboard (Top, Left, Center, Footer):**
     - **Top:** Barra superior con información de usuario, modo de persistencia y botón de logout.
     - **Left:** Menú de navegación con accesos al catálogo JPA, administración de usuarios y las vistas reactivas JettraFlux.
     - **Center:** Tarjetas de métricas KPI, rendimiento de hilos virtuales Loom y gestión de productos.
     - **Footer:** Estado operativo, tecnología Java 25 y modo de almacenamiento activo.
2. **Vistas Reactivas JettraFlux invocadas desde el Menú:**
   - `/flux/storage-monitor`: Monitor de persistencia tri-modal (`JVM_RAM`, `DISK_MEMORY`, `OFF_HEAP`).
   - `/flux/telemetry`: Telemetría del servidor en tiempo real.
   - `/flux/counter`: Demostración interactiva en Java puro.
3. **Servicios RESTful y Persistencia JPA:**
   - Endpoints `/api/productos` y `/api/info`.
   - Transacciones declarativas con `@Transactional`.


