# TaskHub

**Gestor de tareas personal con CLI propia**, construido en Java.  
Sin suscripciones, sin cuentas, sin trackers. Tus datos, tu terminal.

---

## Qué es

TaskHub es una aplicación de gestión de tareas que vive en la terminal.  
La idea es simple: una herramienta que uso yo mismo todos los días, que hace exactamente lo que necesito y nada más.

```
┌─────────────────────────────────────────────────────┐
│    TaskHub — Viernes 26 de junio 2026               │
├─────────────────────────────────────────────────────┤
│  ✖ Vencidas              2  tarea(s)                │
│  ◆ Vencen hoy            1  tarea(s)                │
│  ▶ En progreso           3  tarea(s)                │
│  ▣ Proyectos activos     4                          │
└─────────────────────────────────────────────────────┘

TaskHub>
```

---

## Features

| Feature | Estado |
|---------|--------|
| CLI interactiva con comandos propios | ✅ |
| Dashboard de inicio con resumen del día | ✅ |
| Filtros por proyecto, prioridad, estado y fecha | ✅ |
| Exportar tareas de un proyecto a Markdown | ✅ |
| REST API con Spring Boot + JWT | ✅ |
| Notas por tarea | 🔨 En desarrollo |
| Time tracking (start/stop por tarea) | 🔨 En desarrollo |
| Estadísticas semanales | 🔨 En desarrollo |
| Tareas recurrentes | 📋 Planificado |
| TUI visual con paneles navegables | 📋 Planificado |

---

## Stack

- **Java 23**
- **Maven**
- **JLine 3** — terminal interactiva
- **Spring Boot 3.3.5** — REST API
- **Spring Security + JWT** — autenticación
- **Hibernate 6 / JPA** — ORM
- **H2** — base de datos local (file-based, sin servidor externo)

---

## Arrancar

### CLI (uso personal)
```powershell
mvn exec:java
```

### REST API
```powershell
mvn spring-boot:run
# → http://localhost:8080/swagger-ui.html
```

---

## Comandos disponibles

| Comando | Qué hace |
|---------|----------|
| `Help` | Lista todos los comandos |
| `User-add` | Crear usuario |
| `User-List` | Listar usuarios |
| `Task-Filter` | Filtrar tareas por proyecto / prioridad / estado / fecha |
| `Export-Project` | Exportar tareas de un proyecto a `.md` |
| `Exit` | Salir |

---

## Estructura del proyecto

```
src/main/java/org/example/
├── domain/          # Entidades: User, Project, Task, Status, Priority
├── repository/      # Puertos + implementaciones JPA y en memoria
├── service/         # Lógica de negocio
├── cli/             # Shell, comandos, dashboard, exportador
├── controllers/     # REST API (Spring Boot)
├── security/        # JWT, filtros, configuración Spring Security
└── dto/             # Request/Response para la API
```

---

## Arquitectura

El proyecto usa **Ports & Adapters** para que la CLI y la API REST compartan los mismos servicios sin conflicto:

- Los servicios dependen de interfaces puerto (`UserRepositoryPort`, etc.)
- La CLI usa implementaciones manuales JPA (`JpaUserRepository`)
- La API usa Spring Data JPA (`UserRepository extends JpaRepository`)
- Ambas funcionan en paralelo sobre la misma base de datos H2

---

## Datos

Los datos se guardan en `data/taskhub_jpa.mv.db` — un archivo local en tu máquina.  
Con `H2 AUTO_SERVER=TRUE`, múltiples procesos pueden conectarse simultáneamente.
