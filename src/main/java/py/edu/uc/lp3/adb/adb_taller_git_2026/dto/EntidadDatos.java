package py.edu.uc.lp3.adb.adb_taller_git_2026.dto;

import java.util.List;

/** Datos de alta y reemplazo; el identificador siempre lo genera la base. */
public record EntidadDatos(String nombre, Integer vida, Integer danoBase,
                           Double velocidadMovimiento, HitboxDatos hitbox,
                           List<String> sonidos, List<String> drop) {

    public record HitboxDatos(Double ancho, Double alto, Double profundidad) {
    }
}
