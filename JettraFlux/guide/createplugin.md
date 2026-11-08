# Creación y Gestión de Plugins en JettraFlux

En **JettraFlux**, los plugins son componentes modulares y desacoplados (como temas visuales) que pueden ser desarrollados como proyectos independientes y luego inyectados en la aplicación principal sin necesidad de modificar el código interno de JettraFlux.

## 1. ¿Cómo crear un Plugin de Tema?
Para generar la estructura base de un nuevo plugin de tema (ej. `SkyRed`), puedes utilizar la herramienta de línea de comandos `mvn-flux`.

Ejecuta el siguiente comando en la raíz de tu Workspace:
```bash
./mvn-flux -generate-theme-project SkyRed -url-source https://primeui.store/templates/angular/freya
```

Esto generará automáticamente un proyecto Maven independiente llamado `SkyRed` que contendrá:
- `pom.xml`: Configurado para compilar tu plugin en un `.jar`.
- `src/main/resources/META-INF/theme.json`: El archivo descriptor esencial del plugin.
- Archivos Java y recursos estáticos opcionales.

## 2. El Archivo Descriptor `theme.json`
El núcleo del plugin es el archivo `theme.json`. `JettraFlux` utiliza un escáner (`ThemeRegistry`) en tiempo de ejecución para detectar cualquier `.jar` agregado al proyecto final que contenga un archivo `META-INF/theme.json`.

Ejemplo de `theme.json`:
```json
{
  "name": "SkyRed",
  "primary": "#ef4444",
  "secondary": "#dc2626",
  "background": "#0f172a",
  "surface": "rgba(30, 41, 59, 0.7)",
  "onPrimary": "#ffffff",
  "onSurface": "#f8fafc",
  "buttonStyle": "border: none; border-radius: 8px; padding: 12px 24px...",
  "cardStyle": "border-radius: 16px; backdrop-filter: blur(12px)...",
  "containerStyle": "padding: 24px; border-radius: 12px...",
  "textStyle": "font-family: 'Inter', sans-serif...",
  "customCss": "body { background-color: #0f172a; }",
  "customJs": "console.log('SkyRed Theme Loaded!');"
}
```
**Importante**: Puedes incluir todo el CSS (como animaciones complejas, efectos *Glassmorphism*, etc.) y JS necesarios dentro de las llaves `customCss` y `customJs`.

## 3. Instalación del Plugin
1. **Compilar el Plugin**: Entra a la carpeta de tu nuevo plugin (ej. `SkyRed`) y compílalo usando Maven:
   ```bash
   cd SkyRed
   mvn clean install
   ```
2. **Añadirlo como Dependencia**: Abre el `pom.xml` de tu proyecto final (por ejemplo, `FacturaWeb` o `JettraFluxExample`) y agrega la dependencia de tu plugin:
   ```xml
   <dependency>
       <groupId>com.jettraflux.theme</groupId>
       <artifactId>SkyRed</artifactId>
       <version>1.0-SNAPSHOT</version>
   </dependency>
   ```

## 4. Detección Automática
¡Listo! Cuando inicies tu proyecto final (ej. `FacturaWeb`), **JettraFlux** detectará automáticamente tu plugin al escanear el *classpath* buscando archivos `META-INF/theme.json`. 

El tema "SkyRed" aparecerá instantáneamente en las opciones de temas disponibles del sistema para que el usuario pueda seleccionarlo y aplicarlo de inmediato.
