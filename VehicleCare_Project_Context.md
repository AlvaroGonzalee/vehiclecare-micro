# VehicleCare – Contexto del Proyecto (Arquitectura Hexagonal)

## 1. Visión general

VehicleCare es una aplicación backend cuyo objetivo es gestionar información de vehículos y sus mantenimientos asociados a usuarios.

El dominio del problema es sencillo:

- Existen **usuarios**.
- Cada usuario puede tener **uno o varios vehículos**.
- Cada vehículo puede tener **registros de mantenimiento**.
- Cada registro de mantenimiento puede tener **archivos adjuntos** (facturas, fotos, informes).

La aplicación está pensada como base para una app móvil o web, pero en este momento el foco está en:
- Tener un **modelo de dominio claro**.
- Una **arquitectura limpia (hexagonal)**.
- Separación estricta de responsabilidades.
- Uso de buenas prácticas (DTOs, mappers, casos de uso).

---

## 2. Decisiones técnicas

- **Lenguaje**: Java 17  
  - El código se escribe con mentalidad **Java 11** (sin records, sin features modernas).
- **Framework**: Spring Boot
- **Persistencia**: JPA / Hibernate
- **Base de datos**: MySQL
- **Mappers**: MapStruct
- **Boilerplate**: Lombok

---

## 3. Arquitectura elegida: Hexagonal (Ports & Adapters)

Se utiliza una arquitectura hexagonal **simple**, sin sobre‑ingeniería, pero respetando los conceptos clave:

- El **dominio** no depende de nada.
- La **aplicación** orquesta casos de uso.
- La **infraestructura** implementa detalles técnicos (web, base de datos, mappers, etc.).

No se describen endpoints REST aquí a propósito:  
este documento se centra en **estructura y dominio**, no en HTTP.

---

## 4. Estructura de paquetes

```
com.vehiclecare.vehiclecaremicro
│
├── domain
│   ├── model
│   │   ├── User
│   │   ├── Vehicle
│   │   ├── MaintenanceRecord
│   │   └── Attachment
│   │
│   └── port
│       ├── in
│       │   ├── CreateUserUseCase
│       │   ├── CreateVehicleUseCase
│       │   └── AddMaintenanceRecordUseCase
│       │
│       └── out
│           ├── UserRepositoryPort
│           ├── VehicleRepositoryPort
│           └── MaintenanceRepositoryPort
│
├── application
│   ├── usecase
│   │   ├── CreateUserService
│   │   ├── CreateVehicleService
│   │   └── AddMaintenanceRecordService
│   │
│   └── dto
│       ├── request
│       └── response
│
├── infrastructure
│   ├── rest
│   │   └── controller
│   │
│   ├── persistence
│   │   ├── entity
│   │   ├── repository
│   │   └── adapter
│   │
│   └── mapper
│       └── MapStruct mappers
│
└── config
```

---

## 5. Dominio (Domain)

### 5.1. Modelos de dominio

Los modelos de dominio representan el **negocio**, no la base de datos ni HTTP.

Ejemplos:
- `User`
- `Vehicle`
- `MaintenanceRecord`
- `Attachment`

Características:
- No contienen anotaciones de Spring.
- No dependen de JPA ni de DTOs.
- Contienen solo estado y lógica de dominio (si la hay).

---

## 6. Puertos (Ports)

### 6.1. Puertos de entrada (in)

Representan **qué se puede hacer** en el sistema.

Ejemplos:
- Crear un usuario.
- Crear un vehículo para un usuario.
- Añadir un mantenimiento a un vehículo.

Se definen como **interfaces** en el dominio.

---

### 6.2. Puertos de salida (out)

Representan **qué necesita el dominio del exterior**.

Ejemplos:
- Guardar un usuario.
- Obtener vehículos de un usuario.
- Persistir un mantenimiento.

El dominio no sabe **cómo** se hace, solo **qué** necesita.

---

## 7. Aplicación (Application)

La capa de aplicación implementa los **casos de uso**.

Responsabilidades:
- Orquestar el flujo del caso de uso.
- Validar reglas de negocio simples.
- Llamar a los puertos de salida.

Ejemplo mental:
1. Validar que el usuario existe.
2. Crear el vehículo.
3. Persistirlo usando un puerto.

No conoce:
- HTTP
- JPA
- JSON

---

## 8. Infraestructura (Infrastructure)

Aquí viven los detalles técnicos.

### 8.1. Persistencia

- Entidades JPA (`@Entity`)
- Repositorios Spring Data
- Adaptadores que implementan los puertos de salida

### 8.2. REST

- Controllers
- Traducción HTTP → DTOs → casos de uso
- Traducción de respuestas a DTOs

### 8.3. Mappers

- MapStruct para:
  - DTO → dominio
  - dominio → DTO
  - dominio → entidad JPA y viceversa si es necesario

---

## 9. DTOs

Los DTOs viven fuera del dominio.

Tipos:
- **Request DTOs**: datos de entrada.
- **Response DTOs**: datos de salida.

Nunca se exponen entidades ni modelos de dominio directamente.

---

## 10. Reglas importantes del proyecto

- El dominio **no depende** de Spring.
- Los casos de uso **no dependen** de infraestructura.
- MapStruct se usa siempre para mapear.
- Lombok solo para reducir boilerplate, no para ocultar lógica.
- El proyecto debe poder entenderse leyendo solo:
  - El dominio
  - Los casos de uso
  - Controller

A las clases que implementen interfaces usar la nomenglatura de paquete +impl.

por ejemplo `vehicletalUseCaseImpl`

Las clase que sean objetos llamarlas con la nomenglatura paquete +DTO
por ejemplo `vehicleResponseDTO`

---

## 11. Objetivo de este archivo

Este archivo existe para:

- Dar **contexto completo** del proyecto a otra persona o IA.
- Evitar volver a explicar la arquitectura desde cero.
- Servir como contrato mental del proyecto.

Cualquier cambio importante de arquitectura o dominio
debería reflejarse aquí.
