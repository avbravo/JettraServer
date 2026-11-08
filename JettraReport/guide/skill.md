# JettraReport

## Descripción General
`JettraReport` proporciona las utilidades necesarias para la generación, exportación y manejo de informes dentro del ecosistema JettraStack.

## Detalles Específicos
- **Arquitectura general**: Motor o capa de compatibilidad para reportes que intercepta modelos de datos y los convierte en documentos (PDF, Excel, vistas modales).
- **Dependencias clave**: Librerías de generación de PDFs y parseadores de datos. Depende de las abstracciones de `JettraICore`.
- **Roles dentro del sistema**: Encargado de mostrar y exportar la información procesada para facilitar su análisis y registro por el usuario final.

## Características Detalladas
- **Anotaciones de Modelo**: Control total de cómo se muestran los campos mediante metadatos y anotaciones como `@ModelReportDisabledHeader` (para quitar cabeceras) o anotaciones de formateo.
- **ReportViewer**: Integración de vistas modales nativas para la previsualización del reporte en el navegador o en la UI.
- **Formatos Personalizables**: Permite adaptar el diseño según los requerimientos de la entidad.

## Guía de Entrenamiento (AI / Nuevas Características)
- Para agregar soporte a nuevos formatos de exportación (ej. CSV, Word), se deben implementar los adaptadores dentro de este módulo y mantener la compatibilidad con las anotaciones existentes.
- Al añadir nuevas configuraciones (como el manejo del header, estilos de celdas), primero define una anotación y luego un procesador que lea la anotación del modelo durante el runtime del reporte.
