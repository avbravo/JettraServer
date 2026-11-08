# JettraJWT

## Descripción General
`JettraJWT` provee todo el mecanismo de seguridad y autorización basado en tokens JWT (JSON Web Tokens) para el ecosistema JettraStack, asegurando que las rutas de los microservicios permanezcan protegidas.

## Detalles Específicos
- **Arquitectura general**: Un conjunto de interceptores, generadores de tokens y validadores enfocados en autenticar cada petición de los controladores REST.
- **Dependencias clave**: Librerías de procesamiento de JSON y criptografía JWT.
- **Roles dentro del sistema**: Capa de seguridad por la cual deben pasar todas las peticiones a endpoints restringidos para validación de roles, permisos y caducidad.

## Características Detalladas
- **Generación y Firma**: Creación de tokens firmados de forma segura con los claims correspondientes (usuario, rol, permisos).
- **Validación Automática**: Filtros que validan el token incluido en el encabezado `Authorization: Bearer <token>`.
- **Integración con Roles y Permisos**: Herramientas que facilitan extraer la información de autorización para decidir si una petición puede alcanzar el controlador de negocio.

## Guía de Entrenamiento (AI / Nuevas Características)
- Las actualizaciones sobre las reglas de seguridad o algoritmos criptográficos deben hacerse siempre en este módulo.
- Cuando se añadan nuevas claims o metadatos a la sesión, actualiza los métodos de generación y desencriptación en este proyecto.
- Si se requiere integrar OAuth2 u otro sistema en el futuro, `JettraJWT` debe ser extendido o servir de base para un nuevo adaptador.
