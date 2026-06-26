# CLIs Personales — Investigación de Proyectos Reales

> Investigación sobre developers que construyeron sus propias CLIs para uso personal,
> qué características tienen y qué los hace útiles.

---

## ¿Por qué los developers construyen sus propias CLIs?

La razón más común es siempre la misma: **las apps existentes hacen demasiado o demasiado poco**.
Un developer quiere exactamente lo que necesita, sin suscripciones, sin trackers,
sin interfaces que no controla. La terminal es suya.

---

## Proyectos Reales

### 1. Taskwarrior
**Autor:** Paul Beckingham & equipo | **GitHub:** [GothenburgBitFactory/taskwarrior](https://github.com/GothenburgBitFactory/taskwarrior) | **Estrellas:** ~5k

Empezó como herramienta personal de gestión de tareas en la terminal.
Hoy es el gestor de tareas CLI más conocido del mundo.

**Qué tiene:**
- Tareas con prioridad, fecha de vencimiento, etiquetas, proyectos
- Filtros potentes (`task project:trabajo priority:H`)
- Reportes y estadísticas desde la terminal
- Sincronización con servidor propio (TaskServer)
- Extensible con hooks en cualquier lenguaje

**Lección:** Empezó siendo un simple todo.txt y creció porque el autor lo usaba todos los días.

---

### 2. Lazygit
**Autor:** Jesse Duffield | **GitHub:** [jesseduffield/lazygit](https://github.com/jesseduffield/lazygit) | **Estrellas:** ~55k

Jesse estaba harto de escribir comandos git largos. Lo construyó para él.
Hoy es uno de los proyectos open source más populares del mundo.

**Qué tiene:**
- TUI (interfaz visual en la terminal) para git
- Stage/commit/push/pull con una tecla
- Ver diffs, ramas, stash, todo sin salir de la terminal
- Keybindings completamente configurables

**Lección:** "I built this because I was tired of typing the same git commands over and over."

---

### 3. jrnl
**Autor:** Manuel Ebert | **GitHub:** [jrnl-org/jrnl](https://github.com/jrnl-org/jrnl) | **Estrellas:** ~6k

Quería llevar un diario personal desde la terminal, sin apps de terceros.

**Qué tiene:**
- Escribir entradas con `jrnl Hoy aprendí X`
- Buscar entradas por fecha, etiqueta o texto
- Exportar a Markdown, JSON, texto plano
- Cifrado opcional de entradas
- Múltiples diarios (trabajo, personal, ideas)

**Lección:** Una sola funcionalidad bien hecha. No intenta ser más que un diario.

---

### 4. WTFutil
**Autor:** Chris Cummer | **GitHub:** [wtfutil/wtf](https://github.com/wtfutil/wtf) | **Estrellas:** ~15k

Quería un dashboard personal en la terminal que le mostrara todo lo importante al abrir el día.

**Qué tiene:**
- Dashboard modular con widgets: calendario, GitHub PRs, Jira, tiempo, noticias, crypto...
- Cada widget es configurable en YAML
- Actualización automática en tiempo real
- Más de 40 integraciones

**Lección:** La idea no era nueva, pero nadie había hecho un dashboard terminal *configurable* así.

---

### 5. navi
**Autor:** Denilson Sá Meira | **GitHub:** [denilsonsa/navi](https://github.com/denilsonsa/navi) | **Estrellas:** ~15k  
*(fork mantenido: [nickel-lang/navi](https://github.com/nickel-lang/navi))*

Se olvidaba de comandos que usaba poco. Quería un cheatsheet interactivo en la terminal.

**Qué tiene:**
- Buscar comandos con descripción mientras escribes
- Cheatsheets en archivos `.cheat` propios o de la comunidad
- Integración con fzf para búsqueda fuzzy
- Variables interactivas: te pregunta los parámetros antes de ejecutar

**Lección:** Resolver un problema concreto de memoria/productividad diaria.

---

### 6. tealdeer (tldr)
**Autor:** Comunidad / dbrgn | **GitHub:** [dbrgn/tealdeer](https://github.com/dbrgn/tealdeer) | **Estrellas:** ~4k

`man` pages son lentas e ilegibles. Querían ejemplos prácticos de comandos en segundos.

**Qué tiene:**
- `tldr git commit` → ejemplos de uso en 3 líneas
- Base de datos local (sin internet)
- Actualización de páginas con un comando
- 3000+ comandos cubiertos

**Lección:** Reimaginar una herramienta existente (`man`) para usarla de verdad.

---

### 7. calcurse
**Autor:** Frederic Culot | **GitHub:** [lfos/calcurse](https://github.com/lfos/calcurse) | **Estrellas:** ~1.5k

Quería gestionar su calendario y tareas sin salir de la terminal ni depender de Google.

**Qué tiene:**
- Calendario visual en la terminal
- Gestión de eventos, citas y to-dos
- Sincronización con CalDAV (Google Calendar, Nextcloud)
- Exportación a iCalendar

**Lección:** Privacidad + terminal + calendario. Un nicho específico con usuarios fieles.

---

### 8. Brittany Ellich — Personal Org CLI
**Autora:** Staff Engineer en GitHub | **Referencia:** [GitHub Blog](https://github.blog/ai-and-ml/github-copilot/build-a-personal-organization-command-center-with-github-copilot-cli/)

Construyó su propio centro de organización personal en CLI porque tenía la información
dispersa en doce apps distintas y quería unificarlo todo.

**Qué tiene:**
- Integración con GitHub (PRs, issues pendientes)
- Notas rápidas desde terminal
- Agenda del día
- Todo en un solo comando al abrir la terminal

**Lección:** No necesitas miles de estrellas para que una herramienta sea valiosa.
Si la usas tú todos los días, ya merece la pena construirla.

---

### 9. todo.txt CLI
**Autor:** Gina Trapani | **GitHub:** [todotxt/todo.txt-cli](https://github.com/todotxt/todo.txt-cli) | **Estrellas:** ~5k

La fundadora de Lifehacker quería gestionar tareas en texto plano, portable, sin depender de ningún servicio.

**Qué tiene:**
- Formato de archivo `.txt` legible por humanos
- Prioridades, proyectos (`+proyecto`), contextos (`@trabajo`)
- Scriptable y extensible con add-ons de la comunidad
- Funciona sin internet, sin cuenta, en cualquier máquina

**Lección:** La simplicidad extrema tiene su propia audiencia. 20 años después, aún tiene usuarios activos.

---

### 10. Super Productivity (CLI/TUI)
**Autor:** Johannes Millan | **GitHub:** [johannesjo/super-productivity](https://github.com/johannesjo/super-productivity) | **Estrellas:** ~12k

App de productividad personal enfocada en deep work. Tiene interfaz desktop pero con raíces en el concepto "una sola herramienta para todo".

**Qué tiene:**
- Gestión de tareas con estimación de tiempo
- Pomodoro timer integrado
- Hábitos y tareas recurrentes
- Integración con Jira, GitHub, GitLab
- Sin cuenta, datos locales

---

## Patrones comunes — qué tienen todos estos proyectos

| Patrón | Descripción |
|--------|-------------|
| **Problema personal real** | El autor lo necesitaba para sí mismo, no para venderlo |
| **Sin dependencias externas** | Datos locales, sin cuentas, sin suscripciones |
| **Una cosa bien hecha** | No intentan reemplazar 10 apps, resuelven un problema concreto |
| **Configurables** | El developer puede adaptarlo a su flujo |
| **Texto plano o formatos abiertos** | Los datos son del usuario, no del software |
| **Crecieron por uso real** | Lazygit, Taskwarrior — empezaron siendo privados |

---

## Qué podrías añadir a TaskHub

Viendo estos proyectos, algunas ideas concretas para hacer TaskHub más útil para uso propio:

- **Notas rápidas por tarea** — como `jrnl` pero ligadas a tareas/proyectos
- **Time tracking** — registrar cuánto tiempo llevas en cada tarea (como `zeit`)
- **Dashboard de inicio** — resumen del día al arrancar la CLI (como `wtfutil`)
- **Tareas recurrentes** — hábitos, revisiones semanales
- **Exportar a Markdown** — para llevar las tareas a Obsidian u otros
- **Prioridades visuales** — colores en terminal según urgencia/importancia
- **Búsqueda fuzzy** — encontrar tareas/proyectos escribiendo parte del nombre

---

## Recursos

- [Terminal Trove — directorio completo de CLIs](https://terminaltrove.com)
- [awesome-cli-apps (GitHub)](https://github.com/agarrharr/awesome-cli-apps)
- [Taskwarrior](https://taskwarrior.org)
- [jrnl](https://jrnl.sh)
- [WTFutil](https://wtfutil.com)
- [Lazygit](https://github.com/jesseduffield/lazygit)
- [navi](https://github.com/denilsonsa/navi)
