# VehicleCare Micro

Backend de `VehicleCare`, desarrollado con `Spring Boot 3`, `Java 17`, `MySQL` y `MinIO`.

El proyecto expone una API REST para autenticación, gestión de usuarios, vehículos, mantenimientos y documentos asociados. Está pensado como backend principal de la app Flutter `vehiclecare_app`.

## Objetivo

`VehicleCare` es una aplicación para llevar el control del mantenimiento de vehículos. El backend centraliza:

- autenticación de usuarios mediante JWT
- persistencia de usuarios y vehículos
- historial de mantenimientos por vehículo
- gestión de adjuntos como facturas, imágenes o documentos
- catálogo de marcas y modelos
- generación de URLs públicas para archivos almacenados

## Stack tecnológico

- `Java 17`
- `Spring Boot 3.5.10`
- `Spring Web`
- `Spring Data JPA`
- `Spring Validation`
- `MySQL 8+`
- `MinIO`
- `MapStruct`
- `Lombok`
- `JUnit 5`, `Mockito`, `H2`, `JaCoCo`

Las dependencias están definidas en [pom.xml](/Users/alvarogonzale/Documents/Proyectos/vehiclecare-micro/vehiclecare-micro/pom.xml:1).

## Arquitectura

El backend está organizado con una separación clara entre dominio, aplicación e infraestructura:

- `domain`
  - modelos de dominio
  - puertos de entrada (`port/in`)
  - puertos de salida (`port/out`)
- `application`
  - casos de uso
  - servicios de aplicación
  - DTOs de petición y respuesta
- `infrastructure`
  - controladores REST
  - persistencia JPA
  - mappers
  - seguridad JWT
  - cliente externo de catálogo

Esta estructura permite aislar la lógica de negocio de los detalles de framework, base de datos o almacenamiento externo.

## Funcionalidad principal

La API actualmente permite:

- registro de usuario
- login con token JWT
- consulta y edición de perfil
- subida de imagen de perfil
- alta, edición, consulta y borrado de vehículos
- subida de imagen de vehículo
- alta, edición, consulta y borrado de registros de mantenimiento
- subida, descarga y borrado de adjuntos
- consulta de catálogo de marcas y modelos
- sincronización manual del catálogo desde una fuente externa

## Endpoints principales

### Autenticación

- `POST /auth/register`
- `POST /auth/login`

### Perfil de usuario

- `GET /users/{id}`
- `PUT /users/{id}/profile`
- `POST /users/{id}/profile-image`

### Vehículos

- `GET /vehicles`
- `GET /vehicles/{id}`
- `POST /vehicles`
- `PUT /vehicles/{id}`
- `DELETE /vehicles/{id}`
- `POST /vehicles/{id}/image`

### Mantenimientos

- `GET /vehicles/{vehicleId}/records`
- `GET /records/{id}`
- `POST /vehicles/{vehicleId}/records`
- `PUT /records/{id}`
- `DELETE /records/{id}`
- `POST /records/{id}/attachments`
- `GET /records/{recordId}/attachments/{attachmentId}/download`
- `DELETE /records/{recordId}/attachments/{attachmentId}`

### Catálogo

- `GET /brands`
- `GET /brands/{id}/models`
- `POST /admin/catalog/sync`

### Ficheros

- `POST /files/upload-image`
- `GET /files/object`

La especificación OpenAPI está en [openapi/vehiclecare-swagger.yml](/Users/alvarogonzale/Documents/Proyectos/vehiclecare-micro/vehiclecare-micro/openapi/vehiclecare-swagger.yml).

## Seguridad

La autenticación se resuelve mediante JWT.

- Las rutas públicas son:
  - `/auth/**`
  - `/files/**`
- El resto de endpoints requieren cabecera `Authorization: Bearer <token>`
- El token se valida en el interceptor JWT y se asocia al usuario autenticado de la petición

Archivos relevantes:

