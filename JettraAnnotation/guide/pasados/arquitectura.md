# Arquitectura de JettraServer

JettraServer es un motor de servidor Java de alto rendimiento y ultra-ligero, diseñado específicamente para aprovechar las capacidades de **Java 25** y **Project Loom**. Su arquitectura prioriza la simplicidad, la seguridad y la eficiencia en el arranque (AOT/Project Leyden).

## Pilares Arquitectónicos

### 1. Concurrencia mediante Virtual Threads (Project Loom)
A diferencia de los servidores tradicionales basados en pools de hilos pesados del SO, JettraServer utiliza un ejecutor de **Virtual Threads**. Esto permite manejar miles de conexiones simultáneas con un consumo de memoria mínimo, eliminando el bloqueo de hilos en operaciones de E/S.

### 2. Gestión de Contexto y Scopes (JettraContext)
Implementa un sistema robusto de alcances (Request, Session, Application) mediante `JettraContext`.
- **Ciclo de Vida de Sesión**: Las sesiones se gestionan mediante `jsessionid`.
- **Invalidación Global**: Al detener el servidor (`stop()`), se ejecuta un comando de limpieza total (`clearSessions()`) que invalida todas las sesiones activas, garantizando la seguridad en el reinicio.

### 3. Seguridad Integrada (XSS & Security)
JettraServer incluye una capa de seguridad activa en cada petición:
- **Cabeceras de Seguridad**: Incorpora automáticamente `Content-Security-Policy` (CSP), `X-XSS-Protection`, `Strict-Transport-Security` y `Referrer-Policy`.
- **Sanitización XSS**: Provee la utilidad `SecurityUtil` para la limpieza de contenido dinámico y prevención de ataques de inyección de scripts.
- **JWT nativo**: Soporte para autenticación basada en tokens para servicios REST y gRPC.

### 4. Optimizaciones Java 25
El servidor está preparado para las características más recientes de la plataforma:
- **Compact Object Headers (JEP 450)**: Soporte nativo para la reducción de la cabecera de objetos en memoria, optimizando el uso del heap. Se activa automáticamente en Java 25 o mediante la propiedad `server.compactheader=true`.

### 5. Configuración e Inyección (JettraConfig)
- **Zero Configuration**: Basado en `jettra-config.properties`.
- **DI Ligera**: Uso de `@JettraConfigProperty` para inyección de valores directamente en los componentes sin necesidad de frameworks pesados.

### 6. Hot Reloading
Mecanismo de observación de cambios en `target/classes` para el redespliegue en caliente durante el desarrollo, mejorando la productividad del programador.

## Parámetros de Sesión

En `jettra-config.properties`:
- `server.session.timeout`: Define el tiempo de expiración en minutos. Un valor de `0` indica que la sesión es indefinida mientras el servidor esté activo.
- `server.session.expired`: Define el tiempo de gracia (en segundos) que el servidor espera antes de forzar el cierre de conexiones activas durante el comando `stop()`.

## Integración con JettraWUI
El servidor detecta automáticamente las páginas de `JettraWUI` y gestiona el enrutamiento. Incluye componentes de feedback para el usuario, como el diálogo de advertencia de cierre de sesión inminente, notificando y redirigiendo al login automáticamente si el tiempo de sesión expira o el servidor se detiene.