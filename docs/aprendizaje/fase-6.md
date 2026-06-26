# Fase 6 — REST API con Spring Boot

**Tema DAM:** Empleabilidad — Spring Boot  
**Estado:** Pendiente  
**Objetivo real:** Exponer la lógica de negocio existente como una API REST con Spring Boot, añadir validación de entrada con Bean Validation, proteger los endpoints con autenticación JWT, y migrar la gestión de repositorios a Spring Data JPA. Las entidades del dominio y los servicios de fases anteriores se reutilizan sin modificaciones.

---

## Por qué esto importa

Las fases anteriores construyeron una arquitectura sólida en capas. La CLI demuestra que esa arquitectura funciona: la lógica de negocio vive en los servicios, independiente de cómo se accede a ella. Fase 6 demuestra exactamente eso desde otro ángulo: se añade una nueva capa de presentación —los controllers REST— sin tocar ninguna de las capas inferiores.

Desde el punto de vista del currículo DAM, esta fase cubre empleabilidad directa. Spring Boot es el framework más demandado en ofertas de trabajo Junior en España para el perfil Java. Entender cómo encaja en la arquitectura que ya se tiene es más valioso que aprenderlo de cero en un proyecto vacío.

Los conceptos nuevos que introduce esta fase:

- **Inversión de control (IoC) y contenedor de Spring:** en Fase 1, `Main.java` montaba todo a mano: creaba repositorios, los pasaba a servicios, los pasaba a comandos. Spring hace eso automáticamente mediante el contenedor de beans. Entenderás qué te ahorra porque ya lo has hecho a mano.
- **Inyección de dependencias gestionada:** `@Autowired`, `@Service`, `@Repository`, `@Component` — las anotaciones que le dicen a Spring qué instancias crear y cómo conectarlas.
- **Controllers REST:** clases que mapean rutas HTTP a métodos Java. Reciben peticiones, delegan en servicios, devuelven respuestas.
- **DTOs (Data Transfer Objects):** objetos planos que separan la representación pública de la API del dominio interno. Las entidades JPA no se exponen directamente.
- **Bean Validation:** validación declarativa con anotaciones (`@NotBlank`, `@Email`, `@Size`) en lugar de código imperativo dentro de los servicios.
- **Spring Security + JWT:** autenticación stateless. Cada petición lleva un token firmado; el servidor lo verifica sin mantener sesión.
- **Spring Data JPA:** los repositorios JPA que se escribieron a mano en Fase 4 son reemplazados por interfaces que Spring implementa automáticamente.

---

## Qué NO cambia

Este es el punto pedagógico central de la fase. La inversión de fases anteriores se reutiliza íntegra.

| Capa | Clases | Estado en Fase 6 |
|---|---|---|
| Dominio | `User`, `Project`, `Task`, `Comment`, `Role`, `Status`, `Priority` | Sin cambios. Las anotaciones JPA de Fase 4 son compatibles con Spring Boot. |
| Excepciones | `EntityNotFoundException`, `DuplicateEntityException`, `ValidationException` | Sin cambios. Se usan para devolver códigos HTTP apropiados. |
| Servicios | `UserService`, `ProjectService`, `TaskService` | Sin cambios en su lógica. Solo se añaden `@Service` para que Spring los gestione. |
| CLI | `TaskHubShell`, comandos, `Prompter`, `Output` | Sin cambios. Coexiste con la API REST (ver sección de arquitectura). |

Lo único que se añade son capas nuevas encima y al lado: controllers, DTOs, seguridad, y la configuración de Spring Boot.

---

## Arquitectura resultante

