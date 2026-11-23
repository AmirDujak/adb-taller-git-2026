package py.edu.uc.lp3.rest.dto;

import java.util.List;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Previsualización sin persistencia. Las coordenadas de ambas APIs se interpretan en el mismo mundo y unidad.")
public record ExplosionIntegradaResultado(
        @Schema(description = "Se aplica el cálculo local, no la explosión remota de radio 6", example = "LOCALES") String reglasAplicadas,
        @Schema(description = "Radio local en bloques", example = "3") double radio,
        Long creeperId, boolean cargado,
        @Schema(description = "Vida del emisor, que NO se consume en esta previsualización") int vidaCreeper,
        List<ObjetivoIntegrado> objetivos) {

    @Schema(description = "Estado remoto consultado y efecto estimado del cálculo local; no se guarda ni se elimina la entidad")
    public record ObjetivoIntegrado(UUID id, String nombre, String tipo, double distancia,
            @Schema(description = "Daño calculado por el Creeper local: máximo 20, o 40 si está cargado") int danioCalculado,
            @Schema(description = "Daño limitado a la salud disponible; conserva decimales") double danioAplicado,
            double saludAnterior, double saludEstimada, boolean vivaEstimada) {
    }
}
