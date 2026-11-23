package py.edu.uc.lp3.rest.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import py.edu.uc.lp3.constants.ApiPaths;
import py.edu.uc.lp3.domain.Aldeano;
import py.edu.uc.lp3.domain.Creeper;
import py.edu.uc.lp3.domain.Entidad;
import py.edu.uc.lp3.domain.Esqueleto;
import py.edu.uc.lp3.domain.Lobo;
import py.edu.uc.lp3.domain.Oveja;
import py.edu.uc.lp3.domain.Zombie;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Construye instancias del dominio a partir de los parámetros de la URL.
 * Después de crearlas, las trata siempre como {@link Entidad} (el tipo padre):
 * nunca pregunta de qué tipo concreto es cada una.
 *
 * Ejemplos:
 *   GET /entidad/zombie
 *   GET /entidades/zombie?vida=20&danoBase=5&velocidad=0.7&hostilidad=true
 *   GET /entidad/aldeano?profesion=Herrero
 *   GET /entidad/creeper?danio=5
 *   GET /entidades/esqueleto/disparar?vida=20&danoBase=3&velocidad=0.75&usaArco=true
 *   GET /entidades/esqueleto/disparar?vida=20&danoBase=3&velocidad=0.75&usaArco=true&distancia=10
 *   GET /entidades/comportamientos?vida=20&danoBase=5&velocidad=0.7&hostilidad=true&comercializacion=false
 *   GET /entidades
 */
@RestController
@Tag(name = "Demostraciones", description = "Instancias temporales y comportamientos; no se guardan en H2")
public class EntidadController {

    private static final List<String> TIPOS =
            List.of("creeper", "zombie", "esqueleto", "lobo", "oveja", "aldeano");

    /** Crea una entidad por tipo y opcionalmente le aplica daño. Mantiene la ruta histórica /entidad/{tipo}. */
    @GetMapping(ApiPaths.ENTIDAD_POR_TIPO)
    @Operation(summary = "Demostrar una entidad por tipo", description = "Construye una instancia temporal y opcionalmente le aplica daño.")
    @ApiResponse(responseCode = "200", description = "Entidad, movimientos, comportamiento y estado final")
    @ApiResponse(responseCode = "400", description = "Daño o parámetros inválidos", content = @Content)
    @ApiResponse(responseCode = "404", description = "Tipo desconocido", content = @Content)
    public Map<String, Object> crear(@Parameter(description = "creeper, zombie, esqueleto, lobo, oveja o aldeano", example = "zombie") @PathVariable String tipo,
                                     @Parameter(description = "Profesión usada si el tipo es aldeano") @RequestParam(defaultValue = "Granjero") String profesion,
                                     @Parameter(description = "Daño no negativo que se aplica a la instancia temporal") @RequestParam(defaultValue = "0") int danio) {
        Entidad entidad = crearEntidad(tipo, profesion);
        return describir(entidad, danio);
    }

    /** Construye un Zombie con los parámetros recibidos por URL y devuelve sus datos en JSON. */
    @GetMapping({ApiPaths.DEMO_ZOMBIE, "/zombie"})
    @Operation(summary = "Demostrar un Zombie parametrizado")
    @ApiResponse(responseCode = "200", description = "Zombie temporal con movimientos y estado final")
    @ApiResponse(responseCode = "400", description = "Parámetros inválidos", content = @Content)
    public Map<String, Object> zombie(
            @Parameter(description = "Vida inicial positiva") @RequestParam(defaultValue = "20") int vida,
            @Parameter(description = "Daño base no negativo") @RequestParam(defaultValue = "5") int danoBase,
            @Parameter(description = "Velocidad de movimiento finita no negativa") @RequestParam(defaultValue = "0.7") double velocidad,
            @Parameter(description = "Indica si el Zombie es hostil") @RequestParam(defaultValue = "true") boolean hostilidad) {
        Zombie zombie = new Zombie(vida, danoBase, velocidad, hostilidad);
        return describir(zombie, 0);
    }

