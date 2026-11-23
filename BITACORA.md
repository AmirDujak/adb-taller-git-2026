# Bitácora — Taller Git 2026 (LP3)

**Alumno:** Amir Dujak (GitHub: `AmirDujak`)
**Repositorio:** https://github.com/AmirDujak/adb-taller-git-2026
**Dominio elegido:** Minecraft (opción A: endurecer el modelo)
**Guía de referencia:** https://github.com/alefq/afq-taller-git-2024 (`docs/TALLER_GIT.md`)

## Objetivo

Publicar un servicio HTTP propio (Spring Boot, API REST) que versione el modelado de clases
de Minecraft hecho en clase (POO-03), aplicando herencia, sobreescritura y ocultamiento de la información.

---

## Entrada 1 — Creación del repositorio (consigna 1)

**Fecha:** 2026-09-16

- Creé en GitHub el repositorio público `adb-taller-git-2026` (iniciales ADB) y lo cloné localmente.
- Agregué la licencia **Apache License 2.0** (`LICENSE`) y un `README.md` con los integrantes y el
  comando para ejecutar el proyecto.

```bash
git clone https://github.com/AmirDujak/adb-taller-git-2026.git
cd adb-taller-git-2026
```

**Commits:**
- `fdcd750` — chore: added LICENSE
- `296b251` — Add project contributors and run command to README

---

## Entrada 2 — Proyecto Spring Boot (consigna 2)

**Fecha:** 2026-09-16

- Generé el proyecto en **start.spring.io** con esta configuración:
  - Build: **Maven**
  - Lenguaje: **Java 21**
  - Spring Boot 4.1.1
  - Group `py.edu.uc.lp3.adb`, artifact `adb-taller-git-2026`
  - Dependencias: **Spring Web**, Spring Data JPA y H2
- start.spring.io cambió el nombre del paquete a `py.edu.uc.lp3.adb.adb_taller_git_2026`, porque un paquete
  de Java no puede tener guiones.
- Verifiqué que la aplicación levanta:

```bash
./mvnw spring-boot:run
```

**Commit:** `831f470` — chore: esqueleto Spring Boot API REST desde start.spring.io

---

## Entrada 3 — Clases del modelado (consigna 3)

**Fecha:** 2026-09-17

- Copié las clases del diagrama de POO-03 al proyecto, en el paquete `minecraft`:
  - Interfaz `Movimientos`
  - Clase base abstracta `Entidad`
  - Ramas abstractas `Hostiles` y `NoHostiles`
  - Mobs concretos `Creeper`, `Zombie`, `Esqueleto`, `Lobo`, `Oveja` y `Aldeano`
  - Clase auxiliar `Hitbox`
- Verifiqué que compilan con Maven.
- Practicamos el trabajo colaborativo con mi compañero (`Cemelele`), que agregó el archivo `walter.txt`.
  Después hice `pull` de su cambio, modifiqué el archivo y volví a hacer `push`.

```bash
git add .
git commit -m "agregado clases.java"
git push origin main
```

**Commits:**
- `4b5ec38` — agregado walter.txt (Cemelele)
- `e4d0799` — agregado clases.java
- `11ed4ae` — modified walter.txt

---

## Entrada 4 — Controllers REST (consigna 4)

**Fecha:** 2026-09-25

Creé el paquete `py.edu.uc.lp3.adb.adb_taller_git_2026.controller` con dos controllers:

1. **`IndexController`**: responde en `GET /` con un mensaje de bienvenida y ejemplos de uso.
2. **`EntidadController`**: construye una instancia del dominio con los parámetros de la URL.
   - `{tipo}` es una **variable de ruta** y define qué clase se instancia (`creeper`, `zombie`,
     `esqueleto`, `lobo`, `oveja`, `aldeano`).
   - `?profesion=` es un **parámetro de consulta** que se pasa al constructor de `Aldeano`.
   - `?danio=` es un parámetro opcional que le aplica daño a la entidad recién creada.
   - Si el tipo no existe, responde **404**.

