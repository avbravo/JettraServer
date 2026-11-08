# JettraServer

## Descripción General
`JettraServer` es el módulo de infraestructura de servidor que soporta y arranca las aplicaciones dentro del framework JettraStack. Define las configuraciones de despliegue y maneja el ciclo de vida del servidor web embebido.

## Detalles Específicos
- **Arquitectura general**: Motor de servidor web que levanta los contextos, escuchadores y configuraciones necesarias para las aplicaciones REST y WUI.
- **Dependencias clave**: Servidores embebidos subyacentes, contenedores de servlets y APIs de Jakarta/JAX-RS.
- **Roles dentro del sistema**: Proporciona la infraestructura de red en la cual se montan los microservicios (`JettraBackend`) y las interfaces (`JettraWUI`).

## Características Detalladas
- **Inicialización Autónoma**: Capacidad de arrancar un entorno completo sin necesidad de un contenedor de aplicaciones externo pesado.
- **Gestión de Contextos**: Despliegue de aplicaciones, mapeo de rutas y registro automático de filtros y recursos.
- **Logging y Monitorización Base**: Mecanismos iniciales para registrar actividad en el entorno del servidor.

## Guía de Entrenamiento (AI / Nuevas Características)
- Las modificaciones en este módulo deben realizarse considerando el impacto global, ya que cualquier fallo afectará el inicio de toda aplicación construida sobre JettraStack.
- Para añadir nuevos motores o características de servidor, se deben implementar a nivel de abstracciones, sin romper el código que interactúa con la inicialización predeterminada.
