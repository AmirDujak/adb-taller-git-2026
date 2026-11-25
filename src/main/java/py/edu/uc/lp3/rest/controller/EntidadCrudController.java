package py.edu.uc.lp3.rest.controller;

import java.net.URI;
import java.util.List;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import py.edu.uc.lp3.constants.ApiPaths;
import py.edu.uc.lp3.rest.dto.EntidadDatos;
import py.edu.uc.lp3.domain.Entidad;
import py.edu.uc.lp3.service.EntidadService;

@RestController
@Tag(name = "Entidades", description = "CRUD persistido en H2 y movimientos de las entidades guardadas")
@RequestMapping({ApiPaths.ENTIDADES, ApiPaths.ENTIDADES_COMPAT})
public class EntidadCrudController {

    private final EntidadService service;

    public EntidadCrudController(EntidadService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar entidades persistidas")
    @ApiResponse(responseCode = "200", description = "Lista de entidades; puede estar vacía")
    public List<Entidad> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar una entidad por ID")
    @ApiResponse(responseCode = "200", description = "Entidad encontrada")
    @ApiResponse(responseCode = "400", description = "ID con formato inválido", content = @Content)
    @ApiResponse(responseCode = "404", ref = "#/components/responses/EntidadNoEncontrada")
    public Entidad buscar(@Parameter(description = "ID de la entidad persistida", example = "1") @PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    @Operation(summary = "Crear una entidad", description = "Persiste una nueva entidad y devuelve su ubicación en Location.")
    @ApiResponse(responseCode = "201", description = "Entidad creada")
    @ApiResponse(responseCode = "400", ref = "#/components/responses/DatosInvalidos")
    public ResponseEntity<Entidad> crear(@RequestBody EntidadDatos datos) {
        Entidad entidad = service.crear(datos);
        return ResponseEntity.created(URI.create(ApiPaths.ENTIDADES + "/" + entidad.getId())).body(entidad);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una entidad", description = "Reemplaza los datos obligatorios sin cambiar el ID ni el subtipo. "
            + "Si se omiten tipo, cargado o posicion se conservan sus valores actuales.")
    @ApiResponse(responseCode = "200", description = "Entidad actualizada")
    @ApiResponse(responseCode = "400", ref = "#/components/responses/DatosInvalidos")
    @ApiResponse(responseCode = "404", ref = "#/components/responses/EntidadNoEncontrada")
    public Entidad actualizar(@Parameter(description = "ID de la entidad persistida", example = "1") @PathVariable Long id, @RequestBody EntidadDatos datos) {
        return service.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una entidad", description = "Retira definitivamente la entidad del mundo y de H2.")
    @ApiResponse(responseCode = "204", description = "Entidad eliminada; sin cuerpo", content = @Content)
    @ApiResponse(responseCode = "400", description = "ID con formato inválido", content = @Content)
    @ApiResponse(responseCode = "404", ref = "#/components/responses/EntidadNoEncontrada")
    @ApiResponse(responseCode = "422", ref = "#/components/responses/ReglaJuego")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@Parameter(description = "ID de la entidad persistida", example = "1") @PathVariable Long id) {
        service.eliminar(id);
    }

    @GetMapping("/movimientos")
    @Operation(summary = "Consultar movimientos de las entidades persistidas")
    @ApiResponse(responseCode = "200", description = "ID, tipo y descripciones de avanzar y saltar por entidad")
    public List<Map<String, Object>> movimientos() {
        return service.movimientos();
    }
}