```
                       ┌─────────────────────────────────────────┐
                       │         CLIENTES EXTERNOS               │
                       │  curl / Postman / frontend / mobile     │
                       └───────────────┬─────────────────────────┘
                                       │ HTTP + JSON
                       ┌───────────────▼─────────────────────────┐
                       │         SPRING SECURITY FILTER          │
                       │  Valida el JWT antes de llegar al       │
                       │  controller. Rechaza si no es válido.   │
                       └───────────────┬─────────────────────────┘
                                       │
          ┌────────────────────────────▼────────────────────────────────┐
          │                   CAPA HTTP (controllers/)                  │
          │  UserController   ProjectController   TaskController        │
          │  AuthController                                             │
          │  Mapean rutas → métodos. Convierten DTOs ↔ dominio.        │
          └────────────────────────────┬────────────────────────────────┘
                                       │ llama a
          ┌────────────────────────────▼────────────────────────────────┐
          │                  CAPA SERVICIO (service/)                   │
          │  UserService    ProjectService    TaskService               │
          │  IGUAL que fases anteriores. Spring los gestiona como beans │
          └────────────────────────────┬────────────────────────────────┘
                                       │ usa
          ┌────────────────────────────▼────────────────────────────────┐
          │                  CAPA DOMINIO (domain/)                     │
          │  User   Project   Task   Comment   Role   Status   Priority │
          │  Sin cambios desde Fase 4                                    │
          └────────────────────────────┬────────────────────────────────┘
                                       │ se persiste mediante
          ┌────────────────────────────▼────────────────────────────────┐
          │            CAPA PERSISTENCIA (repository/)                  │
          │  UserRepository   ProjectRepository   TaskRepository        │
          │  Interfaces Spring Data JPA — Spring genera la implementación│
          └────────────────────────────┬────────────────────────────────┘
                                       │
          ┌────────────────────────────▼────────────────────────────────┐
          │                   H2 (archivo, mismo de Fase 5)             │
          └─────────────────────────────────────────────────────────────┘

  APARTE (proceso independiente, opcional):
  ┌─────────────────────────────────────────────────────────────────────┐
  │                  CLI INTERACTIVA (cli/)                             │
  │  TaskHubShell — sigue funcionando como en Fase 5.                  │
  │  Puede coexistir o migrarse a Spring Shell (opcional avanzado).    │
  └─────────────────────────────────────────────────────────────────────┘
```

La CLI y la API REST comparten la misma base de datos H2 en archivo. Si `AUTO_SERVER=TRUE` sigue activo, ambos procesos acceden simultáneamente sin conflicto.

---

## Nuevas clases y capas a crear

### 1. Punto de entrada Spring Boot

**`src/main/java/org/example/TaskHubApplication.java`**

La clase principal de Spring Boot. Lleva la anotación `@SpringBootApplication`, que es un atajo para `@Configuration`, `@EnableAutoConfiguration` y `@ComponentScan`. Al ejecutar su método `main`, Spring arrancar el servidor embebido Tomcat en el puerto 8080 y registra todos los beans.

`Main.java` existente no se elimina. Puede seguir funcionando para lanzar la CLI de forma independiente.

---

### 2. Configuración de la aplicación

**`src/main/resources/application.properties`**

Reemplaza `persistence.xml` para la configuración JPA bajo Spring. Define la URL de la base de datos H2, el dialecto Hibernate, la estrategia de actualización del schema (`ddl-auto`), el puerto del servidor, y las propiedades de la consola H2 para desarrollo.

Las propiedades relevantes a configurar:
- URL JDBC de H2 con `AUTO_SERVER=TRUE` (igual que Fase 5)
- `spring.jpa.hibernate.ddl-auto=update` para que Hibernate gestione el schema
- `spring.h2.console.enabled=true` para acceder a la consola H2 desde el navegador
- La clave secreta para firmar los JWT y su tiempo de expiración

---

### 3. Repositorios Spring Data JPA

**`src/main/java/org/example/repository/`**

Los repositorios JPA escritos a mano en Fase 4 son reemplazados por interfaces que extienden `JpaRepository<Entidad, UUID>`. Spring Data genera la implementación en tiempo de ejecución.

Clases a crear:
- `UserRepository` — extiende `JpaRepository<User, UUID>`. Añade `findByEmail(String email)` y `findByUsername(String username)` como firmas de método; Spring genera el SQL por el nombre del método.
- `ProjectRepository` — extiende `JpaRepository<Project, UUID>`.
- `TaskRepository` — extiende `JpaRepository<Task, UUID>`. Añade `findByProjectId(UUID projectId)` y `findByAssigneeId(UUID userId)`.

Los servicios ya usaban una interfaz `Repository<T, ID>`. La migración implica cambiar esa dependencia por el `JpaRepository` de Spring Data, que tiene los mismos métodos fundamentales (`save`, `findById`, `findAll`, `deleteById`) más los personalizados.

