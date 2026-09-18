# ─────────────────────────────────────────────
# Stage 1: Build
# ─────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copiar wrapper y archivos de configuracion de Gradle
COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle ./gradle

# Descargar dependencias en capa separada (cache-friendly)
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon -q || true

# Copiar el codigo fuente
COPY src ./src

# Construir el JAR de produccion (sin tests: los tests usan H2, aqui usamos Postgres)
RUN ./gradlew bootJar -x test --no-daemon -q

# ─────────────────────────────────────────────
# Stage 2: Runtime
# ─────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# Usuario no-root para seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copiar solo el JAR generado
COPY --from=builder /app/build/libs/EAP03-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]