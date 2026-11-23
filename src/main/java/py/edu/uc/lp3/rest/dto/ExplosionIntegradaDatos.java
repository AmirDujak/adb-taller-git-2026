package py.edu.uc.lp3.rest.dto;

import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Creeper local y dos participantes remotos; previsualizar no escribe, aplicar actualiza la salud remota")
public record ExplosionIntegradaDatos(
        @Schema(description = "ID Long del Creeper local vivo y con posición", minimum = "1", example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED) Long creeperId,
        @Schema(description = "UUID remoto de tipo JUGADOR", example = "22222222-2222-4222-8222-222222222222",
                requiredMode = Schema.RequiredMode.REQUIRED) UUID jugadorExternoId,
        @Schema(description = "UUID remoto de tipo CERDO, distinto del jugador", example = "33333333-3333-4333-8333-333333333333",
                requiredMode = Schema.RequiredMode.REQUIRED) UUID cerdoExternoId) {
}
