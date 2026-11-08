# JettraGRPC

## Descripción General
`JettraGRPC` es el módulo destinado a manejar la capa de comunicación de alto rendimiento y baja latencia utilizando gRPC en el ecosistema JettraStack.

## Detalles Específicos
- **Arquitectura general**: Infraestructura para crear servidores y clientes gRPC basados en Protobuf.
- **Dependencias clave**: Depende de librerías oficiales de gRPC para Java y compresión Protobuf. También interacciona con `JettraChunks` si se manejan datos por fragmentos.
- **Roles dentro del sistema**: Facilitar la interconexión entre microservicios de manera más rápida y eficiente que las peticiones HTTP/REST tradicionales, ideal para tareas en tiempo real o streaming de datos.

## Características Detalladas
- **Soporte Protobuf**: Integración de esquemas de datos tipados y rápidos.
- **Streaming Bidireccional**: Capacidad para soportar streaming de datos en ambas direcciones (servidor a cliente y viceversa).
- **Clientes y Servidores RPC**: Facilidad para exponer servicios remotos con tipado fuerte en Java.

## Guía de Entrenamiento (AI / Nuevas Características)
- Cuando se añada una nueva funcionalidad basada en gRPC, se deben crear los archivos `.proto` correspondientes y regenerar las clases Java asociadas.
- Mantener los servicios en este módulo desacoplados de los repositorios de la base de datos (que pertenecen a JettraBackend) delegando a los servicios inyectables.
- Preferir el uso de gRPC sobre REST únicamente cuando el rendimiento, el contrato tipado y el streaming sean requerimientos indispensables.
