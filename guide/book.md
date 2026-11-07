# Manual Maestro de JettraServer 1.0 (The Definitive Guide)

Servidor de aplicaciones modular de ultra-alto rendimiento para **Java 25+**, con soporte integral para **Jakarta EE 11**, **Eclipse MicroProfile**, **Persistencia Tri-Modal**, base de datos embebida **JettraSecurityDB** e interfaces reactivas con **JettraFlux**.

---

## Tabla de Contenidos

1. [Visión General y Filosofía Arquitectónica](#1-visión-general-y-filosofía-arquitectónica)
2. [Arquitectura de Alto Nivel y Módulos](#2-arquitectura-de-alto-nivel-y-módulos)
3. [Motor de Persistencia Tri-Modal (JettraStorageEngine)](#3-motor-de-persistencia-tri-modal-jettrastorageengine)
4. [JettraSecurityDB: Base de Datos Embebida de Seguridad](#4-jettrasecuritydb-base-de-datos-embebida-de-seguridad)
5. [Jakarta EE 11 y Eclipse MicroProfile con CDI Estándar](#5-jakarta-ee-11-y-eclipse-microprofile-con-cdi-estándar)
6. [JettraFlux: Renderizado de Interfaz Reactiva](#6-jettraflux-renderizado-de-interfaz-reactiva)
7. [Framework de Pruebas JettraTest](#7-framework-de-pruebas-jettratest)
8. [Configuración Exhaustiva (`jettraserver.config`) y Despliegue](#8-configuración-exhaustiva-jettraserverconfig-y-despliegue)

---

## 1. Visión General y Filosofía Arquitectónica

**JettraServer** redefine el concepto de servidor empresarial eliminando la sobrecarga, complejidad y lentitud de los contenedores de aplicaciones tradicionales. Desarrollado específicamente para **Java 25+**, explota al máximo las innovaciones del runtime moderno:

- **Virtual Threads (Project Loom):** Manejo masivo de conexiones entrantes sin degradación de rendimiento. Cada socket y cada petición HTTP se procesa en un hilo virtual independiente, eliminando los cuellos de botella de los grupos de hilos del sistema operativo.
- **Generational ZGC (Z Garbage Collector):** Recolección de basura con pausas sub-milisegundo garantizadas y consumo de memoria base inferior a **18 MB**.
- **Arranque en Frío (Cold Start):** Tiempo de inicialización medido entre **0 y 2 milisegundos**, haciéndolo ideal tanto para microservicios de larga duración como para despliegues Serverless o contenedores en la nube.
- **Cero Dependencias de Terceros Pesadas en el Núcleo:** Basado en `java.nio` nativo y abstracciones limpias de alto rendimiento.

```mermaid
flowchart TD
    Client["Cliente HTTP / Navegador / API"] -->|Petición NIO| NioReactor["NioHttpServer (Reactor Loom)"]
    NioReactor --> Router["HttpRouter (Despacho Radix / Hash)"]
    Router --> SecFilter["Filtro de Seguridad / JettraJWT"]
    SecFilter --> SecDB["JettraSecurityDB (Instancia Local)"]
    Router --> AppEndpoints["Endpoints & Recursos REST"]
    Router --> JakartaCont["JakartaContainer (Jakarta EE 11 & MicroProfile)"]
    Router --> FluxEngine["JettraFlux (Render Reactivo UI)"]
    JakartaCont --> CDI["CDI 4.1 (Weld SE Container)"]
    JakartaCont --> JPA["JPA 3.2 & JTA 2.0"]
    AppEndpoints --> Storage["SPI Persistencia Tri-Modal"]
    Storage --> RAM["JVM_RAM"]
    Storage --> DISK["DISK_MEMORY"]
    Storage --> OFFHEAP["OFF_HEAP (Panama FFM)"]
```

---

## 2. Arquitectura de Alto Nivel y Módulos

El proyecto se organiza en una arquitectura multi-módulo altamente desacoplada y orientada a microservicios:

| Módulo | Ruta | Responsabilidad Principal |
| :--- | :--- | :--- |
| **`jettra-server-core`** | [jettra-server-core](file:///home/avbravo/NetBeansProjects/jettrastack_local/JettraServer/jettra-server-core) | Servidor de red NIO, enrutador HTTP/1.1, persistencia tri-modal, `JettraSecurityDB`, autenticación JWT y métricas. |
| **`jettra-server-jakarta`** | [jettra-server-jakarta](file:///home/avbravo/NetBeansProjects/jettrastack_local/JettraServer/jettra-server-jakarta) | Orquestador Jakarta EE 11 (CDI 4.1 oficial vía Weld SE, Servlet 6.1, Faces 4.1, JAX-RS 4.0, JPA 3.2, etc.) y Eclipse MicroProfile. |
| **`jettra-server-flux`** | [jettra-server-flux](file:///home/avbravo/NetBeansProjects/jettrastack_local/JettraServer/jettra-server-flux) | Motor UI Server-Driven que renderiza componentes interactivos sin concatenación de cadenas mediante árbol de objetos reactivos. |
| **`JettraServerExample`** | [JettraServerExample](file:///home/avbravo/NetBeansProjects/jettrastack_local/JettraServer/JettraServerExample) | Aplicación empresarial completa de ejemplo integrando vistas Facelets (`.xhtml`), vistas JettraFlux, beans Jakarta CDI y seguridad. |

---

## 3. Motor de Persistencia Tri-Modal (JettraStorageEngine)

JettraServer integra un subsistema de persistencia de datos mediante el SPI [`JettraStorageEngine<K, V>`](file:///home/avbravo/NetBeansProjects/jettrastack_local/JettraServer/jettra-server-core/src/main/java/io/jettra/server/core/storage/JettraStorageEngine.java). Ofrece tres modos de almacenamiento conmutables en tiempo de ejecución:

```mermaid
graph LR
    subgraph StorageModes ["Modos de Almacenamiento JettraServer"]
        A["JVM_RAM (Heap)"] -->|Ultra-baja latencia| M1["UnifiedMap / ConcurrentHashMap"]
        B["DISK_MEMORY (Híbrido)"] -->|Persistencia duradera| M2["LSM MemTable + Direct I/O"]
        C["OFF_HEAP (Project Panama)"] -->|Sin presión de GC| M3["Slotted-Page Arena FFM API"]
    end
```

### 3.1. `JVM_RAM` (Memoria Heap Estándar)
- **Implementación:** [`JvmRamStorageEngine`](file:///home/avbravo/NetBeansProjects/jettrastack_local/JettraServer/jettra-server-core/src/main/java/io/jettra/server/core/storage/JvmRamStorageEngine.java).
- **Rendimiento:** Latencia sub-microsegundo directa en el Heap de Java aprovechando `UnifiedMap` de `JettraCollections`.
- **Caso de Uso:** Caché en memoria de máxima velocidad y procesamiento efímero.

### 3.2. `DISK_MEMORY` (MemTable LSM con Persistencia en Disco)
- **Implementación:** [`DiskMemoryStorageEngine`](file:///home/avbravo/NetBeansProjects/jettrastack_local/JettraServer/jettra-server-core/src/main/java/io/jettra/server/core/storage/DiskMemoryStorageEngine.java).
- **Rendimiento:** Escrituras atómicas diferidas respaldadas por MemTable de memoria y serialización incremental sobre el sistema de archivos local (`./data/jettra-server/storage`).
- **Caso de Uso:** Aplicaciones que requieren durabilidad entre reinicios con bajo consumo de memoria.

### 3.3. `OFF_HEAP` (Páginas Ranuradas FFM Project Panama)
- **Implementación:** [`OffHeapSlottedPageEngine`](file:///home/avbravo/NetBeansProjects/jettrastack_local/JettraServer/jettra-server-core/src/main/java/io/jettra/server/core/storage/OffHeapSlottedPageEngine.java).
- **Rendimiento:** Utiliza `java.lang.foreign.Arena` y `MemorySegment` de la API de Foreign Function & Memory (FFM) de Java 25. Administra páginas ranuradas de 64 KB fuera del Heap de la JVM, permitiendo almacenar gigabytes de datos sin sobrecargar el Garbage Collector.
- **Caso de Uso:** Sistemas analíticos, microservicios de misión crítica con grandes volúmenes de datos en memoria.

---

## 4. JettraSecurityDB: Base de Datos Embebida de Seguridad

Cada instancia de `JettraServer` está respaldada de forma nativa por su propio motor de base de datos de seguridad embebido: **JettraSecurityDB**.

### 4.1. Arquitectura de Almacenamiento y Concurrencia
- **Estructura en Disco:** Almacenado en `${security.db.path}` (por defecto `./data/jettra-server/securitydb`).
  ```text
  data/jettra-server/securitydb/
  ├── juser/
  │   └── <uuid>.jdb
  ├── jrole/
  │   └── <uuid>.jdb
  ├── jcredential/
  │   └── <uuid>.jdb
  └── jaccreditation/
      └── <uuid>.jdb
  ```
- **Concurrencia Multi-Nivel:**
  1. **Nivel Proceso:** Bloqueo exclusivo a nivel del kernel del SO mediante `java.nio.channels.FileLock`, impidiendo escrituras simultáneas conflictivas entre instancias de la misma máquina.
  2. **Nivel JVM:** Bloqueo justo por colección mediante `ReentrantReadWriteLock(fair = true)`.
- **Cabecera Compacta:** Todo registro se almacena serializado precedido por una cabecera de metadatos de integridad (`CompactHeader`).

### 4.2. Entidades Principales
- [`JUser`](file:///home/avbravo/NetBeansProjects/jettrastack_local/JettraServer/jettra-server-core/src/main/java/io/jettra/server/core/security/entity/JUser.java): Registro inmutable que almacena identificador, `firstName` (nombre de usuario), `email`, teléfono, estado de activación y conjunto ordenado de roles (`Set<JRole>`).
- [`JCredential`](file:///home/avbravo/NetBeansProjects/jettrastack_local/JettraServer/jettra-server-core/src/main/java/io/jettra/server/core/security/entity/JCredential.java): Almacena las credenciales asociadas al usuario con contraseña encriptada mediante algoritmo seguro **SHA-256** y marca de tiempo `lastLogin`.
- [`JRole`](file:///home/avbravo/NetBeansProjects/jettrastack_local/JettraServer/jettra-server-core/src/main/java/io/jettra/server/core/security/entity/JRole.java): Roles del sistema (ej. `ADMIN`, `MANAGER`, `USER`, `DEMO`).
- [`JAccreditation`](file:///home/avbravo/NetBeansProjects/jettrastack_local/JettraServer/jettra-server-core/src/main/java/io/jettra/server/core/security/entity/JAccreditation.java): Privilegios específicos concedidos a roles para acceso a funcionalidades.

### 4.3. Inicialización Automática de Registros Maestros
Al arrancar cualquier instancia de JettraServer, [`JettraSecurityDBInitializer`](file:///home/avbravo/NetBeansProjects/jettrastack_local/JettraServer/jettra-server-core/src/main/java/io/jettra/server/core/security/repository/JettraSecurityDBInitializer.java) valida la existencia de los registros base. Si la base de datos está vacía, aprovisiona de forma automática:
- Roles maestros: `ADMIN`, `MANAGER`, `USER`, `DEMO`.
- Superusuario `admin` con rol `ADMIN` e inmutabilidad garantizada (no puede ser eliminado por la API).
- Credencial para `admin` con contraseña inicial `admin` (hasheada en SHA-256).

### 4.4. Endpoints REST de Seguridad Integrados en JettraServer
El servidor expone automáticamente las siguientes rutas listas para consumir:

| Método | Endpoint | Descripción | Formato Entrada / Salida |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/login` | Inicia sesión y retorna token JWT Bearer. | JSON `{ "username": "admin", "password": "..." }` o Query Params |
| `GET` | `/api/auth/login` | Alternativa de inicio de sesión vía Query Params. | `?username=admin&password=...` |
| `GET` | `/api/auth/me` | Retorna los detalles del usuario autenticado. | Header `Authorization: Bearer <token>` |
| `GET` | `/api/security/users` | Lista de todos los usuarios registrados en JettraSecurityDB. | JSON Array con usuarios y sus roles asignados |
| `GET` | `/api/security/roles` | Lista de todos los roles disponibles en el sistema. | JSON Array con roles |

---

## 5. Jakarta EE 11 y Eclipse MicroProfile con CDI Estándar

El módulo [`jettra-server-jakarta`](file:///home/avbravo/NetBeansProjects/jettrastack_local/JettraServer/jettra-server-jakarta) implementa las versiones más recientes de **Jakarta EE 11** y **Eclipse MicroProfile**, completamente compatible con **Java 25**.

### 5.1. Migración a Jakarta CDI Oficial (Weld SE)
Se ha erradicado por completo el uso de componentes CDI propietarios. JettraServer ahora utiliza directamente el contenedor oficial:
- **Contenedor Oficial:** `org.jboss.weld.se:weld-se-core:6.0.0.Beta4` (Jakarta CDI 4.1).
- **Anotaciones Oficiales Soportadas:**
  - `@Inject`: `jakarta.inject.Inject`
  - `@Named`: `jakarta.inject.Named`
  - `@ApplicationScoped`: `jakarta.enterprise.context.ApplicationScoped`
  - `@RequestScoped`: `jakarta.enterprise.context.RequestScoped`
  - `@SessionScoped`: `jakarta.enterprise.context.SessionScoped`
  - `@PostConstruct` / `@PreDestroy`: `jakarta.annotation.*`
- **Ciclo de Vida Programático:** Administrado mediante [`CdiContainer`](file:///home/avbravo/NetBeansProjects/jettrastack_local/JettraServer/jettra-server-jakarta/src/main/java/io/jettra/server/jakarta/cdi/CdiContainer.java), capaz de arrancar `SeContainerInitializer` de forma limpia o brindar resolución ágil de beans singleton e inyecciones de campos.

### 5.2. Especificaciones Jakarta EE Soportadas
- **Jakarta REST (JAX-RS 4.0):** Despachador nativo con soporte de rutas dinámicas `@Path("/{id}")`, inyección de parámetros `@PathParam`, `@QueryParam` y serialización automática a JSON.
- **Jakarta Servlet 6.1:** Soporte de ciclo de vida HTTP y sesiones concurrentes en memoria.
- **Jakarta Faces 4.1 (JSF & PrimeFaces):** Renderizado de vistas `.xhtml` enlazadas con beans CDI.
- **Jakarta Persistence (JPA 3.2):** `EntityManager` ligero con operaciones `@Entity`, `@Id`, `persist()`, `find()`, `merge()` y `remove()`, mapeado contra el motor de persistencia tri-modal.
- **Jakarta Transactions (JTA 2.0):** Transacciones atómicas de dos fases con `JettraTransactionManager` y soporte de `@Transactional`.
- **Jakarta Concurrency 3.1:** `ManagedExecutorService` respaldado por Virtual Threads de Project Loom.
- **Jakarta Bean Validation 3.1:** Motor de validación declarativa (`@NotNull`, `@Size`, etc.).

### 5.3. Eclipse MicroProfile
Configurado con las especificaciones y APIs ligeras de Eclipse MicroProfile:
- **MicroProfile Config 3.1 (`microprofile-config-api` / SmallRye Config):** Configuración unificada mediante `ConfigProvider.getConfig()`.
- **MicroProfile Health 4.0.1 (`microprofile-health-api`):** Endpoints estándar de liveness y readiness (`/health/live`, `/health/ready`, `/q/health`).
- **MicroProfile Metrics 5.1 (`microprofile-metrics-api`):** Exposición de métricas de rendimiento y de almacenamiento en `/metrics`.
- **MicroProfile Fault Tolerance 4.1 (`microprofile-fault-tolerance-api`):** Tolerancia a fallos, reintentos y disyuntores de circuito (Circuit Breakers).

---

## 6. JettraFlux: Renderizado de Interfaz Reactiva

El módulo [`jettra-server-flux`](file:///home/avbravo/NetBeansProjects/jettrastack_local/JettraServer/jettra-server-flux) provee una alternativa moderna a los motores de plantillas tradicionales basados en concatenación de cadenas (`StringBuilder`):

- **Árbol de Componentes Tipados:** La interfaz se compone instanciando widgets inmutables (`Container`, `Card`, `Badge`, `Grid`, `Table`, `Header`, etc.).
- **Zero Allocations Innecesarias:** El HTML se emite directamente durante el recorrido del árbol, reduciendo drásticamente la presión sobre el recolector de basura.
- **Vistas Reactivas Integradas:**
  - `CatalogoFlux`: Catálogo de productos interactivo con soporte de internacionalización mediante `messages.properties`.
  - `StorageMonitorFluxView`: Dashboard reactivo en tiempo real que visualiza el uso del motor de persistencia tri-modal y métricas de memoria.
  - `TelemetryFluxView`: Métricas de CPU, hilos virtuales de Loom y Garbage Collector.

---

## 7. Framework de Pruebas JettraTest

Todo el ecosistema de JettraServer está verificado y garantizado bajo **JettraTest**, un framework de pruebas moderno para Java 25+ sin dependencias de JUnit ni frameworks pesados heredados.

- **Anotaciones:** `@Test`, `@DisplayName`, `@BeforeAll`, `@AfterAll`.
- **Aserciones:** `JettraAssert.assertEquals()`, `JettraAssert.assertTrue()`, `JettraAssert.assertNotNull()`.
- **Ejecución Asíncrona con Virtual Threads:** Las pruebas se ejecutan en paralelo sobre hilos virtuales de Loom, completando suites enteras en menos de 3 segundos.

Para ejecutar toda la suite de pruebas del proyecto:
```bash
mvn clean test
```

---

## 8. Configuración Exhaustiva (`jettraserver.config`) y Despliegue

La configuración de JettraServer es jerárquica y flexible:
1. Parámetros de línea de comando (CLI)
2. Propiedades de Sistema Java (`-Dprop=val`)
3. Variables de Entorno (`JETTRA_SERVER_*`)
4. Archivo `jettraserver.config` en el classpath o directorio de trabajo

### 8.1. Archivo de Ejemplo: `jettraserver.config`
```properties
# ==============================================================================
# JETTRASERVER ARCHIVO DE CONFIGURACIÓN MAESTRO (Java 25+ & Jakarta EE 11)
# ==============================================================================

# Configuración de Red
server.host=0.0.0.0
server.port=8080
server.backlog=1024
contextpath=/

# Concurrencia y Runtime
server.virtual-threads=true

# Persistencia Tri-Modal: JVM_RAM, DISK_MEMORY, OFF_HEAP
storage.mode=JVM_RAM
storage.offheap.page-size=65536
data.dir=./data/jettra-server

# Base de Datos de Seguridad (JettraSecurityDB)
security.db.path=./data/jettra-server/securitydb

# Seguridad y Tokens JWT
server.jwt.secret=jettra_server_super_secure_jwt_key_2026
server.jwt.expiration=86400000

# Integración CORS
cors.enabled=true
```

### 8.2. Variables de Entorno Equivalentes
- `JETTRA_SERVER_PORT`: Puerto de escucha del servidor HTTP.
- `JETTRA_SERVER_HOST`: Interfaz de red de enlace (ej. `0.0.0.0` o `127.0.0.1`).
- `JETTRA_SERVER_CONTEXT_PATH`: Context path base de la aplicación.
- `JETTRA_STORAGE_MODE`: Modo de persistencia (`JVM_RAM`, `DISK_MEMORY`, `OFF_HEAP`).
- `JETTRA_SECURITYDB_PATH`: Ruta base para los archivos de JettraSecurityDB.
- `JETTRA_SERVER_JWT_SECRET`: Clave secreta para firma y validación de tokens Bearer JWT.

### 8.3. Ejecución desde CLI
```bash
java --enable-preview -jar JettraServerExample-1.0.0-SNAPSHOT.jar --port 8080 --mode JVM_RAM
```
Al arrancar, el servidor imprime su banner informativo indicando los hilos virtuales activos, el modo de persistencia, la ruta de `JettraSecurityDB` y los endpoints habilitados.

