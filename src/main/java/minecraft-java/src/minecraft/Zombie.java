package minecraft;

import java.util.List;

public class Zombie extends Hostiles {

    private final boolean hostilidad;

    /** Zombie por defecto: hostil, vida 20, daño base 5, velocidad 0.7 y rango de aggro 12.0. */
    public Zombie() {
        this(20, 5, 0.7, true);
    }

    /**
     * Zombie con parámetros personalizados.
     *
     * @param vida      puntos de vida iniciales
     * @param danoBase  daño base que inflige al atacar
     * @param velocidad velocidad de movimiento
     * @param hostilidad si ataca al jugador o deambula sin atacar
     */
    public Zombie(int vida, int danoBase, double velocidad, boolean hostilidad) {
        super("Zombie", vida, new Hitbox(0.6, 1.95, 0.6),
                List.of("gruñido", "gemido"), List.of("carne_podrida"),
                velocidad, danoBase, 12.0);
        this.hostilidad = hostilidad;
    }

    public boolean isHostilidad() {
        return hostilidad;
    }

    @Override
    public String comportamiento() {
        if (!isViva()) {
            return getNombre() + " ya desapareció.";
        }
        if (hostilidad) {
            return accionDeAtaque();
        }
        return getNombre() + " deambula sin atacar.";
    }

    @Override
    protected String accionDeAtaque() {
        return getNombre() + " golpea cuerpo a cuerpo al jugador.";
    }
}
