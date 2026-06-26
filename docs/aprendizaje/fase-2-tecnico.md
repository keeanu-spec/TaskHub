# Fase 2 — Persistencia JSON — Guía Técnica Detallada

**Objetivo:** que los datos no se pierdan al cerrar el programa. Se guardan en archivos `.json` en disco.

---

## El problema que resuelve

En Fase 1, los datos vivían en un `HashMap` en memoria. Al cerrar el programa, la memoria se libera y todo desaparece. En Fase 2 se añade una capa que guarda ese `HashMap` en un archivo `.json` cada vez que algo cambia, y lo carga al arrancar.

---

## Paso 1 — Añadir Gson al pom.xml

**Archivo:** `pom.xml`

**Qué hacer:** añadir la dependencia de Gson dentro de `<dependencies>`.

```xml
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.11.0</version>
</dependency>
```

**Por qué:** Gson es la librería que convierte objetos Java a texto JSON y viceversa. Sin ella no puedes guardar nada en disco de forma legible.

---

## Paso 2 — Crear `JsonRepository<T, ID>`

**Archivo:** `src/main/java/org/example/repository/json/JsonRepository.java`

**Qué hace:** implementa `Repository<T, ID>` igual que `InMemoryRepository`, pero en vez de solo guardar en un `HashMap`, también escribe en un archivo `.json` cada vez que hay un cambio.

**Campos que necesita:**
- `HashMap<ID, T> store` — los datos en memoria (igual que InMemoryRepository)
- `Function<T, ID> idExtractor` — para extraer el id de cada entidad
- `Path filePath` — la ruta del archivo donde se guardan los datos
- `Class<T> type` — la clase concreta de T, necesaria para que Gson sepa qué tipo deserializar

**Constructor — qué hace al arrancar:**
1. Guarda los parámetros en los campos
2. Comprueba si el archivo JSON ya existe con `Files.exists(filePath)`
3. Si existe, lo lee con `Files.readString(filePath)` — devuelve el contenido como String
4. Convierte ese String a una lista de objetos con Gson: `gson.fromJson(...)`
5. Mete cada objeto en el `HashMap` con un for loop

**Por qué necesitas `Class<T> type`:** Java borra los tipos genéricos en compilación (type erasure). En runtime, `T` no existe — es `Object`. Gson necesita saber el tipo real para deserializar correctamente. Por eso guardas `Class<T> type` y usas `TypeToken.getParameterized(List.class, type).getType()` en vez de `new TypeToken<List<T>>(){}`.

**Métodos:**
- `save(T entity)` — mete en el HashMap y llama a `persistir()`
- `findById(ID id)` — busca en el HashMap
- `findAll()` — devuelve todos los valores del HashMap
- `deleteById(ID id)` — borra del HashMap y llama a `persistir()`
- `persistir()` (privado) — convierte el HashMap a JSON con Gson y escribe el archivo con `Files.writeString(filePath, json)`

**Problema con Gson 2.11.0 y java.time:** Gson no sabe serializar `LocalDate` ni `LocalDateTime` en Java moderno porque el sistema de módulos bloquea el acceso por reflexión. La solución es registrar `TypeAdapter` para esos tipos que le dicen a Gson cómo convertirlos manualmente:
- Serializar: objeto Java → String con `.toString()`
- Deserializar: String → objeto Java con `LocalDate.parse()` / `LocalDateTime.parse()`

```java
private Gson buildGson() {
    return new GsonBuilder()
        .registerTypeAdapter(LocalDate.class, new TypeAdapter<LocalDate>() {
            @Override public void write(JsonWriter out, LocalDate v) throws IOException {
                out.value(v == null ? null : v.toString());
            }
            @Override public LocalDate read(JsonReader in) throws IOException {
                return LocalDate.parse(in.nextString());
            }
        }.nullSafe())
        .registerTypeAdapter(LocalDateTime.class, new TypeAdapter<LocalDateTime>() {
            @Override public void write(JsonWriter out, LocalDateTime v) throws IOException {
                out.value(v == null ? null : v.toString());
            }
            @Override public LocalDateTime read(JsonReader in) throws IOException {
                return LocalDateTime.parse(in.nextString());
            }
        }.nullSafe())
        .create();
}
```

---

## Paso 3 — Crear los repositorios concretos JSON

Son tres clases pequeñas, una por entidad. Solo tienen un constructor que llama a `super(...)`.

---

### `UserJsonRepository.java`

**Archivo:** `src/main/java/org/example/repository/json/UserJsonRepository.java`

**Qué hace:** extiende `JsonRepository<User, UUID>` y le dice dónde guardar los datos de usuarios.