**Prueba:**

```bash
curl http://localhost:8080/
curl "http://localhost:8080/entidad/aldeano?profesion=Herrero"
```

```json
{"entidad":{"profesion":"Herrero","nombre":"Aldeano","vida":20,"viva":true,
 "hitbox":{"ancho":0.6,"alto":1.95,"profundidad":0.6}, "...": "..."},
 "avanzar":"Aldeano avanza a velocidad 0.5",
 "comportamiento":"Aldeano (Herrero) abre su menú de comercio.", "...": "..."}
```

---

## Entrada 5 — Método abstracto y endurecimiento del modelo (consigna 5, opción A)

**Fecha:** 2026-09-25

### Problemas encontrados en el modelo original

- Los métodos abstractos estaban en `Hostiles` y `NoHostiles`, no en la clase base `Entidad`.
  Por eso no se podía pedirle su comportamiento a *cualquier* entidad.
- El comportamiento se imprimía con `System.out.println`, así que no se podía devolver como JSON.
- Había campos que se podían modificar y no tenían por qué: `velocidadMovimiento`, `rangoDeAggro`
  y las medidas de `Hitbox` no eran `final`.
- No había validaciones: se podía crear una entidad con vida negativa o aplicarle daño negativo,
  y una entidad muerta podía seguir moviéndose.
- El Creeper no moría al explotar, así que su comportamiento no se diferenciaba del de los demás.
- `Hitbox.colisionaCon()` siempre devolvía `true` y no se usaba.

### Cambios realizados

| Regla de la opción A | Cómo se resolvió |
|---|---|
| Tratar cualquier entidad de forma uniforme | `Entidad` declara `public abstract String comportamiento()`. El controller solo usa el tipo `Entidad` para `avanzar()`, `saltar()`, `comportamiento()` y `recibirDanio()` |
| La clase padre concentra lo común | Las reglas de vida y muerte (`recibirDanio`, `desaparecer`, `isViva`) y de movimiento viven en `Entidad` y son `final`. Una entidad muerta no puede moverse |
| Cada subclase redefine solo lo que cambia | `Hostiles` implementa `comportamiento()` llamando a `accionDeAtaque()`. `NoHostiles` lo implementa llamando a `interactuar()`. Cada mob redefine solo esa parte. El `Creeper` además avanza en silencio (`describirAvance()`) y desaparece al explotar |
| Nadie puede dejar una entidad en un estado imposible | Todos los campos son `private`, no hay setters y las listas son inmutables (`List.copyOf`). Los constructores validan vida > 0, velocidad ≥ 0, hitbox positiva, rango de aggro > 0 y profesión no vacía. `recibirDanio()` rechaza valores negativos |
| Justificar los campos `protected` | **No hay campos `protected`.** Solo hay dos métodos `protected`: `desaparecer()`, porque el Creeper muere sin recibir daño, y `describirAvance()`, para que el Creeper avance distinto. Con un método alcanza, así que no hace falta exponer campos |

- Los métodos devuelven `String` en vez de imprimir, para que el controller pueda armar el JSON.
- Eliminé `Hitbox.colisionaCon()` porque no funcionaba (siempre devolvía `true`) y no se usaba.
- Agregué `GET /entidades`, que recorre todas las entidades como `Entidad` y muestra el comportamiento de cada una.
- Los errores de validación del dominio se convierten en una respuesta **400** con un `@ExceptionHandler`.

### Pruebas

```bash
curl http://localhost:8080/entidades
```

| Entidad | `comportamiento` | ¿Viva después? |
|---|---|---|
| Creeper | Creeper explota. Creeper ha desaparecido y suelta: [polvora] | no |
| Zombie | Zombie golpea cuerpo a cuerpo al jugador. | sí |
| Esqueleto | Esqueleto dispara una flecha a distancia. | sí |
| Lobo | Lobo fue domesticado con huesos. | sí |
| Oveja | Oveja fue esquilada, se obtiene lana. | sí |
| Aldeano | Aldeano (Granjero) abre su menú de comercio. | sí |