- [JwtAuthenticationInterceptor.java](/Users/alvarogonzale/Documents/Proyectos/vehiclecare-micro/vehiclecare-micro/src/main/java/com/vehiclecare/vehiclecaremicro/infrastructure/security/JwtAuthenticationInterceptor.java:1)
- [AuthenticationContext.java](/Users/alvarogonzale/Documents/Proyectos/vehiclecare-micro/vehiclecare-micro/src/main/java/com/vehiclecare/vehiclecaremicro/infrastructure/security/AuthenticationContext.java:1)
- [JwtService.java](/Users/alvarogonzale/Documents/Proyectos/vehiclecare-micro/vehiclecare-micro/src/main/java/com/vehiclecare/vehiclecaremicro/application/service/JwtService.java:1)

## Base de datos

La base de datos principal es `MySQL`.

### Modelo relacional

El esquema está documentado en [sql/schema.sql](/Users/alvarogonzale/Documents/Proyectos/vehiclecare-micro/vehiclecare-micro/sql/schema.sql:1) y se organiza en:

- `users`
- `vehicles`
- `maintenance_records`
- `attachments`
- `brands`
- `models`

Relaciones principales:

- un usuario tiene varios vehículos
- un vehículo tiene varios mantenimientos
- un mantenimiento puede tener varios adjuntos
- una marca tiene varios modelos

### Configuración JPA

En `main` se usa:

- `spring.jpa.hibernate.ddl-auto=validate`

Eso significa que Hibernate valida que la estructura de base de datos coincida con las entidades, pero no crea ni altera tablas automáticamente en producción/local principal.

## Almacenamiento de archivos

Las imágenes y documentos se almacenan en `MinIO`.

Se utiliza para:

- imagen de perfil
- imagen de vehículo
- adjuntos de mantenimiento

Archivo principal:

- [MinioStorageService.java](/Users/alvarogonzale/Documents/Proyectos/vehiclecare-micro/vehiclecare-micro/src/main/java/com/vehiclecare/vehiclecaremicro/application/service/MinioStorageService.java:1)

También existe un servicio que construye URLs públicas a partir de las claves de objeto:

- [PublicFileUrlService.java](/Users/alvarogonzale/Documents/Proyectos/vehiclecare-micro/vehiclecare-micro/src/main/java/com/vehiclecare/vehiclecaremicro/application/service/PublicFileUrlService.java:1)

## Catálogo de marcas y modelos

El backend mantiene un catálogo local en las tablas `brands` y `models`.

El flujo es:

- el frontend consulta `/brands` y `/brands/{id}/models`
- el backend responde desde su base de datos local
- opcionalmente, el catálogo puede sincronizarse con una fuente externa

Archivos clave:

- [BrandController.java](/Users/alvarogonzale/Documents/Proyectos/vehiclecare-micro/vehiclecare-micro/src/main/java/com/vehiclecare/vehiclecaremicro/infrastructure/rest/controller/BrandController.java:1)
- [CatalogQueryService.java](/Users/alvarogonzale/Documents/Proyectos/vehiclecare-micro/vehiclecare-micro/src/main/java/com/vehiclecare/vehiclecaremicro/application/service/CatalogQueryService.java:1)
- [CatalogSyncService.java](/Users/alvarogonzale/Documents/Proyectos/vehiclecare-micro/vehiclecare-micro/src/main/java/com/vehiclecare/vehiclecaremicro/application/service/CatalogSyncService.java:1)
- [VpicClient.java](/Users/alvarogonzale/Documents/Proyectos/vehiclecare-micro/vehiclecare-micro/src/main/java/com/vehiclecare/vehiclecaremicro/infrastructure/external/vpic/VpicClient.java:1)

Por defecto, la sincronización automática está desactivada:

```properties
catalog.sync.enabled=false
```

## Configuración local

La configuración sensible no debe guardarse en el repositorio.

Usa:

