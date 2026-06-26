# TaskHub — Especificación Técnica

**Versión:** 2.0
**Autor:** Keanu
**Estado:** Draft
**Fecha:** 2026-06-16

> Cambios respecto a v1.0: la CLI interactiva pasa a ser la pieza central de la aplicación, no un añadido. Se documenta la arquitectura por capas y el patrón Command.

---

## 1. Contexto y motivación

TaskHub es un sistema de gestión de tareas colaborativo construido íntegramente en Java, con una **terminal interactiva propia** como interfaz principal.

El propósito es doble:

- **Aprender** — cubrir progresivamente todos los bloques Java del ciclo DAM (PROG, Acceso a Datos, Servicios y Procesos) y dar el salto a Spring Boot.
- **Portfolio** — producir una aplicación real, con arquitectura de empresa, que demuestre criterio técnico.

El proyecto escala de forma deliberada: **cada fase introduce exactamente un bloque nuevo del currículo sobre el código ya existente, sin reescribir lo anterior.** La clave que lo hace posible es la separación en capas (ver `ARQUITECTURA.md`): la terminal y la lógica de negocio no cambian cuando cambiamos cómo se guardan los datos.

---

## 2. Objetivos

| Objetivo | Descripción |
|---|---|
| Cobertura curricular | POO, colecciones, excepciones, ficheros, JDBC, Hibernate, hilos, sockets, REST |
| Interactividad | Terminal propia con comandos, formularios paso a paso y salida formateada |
| Escalabilidad | Cambiar la capa de persistencia sin tocar negocio ni interfaz |
| Empleabilidad | Stack final (Spring Boot + JPA) demandado en ofertas Junior España |
| Portfolio | Código limpio, testeado y publicable en GitHub |

---

## 3. Fuera de alcance

- JavaFX / Swing (interfaz gráfica de escritorio)
- XML / bases de datos XML
- Android
- Despliegue en producción

**Nota sobre la "transparencia" de la terminal:** la opacidad real de la ventana la controla el emulador de terminal del sistema operativo (Windows Terminal, PowerShell, etc.), no la aplicación que corre dentro. Lo que **sí** controlamos desde la app son los **temas de color** (esquemas dark/light/matrix/solarized), el prompt y el formato de salida. La personalización visual se documenta en `CLI.md` como comando `theme`.

---

## 4. Dominio del problema