```bash
curl "http://localhost:8080/entidad/zombie?danio=25"
# "recibirDanio": "Zombie recibe 25 de daño y hace el sonido: gruñido. Zombie ha desaparecido y suelta: [carne_podrida]"

curl "http://localhost:8080/entidad/zombie?danio=-3"
# 400 {"error":"El daño no puede ser negativo"}

curl "http://localhost:8080/entidad/aldeano?profesion=%20"
# 400 {"error":"El aldeano necesita una profesión"}
```

### Inconveniente encontrado

En una prueba, el servidor respondió 500 con el error `Unresolved compilation problems`. La causa era
que el IDE había compilado en `target/` una versión vieja de las clases. Se resolvió con una
compilación limpia:

```bash
./mvnw clean package
```

---

## Entrada 6 — Diagrama Mermaid en el README (consigna 6)

**Fecha:** 2026-09-25

- Agregué al `README.md` un diagrama de clases en **Mermaid**. GitHub lo muestra como imagen.
  Incluye:
  - La interfaz `Movimientos`
  - La composición con `Hitbox`
  - La jerarquía `Entidad` → `Hostiles` / `NoHostiles` → mobs concretos
  - La visibilidad de cada miembro (`-` privado, `#` protegido, `+` público; `*` indica los métodos abstractos)
- También agregué la tabla de endpoints y un resumen de las decisiones de diseño.

