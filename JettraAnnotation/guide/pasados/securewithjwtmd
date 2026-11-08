# Ejemplo de Endpoint Seguro en JettraWebBackEnd

El proyecto `JettraWebBackEnd` incluye un sistema de autenticación centralizado mediante el `AuthController`.

## Endpoint de Login

El endpoint `/api/auth/login` recibe las credenciales del usuario, las valida contra la base de datos (por medio de `CredentialRepository` y `UserRepository`), y en caso de éxito, retorna un token JWT que incluye los roles del usuario.

### Estructura de la Petición

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin"
}
```

### Respuesta Exitosa

El backend validará las credenciales y, si son correctas, extraerá los roles del usuario (ej: `ADMIN`, `USER`) y los adjuntará al payload del token JWT.

```json
{
  "token": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlcyI6WyJBRE1JTiIsIk1BTkFHRVIiLCJERU1PIl0sInN1YiI6ImFkbWlu...signature"
}
```

## Uso del Token

Para acceder a cualquier otro endpoint protegido en `JettraWebBackEnd` o en los controladores construidos sobre `JettraRest`, se debe incluir el token en el encabezado de la petición HTTP:

```http
GET /api/admin/dashboard
Authorization: Bearer <tu_token_aqui>
```

El servidor se encargará de decodificar el token, revisar los roles (`RolesAllowed`) y permitir o denegar el acceso a la ruta de manera automática.
