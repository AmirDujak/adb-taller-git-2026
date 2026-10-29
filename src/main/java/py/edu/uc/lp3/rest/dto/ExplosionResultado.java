package py.edu.uc.lp3.rest.dto;

import java.util.List;

/** Resultado de la explosión ya aplicada en H2. */
public record ExplosionResultado(double radio, EstadoCreeper creeper,
                                 List<EstadoObjetivo> objetivos) {

    public record EstadoCreeper(Long id, boolean cargado, int vidaFinal,
                                boolean eliminado, String mensaje) {
    }

    /** Incluye los objetivos dentro del radio; danoRecibido es la vida perdida. */
    public record EstadoObjetivo(Long id, String nombre, double distancia, int vidaInicial,
                                 int danoRecibido, int vidaFinal, boolean eliminado) {
    }
}