---

### 4. DTOs de request y response

**`src/main/java/org/example/dto/`**

Las entidades JPA nunca se exponen directamente en la API. Exponer entidades tiene problemas: ciclos de serialización (User tiene Project, Project tiene User), campos internos que no deberían salir (como `passwordHash`), y acoplamiento entre la API pública y el modelo interno.

Los DTOs son clases planas sin lógica que representan lo que entra y lo que sale.

DTOs de request (lo que llega en el cuerpo de la petición):
- `CreateUserRequest` — `username`, `email`, `password`, `role`
- `LoginRequest` — `email`, `password`
- `CreateProjectRequest` — `name`, `description`
- `CreateTaskRequest` — `title`, `description`, `priority`, `dueDate`, `assigneeId` (nullable)
- `UpdateTaskStatusRequest` — `status`

DTOs de response (lo que se devuelve):
- `UserResponse` — `id`, `username`, `email`, `role`, `createdAt` (sin `passwordHash`)
- `ProjectResponse` — `id`, `name`, `description`, `ownerUsername`, `memberCount`, `createdAt`
- `TaskResponse` — `id`, `title`, `description`, `status`, `priority`, `assigneeUsername` (nullable), `projectName`, `dueDate`, `createdAt`
- `AuthResponse` — `token`, `type` ("Bearer"), `username`, `role`

La conversión entre entidad y DTO se hace dentro del controller o en una clase `DtoMapper` separada.

---

### 5. Validación con Bean Validation

Los DTOs de request llevan anotaciones de validación. Spring ejecuta la validación automáticamente cuando el controller declara `@Valid` en el parámetro del cuerpo.

Anotaciones a usar en los DTOs:
- `@NotBlank` — campo obligatorio, no puede ser solo espacios
- `@Email` — formato de email válido
- `@Size(min, max)` — longitud mínima/máxima
- `@NotNull` — campo no puede ser null
- `@Pattern(regexp)` — expresión regular personalizada (útil para la contraseña)

Cuando la validación falla, Spring lanza `MethodArgumentNotValidException`. Para devolver un error legible en lugar de una respuesta genérica 400, se crea un manejador global de excepciones.

---

### 6. Controllers REST

**`src/main/java/org/example/controllers/`**

Los controllers reciben peticiones HTTP, delegan en servicios, y devuelven respuestas. Llevan `@RestController` (que combina `@Controller` y `@ResponseBody`) y `@RequestMapping` para definir la ruta base.

