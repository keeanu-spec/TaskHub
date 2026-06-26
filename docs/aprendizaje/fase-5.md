# Fase 5 — Servicios y Procesos: Acceso Concurrente con H2 AUTO_SERVER

**Tema DAM:** Servicios y Procesos  
**Estado:** Implementada  
**Objetivo real:** Permitir que múltiples procesos JVM independientes accedan simultáneamente a la misma base de datos H2, aprovechando el servidor TCP interno que H2 levanta de forma transparente mediante la opción `AUTO_SERVER=TRUE`.

---

## Por qué esto importa

El módulo "Servicios y Procesos" de DAM tiene un objetivo pedagógico claro: entender cómo varios procesos pueden coordinarse y compartir estado. El mecanismo clásico para enseñarlo son los TCP sockets manuales (un proceso servidor, uno o varios clientes). Ese enfoque es correcto y pedagógicamente sólido.

Sin embargo, para un proyecto como TaskHub —cuya complejidad ya está en las capas de dominio, servicio y persistencia— añadir una capa de sockets manuales introduce overhead de mantenimiento que supera el beneficio educativo: hay que mantener dos entrypoints, un protocolo JSON propio, serialización/deserialización manual, gestión de hilos, y la CLI pierde acceso directo a los servicios.

La solución adoptada consigue el mismo objetivo del módulo con un cambio de una sola línea: activar `AUTO_SERVER=TRUE` en la URL JDBC de H2. Con esta opción, H2 levanta internamente su propio servidor TCP cuando el primer proceso abre la base de datos, y cualquier proceso adicional se conecta a ese servidor de forma transparente. El concepto de "servidor y comunicación entre procesos" sigue presente —está ocurriendo debajo— pero sin que el desarrollador tenga que implementarlo ni operarlo manualmente.

---

## Qué se implementó realmente

El único cambio de código en toda esta fase fue una modificación de una línea en `persistence.xml`.

**URL anterior (Fase 4):**
```
jdbc:h2:./data/taskhub_jpa;DB_CLOSE_DELAY=-1
```

**URL nueva (Fase 5):**
```
jdbc:h2:./data/taskhub_jpa;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1
```

Nada más cambió. La arquitectura CLI → Service → JPA → H2 de Fase 4 se mantiene intacta. No se crearon clases nuevas, no se modificó el `pom.xml`, no se añadieron dependencias.

---

## Archivo modificado

**`src/main/resources/META-INF/persistence.xml`**

```xml
<property name="jakarta.persistence.jdbc.url"
          value="jdbc:h2:./data/taskhub_jpa;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1"/>
```

Es la única línea relevante. El resto del archivo es idéntico al de Fase 4.

---

## Cómo funciona AUTO_SERVER=TRUE internamente

Cuando el primer proceso JVM abre la base de datos en modo archivo con `AUTO_SERVER=TRUE`, H2 detecta que nadie más la tiene abierta y la abre en modo embebido (el más eficiente). A la vez, levanta un servidor TCP en un puerto aleatorio y escribe ese puerto en un archivo de lock junto al archivo de base de datos.

Cuando un segundo proceso intenta abrir la misma base de datos, H2 lee el archivo de lock, descubre que ya hay un servidor TCP activo, y se conecta a él como cliente en lugar de abrir el archivo directamente. Desde ese momento, todos los accesos del segundo proceso van por red al primer proceso, que actúa de servidor.

Si el proceso servidor termina, el archivo de lock desaparece y el siguiente proceso que abra la base de datos se convierte en el nuevo servidor.

Todo esto ocurre dentro del driver H2, sin que el código de la aplicación sepa nada de ello. Hibernate y JPA ven simplemente un `DataSource` normal.

---

## Decisiones técnicas y por qué

