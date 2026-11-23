package py.edu.uc.lp3.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import py.edu.uc.lp3.constants.ApiPaths;
import py.edu.uc.lp3.rest.dto.ExplosionDatos;
import py.edu.uc.lp3.rest.dto.ExplosionResultado;
import py.edu.uc.lp3.service.ExplosionService;

@RestController
@Tag(name = "Explosiones", description = "Explosiones aplicadas sobre las entidades persistidas")
@RequestMapping(ApiPaths.EXPLOSIONES)
public class ExplosionController {

    private final ExplosionService service;

    public ExplosionController(ExplosionService service) {
        this.service = service;
    }

    @PostMapping("/simular")
    @Operation(summary = "Simular y aplicar una explosión", description = "Operación con efectos persistentes: "
            + "actualiza la vida de los supervivientes y elimina a los muertos y al Creeper emisor. "
            + "La vida, carga y posiciones se leen de H2; los cambios son atómicos.")
    @ApiResponse(responseCode = "200", description = "Resultado de la explosión aplicada")
    @ApiResponse(responseCode = "400", ref = "#/components/responses/DatosInvalidos")
    @ApiResponse(responseCode = "404", ref = "#/components/responses/EntidadNoEncontrada")
    @ApiResponse(responseCode = "409", ref = "#/components/responses/ExplosionNoPermitida")
    public ExplosionResultado simular(@RequestBody ExplosionDatos datos) {
        return service.simular(datos);
    }

}
