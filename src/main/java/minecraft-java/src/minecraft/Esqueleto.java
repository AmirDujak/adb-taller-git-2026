package minecraft;

import java.util.List;

public class Esqueleto extends Hostiles {

    public Esqueleto() {
        super("Esqueleto", 20, new Hitbox(0.6, 1.99, 0.6),
                List.of("hueso", "disparo_arco"), List.of("huesos", "flechas"),
                0.75, 14.0);
    }

    @Override
    public void accionDeAtaque() {
        System.out.println(getNombre() + " dispara una flecha a distancia.");
    }
}