| Decisión | Alternativa descartada | Justificación |
|---|---|---|
| `AUTO_SERVER=TRUE` en la URL JDBC | Implementar `ServerSocket` manual en puerto 8765 | Mismo resultado observable (múltiples procesos comparten datos), cero código adicional, sin protocolo propio que mantener |
| Mantener arquitectura CLI → Service → JPA | Separar en proceso servidor y proceso cliente ligero | La CLI conserva acceso directo a los servicios; la lógica de negocio no se fragmenta entre dos procesos |
| Sin cambios en `pom.xml` ni entrypoints | Añadir `ServerMain` y adaptar `Main` para sockets | No hay servidor que arrancar manualmente; cualquier terminal ejecuta `mvn exec:java` y funciona |
| `DB_CLOSE_DELAY=-1` mantenida junto a `AUTO_SERVER` | Solo una de las dos opciones | `AUTO_SERVER` gestiona el acceso multi-proceso; `DB_CLOSE_DELAY=-1` evita que H2 cierre la conexión cuando la última conexión del pool se libera temporalmente. Son ortogonales y compatibles |

---

## Pitfalls comunes con AUTO_SERVER=TRUE

**Puerto aleatorio en cada arranque.** H2 elige un puerto libre cada vez que el servidor arranca. Esto es transparente para los clientes H2, pero puede causar problemas con firewalls o en entornos donde el puerto debe ser fijo. Para forzar un puerto específico: `AUTO_SERVER_PORT=9090`.

**El archivo de lock no se borra si el proceso muere abruptamente.** Si la JVM se mata con `kill -9` o hay un crash, el archivo `taskhub_jpa.lock.db` puede quedar en el directorio `data/`. Al reiniciar, H2 intentará conectarse al servidor que ya no existe, fallará, y finalmente lo limpiará. Si la aplicación no arranca después de un crash, borrar manualmente los archivos `*.lock.db` del directorio `data/` resuelve el problema.

**Rendimiento inferior al modo embebido puro.** Cuando hay más de un proceso activo, todas las operaciones del proceso secundario pasan por TCP local. Para un proyecto educativo esto es irrelevante, pero es importante saberlo: `AUTO_SERVER=TRUE` no es una opción de producción para cargas altas.

**No funciona con H2 en memoria.** `AUTO_SERVER=TRUE` solo tiene sentido con bases de datos en archivo (`jdbc:h2:./ruta/...`). Una base de datos en memoria (`jdbc:h2:mem:...`) es privada al proceso que la crea por definición.

**Dos procesos deben apuntar exactamente a la misma ruta relativa.** La ruta `./data/taskhub_jpa` se resuelve desde el directorio de trabajo del proceso. Si dos terminales tienen distinto directorio de trabajo, cada una abrirá un archivo diferente y no compartirán datos. Al ejecutar con `mvn exec:java` desde la raíz del proyecto, ambas terminales resuelven la misma ruta.

---

## Cómo verificar que funciona

No hay servidor que arrancar manualmente. Ambas terminales ejecutan exactamente el mismo comando.

**Terminal 1:**
```
mvn exec:java
```
Ejecutar `User-add` y crear un usuario. El proceso queda en ejecución.

**Terminal 2** (con Terminal 1 todavía abierta):
```
mvn exec:java
```
Ejecutar `User-list`. La lista debe mostrar el usuario creado desde Terminal 1.

Si ambas terminales muestran los mismos datos, `AUTO_SERVER=TRUE` está funcionando: Terminal 1 es el servidor H2 interno, Terminal 2 se conecta a él por TCP de forma transparente.

**Verificación adicional:** cerrar Terminal 1 (el proceso servidor original) y ejecutar `User-list` en Terminal 2. Los datos deben seguir ahí: Terminal 2 se convierte en el nuevo servidor, lee el archivo H2, y sirve los datos persistidos.

---

## Relación con el currículo DAM

El objetivo del módulo "Servicios y Procesos" es comprender que los sistemas reales se componen de procesos independientes que se comunican. `AUTO_SERVER=TRUE` ilustra exactamente eso: sin escribir una línea de código de red, hay dos procesos JVM comunicándose por TCP para compartir estado persistente.

La diferencia con la implementación de sockets manual es de abstracción, no de concepto. En ambos casos hay un proceso que actúa de servidor y otros que actúan de clientes. La versión manual hace ese rol explícito en el código; `AUTO_SERVER=TRUE` lo delega al driver de base de datos. Para un proyecto cuyo valor pedagógico está en el dominio y la persistencia, esta abstracción es la correcta.
