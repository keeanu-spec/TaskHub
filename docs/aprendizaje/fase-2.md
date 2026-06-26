# Fase 2 — Persistencia en ficheros JSON

**Objetivo:** que los datos sobrevivan al cerrar el programa. Ahora mismo todo se pierde al salir — en esta fase se guarda en archivos `.json`.

---

## ¿Qué vas a aprender?

- Leer y escribir ficheros en Java con `java.nio`
- Serializar y deserializar objetos Java a JSON con **Gson**
- El patrón Repository aplicado a ficheros
- Por qué la arquitectura en capas hace que la CLI y los servicios no cambien nada

---

## ¿Qué vas a construir?

Un `JsonRepository<T>` que implementa la misma interfaz `Repository<T, ID>` que ya tienes. La diferencia es que en vez de guardar en un `HashMap` en memoria, guarda en un archivo `.json` en disco.

```
InMemoryRepository  →  guarda en HashMap (se pierde al cerrar)
JsonRepository      →  guarda en archivo .json (persiste)
```

Los servicios y la CLI no tocan nada — solo cambias qué repositorio se pasa en `Main`.

---

## Tecnologías nuevas

- **Gson** — librería de Google para convertir objetos Java a JSON y viceversa
- **java.nio** — API moderna de Java para leer y escribir ficheros (`Path`, `Files`)

---

## Pasos

### Paso 1 — Añadir Gson al pom.xml

```xml
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.11.0</version>
</dependency>
```

### Paso 2 — Crear `JsonRepository<T, ID>`

En `repository/json/`. Implementa `Repository<T, ID>` igual que `InMemoryRepository` pero:
- Tiene un campo `Path filePath` — la ruta del archivo donde guarda los datos
- Al arrancar carga los datos del fichero
- Cada vez que se hace `save` o `deleteById`, guarda todo el fichero

El reto: Gson necesita saber el tipo exacto de `T` para deserializar. Tendrás que investigar `TypeToken` de Gson.

### Paso 3 — Crear repositorios concretos JSON

Igual que hiciste con `memory/`, creas `UserJsonRepository`, `ProjectJsonRepository`, `TaskJsonRepository` en `repository/json/`.

### Paso 4 — Actualizar `Main`

Cambias los repositorios en memoria por los JSON. Solo tres líneas cambian en todo el proyecto.

### Paso 5 — Tests

Tests de integración que comprueben que los datos persisten — guardas, reinicias el repositorio y compruebas que siguen ahí.

---

## Hecho cuando

- Creas un usuario, cierras el programa, vuelves a abrir y el usuario sigue ahí
- `mvn test` pasa en verde

---

## Checklist

- [ ] Gson en pom.xml
- [ ] `JsonRepository<T, ID>` implementando `Repository`
- [ ] 3 repositorios concretos JSON
- [ ] `Main` usando los nuevos repositorios
- [ ] Tests en verde
