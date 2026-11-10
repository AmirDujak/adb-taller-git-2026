package py.edu.uc.lp3.domain;

import java.util.List;

@jakarta.persistence.Entity
@jakarta.persistence.DiscriminatorValue("OVEJA")
public class Oveja extends NoHostiles {

    @jakarta.persistence.Column(columnDefinition = "boolean default false")
    private boolean esquilada;

    public Oveja() {
        super("Oveja", 8, new Hitbox(0.9, 1.3, 0.9),
                List.of("balido"), List.of("lana", "carne_cordero"),
                0.6);
        this.esquilada = false;
    }

    public boolean isEsquilada() {
        return esquilada;
    }

    @Override
    protected String interactuar() {
        if (!esquilada) {
            esquilada = true;
            return getNombre() + " fue esquilada, se obtiene lana.";
        }
        return getNombre() + " ya no tiene lana para esquilar.";
    }
}
