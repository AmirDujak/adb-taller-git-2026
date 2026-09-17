package minecraft;

import java.util.List;

public class Zombie extends Hostiles {

    public Zombie() {
        super("Zombie", 20, new Hitbox(0.6, 1.95, 0.6),
                List.of("gruñido", "gemido"), List.of("carne_podrida"),
                0.7, 12.0);
    }

    @Override
    public void accionDeAtaque() {
        System.out.println(getNombre() + " golpea cuerpo a cuerpo al jugador.");
    }
}
