package minecraft;

import java.util.List;

/**
 * Clase "No Hostiles" del diagrama: entidades pacíficas que permiten
 * algún tipo de interacción con el jugador (domesticar, esquilar, comerciar...).
 */
public abstract class NoHostiles extends Entidad {

    protected NoHostiles(String nombre, int vida, Hitbox hitbox,
                          List<String> sonidos, List<String> drop,
                          double velocidadMovimiento) {
        super(nombre, vida, hitbox, sonidos, drop, velocidadMovimiento);
    }

    /**
     * Interacción con el jugador: cada mob no hostil define la suya
     * (esquilar una oveja, comerciar con un aldeano, etc.).
     */
    public abstract void interactuar();
}
