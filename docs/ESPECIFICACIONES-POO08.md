# Especificaciones aplicadas — Minecraft POO-08

**Alumno:** Amir Dujak · **Modalidad:** individual · **Repositorio:** https://github.com/AmirDujak/adb-taller-git-2026

## Objetivo

Publicar una API Spring Boot que conserve entidades Minecraft en H2 y permita crear,
consultar, modificar y retirar mobs; simular explosiones normales y cargadas cuyos
resultados permanezcan después de reiniciar el proceso Java.

## Modelado y alcance

`Entidad` es la clase abstracta persistida. `Player` hereda directamente; `Creeper`
y `Zombie` heredan a través de `Hostiles`. Todos son entidades del mundo y soportan
`Movimientos`. JPA guarda la familia con `SINGLE_TABLE`; `tipo` discrimina el subtipo
en el JSON y en la base. `EntidadRepository` trabaja con la clase base.

Se conservan las clases y demos anteriores. La simulación necesita y prueba al menos
Creeper, Player y Zombie; no utiliza listas temporales como almacenamiento.
El resultado persistido es el estado del mundo, no un historial de explosiones.

## Arranque

Requiere JDK 21 y puerto 8080 libre. Desde la raíz:

```bash
./mvnw spring-boot:run
```

H2 usa `jdbc:h2:file:./data/entidades`, usuario `sa`, contraseña vacía,
`ddl-auto=update`. Los archivos de datos no se versionan. Respaldarlos con el servicio
detenido antes de actualizar. Consola local: `http://localhost:8080/h2-console`.

## URLs

Base: `http://localhost:8080`.

| Verbo | URL | Respuesta esperada |
|---|---|---|
| GET | `/` | 200, aplicación viva |
| POST | `/api/lp3/entidades` | 201 con ID generado y Location |
| GET | `/api/lp3/entidades` | 200, datos de H2 |
| GET | `/api/lp3/entidades/{id}` | 200 o 404 |
| PUT | `/api/lp3/entidades/{id}` | 200 o 400/404 |
| DELETE | `/api/lp3/entidades/{id}` | 204 o 404 |
| GET | `/api/lp3/entidades/movimientos` | 200, mensajes de Movimientos sin mutación |
| POST | `/api/lp3/explosiones/simular` | 200 o 400/404/409 |

## JSON e invariantes

Ejemplo de creación (el ID se omite):

```json
{
  "nombre": "Creeper",
  "tipo": "CREEPER",
  "vida": 20,
  "danoBase": 0,
  "velocidadMovimiento": 0.8,
  "hitbox": {"ancho": 0.6, "alto": 1.7, "profundidad": 0.6},
  "sonidos": ["siseo"],
  "drop": ["polvora"],
  "cargado": false,
  "posicion": {"x": 0, "y": 64, "z": 0}
}
```

- Nombre obligatorio de hasta 255 caracteres y vida inicial mayor a cero.
- Daño base y velocidad no negativos; velocidad finita.
- Dimensiones de hitbox finitas y positivas; sonidos/drops obligatorios, admiten listas vacías.
- Ningún texto de las listas puede ser nulo, blanco ni superar 255 caracteres.
- Posición opcional: al enviarla, las tres coordenadas deben ser finitas y estar presentes.
- Solo un Creeper puede tener carga; la actualización no puede cambiar el subtipo.
- No se expone un setter para la vida. El daño no puede dejarla negativa.
- PUT exige campos comunes completos; omitir/null en tipo, carga o posición conserva
  esos tres valores por compatibilidad. No hay PATCH ni borrado de posición con null.
- DELETE retira definitivamente la entidad y sus colecciones: un GET posterior da 404.

## Dos reglas de negocio

1. La onda del Creeper aplica daño decreciente a Player, Zombie y otros objetivos a menos de 3 bloques según distancia euclidiana tridimensional.
2. La carga eléctrica duplica el daño de la onda; la explosión consume el Creeper y elimina los objetivos que quedan sin vida.

Máximo normal 20, cargado 40. Daño dentro del radio:
`round(maximo * (1 - distancia / 3))`. En el borde y fuera del radio es cero.
No hay daño a objetivos sin posición ni explosiones en cadena.

