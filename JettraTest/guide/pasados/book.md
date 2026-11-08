# Guía de Pruebas con JettraTest

**JettraTest** es el framework oficial para la ejecución de pruebas dentro del ecosistema Jettra. Su objetivo es proporcionar herramientas unificadas para:

- Pruebas Unitarias clásicas.
- Soporte para Mockup de dependencias.
- Pruebas de integración para autenticación JWT.
- Validación de Interfaz Web (Web UI) simulando eventos como clics y registro de formularios.

A diferencia de otros frameworks, **JettraTest** es 100% nativo y cuenta con su propio motor (`JettraTestRunner`) que genera reportes estándar compatibles con Surefire XML, logrando una excelente integración con pipelines de CI/CD.

---

## 1. Configuración de JettraTest en tu Proyecto (Maven)

Como parte de la arquitectura de la pila Jettra (incluyendo **JettraTest** y **JettraBackEnd**), se ha decidido **reemplazar completamente** el uso del `maven-surefire-plugin` para la generación de reportes y ejecución de pruebas. En su lugar, utilizamos una implementación propia impulsada por `JettraTestRunner` y el `exec-maven-plugin`.

Para que tu proyecto pueda utilizar las anotaciones de JettraTest y se ejecute automáticamente durante la fase de validación de Maven, debes actualizar tu archivo `pom.xml` con lo siguiente:

### A. Dependencia de JettraTest
Agrega la dependencia de la librería en la sección `<dependencies>`:
```xml
<dependency>
    <groupId>io.jettra</groupId>
    <artifactId>JettraTest</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### B. Configuración de los Plugins de Ejecución
Debido a que JettraTest usa su propio Runner, debes **deshabilitar el plugin por defecto de Maven Surefire** añadiendo la propiedad `surefire.skip` y configurar el `exec-maven-plugin` para que invoque a JettraTestRunner durante la fase `test`:

```xml
<properties>
    <skipTests>true</skipTests>
</properties>

<build>
    <plugins>
        <!-- Ejecutar el JettraTestRunner en la fase test -->
        <plugin>
            <groupId>org.codehaus.mojo</groupId>
            <artifactId>exec-maven-plugin</artifactId>
            <version>3.1.0</version>
            <executions>
                <execution>
                    <id>jettra-test</id>
                    <phase>test</phase>
                    <goals>
                        <goal>java</goal>
                    </goals>
                    <configuration>
                        <mainClass>io.jettra.test.runner.JettraTestRunner</mainClass>
                        <classpathScope>test</classpathScope>
                        <arguments>
                            <argument>${project.build.testOutputDirectory}</argument>
                        </arguments>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

---

## 2. Cómo Escribir Tests

### 2.1 Pruebas Unitarias Clásicas
Utiliza la anotación `@JettraTest` para marcar los métodos de prueba y `JettraAssert` para aserciones.

```java
import io.jettra.test.annotation.JettraTest;
import io.jettra.test.core.JettraAssert;

public class CalculadoraTest {

    @JettraTest
    public void testSumaBasica() {
        int resultado = 2 + 2;
        JettraAssert.assertEquals(4, resultado, "La suma de 2+2 debería ser 4");
    }
}
```

### 2.2 Pruebas con Mockups
Para simular comportamientos de clases inyectadas o bases de datos, utiliza `@JettraMock`.

```java
import io.jettra.test.annotation.JettraTest;
import io.jettra.test.annotation.JettraMock;
import io.jettra.test.core.JettraAssert;

public class ServicioTest {

    @JettraMock
    private Repositorio mockRepositorio;

    @JettraTest
    public void testConMock() {
        // La implementación de Mocking intercepta llamadas a mockRepositorio
        JettraAssert.assertNotNull(mockRepositorio, "El mockup debe inyectarse");
    }
}
```

### 2.3 Pruebas de Endpoints con JWT
`JwtTestClient` simplifica la autenticación y el envío de tokens en encabezados HTTP.

```java
import io.jettra.test.annotation.JettraTest;
import io.jettra.test.core.JettraAssert;
import io.jettra.test.jwt.JwtTestClient;

public class AuthTest {

    @JettraTest
    public void testFlujoJwt() {
        JwtTestClient jwtClient = new JwtTestClient();
        try {
            // 1. Iniciar sesión y obtener token
            String payload = "{\"username\":\"admin\", \"password\":\"secreto\"}";
            String token = jwtClient.authenticate("http://localhost:9002/auth", payload);
            JettraAssert.assertNotNull(token);
            
            // 2. Realizar petición GET con Token Bearer
            String response = jwtClient.getWithToken("http://localhost:9002/recurso");
            JettraAssert.assertNotNull(response);
        } catch (Exception e) {
            JettraAssert.assertTrue(false, "Falló la prueba: " + e.getMessage());
        }
    }
}
```

