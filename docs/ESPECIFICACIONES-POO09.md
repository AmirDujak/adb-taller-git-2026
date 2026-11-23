# Enunciado aplicado — POO-09 · Minecraft

**Alumno:** Amir Dujak. **Entrega y nota:** individuales.
**Repositorio:** https://github.com/AmirDujak/adb-taller-git-2026

## Escenario en una frase

Registrar una operación de detonación coordinada solo si Sofía valida sus participantes
y Nicolás autoriza un explosivo independiente, guardando la jugada autorizada en H2 de Amir.

No ejecuta daño ni consume Creepers: registra ambas autorizaciones en el estado consultado.
Las entidades de los dos proveedores son independientes; no se intercambian UUID entre bases.

## Participantes

| Rol | Estudiante | Base | OpenAPI |
|---|---|---|---|
| A1 | Amir Dujak | http://100.78.121.31:8080 | http://100.78.121.31:8080/v3/api-docs |
| A2 | Sofía Torres | http://100.79.75.27:8081 | http://100.79.75.27:8081/v3/api-docs |
| A3 | Nicolás Rodriguez | http://100.83.179.97:8080 | http://100.83.179.97:8080/v3/api-docs |

Repositorios:
- Amir: https://github.com/AmirDujak/adb-taller-git-2026
- Sofía: https://github.com/Sofia-torres00/st-taller-git-2026
- Nicolás: https://github.com/NicoRodriguez85677/NRodriguez-taller-git-2026

Las tres direcciones son de Tailscale: requieren acceso autorizado a la red y servicios
encendidos. Amir también puede usar `http://localhost:8080` desde su propia máquina.

Sofía aplica su validación de participantes distintos, tipos correctos y Creeper vivo
mediante `POST /api/explosiones/validar`, sin cambios en su H2. Nicolás consulta la capacidad
actual de detonar mediante `GET /api/minecraft/explosivos/{id}`, sin consumir su explosivo.
Las reglas se consumen por HTTP según sus OpenAPI, sin copiar código del dominio ajeno.

## Implementación

- `POST /api/lp3/jugadas-cruzadas`: recibe los tres UUID de Sofía y el UUID de Nicolás.
- Consulta primero Sofía, descartando participantes inválidos antes de consultar Nicolás.
- Solo si ambas respuestas permiten la operación, guarda una `JugadaCruzada` con ID,
  fecha, participantes y aprobaciones; responde 201 y Location.
- `GET /api/lp3/jugadas-cruzadas` y `GET /api/lp3/jugadas-cruzadas/{id}` leen el registro local.
- Un rechazo identifica `A2_SOFIA` o `A3_NICOLAS` y deja H2 de Amir igual. Un timeout o
  contrato inválido también impide guardar. No se realizan escrituras remotas.

## Cómo probarlo

1. Arrancar Sofía y Nicolás en sus commits acordados y luego Amir con `./mvnw spring-boot:run`.
   Las IP requieren acceso por Tailscale. Para tres servicios locales usar
   puertos 8080/8081/8082 y configurar `SOFIA_API_BASE_URL` y `NICOLAS_API_BASE_URL`.
2. Abrir los tres OpenAPI. Importar `insomnia/poo09.yaml` y revisar las tres bases.
3. Ejecutar carpeta 01: crea participantes de prueba y captura IDs. Esta preparación
   escribe en bases de desarrollo remotas y debe acordarse con sus dueños.
4. Carpeta 02: ambos aceptan, POST devuelve 201 y GET muestra una jugada nueva.
5. Carpeta 03: Creeper muerto de Sofía provoca 409 con `proveedor: A2_SOFIA`;
   la lista completa de jugadas antes/después es idéntica.
6. Carpeta 04: Sofía acepta pero el Zombie de Nicolás provoca 409 con
   `proveedor: A3_NICOLAS`; tampoco cambia la lista.
7. Reiniciar Amir y repetir solamente el GET individual para verificar persistencia.

Pruebas locales: `./mvnw test` y `node --test insomnia/tests/*.test.cjs`.
Ejemplos de respuestas HTTP reales: `insomnia/poo09-respuestas-reales.json`.

## Corrección de POO-08 cerrada

El alumno confirmó que la única observación fue organización en paquetes (2/3).
Se atendió trasladando reglas, validaciones y excepciones del dominio a service;
Main está en ejemplos de test. El README describe los cambios y pruebas de respaldo.
No se declara un 24/24 original ni una modificación retroactiva de la nota.

## Información pendiente antes de adjuntar en Classroom

- Confirmar e incorporar los commits exactos publicados que ejecutan Sofía y Nicolás.
  Los repositorios y direcciones compartidas ya están registrados en el README.
- Publicar los cambios y reemplazar este pendiente con el enlace exacto:
  `https://github.com/AmirDujak/adb-taller-git-2026/commit/HASH_FINAL_POO09`.

**Este archivo todavía no es una entrega cerrada mientras existan esos pendientes.**
El README contiene el detalle de configuración, reglas y límites (sin reservas, daño ni idempotencia).

## Enunciado y evaluación

[Enunciado oficial](https://github.com/alefq/afq-taller-git-2024/blob/main/docs/ejercicio-poo-09-apis-cruzadas-2026-11-16.md).
15 puntos: OpenAPI, round robin, consumo y escenario persistido (3 cada uno), más cierre
de POO-08 (3). La defensa oral es otro ítem de 10; no se reaplica la rúbrica de 24.
La fecha límite es la del ítem Classroom; con 50 horas o más de atraso no se corrige.
