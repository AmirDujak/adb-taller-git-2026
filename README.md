# Taller Git 2026 — Minecraft (opción A)

API REST de entidades de Minecraft desarrollada para Lenguaje de Programación 3.
Expone el modelado de entidades del juego con herencia, polimorfismo, encapsulamiento,
sobrecarga y sobrescritura de métodos.

## Cómo ejecutar

```bash
./mvnw spring-boot:run
```

La aplicación queda disponible en `http://localhost:8080`.

## CRUD persistente de Entidad (3 capas)

El CRUD usa `/api/entidades`, separado de los ejemplos de comportamiento del juego.

```text
HTTP → EntidadCrudController → EntidadService → EntidadRepository → H2
         presentación            negocios         persistencia
```

`EntidadRegistro` es el modelo JPA de los atributos comunes de `minecraft.Entidad`:
nombre, vida, daño base, velocidad, hitbox, sonidos y drop. Se mantiene intacta la
jerarquía abstracta del juego; el CRUD no guarda estados específicos de las subclases
ni ejecuta comportamientos como explotar o esquilar.

| Método | Ruta | Resultado |
|---|---|---|
| POST | `/api/entidades` | Crear: `201 Created`, cuerpo y cabecera `Location` |
| GET | `/api/entidades` | Listar registros: `200 OK` (inicialmente `[]`) |
| GET | `/api/entidades/{id}` | Buscar: `200 OK` o `404 Not Found` |
| PUT | `/api/entidades/{id}` | Reemplazar todos los datos: `200 OK` o `404 Not Found` |
| DELETE | `/api/entidades/{id}` | Eliminar: `204 No Content` o `404 Not Found` |

Ejemplo de alta (el mismo cuerpo sirve para PUT):

```bash
curl -i -X POST http://localhost:8080/api/entidades \
  -H 'Content-Type: application/json' \
  -d '{"nombre":"Zombie","vida":20,"danoBase":5,"velocidadMovimiento":0.7,"hitbox":{"ancho":0.6,"alto":1.95,"profundidad":0.6},"sonidos":["gruñido","gemido"],"drop":["carne_podrida"]}'

curl http://localhost:8080/api/entidades
curl http://localhost:8080/api/entidades/1
curl -i -X DELETE http://localhost:8080/api/entidades/1
```

Todos los campos del ejemplo son obligatorios; `sonidos` y `drop` pueden ser listas
vacías. El id lo genera la base y se toma de la ruta para actualizar o eliminar.
El service valida nombre no vacío (hasta 255 caracteres), vida positiva, daño no
negativo, velocidad finita no negativa y dimensiones finitas positivas. También
rechaza elementos nulos, vacíos o de más de 255 caracteres en las listas.
Las violaciones devuelven `400 Bad Request` con `{"error":"mensaje"}`.

### Base H2

Los datos se guardan en archivo en `./data/entidades` y sobreviven a los reinicios.
Spring Data JPA implementa el repositorio y Hibernate actualiza el esquema al iniciar.
La carpeta `data/` está excluida de Git.

Con la aplicación ejecutándose, abrir `http://localhost:8080/h2-console`:

- JDBC URL: `jdbc:h2:file:./data/entidades`
- Usuario: `sa`
- Contraseña: vacía

Esta configuración es para desarrollo local. Las pruebas usan otra H2 **en memoria**
para no modificar la base local:

```bash
./mvnw test
```

## Endpoints

### Estado del servicio

```text
GET /
```

Devuelve un mensaje de bienvenida indicando que el servicio está funcionando.

### Construcción de una entidad por tipo

```text
GET /entidad/{tipo}?profesion=Herrero&danio=25
```

Crea una entidad (`creeper`, `zombie`, `esqueleto`, `lobo`, `oveja`, `aldeano`) con sus
valores por defecto y opcionalmente le aplica daño. Devuelve sus datos en JSON.

### Construcción de un zombie con parámetros

```text
GET /entidades/zombie?vida=20&danoBase=5&velocidad=0.7&hostilidad=true
```

