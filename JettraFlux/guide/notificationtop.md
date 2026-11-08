# NotificationTop Component

El componente `NotificationTop` se utiliza para mostrar notificaciones interactivas y asíncronas en la barra superior o en distintas partes de la interfaz, combinando un Avatar (o un ícono personalizable de `Icon`) y un Badge dinámico.

## Auto-Actualización
`NotificationTop` incluye un script embebido para el sondeo y refresco **automático** (polling / SSE integrado con JettraSyncManager). Gracias a esto, no es necesario recargar la página ni esperar la interacción de otro componente para que el cliente reciba la notificación. 

Además, los textos de las notificaciones que se despliegan en su panel al hacer clic **están alineados a la izquierda**, ofreciendo una lectura clara para historiales largos.

## Modos de Notificación (NotificationTopType)
El componente soporta una propiedad `type` que utiliza el enumerador `NotificationTopType` para definir en qué canal de comunicación opera:

### 1. GLOBAL
Cuando se define como `GLOBAL`, al actualizar este componente desde el servidor, **todos los usuarios activos** reciben la notificación.
- **Uso:** Alertas generales del sistema, anuncios de mantenimiento, noticias globales.
- **Comportamiento:** Se itera sobre todas las sesiones del servidor (`JettraContext.getSessions()`) y se actualiza el componente en cada sesión.

### 2. PERSONAL
Cuando se define como `PERSONAL`, se emite la notificación exclusivamente al usuario objetivo (por ejemplo, el usuario actual o uno específico).
- **Uso:** Cuando un usuario recibe un correo electrónico directo, un mensaje privado, o finaliza una tarea de fondo que solo le concierne a él.
- **Comportamiento:** Únicamente se actualiza la instancia de `NotificationTop` dentro de su propia sesión de `JettraContext`.

### 3. CHANNEL
Cuando se usa `CHANNEL`, se registra un canal mediante la propiedad `.channel("nombre_del_canal")`. Las notificaciones dirigidas a este canal llegan a todos los demás miembros suscritos. 
- **Uso:** Gestión de grupos, notificaciones de equipo (ej. canal de administradores).
- **Comportamiento:** Evita actualizar la instancia del usuario que originó el mensaje para prevenir que él mismo vea de forma redundante una notificación que acaba de emitir.

---

## Ejemplo de Uso (Declaración en el UI)

Normalmente los instanciamos y agregamos en el `TopBar` o menú superior de nuestra plantilla (`TemplatePage`):

```java
// 1. Instancia Global
NotificationTop globalNotif = TemplatePage.getNotificationTop("global_notif")
    .type(NotificationTop.NotificationTopType.GLOBAL)
    .icon(Icon.of("fas fa-globe"));

// 2. Instancia Personal
NotificationTop personalNotif = TemplatePage.getNotificationTop("personal_notif")
    .type(NotificationTop.NotificationTopType.PERSONAL)
    .icon(Icon.of("fas fa-envelope"));

// 3. Instancia por Canal
NotificationTop channelNotif = TemplatePage.getNotificationTop("channel_notif")
    .type(NotificationTop.NotificationTopType.CHANNEL)
    .channel("admin_channel")
    .icon(Icon.of("fas fa-bullhorn"));
```

## Ejemplo de Uso (Emisión de Eventos desde un Action)

Cuando sucede una acción en la aplicación, puedes acceder a las sesiones para inyectar el mensaje en el canal deseado. El método `.addMessage("...")` agregará el mensaje al historial desplegable (popover) y ajustará el badge automáticamente:

```java
@ActionWidgetAllow(role = { "ADMIN" })
private void notifyGlobal(HttpExchange exchange, Map<String, String> params) {
    Map<String, Map<String, Object>> allSessions = JettraContext.getSessions();
    for (Map.Entry<String, Map<String, Object>> entry : allSessions.entrySet()) {
        Map<String, Object> sessionVars = entry.getValue();
        Map<String, NotificationTop> notifs = (Map<String, NotificationTop>) sessionVars.get("template_notifications");
        
        NotificationTop globalNt = notifs.get("global_notif");
        if (globalNt != null && globalNt.getType() == NotificationTopType.GLOBAL) {
            globalNt.value = globalNt.value + 1;
            globalNt.addMessage("Alerta Global: Sistema actualizado");
        }
    }
}
```
