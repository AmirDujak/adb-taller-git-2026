package py.edu.uc.lp3.domain;

import java.util.List;

@jakarta.persistence.Entity
@jakarta.persistence.DiscriminatorValue("CERDO")
public class Cerdo extends Entidad {

    @jakarta.persistence.Column(columnDefinition = "boolean default false")
    private boolean montable;

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

}
