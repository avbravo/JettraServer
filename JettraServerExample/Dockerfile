# ==============================================================================
# JettraServerExample - Jakarta EE 11, PrimeFaces 16 & JettraFlux
# Java 25 & Project Loom sobre BellSoft Liberica Runtime Musl
# ==============================================================================
FROM bellsoft/liberica-runtime-container:jre-25-stream-musl
LABEL maintainer="Jettra Ecosystem <dev@jettra.io>"
LABEL description="JettraServerExample - Servidor Web Jakarta EE 11 y Reactivo en Java 25"

# Instalar curl para healthchecks y certificados CA en Alpine/musl
RUN if command -v apk > /dev/null; then \
        apk add --no-cache curl ca-certificates tzdata && rm -rf /var/cache/apk/*; \
    elif command -v apt-get > /dev/null; then \
        apt-get update && apt-get install -y --no-install-recommends curl ca-certificates tzdata && rm -rf /var/lib/apt/lists/*; \
    fi

WORKDIR /app

# Crear directorios para datos de seguridad y aplicación
RUN mkdir -p /app/data /app/config && \
    chown -R 1000:1000 /app

# Copiar el Uber JAR generado por Maven
COPY target/JettraServerExample-*-uber.jar /app/app.jar

# Copiar archivo de configuración MicroProfile por defecto
COPY src/main/resources/META-INF/microprofile-config.properties /app/config/microprofile-config.properties

ENV SERVER_PORT=8080 \
    SERVER_HOST=0.0.0.0 \
    JETTRA_STORAGE_MODE=JVM_RAM \
    JETTRA_SERVER_DATA_DIR=/app/data \
    JAVA_OPTS="--enable-preview -XX:+UnlockExperimentalVMOptions -XX:+UseCompactObjectHeaders -XX:+UseContainerSupport -XX:+UseZGC -Xms256m -Xmx1g"

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/health || exit 1

USER 1000:1000

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]

