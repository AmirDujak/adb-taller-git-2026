package py.edu.uc.lp3.rest.dto;

import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

/** Resultado de la explosión ya aplicada en H2. */
@Schema(description = "Resultado de la explosión confirmada en H2")
public record ExplosionResultado(@Schema(description = "Radio de alcance en bloques", example = "3") double radio, EstadoCreeper creeper,
                                 List<EstadoObjetivo> objetivos) {

    @Schema(description = "Estado del Creeper emisor, eliminado después de explotar")
    public record EstadoCreeper(Long id, boolean cargado, int vidaFinal,
                                boolean eliminado, String mensaje) {
    }

    /** Incluye los objetivos dentro del radio; danoRecibido es la vida perdida. */
    @Schema(description = "Objetivo dentro del radio; danoRecibido es la vida efectivamente perdida")
    public record EstadoObjetivo(Long id, String nombre, double distancia, int vidaInicial,
                                 int danoRecibido, int vidaFinal, boolean eliminado) {
    }
}
