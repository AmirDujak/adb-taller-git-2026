package py.edu.uc.lp3.rest.controller;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import py.edu.uc.lp3.constants.ApiPaths;
import py.edu.uc.lp3.rest.dto.EntidadDatos;
import py.edu.uc.lp3.domain.Entidad;
import py.edu.uc.lp3.domain.Movimientos;
import py.edu.uc.lp3.service.EntidadService;

@RestController
@RequestMapping({ApiPaths.ENTIDADES, ApiPaths.ENTIDADES_COMPAT})
public class EntidadCrudController {

    private final EntidadService service;

    public EntidadCrudController(EntidadService service) {
        this.service = service;
    }

    @GetMapping
    public List<Entidad> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Entidad buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<Entidad> crear(@RequestBody EntidadDatos datos) {
        Entidad entidad = service.crear(datos);
        return ResponseEntity.created(URI.create(ApiPaths.ENTIDADES + "/" + entidad.getId())).body(entidad);
    }

    @PutMapping("/{id}")
    public Entidad actualizar(@PathVariable Long id, @RequestBody EntidadDatos datos) {
        return service.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }

    @GetMapping("/movimientos")
    public List<Map<String, Object>> movimientos() {
        return service.listar().stream().map(entidad -> {
            Movimientos movimientos = entidad;
            return Map.<String, Object>of("id", entidad.getId(), "tipo", entidad.getTipo(),
                    "avanzar", movimientos.avanzar(), "saltar", movimientos.saltar());
        }).toList();
    }
}
