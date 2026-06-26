# TASKHUB — Especificación Técnica

**Versión:** 1.0  
**Autor:** Keanu  
**Estado:** Draft  
**Fecha:** 2026-06-16

---

## 1. Contexto y motivación

TaskHub es un sistema de gestión de tareas colaborativo construido íntegramente en Java. El propósito es doble: cubrir progresivamente todos los bloques Java del ciclo DAM (PROG, Acceso a Datos, Servicios y Procesos) y producir un proyecto de portfolio con arquitectura realista que refleje prácticas de empresa.

El proyecto escala de forma deliberada: cada fase introduce exactamente un bloque nuevo del currículo sobre el código ya existente, sin reescribir lo anterior.

---

## 2. Objetivos

| Objetivo | Descripción |
|---|---|
| Cobertura curricular | Cubrir POO, colecciones, excepciones, ficheros, JDBC, Hibernate, hilos, sockets y REST |
| Portfolio | Código publicable en GitHub con README profesional |
| Empleabilidad | Stack final (Spring Boot + JPA) demandado en ofertas Junior España |
| Progresión limpia | Cada fase compila y funciona de forma independiente |

**Fuera de alcance:**
- JavaFX / Swing (GUI de escritorio)
- XML / bases de datos XML
- Android
- Despliegue en producción

---

## 3. Dominio del problema

### Entidades principales

```
User
├── id: UUID
├── username: String (único)
├── email: String
├── passwordHash: String
├── role: Role (ADMIN | MEMBER)
└── createdAt: LocalDateTime

Project
├── id: UUID
├── name: String
├── description: String
├── owner: User
├── members: List<User>
├── tasks: List<Task>
└── createdAt: LocalDateTime

Task
├── id: UUID
├── title: String
├── description: String
├── status: Status (TODO | IN_PROGRESS | DONE | CANCELLED)
├── priority: Priority (LOW | MEDIUM | HIGH | CRITICAL)
├── assignee: User (nullable)
├── project: Project
├── dueDate: LocalDate (nullable)
└── createdAt: LocalDateTime

Comment
├── id: UUID
├── content: String
├── author: User
├── task: Task
└── createdAt: LocalDateTime
```

### Relaciones

```
User      ──< Project   (un usuario puede tener muchos proyectos como owner)
Project   >──< User     (muchos a muchos: miembros del proyecto)
Project   ──< Task      (un proyecto tiene muchas tareas)
User      ──< Task      (un usuario puede tener muchas tareas asignadas)
Task      ──< Comment   (una tarea tiene muchos comentarios)
```

---

## 4. Tech stack por fase

| Fase | Herramientas |
|---|---|
| 1 — Core Java | Java 21, Maven, JUnit 5 |
| 2 — Ficheros | Gson (JSON), Java IO/NIO |
| 3 — JDBC | H2 (embebida), HikariCP |
| 4 — ORM | Hibernate 6, JPA |
| 5 — Red | Java Sockets (TCP), hilos (`ExecutorService`) |
| 6 — REST | Spring Boot 3, Spring Data JPA, Spring Security + JWT |

**Gestor de proyecto:** Maven  
**Java versión:** 21 LTS  
**Tests:** JUnit 5 + AssertJ  
**Regla:** `mvn test` debe pasar en verde al finalizar cada fase

---

## 5. Arquitectura general

```
┌─────────────────────────────────────────────────────┐
│                   CLIENT (CLI / REST)                │
└───────────────────────┬─────────────────────────────┘
                        │ Fase 5: TCP Socket / Fase 6: HTTP
┌───────────────────────▼─────────────────────────────┐
│                    APPLICATION LAYER                 │
│  TaskService  │  ProjectService  │  UserService      │
└───────────────────────┬─────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────┐
│                   REPOSITORY LAYER                   │
│  Fase 2: FileRepository (JSON)                       │
│  Fase 3: JdbcRepository                              │
│  Fase 4: JpaRepository (Hibernate)                   │
│  Fase 6: Spring Data JpaRepository                   │
└───────────────────────┬─────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────┐
│                    STORAGE                           │
│  Fase 2: .json files  │  Fase 3-6: H2 / MySQL        │
└─────────────────────────────────────────────────────┘
```

El `Repository` es una interfaz Java. Cada fase cambia la implementación concreta sin tocar la capa de servicio. Este es el principio que hace el proyecto escalable: el servicio no sabe si los datos vienen de un fichero, de JDBC o de Hibernate.

---

## 6. Fases y entregables

### Fase 1 — Core Java `[PROG]`

**Conceptos DAM cubiertos:** POO, herencia, interfaces, genéricos, colecciones, excepciones, enums

**Entregables:**
- Todas las entidades del dominio implementadas
- Interfaz `Repository<T, ID>` genérica
- Implementación `InMemoryRepository<T>`
- Servicios: `UserService`, `ProjectService`, `TaskService`
- Excepciones: `EntityNotFoundException`, `DuplicateEntityException`, `ValidationException`
- Tests unitarios para todos los servicios (cobertura mínima 80%)

**Criterios de aceptación:**
- [ ] Se puede crear un usuario, proyecto y tarea en memoria
- [ ] No se puede crear un usuario con email duplicado (`DuplicateEntityException`)
- [ ] No se puede asignar una tarea a un usuario que no es miembro del proyecto
- [ ] `mvn test` pasa sin errores

---

### Fase 2 — Persistencia en ficheros `[Acceso a Datos - Ficheros]`

**Conceptos DAM cubiertos:** Java IO/NIO, serialización, JSON con Gson

