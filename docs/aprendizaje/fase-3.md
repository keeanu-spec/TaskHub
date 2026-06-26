# Fase 3 — JDBC y Base de Datos Relacional

**Tema DAM:** Acceso a Datos — JDBC  
**Tecnologías nuevas:** H2, HikariCP, JDBC puro, SQL

---

## Qué problema resuelve esta fase

El JSON funciona, pero tiene límites: no puedes hacer búsquedas eficientes, no hay relaciones reales entre entidades, y el archivo entero se reescribe cada vez que cambias algo. Una base de datos relacional resuelve todo eso.

La buena noticia: como los servicios dependen de la interfaz `Repository`, **no tocan nada**. Solo añades una nueva implementación de repositorio.

---

## Qué vas a aprender

- SQL: crear tablas, insertar, consultar, actualizar y borrar datos
- JDBC: la API de Java para hablar con bases de datos directamente
- Connection pooling: por qué no abres una conexión por consulta y cómo HikariCP lo resuelve
- Transacciones: qué son y cuándo las necesitas
- Tests de integración con una base de datos real (H2 en memoria)

---

## Tecnologías

**H2** — base de datos que vive dentro del propio programa (no necesitas instalar nada). Perfecta para aprender y para tests. En Fase 4 se podría cambiar a PostgreSQL sin tocar el código.

**HikariCP** — gestiona un pool de conexiones a la base de datos. En vez de abrir y cerrar una conexión en cada consulta (caro), mantiene un grupo de conexiones abiertas y las reutiliza.

**JDBC** — la API estándar de Java para ejecutar SQL. Es de bajo nivel: tú escribes el SQL a mano, tú mapeas los resultados a objetos Java. Hibernate (Fase 4) automatiza esto, pero primero hay que entender qué hay debajo.

---

## Paso 1 — Añadir dependencias al pom.xml

Necesitas dos dependencias nuevas:
- H2 (base de datos)
- HikariCP (pool de conexiones)

Investiga en Maven Central el `groupId` y `artifactId` de cada una. H2 va con `scope test` porque en producción (Fase 4+) usarías otra base de datos.

---

## Paso 2 — Crear el schema SQL

**Archivo a crear:** `src/main/resources/schema.sql`

Este archivo define la estructura de la base de datos: qué tablas existen y qué columnas tienen. Una tabla por entidad.

Cosas a tener en cuenta:
- `UUID` en H2 se declara como tipo `UUID`
- `LocalDateTime` se mapea a `TIMESTAMP`
- `LocalDate` se mapea a `DATE`
- Los enums (`Role`, `Status`, `Priority`) se guardan como `VARCHAR`
- Las relaciones entre tablas se modelan con claves foráneas (`FOREIGN KEY`)

Tablas que necesitas: `users`, `projects`, `tasks`.

La tabla `projects` necesita una tabla intermedia `project_members` para la relación muchos a muchos con `users`.

---

## Paso 3 — Crear la conexión y el pool

**Archivo a crear:** `src/main/java/org/example/config/DatabaseConfig.java`

Esta clase se encarga de crear y configurar el pool de HikariCP. Tiene que:
- Configurar la URL de conexión a H2
- Decirle a H2 que ejecute `schema.sql` al arrancar para crear las tablas
- Devolver un `DataSource` (el objeto que representa el pool)

El `DataSource` es lo que luego usarán los repositorios para pedir conexiones.

---

## Paso 4 — Crear `JdbcRepository<T, ID>`

**Archivo a crear:** `src/main/java/org/example/repository/jdbc/JdbcRepository.java`

A diferencia de `InMemoryRepository` y `JsonRepository`, este no puede ser genérico del todo porque cada entidad tiene su propio SQL. Lo que sí puedes abstraer son los patrones comunes.

La clase tiene que:
- Recibir un `DataSource` en el constructor
- Pedir conexiones al pool con `dataSource.getConnection()`
- Usar `PreparedStatement` para ejecutar SQL (nunca construyas SQL concatenando Strings — es inseguro)
- Cerrar conexiones, statements y resultsets después de usarlos (o usar try-with-resources)

JDBC funciona así para una consulta:
1. Pides una conexión al pool
2. Preparas un `PreparedStatement` con el SQL
3. Asignas los parámetros (`?` en el SQL)
4. Ejecutas
5. Si es una consulta (`SELECT`), lees el `ResultSet` fila a fila y construyes objetos Java
6. Cierras todo

---

## Paso 5 — Crear los repositorios concretos JDBC

**Archivos a crear** en `src/main/java/org/example/repository/jdbc/`:
- `UserJdbcRepository.java`
- `ProjectJdbcRepository.java`
- `TaskJdbcRepository.java`

Cada uno implementa `Repository<Entidad, UUID>` y tiene su propio SQL.

Para cada repositorio necesitas implementar:
- `save()` — un `INSERT` si es nuevo, un `UPDATE` si ya existe (puedes usar `INSERT OR REPLACE` en H2 o comprobar si existe primero)
- `findById()` — un `SELECT ... WHERE id = ?`
- `findAll()` — un `SELECT *`
- `deleteById()` — un `DELETE WHERE id = ?`

El reto de esta fase: el `ResultSet` devuelve columnas con nombres y tipos SQL. Tú tienes que leerlas y construir los objetos Java a mano. Por ejemplo, para un `User`:

```
resultSet.getString("username")   → campo username del User
resultSet.getString("role")       → hay que convertir el String a enum Role
resultSet.getObject("id", UUID.class) → el UUID
```

---

## Paso 6 — Actualizar Main

**Archivo:** `Main.java`

Igual que en Fase 2: solo cambias qué repositorios se instancian. Esta vez los pasas el `DataSource`.

---

## Paso 7 — Tests de integración

**Archivo a crear:** `src/test/java/org/example/repository/UserJdbcRepositoryTest.java`

Los tests deben:
- Crear un repositorio JDBC apuntando a una H2 en memoria
- Guardar entidades, buscarlas, borrarlas
- Verificar que persisten dentro de la misma sesión

H2 en memoria se resetea entre tests si así lo configuras — ideal para tests limpios.

---

## Orden recomendado

1. Dependencias en pom.xml → `mvn compile` en verde
2. `schema.sql` con las tablas
3. `DatabaseConfig` con el pool
4. `UserJdbcRepository` (empieza por el más simple)
5. Tests del `UserJdbcRepository`
6. `ProjectJdbcRepository` y `TaskJdbcRepository`
7. Actualizar `Main`
8. Prueba manual: crear usuario desde la terminal y verificar que persiste

---

## Hecho cuando

- La app arranca y puedes crear/listar datos desde la terminal
- Los datos persisten entre reinicios (en el archivo H2)
- `mvn test` pasa en verde con los tests JDBC

---

## Checklist

- [ ] H2 y HikariCP en pom.xml
- [ ] `schema.sql` con tablas `users`, `projects`, `tasks`, `project_members`
- [ ] `DatabaseConfig` con el pool configurado
- [ ] `UserJdbcRepository` implementando `Repository<User, UUID>`
- [ ] `ProjectJdbcRepository` implementando `Repository<Project, UUID>`
- [ ] `TaskJdbcRepository` implementando `Repository<Task, UUID>`
- [ ] `Main` usando los repositorios JDBC
- [ ] Tests de integración en verde