**`AuthController`** — ruta base `/api/auth`

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/auth/register` | Registra un nuevo usuario y devuelve el token JWT |
| `POST` | `/api/auth/login` | Autentica con email y contraseña y devuelve el token JWT |

**`UserController`** — ruta base `/api/users`

| Método | Ruta | Descripción | Acceso |
|---|---|---|---|
| `GET` | `/api/users` | Lista todos los usuarios | ADMIN |
| `GET` | `/api/users/{id}` | Detalle de un usuario | Autenticado |
| `DELETE` | `/api/users/{id}` | Elimina un usuario | ADMIN |

**`ProjectController`** — ruta base `/api/projects`

| Método | Ruta | Descripción | Acceso |
|---|---|---|---|
| `POST` | `/api/projects` | Crea un proyecto | Autenticado |
| `GET` | `/api/projects` | Lista proyectos del usuario autenticado | Autenticado |
| `GET` | `/api/projects/{id}` | Detalle de un proyecto | Miembro |
| `POST` | `/api/projects/{id}/members` | Añade un miembro | Owner |
| `DELETE` | `/api/projects/{id}` | Elimina un proyecto | Owner |

**`TaskController`** — ruta base `/api/projects/{projectId}/tasks`

| Método | Ruta | Descripción | Acceso |
|---|---|---|---|
| `POST` | `/api/projects/{projectId}/tasks` | Crea una tarea | Miembro |
| `GET` | `/api/projects/{projectId}/tasks` | Lista tareas del proyecto | Miembro |
| `GET` | `/api/projects/{projectId}/tasks/{id}` | Detalle de una tarea | Miembro |
| `PATCH` | `/api/projects/{projectId}/tasks/{id}/status` | Cambia el estado | Miembro |
| `PATCH` | `/api/projects/{projectId}/tasks/{id}/assignee` | Reasigna | Miembro |
| `DELETE` | `/api/projects/{projectId}/tasks/{id}` | Elimina una tarea | Owner |

---

### 7. Manejador global de excepciones

**`src/main/java/org/example/controllers/GlobalExceptionHandler.java`**

Lleva la anotación `@RestControllerAdvice`. Intercepta las excepciones de dominio que ya existían en fases anteriores y las convierte en respuestas HTTP apropiadas.

| Excepción | Código HTTP |
|---|---|
| `EntityNotFoundException` | 404 Not Found |
| `DuplicateEntityException` | 409 Conflict |
| `ValidationException` | 400 Bad Request |
| `MethodArgumentNotValidException` | 400 Bad Request (con detalle de campos) |
| `AccessDeniedException` | 403 Forbidden |

---

### 8. Seguridad con Spring Security + JWT

**`src/main/java/org/example/security/`**

La seguridad se implementa en tres piezas:

**`JwtService`** — servicio que genera y valida tokens JWT. Usa la librería `jjwt`. Expone métodos para generar un token a partir de un `UserDetails`, extraer el username del token, y verificar si el token es válido y no ha expirado.

**`JwtAuthenticationFilter`** — filtro de Spring Security que se ejecuta en cada petición. Extrae el token del header `Authorization: Bearer <token>`, lo valida con `JwtService`, y si es válido, establece la autenticación en el `SecurityContextHolder` para que el resto del pipeline de Spring sepa quién hace la petición.

**`SecurityConfig`** — clase de configuración con `@Configuration` y `@EnableWebSecurity`. Define:
- Qué rutas son públicas (sin autenticación): `POST /api/auth/register`, `POST /api/auth/login`, y la consola H2 durante desarrollo.
- Qué rutas requieren autenticación.
- Qué rutas requieren el rol ADMIN.
- Deshabilita CSRF (innecesario en APIs REST stateless).
- Registra el `JwtAuthenticationFilter` antes del filtro de autenticación de Spring.

**`UserDetailsServiceImpl`** — implementa la interfaz `UserDetailsService` de Spring Security. Su método `loadUserByUsername` recibe el email, busca el usuario en el repositorio, y devuelve un objeto `UserDetails` con el email, la contraseña hasheada y el rol. Spring Security usa esto para la autenticación.

---

## Cambios en pom.xml

Se reemplaza el bloque de dependencias de Hibernate standalone por el starter de Spring Boot, que ya incluye Hibernate, HikariCP y configuración automática. H2 y JUnit se mantienen.

**Dependencias a añadir:**

| Dependencia | Artefacto | Para qué sirve |
|---|---|---|
| Spring Boot Web | `spring-boot-starter-web` | Servidor Tomcat embebido, controllers REST, Jackson para JSON |
| Spring Boot Data JPA | `spring-boot-starter-data-jpa` | Spring Data JPA, Hibernate gestionado por Spring |
| Spring Boot Security | `spring-boot-starter-security` | Spring Security, filtros de autenticación |
| Spring Boot Validation | `spring-boot-starter-validation` | Bean Validation (Jakarta Validation API + Hibernate Validator) |
| JJWT API | `io.jsonwebtoken:jjwt-api` | Interfaz de la librería JWT |
| JJWT Impl | `io.jsonwebtoken:jjwt-impl` | Implementación runtime de JWT |
| JJWT Jackson | `io.jsonwebtoken:jjwt-jackson` | Integración de JJWT con Jackson |
| Spring Boot Test | `spring-boot-starter-test` | JUnit 5 y Spring Test Context (reemplaza la dependencia de JUnit 5 standalone) |

**Dependencias que se retiran o reemplazan:**

| Qué se retira | Por qué |
|---|---|
| `hibernate-core` standalone | Lo gestiona `spring-boot-starter-data-jpa` |
| `hibernate-hikaricp` standalone | Lo gestiona `spring-boot-starter-data-jpa` |
| La dependencia de JUnit 5 standalone | La incluye `spring-boot-starter-test` |

H2 se mantiene. JLine3, Gson y AssertJ se mantienen si la CLI se conserva.

**Plugin a añadir:** `spring-boot-maven-plugin` — empaqueta la aplicación como un JAR ejecutable con todo incluido (`java -jar taskhub.jar`).

---

## Decisiones técnicas

| Decisión | Alternativa descartada | Justificación |
|---|---|---|
| JWT stateless | Sesiones HTTP con `HttpSession` | Las APIs REST no mantienen estado de sesión. JWT es el estándar en el sector y es lo que pide el SPEC. |
| DTOs separados del dominio | Serializar las entidades JPA directamente | Las entidades tienen relaciones bidireccionales que causan ciclos infinitos en JSON. Además, exponer entidades acopla la API al modelo interno. |
| Spring Data JPA reemplaza repositorios manuales | Mantener los `JpaRepository` escritos en Fase 4 | Spring Data elimina código repetitivo sin cambiar el comportamiento. Fases anteriores ya enseñaron cómo funciona por debajo. |
| Contraseña hasheada con BCrypt | Guardar la contraseña en texto plano o con MD5 | BCrypt es el estándar para contraseñas. Spring Security lo incluye. Fase 4 guardaba la cadena `passwordHash` — ahora se llena con BCrypt al registrar. |
| H2 en archivo (mismo que fases anteriores) | Migrar a PostgreSQL | Mantener H2 simplifica el setup. El SPEC indica que PostgreSQL queda fuera de alcance. |
| CLI y API coexisten como procesos separados | Eliminar la CLI | La CLI es trabajo de fases anteriores. Eliminarla reduce lo construido. Coexistir demuestra que la arquitectura en capas funciona. |
| `@RestControllerAdvice` para excepciones | Try-catch en cada controller | Centralizar el manejo de errores elimina duplicación y garantiza respuestas consistentes. |
| Rutas anidadas `/projects/{id}/tasks` | Rutas planas `/tasks?projectId=` | Las rutas anidadas expresan mejor la relación de pertenencia (una tarea siempre pertenece a un proyecto) y son la convención REST habitual. |

---

## Pasos de implementación

El orden importa: Spring Boot necesita estar configurado antes de añadir seguridad, y la seguridad antes de probar los endpoints protegidos.

**Paso 1 — Actualizar pom.xml**

Añadir el parent de Spring Boot (`spring-boot-starter-parent`), las dependencias de los starters, las de JJWT, y el plugin de Maven. Verificar que `mvn compile` pasa en verde antes de continuar.

**Paso 2 — Crear `TaskHubApplication.java`**

La clase mínima con `@SpringBootApplication` y el método `main` que llama a `SpringApplication.run`. Verificar que la aplicación arranca en el puerto 8080.

**Paso 3 — Crear `application.properties`**

Configurar la URL de H2 con `AUTO_SERVER=TRUE`, el dialecto Hibernate, `ddl-auto=update`, la consola H2, y las propiedades JWT (clave secreta y tiempo de expiración). La clave secreta debe ser lo suficientemente larga para HS256 (mínimo 32 caracteres).

**Paso 4 — Migrar repositorios a Spring Data JPA**

Crear las interfaces `UserRepository`, `ProjectRepository` y `TaskRepository` que extienden `JpaRepository`. Eliminar las clases de repositorio JPA manuales de Fase 4. Añadir `@Repository` a las interfaces (Spring Data lo añade automáticamente, pero es explícito).

**Paso 5 — Adaptar los servicios para Spring**

Añadir `@Service` a `UserService`, `ProjectService` y `TaskService`. Cambiar la inyección manual del constructor a `@Autowired` o mantener el constructor (Spring también inyecta por constructor sin anotación si hay un solo constructor). Adaptar las firmas de los repositorios al nuevo tipo Spring Data.

**Paso 6 — Crear los DTOs**

Crear los DTOs de request y response en el paquete `dto/`. Añadir las anotaciones de Bean Validation en los DTOs de request. Los DTOs de response son clases planas sin anotaciones de validación.

**Paso 7 — Implementar la seguridad**

Crear `JwtService`, `UserDetailsServiceImpl`, `JwtAuthenticationFilter` y `SecurityConfig`. En este paso, todos los endpoints quedan protegidos. Probar que `POST /api/auth/register` devuelve un token.

**Paso 8 — Crear el `GlobalExceptionHandler`**

Implementar los métodos `@ExceptionHandler` para cada excepción de dominio. Verificar que al llamar a un recurso inexistente se recibe un 404 con cuerpo JSON legible.

**Paso 9 — Crear `AuthController`**

Implementar `register` y `login`. El registro crea el usuario hasheando la contraseña con `BCryptPasswordEncoder` antes de pasarla al servicio. El login verifica credenciales y devuelve el token JWT.

**Paso 10 — Crear `UserController`**

Implementar los endpoints de usuario. Verificar que los endpoints protegidos rechazan peticiones sin token válido.

**Paso 11 — Crear `ProjectController`**

Implementar los endpoints de proyecto. Verificar la creación de proyecto y que el owner queda como miembro.

**Paso 12 — Crear `TaskController`**

Implementar los endpoints de tarea anidados bajo proyecto. Verificar que las reglas de negocio siguen funcionando (un asignado debe ser miembro del proyecto).

**Paso 13 — Tests de integración (opcional pero recomendado)**

Crear tests con `@SpringBootTest` y `MockMvc` que prueben el flujo completo: registrar usuario, obtener token, crear proyecto, crear tarea. Verificar que sin token los endpoints devuelven 401.

---

## Cómo verificar

Arrancar la aplicación con `mvn spring-boot:run` o ejecutando `TaskHubApplication`. Debe aparecer el banner de Spring y la línea `Tomcat started on port 8080`.

**Flujo básico con curl:**

```
# 1. Registrar un usuario
POST /api/auth/register
Body: { "username": "alice", "email": "alice@example.com",
        "password": "password123", "role": "ADMIN" }
