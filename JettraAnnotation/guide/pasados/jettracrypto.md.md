# JettraCrypto

Es una clase para encriptar y desencriptar password.

## Ejemplo

```java

import io.jettra.server.autentification.JettraCrypto;
// Encriptar
String passwordEncriptado = JettraCrypto.encrypt("superSecreto","my-frase-secreta");
System.out.println("Encriptado: " + passwordEncriptado);
// Desencriptar
String passwordDesencriptado = JettraCrypto.decrypt(passwordEncriptado,"my-frase-secreta");
System.out.println("Desencriptado: " + passwordDesencriptado);

```