La simulación recibe `{"creeperId": ID_REAL}`. Lee vidas, posiciones y carga de H2;
no acepta distancias ni vidas inventadas por el cliente. Devuelve distancias, daño
real limitado a la vida disponible, vidas finales y bajas. El POST modifica la base;
todo se confirma en una transacción y un fallo revierte los cambios completos.

## Excepciones

`JuegoException` es la base no comprobada. Tiene dos hijas con handler específico:

- `EstadoEntidadInvalidoException`: HTTP 400 y código `ESTADO_ENTIDAD_INVALIDO`;
  ejemplo: construir vida 0 o Player cargado.
- `ExplosionNoPermitidaException`: HTTP 409 y código `EXPLOSION_NO_PERMITIDA`;
  ejemplo: intentar explotar un Player o un Creeper sin posición.

El handler de la base devuelve 422 como respaldo. `EntidadNoEncontradaException`
responde 404 y `ENTIDAD_NO_ENCONTRADA`; no se confunde ausencia con regla incumplida.
Errores de lectura JSON responden 400 y `JSON_INVALIDO`.
El cuerpo contiene `codigo` y `error`. Las hijas las lanzan objetos del dominio.

## Cómo probar en 5–10 minutos

1. Arrancar y abrir Insomnia: **Import → File → `insomnia/minecraft.yaml`**.
   Si estaba importada la versión manual, volver a importar y usar la colección
   cuyo nombre termina en **IDs automáticos**.
2. Revisar `base_url` en Base Environment y ejecutar la carpeta 01 en orden.
   Los scripts de cada POST válido guardan automáticamente sus IDs en el entorno;
   no copiar valores ni asumir IDs consecutivos. Ejecutar secuencialmente una iteración.
3. Comprobar POST 201 y Location, GET coincidente, PUT a vida 30 y GET con 30.
4. Ejecutar POST con vida 0: 400. El listado posterior debe tener la misma cantidad
   que antes; DELETE 204 y GET posterior 404.
5. Carpeta 02: crear Creeper `(0,64,0)`, Player de vida 20 `(1,64,0)` y Zombie de
   vida 5 `(2,64,0)`. Consultar movimientos: mensajes de la interfaz para la familia.
6. Simular: Player pierde 13 y queda en **7**; Zombie y Creeper se eliminan.
   Confirmar con GET y con `SELECT id, nombre, vida FROM entidades` en la consola H2.
7. Detener con Ctrl+C y arrancar de nuevo: el GET del Player conserva vida **7**.
8. Carpeta 03: Creeper cargado y Player de vida 100 en `(100,64,0)`; simular y
   comprobar vida **60**, demostrando el máximo cargado de 40.
9. Carpeta 04: provocar ambas hijas; distinguir códigos y HTTP 400/409. El Player
   sigue en 7 y el Creeper sin posición permanece intacto tras el intento fallido.
10. Para repetir las explosiones, volver a ejecutar la carpeta desde sus altas:
    los nuevos IDs se capturan automáticamente y los eliminados no se reutilizan.
    Los scripts rechazan respuestas distintas de 201 e IDs inválidos, dejando 0
    en lugar de reutilizar IDs anteriores. Usar datos de desarrollo, porque puede
    afectar otros objetivos cercanos que ya estén en esa base.

Pruebas automatizadas: `./mvnw test`; cubren CRUD, límites, herencia real al leer H2,
protocolo, excepciones, compatibilidad de esquema antiguo, rollback y reinicio con
archivo temporal. No tocan la base personal de `data/`.

Pruebas específicas de la automatización (Node.js y Ruby):
`node --test insomnia/tests/automatic-ids.test.cjs`. Comprueban los siete scripts
del export, sin depender de IDs fijos ni cambiar la base de datos.

## Entrega en Classroom

Adjuntar este Markdown y pegar el enlace exacto al commit de solución.

**Commit de la solución:**
https://github.com/AmirDujak/adb-taller-git-2026/commit/d9c66484eff44c49112be9c1042425c58e8d7e3c

Ese snapshot incluye implementación, pruebas, colección con IDs automáticos y documentación. El commit
posterior únicamente agrega el enlace fijo a los documentos, sin cambiar el servicio.
