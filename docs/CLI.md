# TaskHub — Diseño de la Terminal Interactiva

La CLI es la cara del proyecto. No es un simple menú: es una **terminal propia** que arranca con el programa, reconoce comandos, lanza formularios paso a paso, imprime resultados con formato y se puede personalizar.

---

## 1. Concepto: un REPL

REPL = **R**ead **E**val **P**rint **L**oop. El bucle eterno de toda terminal:

```
1. READ  → leer lo que escribe el usuario
2. EVAL  → interpretarlo y ejecutarlo
3. PRINT → mostrar el resultado
4. LOOP  → volver al paso 1
```

Así se ve al arrancar:

```
╔══════════════════════════════════════╗
║            TASKHUB  v1.0             ║
║   Gestión de tareas colaborativa     ║
╚══════════════════════════════════════╝

Escribe 'help' para ver los comandos. 'exit' para salir.

taskhub>
```

`taskhub>` es el **prompt**. Cada vez que pulsas Enter, se procesa lo escrito y vuelve a aparecer el prompt.

---

## 2. JLine3: por qué no usamos solo Scanner

`Scanner` lee líneas y poco más. **JLine3** convierte la terminal en algo profesional:

| Capacidad | Con `Scanner` | Con JLine3 |
|---|---|---|
| Leer una línea | Sí | Sí |
| Historial (flecha arriba/abajo) | No | Sí |
| Autocompletado con Tab | No | Sí |
| Edición de línea (mover cursor, borrar palabra) | No | Sí |
| Colores ANSI fiables (también en Windows) | Frágil | Sí |
| Ocultar contraseña al escribir | No | Sí |

Es la misma librería que usa Spring Shell por debajo, así que lo que aprendas aquí te sirve en Fase 6.

**Dependencia Maven:**
```xml
<dependency>
    <groupId>org.jline</groupId>
    <artifactId>jline</artifactId>
    <version>3.26.3</version>
</dependency>
```

---

## 3. Catálogo de comandos

### Comandos del sistema
| Comando | Qué hace |
|---|---|
| `help` | Lista todos los comandos con su descripción |
| `help <comando>` | Ayuda detallada de un comando |
| `clear` | Limpia la pantalla |
| `theme` | Gestiona la apariencia (ver sección 6) |
| `exit` / `quit` | Sale de la aplicación |

### Usuarios
| Comando | Qué hace |
|---|---|
| `create-user` | Abre un formulario para crear un usuario |
| `list-users` | Muestra todos los usuarios en tabla |
| `delete-user <id>` | Borra un usuario |

### Proyectos
| Comando | Qué hace |
|---|---|
| `create-project` | Formulario de nuevo proyecto |
| `list-projects` | Tabla de proyectos |
| `add-member <projectId>` | Añade un miembro a un proyecto |

### Tareas
| Comando | Qué hace |
|---|---|
| `create-task` | Formulario de nueva tarea |
| `list-tasks <projectId>` | Tareas de un proyecto |
| `assign-task <taskId>` | Asigna la tarea a un miembro |
| `set-status <taskId>` | Cambia el estado de una tarea |

> El catálogo crece fase a fase. Gracias al patrón Command (ver `ARQUITECTURA.md`), añadir uno nuevo es crear una clase y registrarla.

---

## 4. Formularios interactivos

Cuando un comando necesita varios datos, no se pasan como argumentos sueltos: se abre un **formulario** que pregunta campo a campo. Lo gestiona la clase `Prompter`.

```
taskhub> create-user

  Nuevo usuario
  ─────────────────────────────
  Username      › keanu
  Email         › keanu@mail.com
  Contraseña    › ********
  Rol [MEMBER]  › ADMIN
  ─────────────────────────────

  ✓ Usuario 'keanu' creado correctamente
    id: 550e8400-e29b-41d4-a716-446655440000
```

Características del `Prompter`:

- **Valores por defecto** entre corchetes: `Rol [MEMBER] ›` — si pulsas Enter sin escribir, usa MEMBER.
- **Validación en el sitio**: si el email ya existe o está vacío, te lo dice y vuelve a preguntar ese campo.
- **Campos ocultos**: la contraseña se muestra como `********`.
- **Selección de opciones**: para enums (Role, Status, Priority) ofrece las opciones válidas.

---

## 5. Salida formateada

### Tablas (`TablePrinter`)
```
taskhub> list-users

  ID         USERNAME    EMAIL              ROLE
  ─────────  ──────────  ─────────────────  ───────
  550e8400…  keanu       keanu@mail.com     ADMIN
  6a1f9c22…  marta       marta@mail.com     MEMBER

  2 usuarios
```

### Mensajes (`Output`)
Con colores según el tipo:
- `✓` verde para éxito
- `✗` rojo para error
- `!` amarillo para avisos
- `ℹ` azul para información

---

## 6. Apariencia y temas

> **Aclaración importante sobre la transparencia:** la opacidad/transparencia real de la ventana la controla el emulador de terminal del sistema (Windows Terminal, etc.), **no** la aplicación Java. Una app de consola no puede volver translúcida su propia ventana de forma portable. Lo que **sí** controlamos son los **colores y el formato**, y eso da mucho juego.

El comando `theme` cambia el esquema de colores de toda la salida:

```
taskhub> theme list
  • dark        (actual)
  • light
  • matrix      verdes sobre negro
  • solarized

taskhub> theme set matrix
  ✓ Tema cambiado a 'matrix'
```

Un `Theme` define los colores ANSI de cada elemento (prompt, éxito, error, cabecera de tabla, etc.). El `ThemeManager` guarda el tema activo y lo aplican `Output`, `TablePrinter` y `Prompter`. En una fase posterior, el tema elegido se puede **persistir** para que se recuerde entre sesiones.

También configurable con `set`:
```
taskhub> set prompt "kean@taskhub $ "
taskhub> set banner off
```

---

## 7. Cómo se añade un comando (escalabilidad)

1. Crea una clase que implemente `Command` en el subpaquete adecuado (`commands/user/`, etc.).
2. Implementa `name()`, `description()` y `execute()`.
3. Regístrala en el `CommandRegistry`.

No tocas el shell, ni el parser, ni los demás comandos. Ese es el objetivo del diseño: que crezca sin fricción.
