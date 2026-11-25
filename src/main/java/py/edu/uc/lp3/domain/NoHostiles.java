package py.edu.uc.lp3.domain;

import java.util.List;

/**
 * Clase "No Hostiles" del diagrama: entidades pacíficas que permiten
 * algún tipo de interacción con el jugador (domesticar, esquilar, comerciar...).
 */
@jakarta.persistence.Entity
public abstract class NoHostiles extends Entidad {

    protected NoHostiles() {
    }

    protected NoHostiles(String nombre, int vida, Hitbox hitbox,
                          List<String> sonidos, List<String> drop,
                          double velocidadMovimiento) {
        super(nombre, vida, hitbox, sonidos, drop, velocidadMovimiento);
    }

}
