package py.edu.uc.lp3.rest.controller;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import py.edu.uc.lp3.constants.ApiPaths;
import py.edu.uc.lp3.rest.dto.ExplosionIntegradaDatos;
import py.edu.uc.lp3.rest.dto.ExplosionIntegradaResultado;
import py.edu.uc.lp3.rest.dto.ExplosionAplicadaResultado;
import py.edu.uc.lp3.service.ExplosionIntegradaService;

@RestController
@RequestMapping(ApiPaths.EXPLOSIONES)
@Tag(name = "Explosiones integradas", description = "Previsualización de solo lectura y aplicación explícita de daño remoto")
public class ExplosionIntegradaController {

    private final ExplosionIntegradaService service;

    public ExplosionIntegradaController(ExplosionIntegradaService service) {
        this.service = service;
    }

    @PostMapping("/previsualizar-integrada")
    @Operation(summary = "Previsualizar una explosión local sobre participantes remotos",
            description = "Combina un Creeper local con un JUGADOR y un CERDO remotos de UUID distintos. "
                    + "Se supone que las coordenadas representan el mismo mundo y unidad. "
                    + "Aplica las reglas LOCALES: radio 3, máximo 20 de daño (40 cargado), caída lineal redondeada y daño cero en el borde. "
                    + "No aplica el radio 6 del proyecto remoto. No consume al Creeper ni modifica H2 ni la API externa; "
                    + "solo realiza GET remotos y estima la salud conservando decimales.")
    @ApiResponse(responseCode = "200", description = "Estimación sin cambios persistidos")
    @ApiResponse(responseCode = "400", ref = "#/components/responses/DatosInvalidos")
    @ApiResponse(responseCode = "404", ref = "#/components/responses/ParticipanteNoEncontrado")
    @ApiResponse(responseCode = "409", ref = "#/components/responses/ExplosionNoPermitida")
    @ApiResponse(responseCode = "502", ref = "#/components/responses/ApiMinecraftNoDisponible")
    @ApiResponse(responseCode = "504", ref = "#/components/responses/ApiMinecraftTimeout")
    public ExplosionIntegradaResultado previsualizar(@RequestBody ExplosionIntegradaDatos datos) {
        return service.previsualizar(datos);
    }

    @PostMapping("/aplicar-integrada")
    @Operation(summary = "Aplicar daño real a los participantes remotos",
            description = "Consulta y valida ambos participantes, calcula daño con reglas LOCALES y envía PUT de salud "
                    + "para jugador y cerdo, conservando nombre, tipo y ubicación; omite atributos para conservarlos en el servidor. "
                    + "No consume al Creeper local. No hay transacción distribuida, rollback ni reintentos automáticos. "
                    + "Un fallo detiene las escrituras restantes; 207 informa las confirmadas y las no confirmadas/no intentadas. "
                    + "Un timeout no garantiza que el PUT no se haya aplicado. Consulte el servidor antes de repetir. "
                    + "Repetir una petición aplica daño nuevamente. Sin control de versión remoto, una escritura concurrente puede sobrescribirse.")
    @ApiResponse(responseCode = "200", description = "Todos los objetivos confirmados o sin daño que aplicar")
    @ApiResponse(responseCode = "207", description = "Aplicación incompleta o incierta; revisar el estado de cada objetivo")
    @ApiResponse(responseCode = "400", ref = "#/components/responses/DatosInvalidos")
    @ApiResponse(responseCode = "404", ref = "#/components/responses/ParticipanteNoEncontrado")
    @ApiResponse(responseCode = "409", ref = "#/components/responses/ExplosionNoPermitida")
    @ApiResponse(responseCode = "502", ref = "#/components/responses/ApiMinecraftNoDisponible")
    @ApiResponse(responseCode = "504", ref = "#/components/responses/ApiMinecraftTimeout")
    public ResponseEntity<ExplosionAplicadaResultado> aplicar(@RequestBody ExplosionIntegradaDatos datos) {
        var resultado = service.aplicar(datos);
        return ResponseEntity.status(resultado.completa() ? 200 : 207).body(resultado);
    }
}
