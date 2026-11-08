# Image en JettraFlux

El componente `Image` permite renderizar una etiqueta `<img>` en la interfaz. Soporta tanto URLs externas (ej. https://...) como rutas locales a recursos estáticos servidos por tu backend.

## Uso Básico (URL Externa)
Mantiene su funcionalidad original para imágenes hospedadas en internet.

```java
Image.of("https://primefaces.org/cdn/primevue/images/galleria/galleria1.jpg")
     .alt("Descripción de la imagen")
     .modifier(new Modifier().style("width: 100%; border-radius: 8px;"));
```

## Uso con Imágenes Locales (Archivos del Servidor)
Para utilizar imágenes locales que residen en tu disco o proyecto, debes asegurarte de que tu servidor (como `JettraServer` o cualquier otro servidor en uso) esté configurado para despachar archivos estáticos.

### Ejemplo 1: Ruta Local Absoluta (Recomendado)
Suponiendo que tu servidor tiene un directorio de recursos estáticos montado en `/assets` o `/img`, debes pasarle esa ruta local al componente. El navegador solicitará la imagen a tu mismo servidor.

```java
// Carga una imagen almacenada en la carpeta expuesta "assets/images" de tu proyecto local
Image.of("/assets/images/logo.png")
     .alt("Logotipo de la Empresa")
     .modifier(new Modifier().style("width: 200px; height: auto;"));
```

### Ejemplo 2: Ruta Dinámica de Contexto Local
Si tu aplicación se despliega en un subcontexto (por ejemplo `/miapp/`), es recomendable usar resolutores de rutas relativos a tu servidor para apuntar a la imagen local sin hardcodear el contexto:

```java
// Usando un path local relativo a la ruta base de la aplicación (ej. JettraServer)
String localImagePath = JettraServer.resolvePath("/public/images/banner.png");

Image.of(localImagePath)
     .alt("Banner Promocional")
     .modifier(new Modifier().style("width: 100%; border-radius: 4px;"));
```

Al final, el componente `Image` simplemente colocará este string en el atributo `src` de HTML, dejando que el navegador obtenga la imagen localmente.