Construye un `Zombie` usando los parámetros recibidos por URL y devuelve sus datos en JSON.
Los valores se validan en las entidades del dominio (vida > 0, daño base ≥ 0, velocidad ≥ 0).

### Disparo del esqueleto (sobrecarga)

```text
GET /entidades/esqueleto/disparar?vida=20&danoBase=3&velocidad=0.75&usaArco=true
GET /entidades/esqueleto/disparar?vida=20&danoBase=3&velocidad=0.75&usaArco=true&distancia=10
```

Sin `distancia` se invoca `disparar()`; con `distancia` se invoca `disparar(int)`.
La segunda valida que la distancia no sea negativa.

### Comportamientos polimórficos

```text
GET /entidades/comportamientos?vida=20&danoBase=5&velocidad=0.7&hostilidad=true&comercializacion=false
```

Construye un `Zombie` y un `Aldeano` con los parámetros de la URL y devuelve en JSON el
comportamiento de cada uno. El controller los guarda en una `List<Entidad>` y llama a
`comportamiento()` sin distinguir el tipo concreto; cada clase responde a su manera.
Un valor inválido devuelve `400` (ver "Errores de validación").

### Todas las entidades

```text
GET /entidades
```

Devuelve una lista con una instancia de cada tipo de entidad, mostrando su comportamiento.

### Errores de validación

Si un valor viola una regla del dominio (por ejemplo `vida=0` o `danoBase=-1`), la entidad
lanza `IllegalArgumentException` y el controller responde `400 Bad Request` con
`{"error": "<mensaje>"}`.

## Entidades y comportamientos

`Entidad` es la clase abstracta base. Contiene los atributos privados `vida`, `nombre`,
`sonidos`, `hitbox`, `velocidadMovimiento`, `drop` y `danoBase`, valida sus valores en el
constructor y declara el método abstracto `comportamiento()`. No existen setters públicos;
el estado permanece encapsulado.

- `Player`: explora el mundo y combate a las entidades.
- `Zombie`: persigue y ataca al jugador si es hostil; de lo contrario, deambula sin atacar.
- `Esqueleto`: ataca con arco o cuerpo a cuerpo según su configuración (`usaArco`).
- `Creeper`: explota al atacar (y puede cargarse con un rayo).
- `Lobo`: se puede domesticar con huesos.
- `Oveja`: se puede esquilar para obtener lana.
- `Aldeano`: comercia con el jugador según su profesión.
- `Cerdo`: puede ser montado si tiene esa capacidad habilitada (`montable`).
- `ZombiePequeño`: hereda el comportamiento del `Zombie` y agrega una velocidad aumentada.

`Hostiles` y `NoHostiles` son clases intermedias abstractas que agregan, respectivamente,
el rango de aggro y el contrato de interacción.

## Sobrecarga y sobreescritura

**Sobreescritura (`override`)**: `Entidad` declara `comportamiento()` como abstracto y
`Player`, `Zombie`, `Creeper`, `Esqueleto`, `Lobo`, `Oveja`, `Aldeano` y `Cerdo` lo
reimplementan con `@Override`, con la misma firma. Java elige la versión según la clase
real del objeto, en tiempo de ejecución.

**Sobrecarga (`overload`)**: `Esqueleto` define dos métodos llamados `disparar` con distinta
lista de argumentos: `disparar()` y `disparar(int distancia)`. El compilador elige la
versión según los argumentos de la llamada. La segunda valida que la distancia no sea negativa.
Los constructores de cada clase también están sobrecargados (sin parámetros y con parámetros).

**Cómo distinguirlas**: la sobreescritura mantiene la firma y cambia la clase (padre → hija);
la sobrecarga mantiene el nombre y cambia los parámetros dentro de la misma clase.

## Diagrama de clases

