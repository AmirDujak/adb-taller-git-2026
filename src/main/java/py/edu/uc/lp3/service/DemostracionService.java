package py.edu.uc.lp3.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;
import py.edu.uc.lp3.domain.*;
import py.edu.uc.lp3.exceptions.EntidadNoEncontradaException;

/** Mantiene las demos históricas, con construcción validada y comportamiento en service. */
@Service
public class DemostracionService {
    private final ReglasJuegoService reglas;

    public DemostracionService(ReglasJuegoService reglas) { this.reglas = reglas; }

    public Map<String, Object> crear(String tipo, String profesion, int danio) {
        return describir(crearEntidad(tipo, profesion), danio);
    }

    public Map<String, Object> zombie(int vida, int danoBase, double velocidad, boolean hostilidad) {
        return describir(new Zombie(vida, danoBase, velocidad, hostilidad), 0);
    }

    public Map<String, Object> esqueletoDisparar(int vida, int danoBase, double velocidad, boolean usaArco, Integer distancia) {
        var esqueleto = new Esqueleto(vida, danoBase, velocidad, usaArco);
        reglas.validarInicial(esqueleto);
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("entidad", esqueleto);
        json.put("disparar", distancia == null ? reglas.disparar(esqueleto) : reglas.disparar(esqueleto, distancia));
        json.put("vidaFinal", esqueleto.getVida());
        json.put("viva", esqueleto.isViva());
        return json;
    }

    public List<Map<String, Object>> comportamientos(int vida, int danoBase, double velocidad,
            boolean hostilidad, boolean comercializacion) {
        var entidades = List.<Entidad>of(new Zombie(vida, danoBase, velocidad, hostilidad),
                new Aldeano(comercializacion ? "Comerciante" : "Granjero"));
        entidades.forEach(reglas::validarInicial);
        return entidades.stream().map(e -> {
            Map<String, Object> json = new LinkedHashMap<>();
            json.put("tipo", e.getClass().getSimpleName());
            json.put("nombre", e.getNombre());
            json.put("comportamiento", reglas.comportamiento(e));
            json.put("viva", e.isViva());
            return json;
        }).toList();
    }

    public List<Map<String, Object>> todas() {
        return List.of("creeper", "zombie", "esqueleto", "lobo", "oveja", "aldeano").stream()
                .map(tipo -> describir(crearEntidad(tipo, "Granjero"), 0)).toList();
    }

    private Entidad crearEntidad(String tipo, String profesion) {
        return switch (tipo.toLowerCase(Locale.ROOT)) {
            case "creeper" -> new Creeper();
            case "zombie" -> new Zombie();
            case "esqueleto" -> new Esqueleto();
            case "lobo" -> new Lobo();
            case "oveja" -> new Oveja();
            case "aldeano" -> new Aldeano(profesion);
            default -> throw new EntidadNoEncontradaException("Tipo de entidad desconocido: " + tipo);
        };
    }

    private Map<String, Object> describir(Entidad entidad, int danio) {
        reglas.validarInicial(entidad);
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("entidad", entidad);
        Movimientos movimientos = reglas.movimientos(entidad);
        json.put("avanzar", movimientos.avanzar());
        json.put("saltar", movimientos.saltar());
        json.put("comportamiento", reglas.comportamiento(entidad));
        if (danio != 0) json.put("recibirDanio", reglas.recibirDanio(entidad, danio));
        json.put("vidaFinal", entidad.getVida());
        json.put("viva", entidad.isViva());
        return json;
    }
}
