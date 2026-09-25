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
