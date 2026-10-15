package minecraft;

import java.util.List;

/**
 * Clase "Hostiles" del diagrama: entidades que atacan al jugador
 * dentro de un rango de detección (rango de aggro).
 */
public abstract class Hostiles extends Entidad {

    private final double rangoDeAggro;

    /** Constructor con daño base igual a cero (mantiene compatibilidad con las subclases existentes). */
    protected Hostiles(String nombre, int vida, Hitbox hitbox,
                        List<String> sonidos, List<String> drop,
                        double velocidadMovimiento, double rangoDeAggro) {
        this(nombre, vida, hitbox, sonidos, drop, velocidadMovimiento, 0, rangoDeAggro);
    }

    /** Constructor completo que recibe también el daño base de la entidad. */
    protected Hostiles(String nombre, int vida, Hitbox hitbox,
                        List<String> sonidos, List<String> drop,
                        double velocidadMovimiento, int danoBase, double rangoDeAggro) {
        super(nombre, vida, hitbox, sonidos, drop, velocidadMovimiento, danoBase);
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

    /** Un mob hostil, cuando se activa, ataca. Las subclases pueden redefinirlo (por ejemplo, un Zombie puede no ser hostil). */
    @Override
    public String comportamiento() {
        return isViva() ? accionDeAtaque() : getNombre() + " ya desapareció.";
    }

    /**
     * Acción de ataque: cada mob hostil define cómo ataca
     * (mordisco, flecha, explosión, etc.).
     */
    protected abstract String accionDeAtaque();
}
