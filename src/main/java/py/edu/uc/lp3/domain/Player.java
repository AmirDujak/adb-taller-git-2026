package py.edu.uc.lp3.domain;

import java.util.List;

@jakarta.persistence.Entity
@jakarta.persistence.DiscriminatorValue("PLAYER")
public class Player extends Entidad {

    public Player() {
        this(20, 5, 1.0);
    }

    public Player(int vida, int danoBase, double velocidad) {
        super("Player", vida, new Hitbox(0.6, 1.8, 0.6),
                List.of("pasos"), List.of("experiencia"),
                velocidad, danoBase);
    }

    @Override
    public String comportamiento() {
        if (!isViva()) {
            return getNombre() + " ya fue derrotado.";
        }
        return getNombre() + " explora el mundo y combate a las entidades.";
    }
}