### Entidades

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
User      ──< Project   (un usuario puede ser owner de muchos proyectos)
Project   >──< User     (muchos a muchos: miembros del proyecto)
Project   ──< Task      (un proyecto tiene muchas tareas)
User      ──< Task      (un usuario puede tener muchas tareas asignadas)
Task      ──< Comment   (una tarea tiene muchos comentarios)
```

### Reglas de dominio

- El `username` y el `email` de un `User` son únicos.
- El `owner` de un `Project` es automáticamente miembro.
- Una `Task` solo puede asignarse a un `User` que sea miembro del proyecto.
- No se puede borrar un `User` que es owner de algún proyecto.

---

## 5. Stack por fase

| Fase | Tema DAM | Herramientas que se añaden |
|---|---|---|
| 1 | PROG (POO, colecciones) | Java 25, Maven, JLine3, JUnit 5 |
| 2 | Acceso a Datos — Ficheros | Gson (JSON), Java NIO |
| 3 | Acceso a Datos — JDBC | H2, HikariCP |
| 4 | Acceso a Datos — ORM | Hibernate 6, JPA |
| 5 | Servicios y Procesos | Sockets TCP, hilos (`ExecutorService`) |
| 6 | Empleabilidad | Spring Boot 3, Spring Data JPA, Spring Security, JWT |

**Regla de oro:** al terminar cada fase, `mvn test` debe pasar en verde y la app debe arrancar.

---

## 6. Roadmap

### Fase 1 — Core Java + CLI `[PROG]`
POO, herencia, interfaces, genéricos, colecciones, excepciones, enums + la terminal interactiva.

- Entidades del dominio
- Interfaz `Repository<T, ID>` + implementación en memoria
- Servicios (`UserService`, `ProjectService`, `TaskService`)
- Excepciones de dominio
- **CLI interactiva** con comandos, formularios y temas
- Tests unitarios de los servicios

**Hecho cuando:** puedes crear usuarios/proyectos/tareas desde la terminal, listarlos, y `mvn test` pasa.

### Fase 2 — Ficheros `[Acceso a Datos]`
Persistencia en JSON. Los datos sobreviven a reinicios.

- `JsonRepository<T>` que implementa la misma interfaz `Repository`
- Carga al arrancar, guardado al modificar
- La CLI y los servicios **no cambian**

### Fase 3 — JDBC `[Acceso a Datos]`
Base de datos relacional con JDBC puro.

**Estado:** Implementada

**Archivos creados:**
- `pom.xml` — añadidas dependencias H2 2.4.240 y HikariCP 7.0.2
- `src/main/resources/schema.sql` — tablas `users`, `projects`, `tasks`, `project_members` con claves primarias, foráneas y restricciones NOT NULL / UNIQUE
- `src/main/java/org/example/Config/DatabaseConfig.java` — crea el pool HikariCP y ejecuta `schema.sql` al arrancar
- `src/main/java/org/example/repository/jdbc/JdbcRepository.java` — clase base abstracta que guarda el `DataSource`
- `src/main/java/org/example/repository/jdbc/UserJdbcRepository.java` — CRUD completo + `findByEmail`; usa `MERGE INTO` para upsert; `mapRow` convierte `ResultSet` → `User`
- `src/main/java/org/example/repository/jdbc/ProjectJdbcRepository.java` — CRUD + gestión de `project_members` (delete + batch insert en cada save)
- `src/main/java/org/example/repository/jdbc/TaskJdbcRepository.java` — CRUD; carga `assignee` (nullable) y `project` desde sus repositorios
- `src/main/java/org/example/Main.java` — usa `DatabaseConfig.creaDataSource()` y los repositorios JDBC

**Decisiones técnicas:**
- `MERGE INTO ... KEY(id)` en H2 hace upsert (insert si no existe, update si existe) — evita duplicados sin comprobar manualmente
- `try-with-resources` en todas las conexiones para cerrar automáticamente `Connection`, `PreparedStatement` y `ResultSet`
- Los enums (`Role`, `Status`, `Priority`) se guardan como `VARCHAR` y se reconstruyen con `Enum.valueOf()`
- `assignee_id` nullable en `tasks` — se guarda como `null` si la tarea no tiene asignado
- Los servicios no cambian — siguen dependiendo de `Repository<T, ID>`

**Pendiente:**
- Tests de integración con H2 en memoria

### Fase 4 — ORM `[Acceso a Datos]`
Reemplazar JDBC por Hibernate/JPA.

- Entidades anotadas, relaciones, repositorios JPA

### Fase 5 — Red y concurrencia `[Servicios y Procesos]`
Separar cliente y servidor.

- Servidor TCP multihilo, protocolo JSON sobre socket
- La CLI se convierte en cliente que habla con el servidor

### Fase 6 — REST API `[Spring Boot]`
La única fase con framework.

- Controllers REST, Spring Data JPA, validación, seguridad con JWT
- (Opcional) migrar la CLI a Spring Shell

---

## 7. Convenciones de código

- Código y nombres en **inglés**; documentación en **español**.
- Un fichero por clase pública.
- Interfaces sin prefijo `I` — `Repository`, no `IRepository`.
- Excepciones checked solo en los límites del sistema (IO, red, DB).
- Sin comentarios en código obvio — solo si el *porqué* no es evidente.
- Commits en inglés, estilo convencional: `feat:`, `fix:`, `chore:`, `docs:`, `test:`.

---

## 8. Glosario

| Término | Definición |
|---|---|
| REPL | Read-Eval-Print Loop — bucle que lee un comando, lo ejecuta e imprime el resultado |
| CLI | Command-Line Interface — interfaz por línea de comandos |
| Command (patrón) | Cada acción se encapsula en su propia clase con un método `execute` |
| Repository | Interfaz que abstrae el mecanismo de almacenamiento |
| DAO | Data Access Object — implementación concreta de acceso a datos |
| DTO | Data Transfer Object — objeto plano para transferir datos entre capas |
| ORM | Object-Relational Mapping — mapeo de objetos Java a tablas SQL |
| JLine3 | Librería Java para construir terminales interactivas (la usa Spring Shell) |
| JWT | JSON Web Token — token firmado para autenticación stateless |
