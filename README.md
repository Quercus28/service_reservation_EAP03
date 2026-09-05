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
```
