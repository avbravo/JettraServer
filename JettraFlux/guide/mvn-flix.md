# Comando `mvn-flux`

El comando `mvn-flux` es la herramienta de línea de comandos integrada en el ecosistema Jettra (ejecutada mediante `io.jettra.server.cli.FluxCLI`). Permite automatizar la creación de código, inicialización de estructuras front-end y, más recientemente, la generación de plugins de temas.

## Nuevo Comando: `-generate-theme-project`

Para facilitar la creación de temas dinámicos que JettraFlux detectará automáticamente a través de la arquitectura de plugins (`theme.json`), puedes utilizar el comando `-generate-theme-project`.

### Sintaxis

```bash
./mvn-flux -generate-theme-project <nombre-proyecto-plugin> [-path <path>] [-url-source <url-template-example>] [-css-source <ruta-archivo-css>] [-js-source <ruta-archivo-js>]
```

### Parámetros

- `<nombre-proyecto-plugin>`: El nombre de tu nuevo proyecto (ej. `SkyRed`). Esto creará una carpeta con el mismo nombre en el directorio actual (o en el proporcionado con `-path`).
- `-url-source`: (Opcional) Una URL de referencia que sirvió de inspiración para el diseño (ej. `https://primeui.store/templates/angular/freya`).
- `-css-source`: (Opcional) Indica la ruta del archivo CSS del tema. Si no se indica, se asume que `-url-source` contiene el CSS dentro de él.
- `-js-source`: (Opcional) Indica la ruta del archivo JS del tema. Si no se especifica, indica que `-url-source` contiene las instrucciones del archivo javascript.
- `-path`: (Opcional) Si se omite, se generará en el directorio actual.

### Ejemplos de Uso

```bash
./mvn-flux -generate-theme-project SkyRed -url-source https://primeui.store/templates/angular/freya
```

```bash
./mvn-flux -generate-theme-project SkyRed -css-source ./styles.css -js-source ./app.js
```

Al finalizar la ejecución, este comando creará un proyecto Maven independiente, empaquetado como `jar`, y con la carpeta `src/main/resources/META-INF/` conteniendo el archivo descriptor base **`theme.json`**. 

Luego, solo tendrás que entrar a la carpeta, modificar el `theme.json` para definir tus estilos, y compilar:

```bash
cd SkyRed
mvn clean install
```

Para más detalles sobre la estructura del descriptor de temas, consulta [createplugin.md](createplugin.md).
