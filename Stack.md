Java 23 — el lenguaje principal
Maven — gestiona las dependencias y compila el proyecto
JLine3 — librería para la terminal interactiva. Te da el LineReader, Terminal y el soporte para leer input del usuario
JUnit 5 — framework de tests. Las anotaciones @Test y @BeforeEach
AssertJ — librería para las comprobaciones en los tests. El assertThat, assertThatThrownBy, etc.
ANSI escape codes — no es una librería, son códigos de texto que la terminal interpreta como colores y acciones. Los usas en Output y en ClearCommand
UUID — clase de Java para generar identificadores únicos. Lo usa cada entidad para su id
Optional — clase de Java para manejar valores que pueden no existir. Lo usas en los repositorios y servicios
Function<T,R> — interfaz funcional de Java. La usas en InMemoryRepository para el idExtractor
Record — característica de Java moderna. Lo usas en CommandContext
Streams — API de Java para procesar colecciones. Lo usas en findByEmail y ListUsersCommand
Runtime.exec() — clase de Java para ejecutar procesos del sistema operativo. Lo usas en OpenNotepadCommand y DiscordCommand
Gson — librería de Google para convertir objetos Java a JSON y viceversa. Lo usas en JsonRepository para persistir datos en disco
java.nio (Path, Files) — API moderna de Java para leer y escribir ficheros. Lo usas en JsonRepository con Files.readString y Files.writeString
TypeToken — clase de Gson para preservar tipos genéricos en runtime. Lo necesitas porque Java borra los tipos genéricos en compilación
TypeAdapter — clase de Gson para enseñarle a serializar tipos que no soporta por defecto, como LocalDate y LocalDateTime