Esperado: 201 con { "token": "eyJ...", "type": "Bearer",
                    "username": "alice", "role": "ADMIN" }

# 2. Usar el token en las siguientes peticiones
Header: Authorization: Bearer eyJ...

# 3. Crear un proyecto
POST /api/projects
Body: { "name": "TaskHub Backend", "description": "API REST" }
Esperado: 201 con el proyecto creado

# 4. Crear una tarea
POST /api/projects/{projectId}/tasks
Body: { "title": "Implementar login", "priority": "HIGH" }
Esperado: 201 con la tarea creada

# 5. Cambiar estado de la tarea
PATCH /api/projects/{projectId}/tasks/{taskId}/status
Body: { "status": "IN_PROGRESS" }
Esperado: 200 con la tarea actualizada

# 6. Intentar acceder sin token
GET /api/users
Esperado: 401 Unauthorized

# 7. Intentar acceder con rol insuficiente (con token de MEMBER)
GET /api/users
Esperado: 403 Forbidden

# 8. Acceder a recurso inexistente
GET /api/projects/00000000-0000-0000-0000-000000000000
Esperado: 404 Not Found con mensaje de error
```

**Consola H2 durante desarrollo:**

Con `spring.h2.console.enabled=true` en `application.properties`, la consola web de H2 está disponible en `http://localhost:8080/h2-console`. Permite ejecutar SQL directamente sobre la base de datos para verificar que los datos persisten correctamente.

---

## Relación con el currículo DAM

Esta fase cierra el ciclo educativo del proyecto. Cada fase anterior introdujo un concepto que ahora tiene su equivalente en el mundo profesional:

| Lo que se construyó a mano | Lo que Spring proporciona |
|---|---|
| `Main.java` montando repositorios, servicios y comandos | Contenedor IoC con `@SpringBootApplication` |
| `Repository<T, ID>` + implementaciones manuales | `JpaRepository<T, ID>` de Spring Data |
| Inyección de dependencias por constructor explícito | `@Autowired` / inyección por constructor gestionada por Spring |
| `TaskHubShell` con el bucle REPL | Controllers REST con `@GetMapping`, `@PostMapping`, etc. |
| `persistence.xml` con propiedades Hibernate | `application.properties` con `spring.jpa.*` |
| `try/catch` de excepciones en el shell | `@RestControllerAdvice` centralizado |

El salto a Spring Boot no es un salto en el vacío: es ver automatizado lo que ya se sabe hacer a mano.
