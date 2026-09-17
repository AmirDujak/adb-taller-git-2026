package minecraft;

import java.util.List;

public class Oveja extends NoHostiles {

    private boolean esquilada;

    public Oveja() {
        super("Oveja", 8, new Hitbox(0.9, 1.3, 0.9),
                List.of("balido"), List.of("lana", "carne_cordero"),
                0.6);
        this.esquilada = false;
    }

    @Override
    public void interactuar() {
        if (!esquilada) {
            esquilada = true;
            System.out.println(getNombre() + " fue esquilada, se obtiene lana.");
        } else {
            System.out.println(getNombre() + " ya no tiene lana para esquilar.");
        }
    }
}
