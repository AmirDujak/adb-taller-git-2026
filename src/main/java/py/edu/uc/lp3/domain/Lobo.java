package py.edu.uc.lp3.domain;

import java.util.List;

@jakarta.persistence.Entity
@jakarta.persistence.DiscriminatorValue("LOBO")
public class Lobo extends NoHostiles {

    @jakarta.persistence.Column(columnDefinition = "boolean default false")
    private boolean domesticado;

    public Lobo() {
        super("Lobo", 8, new Hitbox(0.6, 0.85, 0.6),
                List.of("ladrido", "gruñido"), List.of("nada"),
                1.0);
        this.domesticado = false;
    }

    public boolean isDomesticado() {
        return domesticado;
    }

    @Override
    protected String interactuar() {
        if (!domesticado) {
            domesticado = true;
            return getNombre() + " fue domesticado con huesos.";
        }
        return getNombre() + " menea la cola, ya es tu mascota.";
    }
}
