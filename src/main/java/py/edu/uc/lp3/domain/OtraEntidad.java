package py.edu.uc.lp3.domain;

import java.util.List;

/** Conserva los registros de tipos personalizados de la API anterior. */
@jakarta.persistence.Entity
@jakarta.persistence.DiscriminatorValue("OTRA")
public class OtraEntidad extends Entidad {
    public OtraEntidad() {
        super("Otra", 20, new Hitbox(1, 1, 1), List.of(), List.of(), 1);
    }

    @Override
    public String comportamiento() {
        return getNombre() + " recorre el mundo.";
    }
}
