package py.edu.uc.lp3.rest.dto;

import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resultado de escrituras remotas independientes. No hay rollback ni reintentos automáticos; el Creeper local no se consume.")
public record ExplosionAplicadaResultado(String reglasAplicadas, double radio, Long creeperId,
                                         boolean completa, List<AplicacionObjetivo> objetivos) {

    public enum Estado {
        CONFIRMADO, SIN_CAMBIOS, NO_CONFIRMADO, NO_INTENTADO
    }

    public record AplicacionObjetivo(
            ExplosionIntegradaResultado.ObjetivoIntegrado estimacion,
            @Schema(description = "CONFIRMADO: PUT validado; SIN_CAMBIOS: no requiere daño; "
                    + "NO_CONFIRMADO: el PUT falló o su resultado es incierto; NO_INTENTADO: se detuvo tras un fallo previo") Estado estado,
            @Schema(description = "Salud devuelta por un PUT confirmado, o consultada si no hubo cambios. Null si no se puede confirmar") Double saludConfirmada,
            String codigoError, String mensaje) {
    }
}