**Entregables:**
- `JsonRepository<T>` implementando `Repository<T, ID>`
- Persistencia automática en `data/users.json`, `data/projects.json`, `data/tasks.json`
- `DataLoader` para cargar datos al arrancar
- Manejo correcto de fichero no existente (primer arranque)

**Criterios de aceptación:**
- [ ] Los datos sobreviven a reinicios de la aplicación
- [ ] El JSON es legible y tiene formato indentado
- [ ] Si `data/` no existe se crea automáticamente
- [ ] Los tests de Fase 1 siguen pasando

---

### Fase 3 — Base de datos relacional `[Acceso a Datos - JDBC]`

**Conceptos DAM cubiertos:** JDBC, SQL, transacciones, connection pooling

**Entregables:**
- `JdbcUserRepository`, `JdbcProjectRepository`, `JdbcTaskRepository`
- `DatabaseConfig` con HikariCP
- `schema.sql` con el DDL completo
- `DatabaseMigration` para crear tablas en primer arranque
- Tests de integración con H2 en modo in-memory

**Criterios de aceptación:**
- [ ] CRUD completo sobre base de datos real
- [ ] Las relaciones (project → tasks, task → assignee) se persisten con FKs
- [ ] Una operación fallida hace rollback completo
- [ ] Los servicios de Fase 1 no han cambiado nada

---

### Fase 4 — ORM `[Acceso a Datos - Hibernate/JPA]`

**Conceptos DAM cubiertos:** Hibernate, anotaciones JPA, relaciones, lazy/eager loading

**Entregables:**
- Entidades anotadas con `@Entity`, `@Table`, `@OneToMany`, `@ManyToMany`
- `JpaUserRepository`, `JpaProjectRepository`, `JpaTaskRepository`
- Configuración de Hibernate programática
- Eliminación del JDBC manual

**Criterios de aceptación:**
- [ ] Misma funcionalidad que Fase 3 pero con Hibernate
- [ ] Las relaciones se cargan correctamente (sin N+1 en listas)
- [ ] Tests de integración siguen en verde

---

### Fase 5 — Red y concurrencia `[Programación de Servicios y Procesos]`

**Conceptos DAM cubiertos:** Sockets TCP, hilos, `ExecutorService`, sincronización

**Entregables:**
- `TaskHubServer`: servidor TCP que acepta múltiples clientes concurrentes
- `TaskHubClient`: cliente CLI que se conecta por socket
- Protocolo de comunicación propio: JSON sobre TCP con delimitador `\n`
- Pool de hilos con `Executors.newCachedThreadPool()`
- Acceso sincronizado al repositorio compartido

**Protocolo (ejemplo):**
```json
// Request
{"action": "CREATE_TASK", "payload": {"title": "Fix bug", "projectId": "abc-123"}}

// Response
{"status": "OK", "data": {"id": "xyz-789", "title": "Fix bug", "status": "TODO"}}
```

**Criterios de aceptación:**
- [ ] El servidor acepta 10 clientes simultáneos sin race conditions
- [ ] Una desconexión abrupta de un cliente no tumba el servidor
- [ ] Todas las operaciones CRUD funcionan vía socket

---

### Fase 6 — REST API con Spring Boot `[Empleabilidad]`

**Conceptos nuevos:** Spring Boot 3, Spring Data JPA, Spring Security, JWT  
**Nota:** Esta es la única fase que usa un framework externo. Las fases 1-5 son Java puro.

**Entregables:**
- Controllers REST: `/api/users`, `/api/projects`, `/api/tasks`, `/api/comments`
- Autenticación JWT (registro + login)
- Validación de request bodies con Bean Validation (`@Valid`)
- DTOs separados de las entidades JPA
- Archivo `.http` para probar todos los endpoints

**Criterios de aceptación:**
- [ ] `POST /api/auth/register` y `POST /api/auth/login` funcionan
- [ ] Endpoints protegidos devuelven 401 sin token válido
- [ ] Paginación en listados (`?page=0&size=20`)
- [ ] Errores devuelven JSON con `status`, `message`, `timestamp`

---

## 7. Estructura de directorios (desde Fase 1)

```
taskhub/
├── pom.xml
├── src/
│   ├── main/
│   │   └── java/com/taskhub/
│   │       ├── domain/          ← Entidades del dominio
│   │       ├── repository/      ← Interfaz + implementaciones
│   │       ├── service/         ← Lógica de negocio
│   │       ├── exception/       ← Excepciones personalizadas
│   │       ├── server/          ← Fase 5: socket server
│   │       └── Main.java
│   └── test/
│       └── java/com/taskhub/
│           ├── service/         ← Tests unitarios
│           └── repository/      ← Tests de integración
└── data/                        ← Fase 2: ficheros JSON
```

---

## 8. Convenciones de código

- Nombres en inglés
- Un archivo por clase pública
- Interfaces sin prefijo `I` — `Repository` no `IRepository`
- Excepciones checked solo en límites del sistema (IO, red, DB)
- Sin comentarios en código obvio — solo si el WHY no es evidente
- Commits en inglés, estilo convencional: `feat:`, `fix:`, `chore:`, `docs:`

---

## 9. Glosario

| Término | Definición |
|---|---|
| Repository | Interfaz que abstrae el mecanismo de almacenamiento |
| DAO | Data Access Object — implementación concreta de acceso a datos |
| DTO | Data Transfer Object — objeto plano para transferir datos entre capas |
| ORM | Object-Relational Mapping — mapeo de objetos Java a tablas SQL |
| Pool | Conjunto de conexiones reutilizables a base de datos |
| JWT | JSON Web Token — token firmado para autenticación stateless |
| HikariCP | Librería de connection pooling para JDBC, la más rápida disponible |
