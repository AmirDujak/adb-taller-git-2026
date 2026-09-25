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
 *   GET /entidad/aldeano?profesion=Herrero
 *   GET /entidad/creeper?danio=5
 *   GET /entidades
 */
@RestController
public class EntidadController {

    private static final List<String> TIPOS =
            List.of("creeper", "zombie", "esqueleto", "lobo", "oveja", "aldeano");

    @GetMapping("/entidad/{tipo}")
    public Map<String, Object> crear(@PathVariable String tipo,
                                     @RequestParam(defaultValue = "Granjero") String profesion,
                                     @RequestParam(defaultValue = "0") int danio) {
        Entidad entidad = crearEntidad(tipo, profesion);
        return describir(entidad, danio);
    }

    /** Todas las entidades del mundo, cada una mostrando su propio comportamiento. */
    @GetMapping("/entidades")
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
