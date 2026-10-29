package py.edu.uc.lp3.rest.controller;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import py.edu.uc.lp3.constants.ApiPaths;
import py.edu.uc.lp3.rest.dto.EntidadDatos;
import py.edu.uc.lp3.domain.EntidadRegistro;
import py.edu.uc.lp3.exceptions.EntidadNoEncontradaException;
import py.edu.uc.lp3.service.EntidadService;

@RestController
@RequestMapping({ApiPaths.ENTIDADES, ApiPaths.ENTIDADES_COMPAT})
public class EntidadCrudController {

    private final EntidadService service;

    public EntidadCrudController(EntidadService service) {
        this.service = service;
    }

    @GetMapping
    public List<EntidadRegistro> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public EntidadRegistro buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<EntidadRegistro> crear(@RequestBody EntidadDatos datos) {
        EntidadRegistro entidad = service.crear(datos);
        return ResponseEntity.created(URI.create(ApiPaths.ENTIDADES + "/" + entidad.getId())).body(entidad);
    }

    @PutMapping("/{id}")
    public EntidadRegistro actualizar(@PathVariable Long id, @RequestBody EntidadDatos datos) {
        return service.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> datosInvalidos(IllegalArgumentException e) {
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(EntidadNoEncontradaException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> noEncontrada(EntidadNoEncontradaException e) {
        return Map.of("error", e.getMessage());
    }
}
