# TaskHub — Plan de Implementación: 8 Features

> Estado actual del proyecto analizado el 2026-06-26  
> Base: Java 23, Spring Boot 3.3.5, JLine 3.26.3, H2 + JPA, ANSI colors ya disponibles

---

## Resumen del plan

| # | Feature | Fase | Nuevas entidades | Complejidad |
|---|---------|------|-----------------|-------------|
| 1 | Filtros potentes | A | Ninguna | Baja |
| 2 | Dashboard de inicio | A | Ninguna | Baja |
| 3 | Exportar a Markdown | A | Ninguna | Baja |
| 4 | Notas por tarea | B | `TaskNote` (reutiliza Comment.java) | Media |
| 5 | Time tracking | B | `TimeEntry` | Media |
| 6 | Estadísticas | B | Ninguna (usa TimeEntry) | Media |
| 7 | Tareas recurrentes | C | Campo `recurrence` en Task | Media-Alta |
| 8 | TUI visual con paneles | C | Ninguna (UI layer) | Alta |

---

## Fase A — Quick Wins (sin nuevas entidades)

### Feature 1: Filtros potentes

**Qué hace:** `task-list project:web priority:HIGH due:today status:TODO`  
Encontrar cualquier tarea en segundos sin recordar IDs.

**Cambios técnicos:**
- `TaskRepositoryPort` + `JpaTaskRepository`: nuevo método `findByFilters(filters)`  
- `TaskRepository` (Spring Data): `@Query` con JPQL dinámico  
- Nuevo comando `Task-Filter` en `cli/commands/task/`  
- `FilterCommand` parsea argumentos `clave:valor`

**Nuevos comandos CLI:**
```
task-filter project:<nombre> priority:<HIGH|MEDIUM|LOW> status:<TODO|IN_PROGRESS|DONE> due:<today|week>
```

---

### Feature 2: Dashboard de inicio

**Qué hace:** Al ejecutar `taskhub` sin argumentos, muestra el estado del día antes del prompt.

**Aspecto en terminal:**
```
╔══════════════════════════════════════╗
║  TaskHub — Viernes 26 Jun            ║
╠══════════════════════════════════════╣
║  🔴 Vencidas hoy     2 tareas        ║
║  🟡 Alta prioridad   5 tareas        ║
║  🟢 En progreso      3 tareas        ║
║  📁 Proyectos activos  4             ║
╚══════════════════════════════════════╝
```

**Cambios técnicos:**
- Nuevo `DashboardRenderer` en `cli/`  
- Se llama desde `TaskHubShell.run()` antes del loop  
- Usa servicios existentes con filtros de fecha  
- `Output.java`: añadir método `box(lines)` para el marco

---

### Feature 3: Exportar a Markdown

**Qué hace:** `task-export project:web` → genera `web-tasks.md` en el directorio actual.

**Formato de salida:**
```markdown
# Proyecto: Web
_Exportado: 2026-06-26_

## TODO
- [ ] Fix bug login  `HIGH` vence: 2026-06-30
- [ ] Añadir tests    `MEDIUM`

## IN_PROGRESS
- [~] Refactor auth   `HIGH`

## DONE
- [x] Setup CI/CD
```

**Cambios técnicos:**
- Nuevo `MarkdownExporter` en `cli/export/`  
- Nuevo comando `Export-Project`  
- Sin nuevas dependencias (solo `java.nio.file`)

---

## Fase B — Nuevas entidades

### Feature 4: Notas por tarea

**Qué hace:** Añadir contexto, decisiones o progreso a cualquier tarea.

```
task-note add 3 "Revisé el bug, el problema está en AuthFilter línea 42"
task-note list 3
```

**Reutiliza:** `Comment.java` ya existe — solo le añadimos `@Entity` y la mappamos.

**Cambios técnicos:**
- `domain/TaskNote.java` (renombrar/refactorizar `Comment.java`, añadir `@Entity @Table`)  
- `repository/TaskNoteRepository.java` (Spring Data)  
- `repository/jpa/JpaTaskNoteRepository.java` (para CLI)  
- `service/TaskNoteService.java`  
- Nuevos comandos: `Task-Note-Add`, `Task-Note-List`

**Schema nuevo:**
```sql
CREATE TABLE task_notes (
  id UUID PRIMARY KEY,
  task_id UUID NOT NULL REFERENCES tasks(id),
  content VARCHAR(1000),
  created_at TIMESTAMP
);
```

---

### Feature 5: Time Tracking

**Qué hace:** Registrar inicio y fin de trabajo en una tarea. Reporte de horas.

```
task-start 3          → Timer iniciado en "Fix bug login" [14:32]
task-stop 3           → Parado. Tiempo registrado: 1h 23m
task-time 3           → Total invertido: 3h 45m (3 sesiones)
```

