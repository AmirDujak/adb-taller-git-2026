package py.edu.uc.lp3.rest.dto;

import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Operación coordinada con participantes de Sofía y un explosivo independiente de Nicolás. No ejecuta daño.")
public record JugadaCruzadaDatos(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Creeper vivo en la base de Sofía",
                example = "11111111-1111-4111-8111-111111111111") UUID creeperSofiaId,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Jugador en la base de Sofía",
                example = "22222222-2222-4222-8222-222222222222") UUID jugadorSofiaId,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Cerdo en la base de Sofía",
                example = "33333333-3333-4333-8333-333333333333") UUID cerdoSofiaId,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Explosivo independiente en la base de Nicolás",
                example = "44444444-4444-4444-8444-444444444444") UUID explosivoNicolasId) {
}
