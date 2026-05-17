# VehicleCare Backend

Backend de `VehicleCare` desarrollado con `Spring Boot 3`, `Java 17`, `MySQL` y `MinIO`.

## Qué incluye

- API REST para usuarios, autenticación, vehículos, mantenimientos y adjuntos.
- Persistencia con `Spring Data JPA`.
- Almacenamiento de archivos en `MinIO`.
- Tests con `JUnit 5`, `Mockito` y `JaCoCo`.
- Documentación OpenAPI en [openapi/vehiclecare-swagger.yml](/Users/alvarogonzale/Documents/Proyectos/vehiclecare-micro/vehiclecare-micro/openapi/vehiclecare-swagger.yml).

## Requisitos

- `Java 17`
- `Maven` o `./mvnw`
- `MySQL 8+`
- `MinIO`

## Configuración local

El repositorio no incluye credenciales reales.

La configuración sensible se carga solo con el perfil `local` desde:

- `src/main/resources/application-local.properties`

Hay una plantilla segura en:

- [src/main/resources/application-local.example.properties](/Users/alvarogonzale/Documents/Proyectos/vehiclecare-micro/vehiclecare-micro/src/main/resources/application-local.example.properties)

Pasos:

1. Copia `src/main/resources/application-local.example.properties` a `src/main/resources/application-local.properties`.
2. Ajusta usuario, contraseña, endpoint y secreto JWT con tus valores locales.
3. Arranca la aplicación con el perfil `local`.

## MySQL

Debes tener una base de datos accesible desde el backend. Ejemplo rápido con MySQL local:

```sql
CREATE DATABASE vehiclecare;
CREATE USER 'vehiclecare'@'localhost' IDENTIFIED BY 'change-me';
GRANT ALL PRIVILEGES ON vehiclecare.* TO 'vehiclecare'@'localhost';
FLUSH PRIVILEGES;
```

Ejemplo de configuración en `application-local.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/vehiclecare?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=vehiclecare
spring.datasource.password=change-me
```

Notas:

- El proyecto usa `spring.jpa.hibernate.ddl-auto=validate` en `main`.
- La estructura de base de datos documentada está en [sql/schema.sql](/Users/alvarogonzale/Documents/Proyectos/vehiclecare-micro/vehiclecare-micro/sql/schema.sql).
- La carpeta [sql](/Users/alvarogonzale/Documents/Proyectos/vehiclecare-micro/vehiclecare-micro/sql) documenta el esquema actual, pero no sustituye un sistema de migraciones.

## MinIO

El backend usa MinIO para almacenar imágenes y adjuntos.

Ejemplo rápido con Docker:

```bash
docker run -p 9000:9000 -p 9001:9001 \
  -e MINIO_ROOT_USER=minioadmin \
  -e MINIO_ROOT_PASSWORD=minioadmin \
  quay.io/minio/minio server /data --console-address ":9001"
```

Configuración local esperada:

```properties
minio.endpoint=http://localhost:9000
minio.access-key=minioadmin
minio.secret-key=minioadmin
minio.bucket=vehiclecare
```

Notas:

- La consola web de MinIO suele quedar en `http://localhost:9001`.
- `minio.initialize-bucket=true` hace que la aplicación intente crear el bucket si no existe.

## Arranque

Con `Maven Wrapper`:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Alternativa compilando primero:

```bash
./mvnw clean package
java -jar target/vehiclecare-micro-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

## Tests

Ejecutar tests:

```bash
./mvnw test
```

Generar cobertura:

```bash
./mvnw clean test
```

Los tests usan `H2` en memoria y configuración propia en:

- [src/test/resources/application.properties](/Users/alvarogonzale/Documents/Proyectos/vehiclecare-micro/vehiclecare-micro/src/test/resources/application.properties)

No necesitan MySQL real ni MinIO real para pasar.

## Seguridad del repositorio

Antes de subir a GitHub:

- No subas `src/main/resources/application-local.properties`.
- No subas `.env`, claves privadas ni certificados locales.
- No reutilices `security.jwt.secret` de ejemplo en entornos reales.
- Revisa cualquier cambio en `.idea` o ficheros de entorno antes de hacer commit.

Actualmente el repositorio está preparado para publicarse sin credenciales hardcodeadas en la configuración principal.
