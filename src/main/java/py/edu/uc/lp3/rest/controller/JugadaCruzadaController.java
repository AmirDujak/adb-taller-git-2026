package py.edu.uc.lp3.rest.controller;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import py.edu.uc.lp3.domain.JugadaCruzada;
import py.edu.uc.lp3.rest.dto.JugadaCruzadaDatos;
import py.edu.uc.lp3.service.JugadaCruzadaService;

@RestController
@RequestMapping("/api/lp3/jugadas-cruzadas")
@Tag(name = "POO-09 - Jugadas cruzadas", description = "Operaciones autorizadas por Sofía y Nicolás, registradas en H2")
public class JugadaCruzadaController {
    private final JugadaCruzadaService service;

    public JugadaCruzadaController(JugadaCruzadaService service) { this.service = service; }

    public record ErrorCompanero(String codigo, String proveedor, String error) { }

    @PostMapping
    @Operation(summary = "Autorizar y registrar una jugada coordinada", description = "Primero Sofía valida los participantes "
            + "con POST /api/explosiones/validar; luego Nicolás autoriza un explosivo independiente con "
            + "GET /api/minecraft/explosivos/{id}. Solo si aceptada y puedeExplotar son true se guarda en H2. "
            + "No ejecuta daño ni consume Creepers. Rechazo o fallo remoto deja H2 igual. "
            + "Las aprobaciones no reservan estados futuros. Repetir un éxito crea otro registro (sin idempotencia).")
    @ApiResponse(responseCode = "201", description = "Ambas APIs aceptaron; jugada guardada con Location")
    @ApiResponse(responseCode = "400", ref = "#/components/responses/DatosInvalidos")
    @ApiResponse(responseCode = "404", description = "Participante remoto inexistente; proveedor identifica al compañero",
            content = @Content(schema = @Schema(implementation = ErrorCompanero.class)))
    @ApiResponse(responseCode = "409", description = "Rechazo de A2_SOFIA o A3_NICOLAS; sin cambios locales",
            content = @Content(schema = @Schema(implementation = ErrorCompanero.class)))
    @ApiResponse(responseCode = "502", description = "Fallo de conexión, HTTP o contrato remoto",
            content = @Content(schema = @Schema(implementation = ErrorCompanero.class)))
    @ApiResponse(responseCode = "504", description = "Timeout del proveedor",
            content = @Content(schema = @Schema(implementation = ErrorCompanero.class)))
    public ResponseEntity<JugadaCruzada> ejecutar(@RequestBody JugadaCruzadaDatos datos) {
        var jugada = service.ejecutar(datos);
        return ResponseEntity.created(URI.create("/api/lp3/jugadas-cruzadas/" + jugada.getId())).body(jugada);
    }

    @GetMapping
    @Operation(summary = "Listar jugadas guardadas", description = "Solo lee H2 local; no consulta a los compañeros.")
    @ApiResponse(responseCode = "200", description = "Jugadas en orden de ID, o lista vacía")
    public List<JugadaCruzada> listar() { return service.listar(); }

    @GetMapping("/{id}")
    @Operation(summary = "Recuperar una jugada guardada", description = "El ID es Long local, no un UUID remoto.")
    @ApiResponse(responseCode = "200", description = "Jugada persistida")
    @ApiResponse(responseCode = "404", ref = "#/components/responses/EntidadNoEncontrada")
    public JugadaCruzada buscar(@PathVariable Long id) { return service.buscar(id); }
}
