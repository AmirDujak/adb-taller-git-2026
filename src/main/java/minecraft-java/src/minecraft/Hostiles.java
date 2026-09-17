package minecraft;

import java.util.List;

/**
 * Clase "Hostiles" del diagrama: entidades que atacan al jugador
 * dentro de un rango de detección (rango de aggro).
 */
public abstract class Hostiles extends Entidad {

    private double rangoDeAggro;

    protected Hostiles(String nombre, int vida, Hitbox hitbox,
                        List<String> sonidos, List<String> drop,
                        double velocidadMovimiento, double rangoDeAggro) {
        super(nombre, vida, hitbox, sonidos, drop, velocidadMovimiento);
        this.rangoDeAggro = rangoDeAggro;
    }

    public double getRangoDeAggro() {
        return rangoDeAggro;
    }

    /** Devuelve true si el objetivo está dentro del rango de aggro. */
    public boolean detecta(double distanciaAlJugador) {
        return distanciaAlJugador <= rangoDeAggro;
    }

    /**
     * Acción de ataque: cada mob hostil define cómo ataca
     * (mordisco, flecha, explosión, etc.).
     */
    public abstract void accionDeAtaque();
}
