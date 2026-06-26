# Fase 1 — Guía de aprendizaje: Core Java + CLI

Esta guía **no te da el código resuelto**. Te da, en cada paso: el concepto, qué tienes que construir, una pista para orientarte y cómo saber que está bien (“Hecho cuando”). El código lo escribes tú — ese es el objetivo: reaprender programando.

> Trabaja los pasos **en orden**. No pases al siguiente hasta cumplir el “Hecho cuando”. Si te atascas en la sintaxis, intenta recordarla o búscala, y si no sale, pídeme una pista concreta (no la solución).

**Conceptos de Java que vas a practicar:** enums, POO, interfaces, genéricos (`<T>`), colecciones (`Map`, `List`, `Optional`), excepciones personalizadas, polimorfismo y el patrón Command.

---

## Paso 0 — Preparar el `pom.xml`

> Esto es **configuración**, no lo que vienes a reaprender. Te dejo las coordenadas para que no te bloquees con el setup.

**Qué:** añadir tres dependencias dentro de un bloque `<dependencies>` en el `pom.xml`:

- **JLine3** — `org.jline : jline : 3.26.3` (la terminal interactiva)
- **JUnit 5** — `org.junit.jupiter : junit-jupiter : 5.11.0` con `<scope>test</scope>`
- **AssertJ** — `org.assertj : assertj-core : 3.26.3` con `<scope>test</scope>`

**Hecho cuando:** Maven recarga sin errores y puedes importar `org.jline...` sin que salga en rojo.

---

## Paso 1 — Repasar el dominio

Ya tienes las entidades y enums. Revisa que:

- `Task` tiene `assignee` (puede ser null), `dueDate` es `LocalDate` y el campo del proyecto se llama `project`.
- Todas las entidades generan su `id` y su `createdAt` dentro del constructor (no se piden por parámetro).
- Los nombres son consistentes (`createdAt` con A mayúscula en todas).

**Hecho cuando:** el dominio compila y cada entidad nace con su id puesto.

---

## Paso 2 — Excepciones de dominio

**Qué:** tres clases en el paquete `exception`: `EntityNotFoundException`, `DuplicateEntityException`, `ValidationException`.

**Por qué:** cuando una regla de negocio se incumple (email duplicado, entidad inexistente, dato inválido), lanzamos un error con significado en vez de uno genérico. Es “fallar con intención”.

**A recordar:**
- Heredan de `RuntimeException` (unchecked): son errores de uso, no quieres `try/catch` por todo el código.
- A `super()` solo le pasas **un** mensaje (String). No existe constructor de dos Strings.
- Quien lanza la excepción decide el texto del mensaje, no la propia clase.

**Hecho cuando:** las tres clases compilan.

---

## Paso 3 — La interfaz `Repository<T, ID>`

**Qué:** una interfaz genérica en el paquete `repository` que represente “un almacén de entidades”.

**Por qué:** es la pieza clave de la arquitectura. Los servicios hablarán con esta interfaz, nunca con la base de datos directa. En fases futuras cambiarás la implementación (memoria → JSON → JDBC) sin tocar nada más. (Relee `ARQUITECTURA.md` §2 si necesitas el porqué; el *qué* lo diseñas tú.)

**A recordar / pistas:**
- Sintaxis de interfaz: `public interface Nombre { ... }`, solo firmas, sin cuerpo.
- Es **genérica**: dos parámetros de tipo entre `< >`. Uno para el tipo de entidad, otro para el tipo de su id.
- Piensa qué operaciones básicas necesita cualquier almacén: guardar, buscar uno por su id, listar todos, borrar por id. (CRUD.)
- Para “buscar uno por id”, en vez de devolver `null` cuando no existe, usa `Optional<...>`. Te obliga a contemplar el caso “no está”.

**Hecho cuando:** la interfaz compila y declara las operaciones CRUD básicas.

---

## Paso 4 — `InMemoryRepository`

**Qué:** una implementación de `Repository` que guarde los datos en memoria.

**Por qué:** en Fase 1 no hay base de datos. Se guarda en un `Map` cuya clave es el id. Al cerrar el programa se pierde — es lo esperado en esta fase.

