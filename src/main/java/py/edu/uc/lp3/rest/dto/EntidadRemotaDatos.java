package py.edu.uc.lp3.rest.dto;

import java.util.UUID;

/** Contrato de GET /api/entidades/{id} del proyecto remoto, no una entidad JPA local. */
public record EntidadRemotaDatos(UUID id, String nombre, String tipo, Double salud,
                                Ubicacion ubicacion) {

    public record Ubicacion(Double x, Double y, Double z) {
    }
}