**Cambios técnicos:**
- `domain/TimeEntry.java`: nueva entidad  
- `repository/TimeEntryRepository.java` (Spring Data)  
- `repository/jpa/JpaTimeEntryRepository.java` (para CLI)  
- `service/TimeTrackingService.java`  
- Estado de timer activo: guardado en `data/timer.json` (simple, no en BD)  
- Nuevos comandos: `Task-Start`, `Task-Stop`, `Task-Time`

**Schema nuevo:**
```sql
CREATE TABLE time_entries (
  id UUID PRIMARY KEY,
  task_id UUID NOT NULL REFERENCES tasks(id),
  started_at TIMESTAMP NOT NULL,
  ended_at TIMESTAMP,           -- NULL si el timer está activo
  duration_minutes INTEGER      -- calculado al parar
);
```

---

### Feature 6: Estadísticas

**Qué hace:** Ver métricas de productividad sin salir de la terminal.

```
stats week            → Resumen de esta semana
stats project:web     → Métricas del proyecto web
stats tasks           → Top tareas por tiempo invertido
```

**Salida ejemplo:**
```
Esta semana (23–26 Jun)
  Tareas completadas:   8
  Tiempo total:        14h 30m
  Proyecto más activo: web (8h)
  Racha actual:        4 días
```

**Cambios técnicos:**
- `service/StatsService.java`: queries sobre `TimeEntry` + `Task`  
- Nuevo comando `Stats` con subcomandos  
- Sin nuevas entidades

---

## Fase C — Features avanzados

### Feature 7: Tareas recurrentes

**Qué hace:** Definir tareas que se autogeneran periódicamente.

```
task-recurring add "Revisar objetivos" --every WEEKLY --project personal
task-recurring list
```

**Cambios técnicos:**
- Nuevo campo en `Task`: `recurrenceRule` (String, nullable) — formato: `DAILY`, `WEEKLY`, `MONTHLY`  
- Nuevo campo: `lastGeneratedAt` (LocalDateTime)  
- `RecurringTaskService`: al arrancar la CLI, comprueba y genera las pendientes  
- Se llama desde `TaskHubShell.run()` antes del dashboard

**Lógica:**  
Al iniciar TaskHub → para cada tarea recurrente, si `now > lastGeneratedAt + period` → crea copia nueva con status TODO.

---

### Feature 8: TUI visual con paneles

**Qué hace:** Interfaz navegable con teclado. Tres paneles: Proyectos | Tareas | Detalle.

```
┌─ Proyectos ──┬─ Tareas (web) ──────────────┬─ Detalle ───────────────┐
│ > web        │ > [HIGH] Fix bug login  TODO │ Fix bug login           │
│   personal   │   [MED]  Añadir tests   TODO │ Prioridad: HIGH         │
│   trabajo    │   [HIGH] Refactor auth  WIP  │ Vence: 2026-06-30       │
│              │                             │ Tiempo: 3h 45m          │
│              │                             │ Notas: 2                │
└──────────────┴─────────────────────────────┴─────────────────────────┘
[↑↓] navegar  [Tab] cambiar panel  [n] nota  [s] start  [Enter] detalle  [q] salir
```

**Nueva dependencia:**
```xml
<dependency>
    <groupId>com.googlecode.lanterna</groupId>
    <artifactId>lanterna</artifactId>
    <version>3.1.2</version>
</dependency>
```

**Cambios técnicos:**
- Nuevo paquete `cli/tui/`  
- `TuiApp.java`: entry point del modo TUI  
- `ProjectPanel.java`, `TaskPanel.java`, `DetailPanel.java`  
- Nuevo comando `tui` en el shell para entrar al modo visual  
- El shell normal sigue funcionando igual

---

## Orden de implementación

```
Fase A (esta semana)
  └─ 1. Filtros         → base para todo lo demás
  └─ 2. Dashboard       → valor inmediato al abrir
  └─ 3. Export MD       → fácil, cierra la fase

Fase B (siguiente)
  └─ 4. Notas           → reutiliza Comment.java
  └─ 5. Time tracking   → nueva entidad simple
  └─ 6. Estadísticas    → usa datos de time tracking

Fase C (después)
  └─ 7. Recurrentes     → lógica de scheduling
  └─ 8. TUI             → refactor visual completo
```

---

## Nuevas dependencias

| Dependencia | Para qué | Ya en proyecto |
|-------------|---------|----------------|
| `lanterna 3.1.2` | TUI visual (Fase C) | ❌ Añadir en Fase C |
| Todo lo demás | — | ✅ Ya disponible |

Solo una dependencia nueva en todo el plan. El resto usa lo que ya hay.

---

## Archivos nuevos estimados

```
Fase A:  ~6 archivos  (DashboardRenderer, MarkdownExporter, FilterCommand, ...)
Fase B:  ~12 archivos (TaskNote, TimeEntry, services, repos, comandos)
Fase C:  ~8 archivos  (RecurringTask logic, TUI panels)
Total:   ~26 archivos nuevos
```
