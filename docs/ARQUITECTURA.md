# TaskHub — Arquitectura

Este documento explica **cómo está organizado el código por dentro** y, sobre todo, **por qué**. Si entiendes esto, entiendes por qué el proyecto puede crecer fase a fase sin romperse.

---

## 1. La idea central

El proyecto se divide en **capas**. Cada capa solo conoce a la de debajo, nunca a la de arriba.

```
┌──────────────────────────────────────────────┐
│  PRESENTACIÓN  (cli/)                          │
│  La terminal: lee comandos, muestra formularios│
│  y resultados. NO contiene lógica de negocio.  │
└───────────────────────┬────────────────────────┘
                        │ llama a
┌───────────────────────▼────────────────────────┐
│  APLICACIÓN  (service/)                         │
│  Lógica de negocio y validación. Orquesta el    │
│  dominio. NO sabe nada de la terminal ni de SQL.│
└───────────────────────┬────────────────────────┘
                        │ usa
┌───────────────────────▼────────────────────────┐
│  DOMINIO  (domain/)                             │
│  Las entidades y sus reglas. El corazón. No     │
│  depende de NADA externo.                       │
└───────────────────────┬────────────────────────┘
                        │ se guarda mediante
┌───────────────────────▼────────────────────────┐
│  PERSISTENCIA  (repository/)                    │
│  Cómo se guardan los datos. Cambia cada fase:   │
│  memoria → JSON → JDBC → JPA.                   │
└──────────────────────────────────────────────────┘
```

**La regla de las dependencias:** las flechas apuntan siempre hacia abajo (hacia el dominio). El dominio no importa nada de las otras capas. Esto es lo que permite cambiar la terminal o la base de datos sin tocar el resto.

---

## 2. Por qué esto hace el proyecto escalable

El truco está en la capa de **persistencia**. Definimos una **interfaz**:

```java
public interface Repository<T, ID> {
    T save(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    void deleteById(ID id);
}
```

Los servicios trabajan con `Repository`, **nunca** con una implementación concreta. Entonces:

- **Fase 1** → `InMemoryRepository` (un `HashMap`)
- **Fase 2** → `JsonRepository` (lee/escribe ficheros)
- **Fase 3** → `JdbcRepository` (SQL a mano)
- **Fase 4** → `JpaRepository` (Hibernate)

En cada fase **cambias la implementación, no la interfaz**. El servicio sigue llamando a `repository.save(user)` sin enterarse de nada. La terminal tampoco cambia. Eso es escalabilidad real, y es exactamente lo que hacen las empresas.

---

## 3. El patrón Command (la columna vertebral de la CLI)

La terminal tiene que reconocer comandos que iremos añadiendo. Si lo hiciéramos con un `switch` gigante, el código se volvería inmanejable. En su lugar usamos el **patrón Command**: cada comando es su propia clase.

```java
public interface Command {
    String name();                                   // "create-user"
    String description();                            // para el help
    void execute(CommandContext ctx, String[] args); // qué hace
}
```

Un `CommandRegistry` guarda todos los comandos en un `Map<String, Command>`:

```java
registry.register(new CreateUserCommand());
registry.register(new ListUsersCommand());
// ...
```

Cuando el usuario escribe algo, el shell busca el comando por su nombre y llama a `execute`. **Añadir un comando nuevo = crear una clase + registrarla.** Cero modificaciones en el resto del código. Esto enseña polimorfismo e interfaces (POO avanzada de DAM) de forma práctica.

---

## 4. Estructura de paquetes

> El paquete raíz es `org.example` (el que generó IntelliJ). Si quieres, puedes renombrarlo a `com.taskhub`, pero no es necesario.

```
org.example/
│
├── Main.java                       ← punto de entrada: monta todo y arranca la CLI
│
├── domain/                         ← CAPA DOMINIO (ya construida)
│   ├── User.java
│   ├── Project.java
│   ├── Task.java
│   ├── Comment.java
│   ├── Role.java
│   ├── Status.java
│   └── Priority.java
│
├── exception/                      ← excepciones de dominio
│   ├── EntityNotFoundException.java
│   ├── DuplicateEntityException.java
│   └── ValidationException.java
│
├── repository/                     ← CAPA PERSISTENCIA
│   ├── Repository.java             ← la interfaz genérica
│   └── memory/                     ← implementación de Fase 1
│       ├── InMemoryRepository.java
│       ├── InMemoryUserRepository.java
│       ├── InMemoryProjectRepository.java
│       └── InMemoryTaskRepository.java
│
├── service/                        ← CAPA APLICACIÓN
│   ├── UserService.java
│   ├── ProjectService.java
│   └── TaskService.java
│
└── cli/                            ← CAPA PRESENTACIÓN (la terminal)
    ├── TaskHubShell.java           ← el bucle REPL principal
    ├── Command.java                ← interfaz de un comando
    ├── CommandRegistry.java        ← registro de comandos
    ├── CommandContext.java         ← acceso a servicios + terminal + config
    │
    ├── commands/                   ← un comando por clase
    │   ├── HelpCommand.java
    │   ├── ExitCommand.java
    │   ├── ClearCommand.java
    │   ├── ThemeCommand.java
    │   ├── user/
    │   │   ├── CreateUserCommand.java
    │   │   ├── ListUsersCommand.java
    │   │   └── DeleteUserCommand.java
    │   ├── project/   (create, list, add-member...)
    │   └── task/      (create, list, assign, set-status...)
    │
    ├── io/                         ← entrada/salida de la terminal
    │   ├── Prompter.java           ← formularios interactivos (campo a campo)
    │   ├── TablePrinter.java       ← imprime listas como tablas
    │   └── Output.java             ← salida con colores ANSI
    │
    └── theme/                      ← apariencia
        ├── Theme.java
        └── ThemeManager.java
```

---

## 5. Flujo de una acción completa

Ejemplo: el usuario escribe `create-user`.

```
1. TaskHubShell lee la línea "create-user"
2. CommandRegistry busca el comando con name() == "create-user"
3. Encuentra CreateUserCommand y llama a execute(ctx, args)
4. CreateUserCommand usa el Prompter para pedir username, email, role
5. CreateUserCommand llama a ctx.userService().create(...)
6. UserService valida (¿email duplicado?) y llama a repository.save(user)
7. El Repository (InMemory en Fase 1) guarda en su HashMap
8. Vuelve el User creado hacia arriba
9. CreateUserCommand usa Output para imprimir "✓ Usuario creado"
```

Fíjate: el `Prompter` y el `Output` (terminal) están arriba; el `Repository` (datos) está abajo; en medio el `Service` (negocio). Cada uno hace su trabajo y no se mete en el de los demás.

---

## 6. Principios que seguimos

1. **Una clase, una responsabilidad.** Si una clase hace dos cosas, sepárala.
2. **Depender de interfaces, no de implementaciones.** Los servicios dependen de `Repository`, no de `InMemoryRepository`.
3. **El dominio no depende de nada.** `User` no sabe que existe una base de datos ni una terminal.
4. **La interactividad vive solo en `cli/`.** Si mañana añadimos una API REST, los servicios se reutilizan tal cual.
5. **Cada fase compila y los tests pasan.** Nunca dejamos el proyecto roto entre fases.