**Reto de diseño que tienes que resolver:** el `Map` necesita la clave (el id) para guardar, pero el método de guardar recibe la entidad entera. ¿Cómo saca un repositorio *genérico* el id de una entidad que no conoce?
- Pista: el repositorio no puede saber por sí mismo cómo obtener el id de un `T` cualquiera. Tendrás que **dárselo** de alguna forma al crearlo (piensa en pasarle “la manera de extraer el id” como algo configurable). Investiga `Function<T, ID>` y las referencias a método (`Clase::getId`).

**Hecho cuando:** compila e implementa las cuatro operaciones de la interfaz.

---

## Paso 5 — Repositorios concretos

**Qué:** uno por entidad en `repository/memory/`: usuarios, proyectos, tareas.

**Por qué métodos extra:** algunas búsquedas solo tienen sentido para una entidad (p. ej. “buscar usuario por email”). Esas no van en la interfaz genérica; las añades en el repositorio concreto.
- Pista para `findByEmail`: recorre los valores del `Map` con un stream y filtra por el campo.

**Hecho cuando:** tienes los tres repositorios concretos y compilan.

---

## Paso 6 — Servicios

**Qué:** la lógica de negocio en el paquete `service`: `UserService`, `ProjectService`, `TaskService`.

**Por qué:** el repositorio solo guarda y recupera. El servicio decide las **reglas**.

**Reglas a implementar (aquí es donde lanzas las excepciones del Paso 2 con `throw`):**
- `UserService.create` → username y email no vacíos (`ValidationException`); email único (`DuplicateEntityException`).
- `ProjectService.create` → el owner se añade automáticamente a `members`.
- `TaskService.assign` → el usuario debe ser miembro del proyecto; si no, `ValidationException`.
- Buscar por id que no existe → `EntityNotFoundException`.

**Concepto clave — inyección de dependencias:** el servicio **recibe** el repositorio por su constructor, no lo crea él dentro. Así, en los tests puedes pasarle uno de prueba. Esto es la base de lo que Spring automatiza en Fase 6.

**Pista de diseño:** ¿de qué tipo declaras el repositorio dentro del servicio: de la clase concreta o de la interfaz? Piensa en el Paso 3 y en por qué hicimos la interfaz.

**Hecho cuando:** los tres servicios compilan con sus reglas y lanzan las excepciones correctas.

---

## Paso 7 — Tests de los servicios

**Qué:** tests JUnit 5 en `src/test/java/org/example/service/`.

**Por qué:** son tu red de seguridad. En las próximas fases cambiarás la persistencia; estos tests te avisarán al instante si rompiste algo.

**A recordar / pistas:**
- Anotaciones: `@Test` marca un test; `@BeforeEach` prepara algo antes de cada uno.
- Con AssertJ: `assertThat(valor).isEqualTo(...)`, `assertThat(x).isNotNull()`.
- Para comprobar que algo lanza una excepción: `assertThatThrownBy(() -> ...).isInstanceOf(...)`.
- Cubre el caso feliz **y** el de error de cada regla (p. ej. crear usuario OK, y crear con email duplicado → excepción).

**Hecho cuando:** `mvn test` pasa en verde y cubres casos felices y de error.

---

## Paso 8 — La terminal: piezas base

> A partir de aquí construyes la CLI. Relee `CLI.md` para el diseño general (concepto REPL, comandos, formularios).

**Qué:** tres piezas en `cli/`:

1. **`Command`** — una **interfaz** que represente “un comando ejecutable”. Piensa qué necesita saber el shell de cada comando: su nombre (para encontrarlo), una descripción (para el help) y una forma de ejecutarlo.
2. **`CommandContext`** — un objeto que agrupa lo que un comando necesita para trabajar (los servicios y las utilidades de salida/entrada de la terminal). Pista: un `record` encaja bien porque solo agrupa datos.
3. **`CommandRegistry`** — guarda los comandos y los busca por nombre. Pista: por dentro es un `Map<String, Command>` con un método para registrar y otro para buscar.

**Concepto — patrón Command:** cada acción es su propia clase que implementa `Command`. Añadir un comando = crear una clase + registrarla. Nada de un `switch` gigante.

**Hecho cuando:** las tres piezas compilan.

---

## Paso 9 — Salida con colores: `Output`

**Qué:** una clase en `cli/io/` con métodos para mensajes de éxito, error, aviso e info.

**Por qué:** centralizar cómo se imprime. Si todo pasa por aquí, cambiar el tema afecta a toda la app de golpe.

