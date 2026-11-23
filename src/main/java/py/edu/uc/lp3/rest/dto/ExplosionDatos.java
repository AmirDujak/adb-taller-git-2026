package py.edu.uc.lp3.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** La vida, la carga y las distancias se obtienen exclusivamente de la base. */
@Schema(description = "Identifica al Creeper persistido que emitirá la explosión")
public record ExplosionDatos(
        @Schema(description = "ID positivo de un Creeper con posición", minimum = "1", example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED) Long creeperId) {
}