### 2.4 Pruebas de Interfaz Web (Web UI)
Valida y simula el renderizado y clics usando `WuiTestRunner`.

```java
import io.jettra.test.annotation.JettraTest;
import io.jettra.test.core.JettraAssert;
import io.jettra.test.wui.WuiTestRunner;

public class VistaTest {

    @JettraTest
    public void testSimularBoton() {
        WuiTestRunner runner = new WuiTestRunner();
        runner.registerFormData("form1", "{\"data\":\"test\"}");
        runner.simulateClick("btnSubmit");
        runner.validateInterface("Dashboard");
        JettraAssert.assertTrue(true, "Flujo completado");
    }
}
```

---

## 2.5 Ciclo de Vida del Servidor de Pruebas

JettraTest provee una gestión nativa para levantar y detener automáticamente el servidor del proyecto antes de ejecutar las pruebas que lo necesiten.

### Anotaciones de Ciclo de Vida
- `@RequiresRunningServer`: Indica que el framework debe buscar un puerto disponible, levantar el servidor y ajustar la propiedad de puerto antes de ejecutar la prueba. (Por defecto, JettraTest asume que las pruebas requieren servidor).
- `@NotRequiresRunningServer`: Indica que la prueba es puramente aislada y no necesita que se levante el servidor.
- `@JettraTestLauncher`: Se coloca en una clase de tu proyecto (ej. `MyTestLauncher`) que JettraTest instanciará para levantar y detener el servidor.

### Clase Lanzadora (Launcher)
Debes implementar una clase en tu código de prueba que indique a JettraTest cómo arrancar el servidor:

```java
import io.jettra.test.annotation.JettraTestLauncher;

@JettraTestLauncher
public class MyTestLauncher {

    public void startServer(int port) {
        // Asignar el puerto dinámico y arrancar el servidor
        System.setProperty("server.port", String.valueOf(port));
        // Aquí llamas al arranque de tu App
    }

    public void stopServer() {
        // Lógica para detener tu servidor
    }
}
```

### Inyección de Puerto Dinámico
Si tu clase de prueba define un atributo llamado `ServerPortTest` o `serverPortTest` (ya sea `int` o `Integer`), el framework inyectará allí el puerto dinámico generado.

```java
import io.jettra.test.annotation.RequiresRunningServer;
import io.jettra.test.annotation.JettraTest;

@RequiresRunningServer
public class AppTest {

    public Integer ServerPortTest;

    @JettraTest
    public void testFlujo() {
        // En lugar de usar puerto fijo, utiliza ServerPortTest
        String url = "http://localhost:" + ServerPortTest + "/auth";
        // ...
    }
}
```

---

## 3. Cómo Ejecutar las Pruebas

Debido a que hemos enlazado nuestro runner personalizado a la fase normal de Maven, la ejecución es completamente estándar:

**Desde la consola:**
Ejecuta el siguiente comando en la raíz del proyecto:
```bash
mvn clean test
```

**Proceso Interno:**
1. Maven compilará los tests.
2. `exec-maven-plugin` lanzará `io.jettra.test.runner.JettraTestRunner`.
3. El Runner escaneará los binarios detectando tus métodos `@JettraTest`.
4. Mostrará los resultados en consola y generará automáticamente dos archivos de reporte (`TEST-*.xml` formato compatible con CI/CD, y `TEST-*.html` formato visual) en el directorio `target/jettra-test-reports/`.
5. Si alguna prueba falla (ej. si `JettraAssert` falla), Maven retornará un `BUILD FAILURE` y detendrá el proceso de integración o compilación.

---

## 4. Estilos de Consola (Soporte Semáforo)

Para brindar una mejor experiencia al desarrollador, **JettraTestRunner** emite automáticamente sus reportes en consola con códigos de colores ANSI en un estilo "semáforo":

- **Verde**: Indica pruebas exitosas (`Tests run: 1, Failures: 0`).
- **Rojo**: Resalta los fallos, excepciones y el estado final con errores, ayudando a detectar rápidamente en qué método falló.
- **Amarillo**: Indica saltos (ej. cuando se utiliza `-DskipTests`) o advertencias (como no encontrar un *Launcher* requerido).
- **Cian**: Mensajes informativos estructurales y log del ciclo de vida del servidor (arranque y apagado).

### Ejemplo de Salida Coloreada:

```text
[CYAN] -------------------------------------------------------
[CYAN]  T E S T S  (JettraTest Framework)
[CYAN] -------------------------------------------------------
[CYAN] [JettraTestRunner] Server required. Starting via com.jettra.example.MyTestLauncher on port 42829

Running com.jettra.example.AppTest
[GREEN] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.158 s

Results:

[GREEN] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[CYAN] [JettraTestRunner] Stopping server via com.jettra.example.MyTestLauncher
```

*(En una terminal compatible con ANSI, los textos entre corchetes aparecerán del color indicado)*
