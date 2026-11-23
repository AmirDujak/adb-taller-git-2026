package py.edu.uc.lp3.rest.dto;

import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;
import py.edu.uc.lp3.domain.TipoEntidad;

/** Datos de alta y reemplazo; tipo, cargado y posicion son extensiones opcionales. */
@Schema(description = "Datos para crear o actualizar una entidad", example = """
        {"nombre":"Creeper","vida":20,"danoBase":5,"velocidadMovimiento":0.7,
         "hitbox":{"ancho":0.6,"alto":1.7,"profundidad":0.6},
         "sonidos":["ssss"],"drop":["polvora"],"tipo":"CREEPER","cargado":false,
         "posicion":{"x":0,"y":64,"z":0}}
        """)
public record EntidadDatos(
        @Schema(description = "Nombre no vacío", maxLength = 255, requiredMode = Schema.RequiredMode.REQUIRED) String nombre,
        @Schema(description = "Vida inicial positiva", minimum = "1", example = "20", requiredMode = Schema.RequiredMode.REQUIRED) Integer vida,
        @Schema(minimum = "0", example = "5", requiredMode = Schema.RequiredMode.REQUIRED) Integer danoBase,
        @Schema(description = "Velocidad finita no negativa", minimum = "0", example = "0.7", requiredMode = Schema.RequiredMode.REQUIRED) Double velocidadMovimiento,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) HitboxDatos hitbox,
        @Schema(description = "Lista de textos no vacíos; puede estar vacía", requiredMode = Schema.RequiredMode.REQUIRED) List<String> sonidos,
        @Schema(description = "Objetos soltados al morir; puede estar vacía", requiredMode = Schema.RequiredMode.REQUIRED) List<String> drop,
        @Schema(description = "En alta se infiere del nombre si se omite; en actualización no puede cambiar") TipoEntidad tipo,
        @Schema(description = "Solo un Creeper puede estar cargado; por defecto false en alta") Boolean cargado,
        @Schema(description = "Opcional; si se omite en actualización se conserva la posición") PosicionDatos posicion) {

    @Schema(description = "Coordenadas finitas del mundo")
    public record PosicionDatos(
            @Schema(example = "0", requiredMode = Schema.RequiredMode.REQUIRED) Double x,
            @Schema(example = "64", requiredMode = Schema.RequiredMode.REQUIRED) Double y,
            @Schema(example = "0", requiredMode = Schema.RequiredMode.REQUIRED) Double z) {
    }

    @Schema(description = "Dimensiones finitas y estrictamente positivas")
    public record HitboxDatos(
            @Schema(minimum = "0", exclusiveMinimum = true, example = "0.6", requiredMode = Schema.RequiredMode.REQUIRED) Double ancho,
            @Schema(minimum = "0", exclusiveMinimum = true, example = "1.7", requiredMode = Schema.RequiredMode.REQUIRED) Double alto,
            @Schema(minimum = "0", exclusiveMinimum = true, example = "0.6", requiredMode = Schema.RequiredMode.REQUIRED) Double profundidad) {
    }
}
