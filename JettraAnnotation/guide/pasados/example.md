# Guía de Uso de JettraServer y Componentes

Esta guía muestra cómo usar los componentes de JettraServer para inyectar configuraciones y cómo integrarse con el API de generación de interfaces de usuario.

## Inyección de Configuración (JettraConfig)

El archivo base de configuración debe llamarse `jettra-config.properties` y estar disponible en el *classpath* (comúnmente en `src/main/resources`). Se usa para definir las variables del entorno:

```properties
title=Jettra
system.version=1.0
```

Dichas propiedades se pueden inyectar directamente en los atributos de cualquiera de sus clases, usando la anotación `@JettraConfigProperty`. El motor de Inyección de Dependencias de JettraServer se encarga de asignar el valor en tiempo de ejecución.

### Ejemplo de Implementación (`ExampleRest`)

```java
import java.io.Serializable;
import io.jettra.server.config.JettraConfigProperty;
import io.jettra.server.core.IO;

public class ExampleRest implements Serializable {

    @JettraConfigProperty(name = "title")
    private String title;

    @JettraConfigProperty(name = "system.version")
    private String systemVersion;

    public void draw() {
        IO.println(title + " " + systemVersion);
    }
}
```

*Nota: Durante el inicio de la aplicación o del controlador, se puede invocar manualmente a `ConfigInjector.inject(this)` para asegurar la inyección si la clase misma no fue instanciada por el propio contenedor DI del framework.*

## Generación de Interfaces de Usuario (JettraUI)

JettraServer está pensado para servir de motor backend a **JettraUI**. Al utilizar JettraServer en el backend, no necesita plantillas complejas ni SSR tradicional (Server Side Rendering). Puede devolver directamente objetos compatibles u ordenarle a la librería JettraUI que los renderice dinámicamente:

1. **Definir el Componente**: Utilice las clases base provistas por JettraUI (por ej., elementos 3D, HTML dinámico) en un controlador de JettraServer.
2. **Configuración de sesión**: Aproveche el módulo interno de JWT de JettraServer para verificar la sesión del usuario antés de servirle el componente.
3. **Respuesta Serializada**: Retorne la interfaz en un formato comprensible por el visor del lado del cliente.
