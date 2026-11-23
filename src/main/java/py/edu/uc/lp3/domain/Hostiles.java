package py.edu.uc.lp3.domain;

import java.util.List;

/**
 * Clase "Hostiles" del diagrama: entidades que atacan al jugador
 * dentro de un rango de detección (rango de aggro).
 */
@jakarta.persistence.Entity
public abstract class Hostiles extends Entidad {

    @jakarta.persistence.Column(columnDefinition = "double precision default 16")
    private double rangoDeAggro;

    protected Hostiles() {
        rangoDeAggro = 16;
    }

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
        this.rangoDeAggro = rangoDeAggro;
    }

    public double getRangoDeAggro() {
        return rangoDeAggro;
    }

}
