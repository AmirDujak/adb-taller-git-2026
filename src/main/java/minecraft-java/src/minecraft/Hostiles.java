package minecraft;

import java.util.List;

/**
 * Clase "Hostiles" del diagrama: entidades que atacan al jugador
 * dentro de un rango de detección (rango de aggro).
 */
public abstract class Hostiles extends Entidad {

    private final double rangoDeAggro;

    protected Hostiles(String nombre, int vida, Hitbox hitbox,
                        List<String> sonidos, List<String> drop,
                        double velocidadMovimiento, double rangoDeAggro) {
        super(nombre, vida, hitbox, sonidos, drop, velocidadMovimiento);
        if (rangoDeAggro <= 0) {
            throw new IllegalArgumentException("El rango de aggro debe ser positivo");
        }
        this.rangoDeAggro = rangoDeAggro;
    }

    public double getRangoDeAggro() {
        return rangoDeAggro;
    }

    /** Devuelve true si el objetivo está dentro del rango de aggro. */
    public boolean detecta(double distanciaAlJugador) {
        if (distanciaAlJugador < 0) {
            throw new IllegalArgumentException("La distancia no puede ser negativa");
        }
        return distanciaAlJugador <= rangoDeAggro;
    }

    /** Un mob hostil, cuando se activa, ataca. */
    @Override
    public final String comportamiento() {
        return isViva() ? accionDeAtaque() : getNombre() + " ya desapareció.";
    }

    /**
     * Acción de ataque: cada mob hostil define cómo ataca
     * (mordisco, flecha, explosión, etc.).
     */
    protected abstract String accionDeAtaque();
}