```java
public class UserJsonRepository extends JsonRepository<User, UUID> {
    public UserJsonRepository() {
        super(User::getId, Path.of("data/users.json"), User.class);
    }
}
```

**Por qué:** `JsonRepository` es genérico — no sabe qué entidad maneja. Esta clase concreta le dice: "maneja `User`, extrae el id con `User::getId`, guarda en `data/users.json`, y el tipo es `User.class`".

---

### `ProjectJsonRepository.java`

**Archivo:** `src/main/java/org/example/repository/json/ProjectJsonRepository.java`

**Qué hace:** igual que `UserJsonRepository` pero para `Project`.

```java
public class ProjectJsonRepository extends JsonRepository<Project, UUID> {
    public ProjectJsonRepository() {
        super(Project::getId, Path.of("data/projects.json"), Project.class);
    }
}
```

---

### `TaskJsonRepository.java`

**Archivo:** `src/main/java/org/example/repository/json/TaskJsonRepository.java`

**Qué hace:** igual pero para `Task`.

```java
public class TaskJsonRepository extends JsonRepository<Task, UUID> {
    public TaskJsonRepository() {
        super(Task::getId, Path.of("data/tasks.json"), Task.class);
    }
}
```

---

## Paso 4 — Actualizar los servicios

**Archivos:** `UserService.java`, `ProjectService.java`, `TaskService.java`

**Problema:** los servicios tenían el tipo concreto del repositorio de memoria hardcodeado, por ejemplo:

```java
private final UserRepository userRepository; // solo acepta UserRepository
```

Esto impide pasarle `UserJsonRepository` porque son clases distintas.

**Solución:** cambiar el tipo del campo y del constructor a la interfaz `Repository<T, ID>`:

```java
private final Repository<User, UUID> userRepository; // acepta cualquier implementación
```

**Por qué funciona:** tanto `UserRepository` como `UserJsonRepository` implementan `Repository<User, UUID>`. Al depender de la interfaz, el servicio acepta cualquier repositorio sin importar cómo guarda los datos.

**Efecto secundario en `UserService`:** `findByEmail` estaba en `UserRepository` pero no en la interfaz `Repository`. Al cambiar el tipo, el servicio pierde acceso a ese método. Solución: mover la lógica de `findByEmail` al propio servicio usando streams:

```java
public Optional<User> findByEmail(String email) {
    return userRepository.findAll().stream()
        .filter(user -> user.getEmail().equals(email))
        .findFirst();
}
```

Y en `create()`, llamar a `findByEmail(email)` del propio servicio en vez de `userRepository.findByEmail(email)`.

---

## Paso 5 — Actualizar `Main.java`

**Archivo:** `src/main/java/org/example/Main.java`

**Qué cambia:** solo tres líneas — el tipo y la instancia de cada repositorio.

```java
// Antes
UserRepository userRepository = new UserRepository();
ProjectRepository projectRepository = new ProjectRepository();
TaskRepository taskRepository = new TaskRepository();

// Después
UserJsonRepository userRepository = new UserJsonRepository();
ProjectJsonRepository projectRepository = new ProjectJsonRepository();
TaskJsonRepository taskRepository = new TaskJsonRepository();
```

**Por qué solo cambia Main:** porque los servicios ahora dependen de la interfaz, no de la implementación. El resto del proyecto — servicios, CLI, comandos — no toca nada.

---

## Dónde se guardan los archivos

Los archivos JSON se crean en `data/` relativo al directorio desde donde ejecutas el programa. Si usas `run.bat`, quedan en `TaskHub-main/data/`:

```
TaskHub-main/
  data/
    users.json
    projects.json
    tasks.json
```

---

## Resumen del flujo completo

```
Arranque
  └─ JsonRepository constructor
       └─ ¿existe el .json? → sí → readString → fromJson → llena el HashMap

Operación (ej. crear usuario)
  └─ UserService.create()
       └─ userRepository.save(user)
            └─ HashMap.put(id, user)
            └─ persistir()
                 └─ toJson(store.values()) → writeString → archivo .json

Próximo arranque
  └─ carga el .json → el usuario sigue ahí
```

---

## Checklist

- [x] Gson en pom.xml
- [x] `JsonRepository<T, ID>` con TypeAdapters para LocalDate y LocalDateTime
- [x] `UserJsonRepository`, `ProjectJsonRepository`, `TaskJsonRepository`
- [x] Servicios dependen de `Repository<T, ID>` en vez de clases concretas
- [x] `Main` usa los repositorios JSON
- [ ] Tests de integración (Paso 5 pendiente)
