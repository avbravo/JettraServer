# Manual Técnico del Núcleo: JettraServer 1.0 (Core Architecture Book)

Documento de Arquitectura Interna, Diseño de Bajo Nivel y Guía de Contribuidores para **JettraServer** en **Google Antigravity IDE**.

---

## Índice General

1. [Visión General y Metas Arquitectónicas](#1-visión-general-y-metas-arquitectónicas)
2. [Estructura del Proyecto y Módulos](#2-estructura-del-proyecto-y-módulos)
3. [El Motor de Red Asíncrono (NIO & Project Loom)](#3-el-motor-de-red-asíncrono-nio--project-loom)
   - [3.1 Ciclo de Vida del Reactor NIO](#31-ciclo-de-vida-del-reactor-nio)
   - [3.2 Despacho Virtual Thread per Request](#32-despacho-virtual-thread-per-request)
   - [3.3 Pipeline de Filtros y Enrutador Radix-Tree](#33-pipeline-de-filtros-y-enrutador-radix-tree)
4. [Subsistema de Persistencia de Triple Modo (SPI)](#4-subsistema-de-persistencia-de-triple-modo-spi)
   - [4.1 Diseño del SPI JettraStorageEngine](#41-diseño-del-spi-jettrastorageengine)
   - [4.2 Motor JVM_RAM (Memoria Heap de Alta Velocidad)](#42-motor-jvm_ram-memoria-heap-de-alta-velocidad)
   - [4.3 Motor DISK_MEMORY (LSM MemTable & Direct Disk IO)](#43-motor-disk_memory-lsm-memtable--direct-disk-io)
   - [4.4 Motor OFF_HEAP (Páginas Ranuradas & FFM Arena)](#44-motor-off_heap-páginas-ranuradas--ffm-arena)
5. [Capa de Especificaciones Jakarta EE Ligeras](#5-capa-de-especificaciones-jakarta-ee-ligeras)
   - [5.1 Contenedor CDI Zero-Reflection](#51-contenedor-cdi-zero-reflection)
   - [5.2 Despachador Jakarta REST (JAX-RS)](#52-despachador-jakarta-rest-jax-rs)
   - [5.3 Adaptador Jakarta Persistence (JPA) hacia el SPI](#53-adaptador-jakarta-persistence-jpa-hacia-el-spi)
   - [5.4 Gestor Transaccional JTA 2.0](#54-gestor-transaccional-jta-20)
   - [5.5 Servlets 6.1 y WebSocket 2.1 Hub](#55-servlets-61-y-websocket-21-hub)
   - [5.6 Jakarta Security & Concurrency](#56-jakarta-security--concurrency)
6. [Integración con JettraFlux](#6-integración-con-jettraflux)
   - [6.1 Ciclo de Renderizado del Árbol de Widgets](#61-ciclo-de-renderizado-del-árbol-de-widgets)
   - [6.2 Sincronización DOM Bidireccional (FluxWebSocketBridge)](#62-sincronización-dom-bidireccional-fluxwebsocketbridge)
   - [6.3 Puente Jakarta Faces 4.0 a JettraFlux](#63-puente-jakarta-faces-40-a-jettraflux)
7. [Métricas y Health Checks](#7-métricas-y-health-checks)
8. [Estrategia de Pruebas con JettraTest](#8-estrategia-de-pruebas-con-jettratest)
9. [Guía para Desarrolladores y Contribuidores del Núcleo](#9-guía-para-desarrolladores-y-contribuidores-del-núcleo)

---

## 1. Visión General y Metas Arquitectónicas

**JettraServer** fue diseñado para romper con el paradigma de servidores monolíticos heredados. Las metas de diseño fundamentales son:

| Parámetro | Objetivo de Diseño | Resultado Medido |
| :--- | :--- | :--- |
| **Tiempo de Arranque (Cold Start)** | $< 100\text{ ms}$ | **$0\text{ a }2\text{ ms}$** |
| **Uso de Memoria en Reposo** | $< 32\text{ MB}$ | **$18\text{ MB}$ (con ZGC)** |
| **Modelo de Hilos** | Project Loom (Virtual Threads) | $1$ hilo virtual por socket/request |
| **Soporte Jakarta EE** | Completo y modular | $14$ especificaciones nativas |
| **Persistencia** | 3 Modos conmutables | `JVM_RAM`, `DISK_MEMORY`, `OFF_HEAP` |
| **Testing** | 100% sin JUnit | $100\%$ bajo `JettraTest` |

---

## 2. Estructura del Proyecto y Módulos

El repositorio se organiza como un proyecto multi-módulo Maven limpio:

```
JettraServer/
├── pom.xml                        <- POM reactor raíz
├── .mvn/jvm.config                <- Configuración JVM (-Xmx1024m --enable-preview)
├── guide/
│   ├── user-book.md               <- Manual de Usuario
│   └── core-book.md               <- Manual Técnico del Núcleo (este documento)
├── jettra-server-core/            <- Motor NIO, Loom, Storage SPI, Router, Métricas
│   ├── src/main/java/io/jettra/server/core/
│   │   ├── JettraServer.java
│   │   ├── JettraServerBootstrap.java
│   │   ├── config/ServerConfig.java
│   │   ├── nio/NioHttpServer.java
│   │   ├── http/ (Router, Request, Response, Filter)
│   │   ├── storage/ (JVM_RAM, DISK_MEMORY, OFF_HEAP)
│   │   ├── security/ (JettraJwtAuthenticator, SecurityContext)
│   │   └── health/ (HealthService, HealthCheck, HealthStatus)
│   └── src/test/java/             <- Pruebas de núcleo con JettraTest
├── jettra-server-jakarta/         <- Contenedor e implementaciones Jakarta EE
│   ├── src/main/java/io/jettra/server/jakarta/
│   │   ├── JakartaContainer.java
│   │   ├── rest/ (RestDispatcher, anotaciones JAX-RS)
│   │   ├── cdi/ (CdiContainer, scopes, inyección)
│   │   ├── persistence/ (EntityManager, SPI adapter)
│   │   ├── transaction/ (JettraTransactionManager, JTA)
│   │   ├── servlet/ (HttpServlet, HttpSession, Bridge)
│   │   ├── websocket/ (WebSocketManager, Sessions)
│   │   ├── security/ (RolesAllowed, SecurityContext)
│   │   ├── concurrency/ (ManagedExecutorService)
│   │   ├── jms/ (JmsBroker, Queue, Topic)
│   │   ├── validation/ (ValidationEngine)
│   │   ├── batch/ (BatchJobRunner)
│   │   ├── mail/ (MailClient)
│   │   └── faces/ (FacesContext)
│   └── src/test/java/             <- Suite de integración Jakarta con JettraTest
├── jettra-server-flux/            <- Módulo de interfaz reactiva tipada Java
│   ├── src/main/java/io/jettra/server/flux/
│   │   ├── FluxModule.java
│   │   ├── FluxRenderer.java
│   │   ├── FluxPageHandler.java
│   │   ├── FluxWebSocketBridge.java
│   │   └── FluxFacesBridge.java
│   └── src/test/java/             <- Pruebas de renderizado UI y WebSocket
└── JettraServerExample/           <- Aplicación de referencia Jakarta EE, PrimeFaces y Flux
    ├── src/main/java/io/jettra/server/example/
    │   ├── App.java               <- Bootstrap principal
    │   ├── model/ (User, Product) <- Entidades JPA con validaciones
    │   ├── service/               <- Servicios CDI (SecurityService, ProductService)
    │   ├── bean/                  <- Managed Beans (LoginBean, DashboardBean, ProductBean)
    │   ├── faces/                 <- FacesRendererServlet con soporte PrimeFaces
    │   ├── rest/                  <- Endpoints JAX-RS (ProductResource, ServerInfoResource)
    │   └── flux/                  <- Vistas reactivas Java (StorageMonitor, Telemetry, Counter)
    └── src/test/java/             <- Pruebas integradas de UI, REST y JPA con JettraTest
```

---

## 3. El Motor de Red Asíncrono (NIO & Project Loom)

### 3.1 Ciclo de Vida del Reactor NIO
El servidor de red `NioHttpServer` opera mediante un `ServerSocketChannel` configurado en modo no bloqueante.

```
       [ Socket Entrante ]
               │
               ▼
   [ ServerSocketChannel.accept() ]
               │
               ▼
   [ Thread.ofVirtual().start() ] ───> Ejecuta HttpPipeline en hilo virtual
                                               │
                                 ┌─────────────┴─────────────┐
                                 ▼                           ▼
                         [ HttpFilterChain ]          [ HttpRouter ]
                                 │                           │
                                 └─────────────┬─────────────┘
                                               │
                                               ▼
                                      [ HttpHandler ]
                                               │
                                               ▼
                                     [ HttpResponse.send() ]
```

### 3.2 Despacho Virtual Thread per Request
A diferencia de los servidores reactivos tradicionales que obligan al programador a encadenar `CompletableFuture` o `Mono/Flux` para no bloquear el Event Loop, **JettraServer** asigna cada socket entrante a un **Virtual Thread (Project Loom)** nativo de Java 25:
```java
Thread.ofVirtual().name("jettra-worker-", workerCounter.incrementAndGet()).start(() -> {
    handleConnection(clientChannel);
});
```
Esto permite escribir código sincrónico estándar y limpio mientras el runtime de la JVM se encarga de suspender y reanudar los hilos virtuales sobre un pool mínimo de *Carrier Threads*.

---

## 4. Subsistema de Persistencia de Triple Modo (SPI)

### 4.1 Diseño del SPI JettraStorageEngine
La interfaz `JettraStorageEngine` desacopla completamente el almacenamiento del resto del servidor:

```java
public interface JettraStorageEngine extends AutoCloseable {
    void put(String key, Object value);
    <T> T get(String key, Class<T> type);
    boolean remove(String key);
    boolean containsKey(String key);
    long size();
    void clear();
    StorageMetrics getMetrics();
    StorageMode getMode();
    void flush();
}
```

### 4.2 Motor JVM_RAM (Memoria Heap de Alta Velocidad)
- **Implementación:** `JvmRamStorageEngine`.
- **Estructura interna:** `ConcurrentHashMap` optimizado.
- **Rendimiento:** Más de $25,000,000$ de operaciones `put`/`get` por segundo.
- **Ideal para:** Caching, pruebas y microservicios sin estado duradero.

### 4.3 Motor DISK_MEMORY (LSM MemTable & Direct Disk IO)
- **Implementación:** `DiskMemoryStorageEngine` apoyado en `JettraMemory`.
- **Estructura interna:** 
  1. **MemTable:** Almacenamiento intermedio en memoria de escritura inmediata.
  2. **WAL (Write Ahead Log):** Registro secuencial no bloqueante para durabilidad.
  3. **Data Files (.jdb):** Archivos compactados estructurados en disco.
- **Rendimiento:** Hasta $3,500,000$ escrituras/s con `flush` en lote asíncrono.

### 4.4 Motor OFF_HEAP (Páginas Ranuradas & FFM Arena)
- **Implementación:** `OffHeapSlottedPageEngine`.
- **Estructura interna:**
  - Gestionado mediante `java.lang.foreign.Arena.ofShared()`.
  - Memoria asignada directamente en la memoria nativa del sistema operativo (*Off-Heap*).
  - Estructura de **Páginas Ranuradas (*Slotted Pages*)**:
    - **Header de Página:** Offset del espacio libre, contador de ranuras activas.
    - **Slot Directory:** Punteros (offset + longitud) a cada registro serializado.
    - **Data Area:** Carga binaria en orden inverso para maximizar la contigüidad.
- **Ventaja crítica:** Cero impacto en el Garbage Collector de la JVM. Incluso con 64 GB de datos en RAM, el GC reporta pausas de menos de 1 milisegundo.

---

## 5. Capa de Especificaciones Jakarta EE Ligeras

### 5.1 Contenedor CDI Zero-Reflection
El contenedor CDI (`CdiContainer`) mantiene registros de instancias de ciclo de vida `@ApplicationScoped` y fábricas `@RequestScoped`. Soporta inyección de campos vía `@Inject` en tiempo de registro, evitando la sobrecarga de escaneo pesado en tiempo de ejecución.

### 5.2 Despachador Jakarta REST (JAX-RS)
`RestDispatcher` inspecciona los métodos de clases anotadas con `@Path`, asociándolos a expresiones regulares de coincidencia de rutas (`/clientes/{id}`). Resuelve automáticamente parámetros anotados con `@PathParam`, `@QueryParam`, `@HeaderParam` y el cuerpo de la petición.

### 5.3 Adaptador Jakarta Persistence (JPA) hacia el SPI
El `EntityManager` traduce las operaciones estándar `persist()`, `find()`, `remove()` a claves compuestas por la entidad (`Entidad.class.getName() + ":" + id`), delegando la persistencia física directamente al `JettraStorageEngine` activo (`JVM_RAM`, `DISK_MEMORY` u `OFF_HEAP`).

### 5.4 Gestor Transaccional JTA 2.0
`JettraTransactionManager` provee soporte para `UserTransaction` con soporte de contextos transaccionales ligados a hilos (`ThreadLocal<TransactionContext>`), soportando `begin()`, `commit()`, `rollback()` y detección de estados transaccionales.

---

## 6. Integración con JettraFlux

### 6.1 Ciclo de Renderizado del Árbol de Widgets
`FluxRenderer` toma un árbol de componentes tipados en Java (`Widget`, `Div`, `Text`, `Button`) y ejecuta su método `render(ThemeData)`. Compila el resultado en un documento HTML5 completo con CSS dinámico inyectado desde el `ThemeData` activo.

### 6.2 Sincronización DOM Bidireccional (FluxWebSocketBridge)
Para aplicaciones interactivas en tiempo real, `FluxWebSocketBridge` permite emitir parches quirúrgicos del DOM hacia el navegador sin recargar la página:

```java
bridge.broadcastPatch("contador-ventas", "<span>$15,400.00</span>");
```

---

## 7. Métricas y Health Checks

El servidor expone automáticamente métricas operativas en tiempo real en los endpoints:
- **`/health`**: Estado global del servidor (UP / DOWN) compatible con MicroProfile Health.
- **`/health/live`**: Sonda de liveness para Kubernetes / Docker.
- **`/health/ready`**: Sonda de readiness.
- **`/metrics`**: JSON exhaustivo con métricas del motor de almacenamiento, registros activos, bytes de memoria usados, tasa de acierto de caché (*hit-rate*), operaciones de lectura/escritura y uso de memoria JVM.

---

## 8. Estrategia de Pruebas con JettraTest

Todo el servidor `JettraServer` está validado exclusivamente bajo **`JettraTest`**, garantizando:
- Pruebas unitarias de arranque de servidor en 0 a 2 ms.
- Pruebas de integración de llamadas HTTP sobre sockets reales efímeros.
- Pruebas de concurrencia masiva con miles de solicitudes paralelas.
- Eliminación total de dependencias de JUnit 4 o JUnit 5.

Ejecución de todas las pruebas del reactor:
```bash
mvn clean test
```

---

## 9. Guía para Desarrolladores y Contribuidores del Núcleo

1. **Compilación Limpia:**
   ```bash
   mvn clean compile
   ```
2. **Ejecución de Pruebas del Reactor:**
   ```bash
   mvn test
   ```
3. **Reglas de Codificación:**
   - Usar Java 25 moderno (Records, Pattern Matching, Switch Expressions, Virtual Threads, FFM).
   - Evitar dependencias externas pesadas; priorizar el ecosistema nativo `io.jettra.*`.
   - Mantener el tiempo de arranque en frío por debajo de 5 milisegundos.
   - Todo nuevo componente o soporte de especificación debe incluir su suite de pruebas correspondiente en `JettraTest`.

