# Service Reservation - EAP03

Servicio backend para gestión de reservas, construido con **Spring Boot** y **Java 21**.

## Tecnologías

- Java 21
- Spring Boot 4.1.1
  - Spring Data JPA
  - Spring Security
  - Spring Validation
  - Flyway (migraciones de base de datos)
- PostgreSQL
- Gradle (wrapper incluido)
- JUnit 5 + H2 (pruebas)

## Requisitos previos

- JDK 21
- Una instancia de PostgreSQL accesible

## Configuración

La aplicación se configura mediante variables de entorno (con valores por defecto para desarrollo local):

| Variable      | Descripción              | Default     |
|---------------|---------------------------|-------------|
| `DB_HOST`     | Host de PostgreSQL        | `localhost` |
| `DB_PORT`     | Puerto de PostgreSQL      | `5432`      |
| `DB_NAME`     | Nombre de la base de datos| `eap03`     |
| `DB_USERNAME` | Usuario de la base de datos | `postgres` |
| `DB_PASSWORD` | Contraseña de la base de datos | `postgres` |

Las migraciones de esquema se gestionan con Flyway desde `src/main/resources/db/migration`.

## Cómo ejecutar

```bash
./gradlew bootRun
```

En Windows:

```bash
gradlew.bat bootRun
```

## Cómo compilar y probar

```bash
./gradlew build
```

Las pruebas usan una base de datos H2 en memoria (ver `src/test/resources/application.yaml`), por lo que no requieren una instancia de PostgreSQL activa.

## Estructura del proyecto

```
src/main/java     Código fuente de la aplicación
src/main/resources  Configuración y migraciones
src/test          Pruebas
monitoring/       Configuración de Prometheus
```

## Observabilidad con Prometheus

El proyecto cuenta con integración de métricas para **Prometheus** mediante Spring Boot Actuator y Micrometer.

### 1. Endpoints de Métricas y Salud

#### Entorno Desplegado (Render)
- **Métricas Prometheus**: `https://service-reservation-eap03-latest.onrender.com/actuator/prometheus`
- **Métricas en JSON**: `https://service-reservation-eap03-latest.onrender.com/actuator/metrics`
- **Salud del servicio**: `https://service-reservation-eap03-latest.onrender.com/actuator/health`

#### Entorno Local (Docker / Host)
- **Métricas Prometheus**: `http://localhost:8080/actuator/prometheus`
- **Métricas en JSON**: `http://localhost:8080/actuator/metrics`
- **Salud del servicio**: `http://localhost:8080/actuator/health`

### 2. Levantar el Servidor Prometheus con Docker Compose
```bash
docker compose up -d prometheus
```
O el stack completo local:
```bash
docker compose up --build
```

Servicios disponibles:
- **App Backend**: `http://localhost:8080`
- **PostgreSQL**: `localhost:5433` (interno 5432)
- **Servidor Prometheus**: `http://localhost:9090`

### 3. Targets configurados en Prometheus
En `monitoring/prometheus/prometheus.yml` se encuentran configurados tres entornos de recolección:
1. `eap03-render`: Scraping al backend desplegado en Render vía HTTPS.
2. `eap03-service-reservation`: Scraping al contenedor local en la red Docker (`app:8080`).
3. `eap03-local-host`: Scraping a la aplicación corriendo localmente fuera de Docker (`host.docker.internal:8080`).

Se puede verificar el estado de los targets en: [http://localhost:9090/targets](http://localhost:9090/targets).

### 4. Consultas PromQL Útiles en Prometheus
- **Tasa de peticiones HTTP por segundo (RPS)**:
  ```promql
  rate(http_server_requests_seconds_count[1m])
  ```
- **Latencia promedio de peticiones HTTP**:
  ```promql
  rate(http_server_requests_seconds_sum[1m]) / rate(http_server_requests_seconds_count[1m])
  ```
- **Errores HTTP (4xx y 5xx)**:
  ```promql
  sum(rate(http_server_requests_seconds_count{status=~"4..|5.."}[1m])) by (status, uri)
  ```
- **Uso de memoria JVM (Heap en MB)**:
  ```promql
  jvm_memory_used_bytes{area="heap"} / 1024 / 1024
  ```
- **Uso de CPU del proceso**:
  ```promql
  process_cpu_usage * 100
  ```
- **Conexiones activas del pool de base de datos (HikariCP)**:
  ```promql
  hikaricp_connections_active
  ```

#### Métricas de Negocio Personalizadas
- `negocio_reservas_creadas_total`: Total de reservas creadas satisfactoriamente.
- `negocio_reservas_canceladas_total`: Total de reservas canceladas.
- `negocio_auth_login_fallidos_total`: Total de intentos fallidos de inicio de sesión.


