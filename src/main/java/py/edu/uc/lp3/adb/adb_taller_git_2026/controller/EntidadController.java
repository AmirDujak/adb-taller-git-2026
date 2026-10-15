package py.edu.uc.lp3.adb.adb_taller_git_2026.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import minecraft.Aldeano;
import minecraft.Creeper;
import minecraft.Entidad;
import minecraft.Esqueleto;
import minecraft.Lobo;
import minecraft.Oveja;
import minecraft.Player;
import minecraft.Zombie;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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
public class EntidadController {

    private static final List<String> TIPOS =
            List.of("creeper", "zombie", "esqueleto", "lobo", "oveja", "aldeano");

    /** Crea una entidad por tipo y opcionalmente le aplica daño. Mantiene la ruta histórica /entidad/{tipo}. */
    @GetMapping("/entidad/{tipo}")
    public Map<String, Object> crear(@PathVariable String tipo,
                                     @RequestParam(defaultValue = "Granjero") String profesion,
                                     @RequestParam(defaultValue = "0") int danio) {
        Entidad entidad = crearEntidad(tipo, profesion);
        return describir(entidad, danio);
    }

    /** Construye un Zombie con los parámetros recibidos por URL y devuelve sus datos en JSON. */
    @GetMapping("/zombie")
    public Map<String, Object> zombie(
            @RequestParam(defaultValue = "20") int vida,
            @RequestParam(defaultValue = "5") int danoBase,
            @RequestParam(defaultValue = "0.7") double velocidad,
            @RequestParam(defaultValue = "true") boolean hostilidad) {
        Zombie zombie = new Zombie(vida, danoBase, velocidad, hostilidad);
        return describir(zombie, 0);
    }

    /** Disparo del esqueleto (sobrecarga). Sin distancia invoca disparar(); con distancia invoca disparar(int). */
    @GetMapping("/esqueleto/disparar")
    public Map<String, Object> esqueletoDisparar(
            @RequestParam(defaultValue = "20") int vida,
            @RequestParam(defaultValue = "3") int danoBase,
            @RequestParam(defaultValue = "0.75") double velocidad,
            @RequestParam(defaultValue = "true") boolean usaArco,
            @RequestParam(required = false) Integer distancia) {
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
    @GetMapping("/comportamientos")
    public List<Map<String, Object>> comportamientos(
            @RequestParam(defaultValue = "20") int vida,
            @RequestParam(defaultValue = "5") int danoBase,
            @RequestParam(defaultValue = "0.7") double velocidad,
            @RequestParam(defaultValue = "true") boolean hostilidad,
            @RequestParam(defaultValue = "false") boolean comercializacion) {
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
    @GetMapping
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
