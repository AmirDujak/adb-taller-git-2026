package minecraft;

import java.util.List;

public class Cerdo extends Entidad {

    private final boolean montable;

    public Cerdo() {
        this(10, 2, 0.8, false);
    }

    public Cerdo(int vida, int danoBase, double velocidad, boolean montable) {
        super("Cerdo", vida, new Hitbox(0.7, 0.9, 0.7),
                List.of("oink"), List.of("carne_cerdo"),
                velocidad, danoBase);
        this.montable = montable;
    }

    public boolean isMontable() {
        return montable;
    }

    @Override
    public String comportamiento() {
        if (!isViva()) {
            return getNombre() + " ya desapareció.";
        }
        if (montable) {
            return getNombre() + " puede ser montado por el jugador.";
        }
        return getNombre() + " no se puede montar.";
    }
}