**Commit de las entradas 4, 5 y 6:** `8475407` — agregado de las consignas restantes (integrado a `main`
con el PR #2, ver entrada 7).

---

## Entrada 7 — Trabajo colaborativo con Pull Requests

**Fecha:** 2026-09-25

En la entrada 3 mi compañero pusheó directo a `main`. Para que ningún cambio llegue a `main` sin
revisión, adoptamos un flujo con **ramas y Pull Requests**: cada uno trabaja en una rama propia, abre
un PR y el otro lo revisa, lo aprueba y hace el merge.

### Configuración del repositorio (dueño)

1. Agregué a mi compañero como colaborador en **Settings → Collaborators → Add people**.
2. Protegí la rama `main` en **Settings → Branches → Add branch ruleset** con estas reglas:
   - *Require a pull request before merging*
   - *Require approvals: 1*

   Con esto nadie puede hacer push directo a `main`, y un PR no se puede mezclar sin aprobación.

### Flujo de quien propone el cambio

```bash
git checkout main
git pull origin main                      # traer lo último de main
git checkout -b feature/nombre-del-cambio # trabajar en una rama propia

# ... cambios ...
git add .
git commit -m "descripción del cambio"
git push -u origin feature/nombre-del-cambio
```

Después, en GitHub, abre el PR con **Compare & pull request** y asigna al otro como *Reviewer*.

### Flujo de quien revisa

1. En **Pull requests → Files changed** reviso las diferencias y comento las líneas que haga falta.
2. En **Review changes** elijo una opción:
   - **Approve** para aprobar el PR.
   - **Request changes** para pedir correcciones. Quien abrió el PR sube nuevos commits a la misma
     rama y el PR se actualiza solo.
3. Una vez aprobado, hago el merge con **Merge pull request → Confirm merge** y borro la rama.
4. Actualizo la copia local:

```bash
git checkout main
git pull origin main
```

### Pull Requests realizados

| PR | Rama | Commit | Autor del cambio | Mergeado por |
|---|---|---|---|---|
| #1 | `feature/agregado-archivo-de-texto-prueba` | `4cf54ba` agregado archivo de texto prueba.txt | Cemelele (compañero) | Amir Dujak → merge `54845eb` |
| #2 | `feature/agregado-consignas-restantes` | `8475407` agregado de las consignas restantes (consignas 4, 5 y 6) | AmirDujak | Matías Duarte → merge `c041325` |

Probamos el flujo en los dos sentidos. En el PR #1 mi compañero propuso un cambio y yo lo integré.
En el PR #2 yo subí las consignas 4 a 6 y él las revisó e integró a `main`.

Historial resultante (`git log --oneline --graph`):

```
*   c041325 Merge pull request #2 from AmirDujak/feature/agregado-consignas-restantes
|\
| * 8475407 agregado de las consignas restantes
* |   54845eb Merge pull request #1 from AmirDujak/feature/agregado-archivo-de-texto-prueba
|\ \
| |/
|/|
| * 4cf54ba agregado archivo de texto prueba.txt
|/
* 11ed4ae modified walter.txt
```

### Resolución de conflictos

Si GitHub indica *"This branch has conflicts"*, quien abrió el PR trae `main` a su rama, resuelve
los conflictos y vuelve a subir:

```bash
git checkout feature/nombre-del-cambio
git pull origin main
# resolver los archivos marcados con <<<<<<< ======= >>>>>>>
git add .
git commit
git push
```

### Aprendizajes

- `git pull` baja cambios del remoto. Un **Pull Request** es una solicitud en GitHub para revisar y
  mezclar una rama; son cosas distintas.
- El autor de un PR no puede aprobar el suyo. La revisión siempre la hace otra persona.
- Trabajar en ramas mantiene `main` estable y deja registrado en GitHub quién propuso, revisó y
  aprobó cada cambio.

---

## Estado final

| # | Consigna | Estado |
|---|---|---|
| 1 | Repositorio público con README y Apache 2.0 | ✅ |
| 2 | Spring Boot (Maven, Java 21, Spring Web) | ✅ |
| 3 | Clases del modelado, compilan, add/commit/push | ✅ |
| 4 | `IndexController` y controller con parámetros de URL | ✅ |
| 5 | Método abstracto en la base, dos hijas, JSON por tipo padre | ✅ |
| 6 | Diagrama Mermaid en el README | ✅ |

## Conclusiones

- Poner el método abstracto en la clase base hace que el código cliente (el controller) no dependa de
  los tipos concretos. Si se agrega un mob nuevo, basta con crear la subclase y sumarla al `switch`
  de creación. No hay que cambiar nada más.
- Declarar los métodos comunes como `final` impide que una subclase rompa las reglas de vida y muerte
  por accidente. Los puntos de extensión `protected` indican exactamente qué se puede redefinir.
- Mantener los campos privados y validar en los constructores garantiza que ninguna entidad pueda
  quedar en un estado imposible.
- Con Git cada paso del taller quedó registrado en el historial, y pudimos integrar los cambios de un
   compañero sin conflictos.

---

## Entrada 8 — POO-08: persistencia, simulación y entrega individual

**Fecha:** 2026-11-09

**Marca del agente:** OpenCode. **Proveedor:** OpenAI.
**Modelo exacto informado por la herramienta:** `openai/gpt-6.1-sol` (GPT-6.1 Sol).

### Resumen de prompts y alcance de la asistencia

- Se proporcionó el enunciado POO-08, la rúbrica y el template de paquetes, solicitando
  CRUD H2, tres clases persistidas, dos reglas, excepciones, interfaz y entregables.
- Se pidió primero revisar el repositorio y proponer un plan; el agente contrastó
  código y documentación sin modificar archivos durante la planificación.
- Luego se indicó «Hazlo»: implementar el plan conservando Minecraft, las rutas
  existentes y los datos H2 anteriores, y completar pruebas y documentación.

### Cambios asistidos

- Unificación de la representación persistente con la jerarquía real de `Entidad`;
  Creeper, Player y Zombie se recuperan como sus clases concretas, no como registros paralelos.
- Validaciones dentro del dominio, constructores y callbacks JPA; colecciones encapsuladas.
- Dos reglas documentadas: onda según distancia y daño doble por carga eléctrica.
  La explosión consume el origen, guarda supervivientes y elimina objetivos muertos.
- Familia `JuegoException`, `EstadoEntidadInvalidoException` y
  `ExplosionNoPermitidaException`, con handlers diferenciados y 404 separado.
- Endpoint de lectura con referencias `Movimientos`; colección Insomnia completa
  en `insomnia/`, README y especificaciones Markdown para Classroom.
- Pruebas de CRUD, subtipos, reglas, errores, rollback, esquema anterior y reapertura
  de H2 en archivo temporal; la base personal de `data/` no se usa para estas pruebas.

Las entradas 1–7 reflejan ejercicios anteriores que incluyeron prácticas colaborativas;
no se borran ni se atribuyen a este trabajo. **La entrega POO-08 es individual.**
No se solicita revisión de un compañero ni se inventan aprobaciones o pruebas manuales
en Insomnia que no se hayan realizado. El alumno debe revisar y poder defender el diseño.

### Verificación realizada

- `./mvnw test`: 59 pruebas, sin fallos ni errores.
- Export YAML y cuerpos JSON verificados sintácticamente; los 28 pedidos se
  reprodujeron por HTTP contra `./mvnw spring-boot:run`, usando puerto y H2 temporal
  aislados. Se comprobó un reinicio real y el Player conservó vida 7.
- No se operó la interfaz gráfica de Insomnia; la importación visual queda para la
  revisión del alumno. Se conserva el formato de exportación v5 utilizado previamente.
- Publicación prevista mediante commits separados en la rama existente
  `feature/markdown`; sin reescribir el historial ni modificar `main`.

## Entrada 9 — IDs automáticos en Insomnia

**Fecha:** 2026-11-09. **Agente:** OpenCode. **Proveedor:** OpenAI.
**Modelo exacto:** `openai/gpt-6.1-sol` (GPT-6.1 Sol).

**Resumen de prompts:** el alumno compartió una corrida que creaba un Zombie con
ID 10 pero consultaba/actualizaba/borraba ID 0, preguntó por qué la captura no era
automática y luego indicó «Hazlo» para automatizar la colección y actualizar la entrega.

- Se agregó un script previo y posterior a cada una de las siete altas válidas:
  limpiar el ID anterior, verificar 201 e ID entero positivo y guardar el ID real
  en el entorno. Los POST ilegales y las explosiones no sobrescriben IDs.
- Se consultaron la documentación oficial de scripts de Kong y el importador
  YAML v5 de Insomnia para confirmar `scripts.preRequest`, `scripts.afterResponse`
  y `insomnia.environment.set`, sin inventar campos del export.
- Se agregaron pruebas Node.js que ejecutan los scripts reales del YAML y se
  actualizaron README y especificaciones para correr las carpetas sin copiar IDs.
- No se modificó código Java ni se operó la interfaz gráfica de Insomnia del alumno.
  Las pruebas HTTP usan una H2 temporal aislada, no su base en `data/`.
- Verificación: 15 pruebas de scripts aprobadas; 59 pruebas Maven aprobadas;
  dos recorridos completos (56 pedidos HTTP) ejecutando los scripts del YAML con
  la API de entorno/respuesta emulada en Node.js, sin asignación manual de IDs.
  Se verificaron reemplazo de IDs entre corridas y persistencia tras reiniciar.
  Esto no sustituye la verificación visual de importación y corrida en Insomnia.

## Entrada 10 — Corrección de code/status en los scripts

**Fecha:** 2026-11-09. **Agente:** OpenCode. **Proveedor:** OpenAI.
**Modelo exacto:** `openai/gpt-6.1-sol` (GPT-6.1 Sol).

**Resumen de prompts:** el alumno compartió otra corrida de Insomnia 13.4.0 donde
los POST guardaban entidades pero los scripts fallaban y los IDs seguían en 0;
después del diagnóstico indicó «Hazlo» para corregir y volver a publicar.

- El agente había confundido `insomnia.response.status` (frase textual) con
  `insomnia.response.code` (número HTTP). Las pruebas anteriores emulaban mal
  `status` y por eso no detectaban el error. Esta entrada corrige la conclusión
  de la entrada 9: aquella verificación no acreditaba compatibilidad real.
- Se contrastó el objeto Response del código fuente de Insomnia y su manejo de
  entornos. Se corrigieron los siete scripts para comprobar `code !== 201`.
- Primero se ajustó el fixture al contrato real y se reprodujo el fallo; luego se
  corrigió el YAML y se agregaron casos 201 con status textual, vacío o traducido,
  además de un error 400 con status engañoso para exigir el uso del número.
- La colección se distingue como **IDs automáticos (corregido)** para evitar usar
  la copia anterior. No se cambió Java ni se tocó la H2 personal del alumno.
- Verificación posterior: 22 pruebas de scripts aprobadas y 59 pruebas Maven sin
  fallos; dos recorridos de 28 pedidos HTTP con los scripts del YAML ejecutados
  en Node.js y la respuesta emulada con `code` numérico y `status` textual real
  del transporte. Se repitió el GET del Player tras reiniciar y conservó vida 7.
  No se afirma haber ejecutado la interfaz gráfica o el motor real de Insomnia.

## Entrada 11 — POO-09: contratos, escenario y pruebas de APIs cruzadas

**Fecha:** 2026-11-23. **Agente:** OpenCode. **Proveedor:** OpenAI.
**Modelo exacto:** `openai/gpt-6.1-sol` (GPT-6.1 Sol).

**Prompts y decisiones:** el alumno pidió comparar su repositorio con POO-09, identificó
a Sofía Torres y Nicolás Rodriguez como compañeros, pidió revisar sus OpenAPI tras
reinicios y acordó implementar el plan con «Hazlo». Se propuso a Sofía publicar
`POST /api/explosiones/validar` sin efectos secundarios; tras verificar su publicación
se usó ese contrato, no código fuente de compañeros.

- Se releyó el enunciado oficial; los roles en esta entrega son Amir A1, Sofía A2 y Nicolás A3.
- Se reutilizaron WebClient, H2, JPA y OpenAPI previamente incorporados. El estado inicial
  de esta implementación era el commit `882fe59` («previo POO-09»), sin cambios locales.
- Se agregaron clientes independientes configurables, con timeouts y sin reintentos,
  comprobación de booleanos e IDs y errores atribuidos al proveedor.
- El escenario guarda una operación **autorizada**, no daño ejecutado. Los explosivos
  de los proveedores son independientes. Sofía se consulta antes de Nicolás; se escribe
  recién después de ambas aprobaciones, dentro de una transacción local corta.
- Se agregó la entidad/repo de jugadas y POST/GET; los rechazos no insertan auditorías.
  Se conservan las funcionalidades anteriores y se amplió la descripción OpenAPI de las reglas.
- Se agregaron pruebas con dos servidores HTTP simulados, incluyendo rollback después
  del INSERT. Se extendió la prueba de cierre/reapertura de H2 con una jugada fixture.
- Se preparó `insomnia/poo09.yaml`, con creación de participantes e IDs automáticos,
  feliz y rechazos de ambos, y pruebas Node de sus scripts. La preparación de esa
  colección sí escribe datos nuevos en los compañeros; el escenario no lo hace.
- Verificación real con A1 en puerto 18090 y H2 en archivo temporal: 201 y GET exitosos,
  rechazo de Sofía por Creeper muerto y rechazo de Nicolás por Zombie, ambos 409 atribuidos
  y con la lista completa local sin cambios. Se reutilizaron participantes existentes:
  no se crearon, dañaron ni actualizaron entidades de compañeros. Captura en
  `insomnia/poo09-respuestas-reales.json`. No se ejecutó la GUI de Insomnia.
- Se redactó el archivo aplicado POO-09 y README. Quedan explícitamente pendientes la
  corrección/nota POO-08, repositorios/commits de compañeros, URL compartible de A1 y
  publicación del commit final. No se inventaron resultados ni enlaces de entrega.
- Resultado final: **140 pruebas Maven y 32 pruebas Node aprobadas**, sin fallos.
  Se reinició también la instancia temporal real y el GET recuperó la misma jugada
  con la misma fecha, confirmando persistencia. Se detuvo esa instancia al terminar;
  no se dejó un servicio adicional ni se modificó la base habitual del alumno.

## Entrada 12 — Cierre de observación POO-08: dominio solo con estado

**Fecha:** 2026-11-23. **Agente:** OpenCode. **Proveedor:** OpenAI.
**Modelo exacto:** `openai/gpt-6.1-sol` (GPT-6.1 Sol).

**Prompt recibido:** el alumno compartió la corrección de «Organización en paquetes —
2 de 3»: el dominio no debe lanzar excepciones ni contener reglas de negocio; las
validaciones y reglas deben quedar exclusivamente en service. También se sugirió sacar
Main de domain, sin descuento adicional. Esto reemplaza el pendiente de conocer esa
observación, pero no permite inferir el puntaje global ni otras partes de la corrección.

- Se retiraron validaciones/excepciones de constructores, fábrica y callbacks JPA de
  Entidad, Hostiles, Creeper, Aldeano, Esqueleto, Hitbox, Posicion y los otros tipos que
  aún las contenían (TipoEntidad, ZombiePequeño y la nueva JugadaCruzada).
- Se trasladaron daño, radio/carga, precondiciones de explosión, distancia, detección,
  movimiento y comportamientos a `ReglasJuegoService`. Los services CRUD y explosión
  validan y coordinan usando esa capa; las entidades tienen estado, accesores y mapeo.
- Se creó `DemostracionService` para que las rutas históricas tampoco construyan estados
  sin validar ni ejecuten reglas en el controller. Movimientos es implementado por un
  adaptador de servicio, no por la Entity. Se mantiene herencia y esquema JPA existente.
- Se preservan restricciones SQL de integridad, no callbacks de validación de negocio.
  Se corrigió la asignación del discriminador del ZombiePequeño sin depender de callbacks.
- Main se conservó como ejemplo en `src/test/java/py/edu/uc/lp3/demo/Main.java`.
- Las pruebas unitarias de reglas se movieron a service y siguen comprobando fórmulas,
  límites, vida no negativa y errores. Se agregó `ArquitecturaDominioTests` para detectar
  dependencias a excepciones/services, callbacks de validación o reglas dentro de domain.
- `./mvnw clean test`: **142 pruebas aprobadas**, 0 fallos/errores/omitidas.
  `node --test insomnia/tests/*.test.cjs`: **32 aprobadas**.
- Verificación HTTP adicional en una H2 temporal: CRUD 201, vida cero 400, origen Player
  409, explosión 200 y superviviente con vida 7; demo de Aldeano con profesión válida
  200 e inválida 400. También se repitieron éxito y ambos rechazos POO-09 contra las
  APIs reales: 201/409/409 y lista sin cambios tras rechazos. No hubo escrituras remotas
  ni cambios en `data/entidades`. Se detuvo el servidor temporal al terminar.
- README, diagrama y especificaciones fueron actualizados para no seguir afirmando
  que las reglas viven en el dominio. Falta confirmar si el documento completo tiene
  más observaciones antes de declarar cerrados los 3 puntos de corrección.

## Entrada 13 — Datos confirmados del grupo y cierre de POO-08

**Fecha:** 2026-11-23. **Agente:** OpenCode. **Proveedor:** OpenAI.
**Modelo exacto:** `openai/gpt-6.1-sol` (GPT-6.1 Sol).

**Prompts recibidos:** el alumno proporcionó los repositorios GitHub de Sofía y Nicolás,
las tres URLs de Swagger UI sobre Tailscale y confirmó «Sí, era lo único. Hazlo» respecto
a la observación de POO-08. Se incorporan esos datos sin inferir commits en ejecución.

- A1 compartible: `http://100.78.121.31:8080`; A2 Sofía:
  `http://100.79.75.27:8081`; A3 Nicolás: `http://100.83.179.97:8080`.
- Repositorios: `Sofia-torres00/st-taller-git-2026` y
  `NicoRodriguez85677/NRodriguez-taller-git-2026`, ambos en GitHub.
- README y especificaciones POO-09 registran que la única observación recibida ya fue
  atendida y no hay otras pendientes según confirmación del alumno. No se inventa la
  nota global de POO-08 ni se afirma 24/24 original.
- Se actualizó `a1_base_url` de `insomnia/poo09.yaml` a la IP de Tailscale confirmada;
  `localhost:8080` sigue documentado como alternativa local.
- Los hashes de los compañeros siguen pendientes de confirmación con cada autor:
  no se equipara el HEAD publicado con el proceso ejecutado. Se documentaron repositorios,
  carpetas, puertos y cómo seleccionar los hashes cuando estén disponibles.
- No se crearon commits ni se hizo push. La publicación final sigue pendiente.

## Entrada 14 — Versionado de la solución para compartir con el grupo

**Fecha:** 2026-11-23. **Agente:** OpenCode. **Proveedor:** OpenAI.
**Modelo exacto:** `openai/gpt-6.1-sol` (GPT-6.1 Sol).

**Prompt recibido:** el alumno pidió crear los commits, publicarlos y pasarle el hash
correcto de su implementación, no el anterior `882fe59` que no contenía POO-09.

- Se verificó la rama actual `feature/markdown` y su remoto `origin`, sin modificar main
  ni reescribir historial. Se consultó el remoto con fetch antes de publicar.
- Se separaron los cambios en commits: corrección POO-08 (`d0b8e85`), documentación
  de reglas OpenAPI (`f6e54a8`), clientes de compañeros (`6a068ae`), escenario persistido
  y pruebas (`de04d06`) y colección Insomnia (`e8d55f0`). La instalación inicial de
  OpenAPI ya estaba incluida en el commit previo `882fe59`.
- Antes del versionado se repitieron `./mvnw clean test` y las pruebas Node de Insomnia.
  Se conserva el alcance individual: hashes de Sofía/Nicolás todavía pendientes de confirmación.
- Se prepara un snapshot con README, bitácora y especificaciones aplicadas; un commit
  posterior únicamente fija el enlace a ese snapshot, evitando un hash autorreferente.
  No se hace merge ni push forzado; se publica la rama de trabajo existente.
- Snapshot de implementación con todos los archivos: `d283f6594c9f727499c71332ae2427d5c8868bf0`.
  El commit de enlace actualiza README y especificaciones para compartir ese hash;
  no cambia el código probado. Resultado de la verificación previa: 142 pruebas Java
  y 32 de scripts aprobadas. Los hashes de compañeros siguen pendientes.

## Entrada 15 — Commits de compañeros confirmados

**Fecha:** 2026-11-23. **Agente:** OpenCode. **Proveedor:** OpenAI.
**Modelo exacto:** `openai/gpt-6.1-sol` (GPT-6.1 Sol).

**Prompt recibido:** el alumno proporcionó el enlace al commit de Sofía
`06a6fb297a5e9f9186d23b6dee7086fdaba94e30` y el hash de Nicolás
`e9db16a93c925928734932e2759459a08747a1dd` para completar la documentación del grupo.

- Se verificó la existencia de ambos commits en sus repositorios mediante la API de GitHub.
  La primera consulta con Python falló por certificados locales; se usó curl con
  verificación TLS normal, sin desactivar controles de seguridad ni cambiar configuración.
- Se reemplazaron los pendientes y marcadores del README y se incorporaron enlaces
  exactos en las especificaciones POO-09. No se clonó código ajeno ni se copiaron reglas.
- Esta actualización es exclusivamente documental: no cambia el código previamente
  probado ni atribuye a estos snapshots una prueba de arranque por clonación no realizada.
- Se prepara un nuevo snapshot de entrega con los datos del grupo completos; el commit
  posterior solo fija su enlace. La rama de publicación sigue siendo `feature/markdown`.
- Snapshot de solución con hashes del grupo incorporados:
  `a6e6b14a3626f1cf6cfa48547fb4b1d5596a03fb`. Se fija este enlace en README y
  especificaciones mediante un commit posterior exclusivamente documental.