    /** Disparo del esqueleto (sobrecarga). Sin distancia invoca disparar(); con distancia invoca disparar(int). */
    @GetMapping({ApiPaths.DEMO_DISPARAR, "/esqueleto/disparar"})
    @Operation(summary = "Demostrar el disparo de un Esqueleto", description = "Sin distancia usa disparar(); con distancia usa disparar(int).")
    @ApiResponse(responseCode = "200", description = "Esqueleto, descripción del disparo y estado final")
    @ApiResponse(responseCode = "400", description = "Parámetros inválidos", content = @Content)
    public Map<String, Object> esqueletoDisparar(
            @Parameter(description = "Vida inicial positiva") @RequestParam(defaultValue = "20") int vida,
            @Parameter(description = "Daño base no negativo") @RequestParam(defaultValue = "3") int danoBase,
            @Parameter(description = "Velocidad de movimiento finita no negativa") @RequestParam(defaultValue = "0.75") double velocidad,
            @Parameter(description = "Indica si el Esqueleto dispone de arco") @RequestParam(defaultValue = "true") boolean usaArco,
            @Parameter(description = "Distancia al objetivo; opcional, selecciona la sobrecarga del disparo", example = "10") @RequestParam(required = false) Integer distancia) {
        Esqueleto esqueleto = new Esqueleto(vida, danoBase, velocidad, usaArco);
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("entidad", esqueleto);
        json.put("disparar", distancia != null ? esqueleto.disparar(distancia) : esqueleto.disparar());
        json.put("vidaFinal", esqueleto.getVida());
        json.put("viva", esqueleto.isViva());
        return json;
    }

    /**
     * Construye un Zombie y un Aldeano con los parámetros de la URL y devuelve en JSON el
     * comportamiento de cada uno. Los guarda en una List<Entidad> y llama a comportamiento()
     * sin distinguir el tipo concreto; cada clase responde a su manera.
     */
    @GetMapping({ApiPaths.DEMO_COMPORTAMIENTOS, "/comportamientos"})
    @Operation(summary = "Demostrar comportamientos polimórficos", description = "Devuelve los comportamientos de un Zombie y un Aldeano temporales.")
    @ApiResponse(responseCode = "200", description = "Tipo, nombre, comportamiento y estado de cada entidad")
    @ApiResponse(responseCode = "400", description = "Parámetros inválidos", content = @Content)
    public List<Map<String, Object>> comportamientos(
            @Parameter(description = "Vida inicial positiva del Zombie") @RequestParam(defaultValue = "20") int vida,
            @Parameter(description = "Daño base no negativo del Zombie") @RequestParam(defaultValue = "5") int danoBase,
            @Parameter(description = "Velocidad de movimiento del Zombie") @RequestParam(defaultValue = "0.7") double velocidad,
            @Parameter(description = "Indica si el Zombie es hostil") @RequestParam(defaultValue = "true") boolean hostilidad,
            @Parameter(description = "Usa profesión Comerciante si es true; Granjero si es false") @RequestParam(defaultValue = "false") boolean comercializacion) {
        Zombie zombie = new Zombie(vida, danoBase, velocidad, hostilidad);
        String profesion = comercializacion ? "Comerciante" : "Granjero";
        Aldeano aldeano = new Aldeano(profesion);

        List<Entidad> entidades = List.of(zombie, aldeano);
        return entidades.stream()
                .map(e -> {
                    Map<String, Object> json = new LinkedHashMap<>();
                    json.put("tipo", e.getClass().getSimpleName());
                    json.put("nombre", e.getNombre());
                    json.put("comportamiento", e.comportamiento());
                    json.put("viva", e.isViva());
                    return json;
                })
                .toList();
    }

    /** Todas las entidades del mundo, cada una mostrando su propio comportamiento. */
    @GetMapping(ApiPaths.DEMO_ENTIDADES)
    @Operation(summary = "Listar las entidades de demostración", description = "No consulta las entidades persistidas; construye los seis tipos de ejemplo.")
    @ApiResponse(responseCode = "200", description = "Entidades temporales con movimientos, comportamiento y estado")
    public List<Map<String, Object>> todas() {
        return TIPOS.stream()
                .map(tipo -> crearEntidad(tipo, "Granjero"))
                .map(entidad -> describir(entidad, 0))
                .toList();
    }

    private Entidad crearEntidad(String tipo, String profesion) {
        return switch (tipo.toLowerCase()) {
            case "creeper" -> new Creeper();
            case "zombie" -> new Zombie();
            case "esqueleto" -> new Esqueleto();
            case "lobo" -> new Lobo();
            case "oveja" -> new Oveja();
            case "aldeano" -> new Aldeano(profesion);
            default -> throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Tipo de entidad desconocido: " + tipo);
        };
    }

    /** Usa solo la API de Entidad: el mismo código sirve para cualquier subclase. */
    private Map<String, Object> describir(Entidad entidad, int danio) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("entidad", entidad);
        json.put("avanzar", entidad.avanzar());
        json.put("saltar", entidad.saltar());
        json.put("comportamiento", entidad.comportamiento());
        if (danio != 0) {
            json.put("recibirDanio", entidad.recibirDanio(danio));
        }
        json.put("vidaFinal", entidad.getVida());
        json.put("viva", entidad.isViva());
        return json;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> argumentoInvalido(IllegalArgumentException e) {
        return Map.of("error", e.getMessage());
    }
}