**A recordar:** los colores de terminal son **códigos ANSI** (texto especial que el terminal interpreta como color). Busca “ANSI escape codes Java” — verde, rojo, amarillo, y el código de “reset” para volver al color normal al final de cada línea.

**Hecho cuando:** imprimes mensajes de colores por consola.

---

## Paso 10 — Formularios: `Prompter`

**Qué:** la clase que pregunta campo a campo (ver `CLI.md` §4).

**Métodos que necesitarás (piensa tú la firma):** preguntar texto; preguntar con valor por defecto (Enter = el defecto); preguntar contraseña (oculta); elegir un valor de un enum.

**Pistas:**
- Apóyate en el `LineReader` de JLine3 para leer líneas.
- Para la contraseña, JLine permite leer con una máscara.
- Para el enum genérico, investiga `Class<E>` y el método `values()` de los enums.

**Hecho cuando:** puedes pedir varios campos seguidos por consola.

---

## Paso 11 — Tu primer comando real

**Qué:** `CreateUserCommand` en `cli/commands/user/`. También `ListUsersCommand`, `HelpCommand` y `ExitCommand`.

**Cómo encaja:** la clase implementa `Command`. En su método de ejecución: pide los datos con el `Prompter`, llama al servicio correspondiente para crear/listar, y muestra el resultado con `Output`. El comando **no** valida ni guarda — de eso se encargan el servicio y el repositorio.

**Pistas:**
- `HelpCommand` recorre el `CommandRegistry` e imprime nombre + descripción de cada comando.
- `ListUsersCommand` pide los usuarios al servicio y los muestra (en tabla si ya tienes `TablePrinter`).

**Hecho cuando:** las clases compilan y están listas para registrarse.

---

## Paso 12 — El bucle REPL: `TaskHubShell`

**Qué:** el corazón de la terminal. Un bucle que: lee una línea, separa el nombre del comando de sus argumentos, busca el comando en el registry, lo ejecuta y vuelve a empezar.

**Puntos a resolver:**
- Si la línea está vacía, ignórala y vuelve a pedir.
- Si el comando no existe, muestra un error claro y sigue.
- Envuelve la ejecución del comando en **un único** `try/catch` que recoja las excepciones de negocio (Paso 2) y las imprima con `Output`. Aquí es donde se “atrapan” todos los `throw` de los servicios.

**Concepto:** ese único `try/catch` es la razón por la que los servicios pueden lanzar excepciones sin preocuparse de capturarlas — el shell es el punto central que las maneja.

**Hecho cuando:** arrancas el programa, ves el prompt, y `create-user` / `list-users` funcionan de principio a fin.

---

## Paso 13 — `Main`: montarlo todo

**Qué:** el punto de entrada que crea las piezas y arranca el shell.

**Qué hace, en orden:** crea los repositorios → crea los servicios pasándoles sus repositorios → crea el `CommandRegistry` y registra los comandos → crea el `CommandContext` → arranca el `TaskHubShell`.

**Concepto — composición:** este es el único sitio que conoce las implementaciones concretas (qué repositorio, qué comandos). Todo lo demás trabaja con interfaces. En Fase 6 esto lo hará Spring por ti, y entenderás exactamente qué te ahorra.

**Hecho cuando:** `mvn compile` y arrancar el programa te deja usar la terminal completa.

---

## Paso 14 (opcional) — Temas

Cuando todo funcione, añade `theme/` y el comando `theme` (ver `CLI.md` §6). Buen ejercicio de polimorfismo, no crítico para la fase.

---

## Checklist de la Fase 1

- [ ] `pom.xml` con JLine3, JUnit 5 y AssertJ
- [ ] Dominio con ids autogenerados y nombres consistentes
- [ ] 3 excepciones de dominio
- [ ] Interfaz `Repository` + `InMemoryRepository`
- [ ] 3 repositorios concretos
- [ ] 3 servicios con sus reglas de negocio
- [ ] Tests en verde (`mvn test`)
- [ ] `Output`, `Prompter`, `TablePrinter`
- [ ] `Command`, `CommandRegistry`, `CommandContext`
- [ ] Comandos: help, exit, create-user, list-users (mínimo)
- [ ] `TaskHubShell` con el bucle REPL
- [ ] `Main` que arranca la terminal
- [ ] La app arranca y puedes crear y listar usuarios desde la terminal

Cuando todo esté marcado, la Fase 1 está completa y pasamos a la Fase 2 (persistencia en ficheros).
