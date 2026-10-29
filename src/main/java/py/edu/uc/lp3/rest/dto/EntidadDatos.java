package py.edu.uc.lp3.rest.dto;

import java.util.List;
import py.edu.uc.lp3.domain.TipoEntidad;

/** Datos de alta y reemplazo; tipo, cargado y posicion son extensiones opcionales. */
public record EntidadDatos(String nombre, Integer vida, Integer danoBase,
                           Double velocidadMovimiento, HitboxDatos hitbox,
                           List<String> sonidos, List<String> drop,
                           TipoEntidad tipo, Boolean cargado, PosicionDatos posicion) {

    public record PosicionDatos(Double x, Double y, Double z) {
    }

    public record HitboxDatos(Double ancho, Double alto, Double profundidad) {
    }
}
