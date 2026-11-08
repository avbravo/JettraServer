# Usaremos una hipotética imagen base de OpenJDK 25 (Project Leyden support optimizado)
# En caso de no estar disponible temporalmente en entornos de CI, se puede usar eclipse-temurin u otro fork
FROM openjdk:25-jdk-slim

# Etiqueta orientativa del mantenedor
LABEL maintainer="Jettra Administrator"
LABEL description="Contenedor para JettraAppServer optimizado"

# Crear directorio de trabajo en el contenedor
WORKDIR /app

# Copiar el archivo empaquetado JAR desde la fase de build
COPY target/JettraAppServer-1.0.0-SNAPSHOT.jar /app/JettraAppServer.jar

# Exponer el puerto por el que el servidor HTTP / REST va a escuchar tráfico
EXPOSE 8080

# Punto de entrada por defecto
# Se lanza como una aplicación Java normal, con optimizaciones futuras de Project Leyden
ENTRYPOINT ["java", "-jar", "JettraAppServer.jar"]