- [src/main/resources/application-local.example.properties](/Users/alvarogonzale/Documents/Proyectos/vehiclecare-micro/vehiclecare-micro/src/main/resources/application-local.example.properties)

Pasos recomendados:

1. Crear `src/main/resources/application-local.properties`
2. Copiar el contenido del ejemplo
3. Ajustar credenciales y endpoints locales
4. Arrancar la aplicación con perfil `local`

Valores que deben definirse ahí:

- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`
- `minio.endpoint`
- `minio.access-key`
- `minio.secret-key`
- `security.jwt.secret`

## Ejemplo de configuración local

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/vehiclecare?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=vehiclecare
spring.datasource.password=change-me

minio.endpoint=http://localhost:9000
minio.access-key=minioadmin
minio.secret-key=minioadmin
minio.bucket=vehiclecare

security.jwt.secret=change-this-jwt-secret
```

## Puesta en marcha

### Requisitos

- `Java 17`
- acceso a `MySQL`
- acceso a `MinIO`

### Arranque con Maven Wrapper

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### Empaquetado y ejecución

```bash
./mvnw clean package
java -jar target/vehiclecare-micro-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

## Ejemplo rápido de MySQL

```sql
CREATE DATABASE vehiclecare;
CREATE USER 'vehiclecare'@'localhost' IDENTIFIED BY 'change-me';
GRANT ALL PRIVILEGES ON vehiclecare.* TO 'vehiclecare'@'localhost';
FLUSH PRIVILEGES;
```

## Ejemplo rápido de MinIO con Docker

```bash
docker run -p 9000:9000 -p 9001:9001 \
  -e MINIO_ROOT_USER=minioadmin \
  -e MINIO_ROOT_PASSWORD=minioadmin \
  quay.io/minio/minio server /data --console-address ":9001"
```

Consola:

- [http://localhost:9001](http://localhost:9001)

## Tests

El proyecto incluye tests unitarios y de capa web.

Ejecutar tests:

```bash
./mvnw test
```

Generar build y cobertura:

```bash
./mvnw clean test
```

En tests se usa `H2` en memoria con configuración propia en:

- [src/test/resources/application.properties](/Users/alvarogonzale/Documents/Proyectos/vehiclecare-micro/vehiclecare-micro/src/test/resources/application.properties)

Eso permite ejecutar la suite sin depender de un MySQL o MinIO reales.

## Convenciones y comportamiento

- La validación de entrada se realiza tanto con anotaciones como con servicios de validación de negocio.
- Los errores de API se devuelven en un formato uniforme con `timestamp`, `status`, `error`, `path` y `details`.
- El backend aplica comprobaciones de ownership para impedir que un usuario acceda a recursos de otro.
- La fuente de verdad siempre es la base de datos; el frontend refresca contra esta API para recuperar cambios desde otros dispositivos.

## Archivos útiles

- [sql/schema.sql](/Users/alvarogonzale/Documents/Proyectos/vehiclecare-micro/vehiclecare-micro/sql/schema.sql:1)
- [openapi/vehiclecare-swagger.yml](/Users/alvarogonzale/Documents/Proyectos/vehiclecare-micro/vehiclecare-micro/openapi/vehiclecare-swagger.yml)
- [src/main/resources/application.properties](/Users/alvarogonzale/Documents/Proyectos/vehiclecare-micro/vehiclecare-micro/src/main/resources/application.properties:1)
- [src/main/resources/application-local.example.properties](/Users/alvarogonzale/Documents/Proyectos/vehiclecare-micro/vehiclecare-micro/src/main/resources/application-local.example.properties)

## Seguridad del repositorio

Antes de publicar o compartir el proyecto:

- no subir `application-local.properties`
- no subir secretos JWT reales
- no subir credenciales de MySQL o MinIO
- no subir certificados, `.env` ni claves privadas

El repositorio está preparado para mantenerse sin credenciales hardcodeadas en la configuración principal.