```mermaid
classDiagram
    direction TB

    class Movimientos {
        <<interface>>
        +avanzar() String
        +rotar(angulo) String
        +saltar() String
    }

    class Hitbox {
        -double ancho
        -double alto
        -double profundidad
    }

    class Entidad {
        <<abstract>>
        -int vida
        -String nombre
        -List~String~ sonidos
        -Hitbox hitbox
        -double velocidadMovimiento
        -List~String~ drop
        -int danoBase
        #Entidad()
        #Entidad(int vida, int danoBase, double velocidad)
        +int getVida()
        +int getDanoBase()
        +double getVelocidadMovimiento()
        +String comportamiento()*
        +String recibirDanio(cantidad) String
        +boolean isViva() boolean
    }

    class Player {
        +Player()
        +Player(int vida, int danoBase, double velocidad)
        +String comportamiento()
    }

    class Hostiles {
        <<abstract>>
        -double rangoDeAggro
        #Hostiles(...)
        +boolean detecta(distancia) boolean
        +String comportamiento()
        #String accionDeAtaque()*
    }

    class NoHostiles {
        <<abstract>>
        #NoHostiles(...)
        +String comportamiento()
        #String interactuar()*
    }

    class Zombie {
        -boolean hostilidad
        +Zombie()
        +Zombie(int vida, int danoBase, double velocidad, boolean hostilidad)
        +boolean isHostilidad()
        +String comportamiento()
    }

    class Esqueleto {
        -boolean usaArco
        +Esqueleto()
        +Esqueleto(int vida, int danoBase, double velocidad, boolean usaArco)
        +boolean isUsaArco()
        +String disparar()
        +String disparar(int distancia)
    }

    class Creeper {
        -boolean cargado
        +Creeper()
        +void cargarConRayo()
    }

    class Lobo {
        -boolean domesticado
        +Lobo()
        +boolean isDomesticado()
    }

    class Oveja {
        -boolean esquilada
        +Oveja()
        +boolean isEsquilada()
    }

    class Aldeano {
        -String profesion
        +Aldeano(String profesion)
        +String getProfesion()
    }

    class Cerdo {
        -boolean montable
        +Cerdo()
        +Cerdo(int vida, int danoBase, double velocidad, boolean montable)
        +boolean isMontable()
    }

    class ZombiePequeño {
        -int velocidadAumentada
        +ZombiePequeño()
        +ZombiePequeño(int vida, int danoBase, double velocidad, boolean hostilidad, int velocidadAumentada)
        +int getVelocidadAumentada()
    }

    Movimientos <|.. Entidad
    Entidad *-- Hitbox
    Entidad <|-- Hostiles
    Entidad <|-- NoHostiles
    Entidad <|-- Player
    Entidad <|-- Cerdo
    Hostiles <|-- Creeper
    Hostiles <|-- Zombie
    Hostiles <|-- Esqueleto
    NoHostiles <|-- Lobo
    NoHostiles <|-- Oveja
    NoHostiles <|-- Aldeano
    Zombie <|-- ZombiePequeño
```

## Decisiones de diseño

- **Método abstracto en la clase base:** `Entidad.comportamiento()`. Las dos hijas `Hostiles` y `NoHostiles`
  lo implementan de forma independiente (atacar o interactuar), y cada mob concreto redefine solo su parte
  (`accionDeAtaque()` o `interactuar()`).
- **Trato uniforme:** el controller y `Main` solo usan el tipo `Entidad` para moverse, recibir daño y
  desaparecer, sin preguntar el tipo concreto.
- **Ocultamiento:** todos los campos son `private` y no hay setters. La vida solo cambia con `recibirDanio()`
  o `desaparecer()`. Los constructores validan los datos (vida > 0, daño ≥ 0, hitbox positiva, daño base ≥ 0,
  profesión no vacía) y una entidad muerta no puede moverse ni recibir daño.
- **Constructores sobrecargados:** cada entidad tiene un constructor por defecto con valores razonables y otro
  que recibe parámetros para ser construido desde la API REST.
- **Sin campos `protected`:** donde una hija necesita algo de la base se usa un método `protected`.
  `desaparecer()` existe porque el `Creeper` muere al explotar sin recibir daño, y `describirAvance()`
  porque el `Creeper` avanza de forma sigilosa.
