# ==============================================================================
# JettraServerBackEndExample - Microservicios Backend con Java 25 & Project Loom
# Soporte nativo para Acreditación JWT, JettraSecurityDB y Swagger-UI JettraFlux
# ==============================================================================
FROM bellsoft/liberica-runtime-container:jre-25-stream-musl
LABEL maintainer="Jettra Ecosystem <dev@jettra.io>"
LABEL description="JettraServerBackEndExample - Microservicios Backend en Java 25 con Virtual Threads"

# Instalar curl para healthchecks y certificados CA en Alpine/musl
RUN if command -v apk > /dev/null; then \
        apk add --no-cache curl ca-certificates tzdata && rm -rf /var/cache/apk/*; \
    elif command -v apt-get > /dev/null; then \
        apt-get update && apt-get install -y --no-install-recommends curl ca-certificates tzdata && rm -rf /var/lib/apt/lists/*; \
    fi

WORKDIR /app

# Crear directorios para datos de microservicios y seguridad
RUN mkdir -p /app/data /app/config && \
    chown -R 1000:1000 /app

# Copiar el Uber JAR generado por Maven
COPY target/JettraServerBackEndExample-*-uber.jar /app/app.jar

# Copiar archivo de configuración MicroProfile por defecto
COPY src/main/resources/META-INF/microprofile-config.properties /app/config/microprofile-config.properties

ENV SERVER_PORT=8083 \
    SERVER_HOST=0.0.0.0 \
    JETTRA_STORAGE_MODE=JVM_RAM \
    JETTRA_SERVER_DATA_DIR=/app/data \
    JAVA_OPTS="--enable-preview -XX:+UnlockExperimentalVMOptions -XX:+UseCompactObjectHeaders -XX:+UseContainerSupport -XX:+UseZGC -Xms256m -Xmx1g"

EXPOSE 8083

HEALTHCHECK --interval=15s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8083/api/v1/system/ping || exit 1

USER 1000:1000

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]

